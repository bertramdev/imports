package grails.plugins.imports

import grails.plugins.imports.ImportsService
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.web.binding.DataBindingUtils

class DefaultImporter {
	static EXCLUDED = ['metaClass','class','beforeDelete', 'delegate', 'grailsApplication',
                       'beforeUpdate','beforeInsert','afterUpdate', 'transients','dateCreated',
                       'afterInsert','afterDelete','hibernateLazyInitializer','lastUpdated', 'version',
                       'compareTo', 'dataSource',"_ref",'properties', 'errors','className','maxState',
                       'restExcluded'
                       ]
    static IMPORT_ERROR = 'IMPORT_ERROR'
    static IMPORT_INDEX = 'IMPORT_INDEX'

    static final DEFAULT_SUMMARY_EMAIL_CONTENT='''
    Hi,

    Your recent import of ${entityName} has been completed. Please find the result statistics below.

    Filename : ${fileName}
    Uploaded at : ${created}
    Total rows provided: ${total}

    Total success : ${successCount}
    Total errors : ${errorCount}
    Attempted inserts : ${insertCount}
    Attempted updates : ${updateCount}

    If you experience issues related to your import process, please contact support.

    Thank you very much!
    '''

    static final DEFAULT_CONFIRMATION_EMAIL_CONTENT='''
    Hi,

    Your recent import of ${entityName} has been received. 

    Filename : ${fileName}
    Uploaded at : ${created}
    ${(total ? 'Total rows provided: ' +total : '')}

    If you experience issues related to your import process, please contact support.

    Thank you very much!
    '''

    static final DEFAULT_SUMMARY_EMAIL_SUBJECT='Your import of ${entityName} has been processed'
    static final DEFAULT_CONFIRMATION_EMAIL_SUBJECT='Your import of ${entityName} has been received'

    static SERVICE_METHODS = [processCsv:3, defaultValue:3, columns:2, columns__:2, column:3, marshall:4,archiveFile:3,retrieveArchivedFile:2, canUseQueue:1,
                                validateFile:3, validateHeaders:3, beforeBindRow:5, bindRow:5, afterBindRow:6, processRow:5, formatErrors:2,
                                validateRow:6, saveRow:6, afterSaveRow:7, processComplete:2, sendSummaryEmail:2, getRowCount:3,
                                summaryEmailAddress:2, summaryEmailContent:2, confirmationEmailAddress:2,summaryEmailSubjectTemplate:2,
                                confirmationEmailContent: 2, sendConfirmationEmail:2, confirmationEmailContentTemplate:2,confirmationEmailSubjectTemplate:2,
                                summaryEmailContentTemplate:2, confirmationEmailSubject:2, summaryEmailSubject:2, summaryEmailBindVariables:3,
                                confirmationEmailBindVariables:3, initParams:1,fetchObject:5 ]

    static SERVICE_PROPERTIES = [getInfoUrl:null,getAsync:false,getParameterName:'import',getMatchProperties:['id'],getUseQueue:false, 
                                   getMaxErrors:Integer.MAX_VALUE, getDoValidation:false, getCancelCheckIncrement:50,
                                   getDoConfirmationEmail:false, getDoSummaryEmail:false, getDoArchiveFile:false, getFromEmailAddress:'imports@myapp.com',
                                   getDoIncludeErrorsInSummary:true, getConfirmationEmailContentTemplate: DEFAULT_CONFIRMATION_EMAIL_CONTENT,
                                   getSummaryEmailContentTemplate:DEFAULT_SUMMARY_EMAIL_CONTENT, getConfirmationEmailSubjectTemplate:DEFAULT_CONFIRMATION_EMAIL_SUBJECT,
                                   getSummaryEmailSubjectTemplate:DEFAULT_SUMMARY_EMAIL_SUBJECT]

	static initParams = {params->
		
	}

	static getLogger() {
		grails.util.Holders.grailsApplication.mainContext['importsLogger']
	}

    static forEachImportRow(inputStream,headers, closure) {
        def delimiter  = ",",
            seperator = "\"",
            regex = ~"\\G(?:^|\\${delimiter})(?:\"([^\"]*+)\"|([^\"\\${delimiter}]*+))",
            rowCount = 0
        inputStream.eachLine { line ->
        	if (line?.trim()?.length() > 0) {
	            def row = [:],
	                matcher = regex.matcher(line),
	                colCount = 0
	            while (matcher.find()) {
	              row[headers[colCount]] = (matcher.group(1) ?: matcher.group(2))
	              colCount++
	            }
	            closure(row,rowCount)
	            rowCount++
        	}
        }
    }

    static validateFile = {uploadedFile, params, importLogId->
		def inputStream,
		    headers,
		    fileSizeBytes,
		    fileRowIndex = 1,
		    count = 0,
		    errorFound = false,
		    eachRow,
		    importLogger = getLogger()
		try {
			inputStream = new BufferedReader(new InputStreamReader (uploadedFile.inputStream))
			fileSizeBytes = uploadedFile.getSize().toInteger()
			importLogger.setImportLogValue(importLogId, 'fileSizeBytes', fileSizeBytes)
			inputStream.mark(fileSizeBytes*2)
			headers = inputStream.readLine()?.split(',').collect()
			importLogger.setImportLogValue(importLogId, 'headers', headers)

			errorFound = delegate.validateHeaders(headers, params, importLogId) == false
			eachRow = inputStream.readLine()
			while(eachRow != null) {
				if (eachRow.trim().length() > 0) {
					eachRow = eachRow.split(',').collect()
					if (headers.size() != eachRow.size()) {
						def row = [:]
						headers.eachWithIndex {h, i->
							row[h] = eachRow[i]
						}
						importLogger.logErrorRow(importLogId, row, fileRowIndex, 'Row column count does not match header count')
						errorFound = true
					}
					count++
				}
				eachRow = inputStream.readLine()
				fileRowIndex++
			}
			importLogger.setImportLogValue(importLogId, 'total', count)
		} finally {
			try {inputStream?.reset()} catch (ee) {delegate.log.error(ee)}
		}
		return !errorFound
    }

    static getRowCount = {uploadedFile, params, importLogId->
		def inputStream,
			fileSizeBytes,
		    totalRowsToImport = 0,
		    eachRow
		try {
			inputStream = new BufferedReader(new InputStreamReader (uploadedFile.inputStream))
			fileSizeBytes = uploadedFile.getSize().toInteger()
			inputStream.mark(fileSizeBytes*2)
			eachRow = inputStream.readLine()
			while(eachRow != null) {
				if (eachRow.trim().length() > 0) {
					totalRowsToImport++
				}
				eachRow = inputStream.readLine()
			}
		} finally {
			try {inputStream?.reset()} catch (ee) {log.error(ee)}
		}
		return (totalRowsToImport - 1) // 1 was headers
    }

	static processCsv = {uploadedFile, params, importLogId->
		def importLogger = getLogger()
		delegate.log.debug('processCsv')
		if (delegate.doValidation == true && delegate.validateFile(uploadedFile, params, importLogId) == false) {
			importLogger.setImportLogValue(importLogId, 'invalidFile', true)
			importLogger.setImportLogValue(importLogId, 'processing', false)
			throw new ImportsException('Import file is not valid', 400, importLogId)
		}
		if (delegate.doConfirmationEmail) {
			try {
				delegate.sendConfirmationEmail(params, importLogId)
			} catch(e) {
				importLogger.logMessage(importLogId, [text:'Unable to send confimation email', trace: org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e)])
			}
		}
		def fileSizeBytes = uploadedFile.getSize(),
		    inputStream = new BufferedReader(new InputStreamReader (uploadedFile.inputStream)),
		    headers,
		    rowCount = getRowCount(uploadedFile, params, importLogId)
		    columns = delegate.columns(params, importLogId)
		inputStream.mark(fileSizeBytes.toInteger()*2)
		headers = inputStream.readLine()?.split(',').collect()
		headers.remove(DefaultImporter.IMPORT_ERROR)
		headers.remove(DefaultImporter.IMPORT_INDEX)
		importLogger.setImportLogValue(importLogId, 'headers', headers)
		importLogger.setImportLogValue(importLogId, 'fileSizeBytes', fileSizeBytes)
		importLogger.setImportTotal(importLogId, rowCount)
		if (delegate.async) {
			runAsync {
				DefaultImporter.forEachImportRow(inputStream,headers) { row, rowIndex ->
					if (delegate.canUseQueue(importLogId))	{
						grails.util.Holders.grailsApplication.mainContext['importsService'].publishRow(row, rowIndex, columns, params, importLogId)
					} else {
						delegate.processRow(row, rowIndex, columns, params, importLogId)
					}		
				}
				if (delegate.canUseQueue(importLogId) == false) delegate.processComplete(params, importLogId)
			}
		} else {
			DefaultImporter.forEachImportRow(inputStream,headers) { row, rowIndex ->
				if (delegate.canUseQueue(importLogId))	{
					grails.util.Holders.grailsApplication.mainContext['importsService'].publishRow(row, rowIndex, columns, params, importLogId)
				} else {
					delegate.processRow(row, rowIndex, columns, params, importLogId)
				}		
			}
			if (delegate.canUseQueue(importLogId) == false) delegate.processComplete(params, importLogId)
		}
    }

    static columns = {params, importLogId->
		delegate.log.debug('columns')
		return delegate.columns__(params, importLogId)
    }

    static columns__ = {params, importLogId->
		delegate.log.debug('columns__')
	    def domainArtefact = grails.util.Holders.grailsApplication.getArtefactByLogicalPropertyName('Domain', params.entityName),
	        columns = [:]
		domainArtefact?.properties.each{ if (!EXCLUDED.contains(it.name)) columns[it.name] = delegate.column(params, it.name, importLogId)}
		return  columns
    }

    static column = {params, nm, importLogId->
		delegate.log.debug('column')
    	return nm
    }

    static archiveFile = { uploadedFile, params, importLogId->
		delegate.log.debug('archiveFile')
		// save archivedFileLocator
	}

    static retrieveArchivedFile = { params, importLogId->
		delegate.log.debug('retrieveArchivedFile')
		// save archivedFileLocator
	}

	static processRow = {row, index, columns, params, importLogId->
		def importLogger = getLogger()
		importLogger.incrementImportCounter(importLogId)
		if (index > 0 && (index+1) % delegate.cancelCheckIncrement == 0 || delegate.canUseQueue(importLogId)) {
			if (importLogger.isCanceled(importLogId)) {
				importLogger.logCancelRow(importLogId, row, index)
				return
			}
		}
		try {
			delegate.log.debug('processRow')
			def obj = delegate.bindRow(row, index, columns, params, importLogId),
				success = delegate.saveRow(obj, row, index, columns, params, importLogId)
			if (!success) {
				importLogger.logErrorRow(importLogId, row, index, delegate.formatErrors(obj, importLogId))
			} else {
				importLogger.logSuccessRow(importLogId, row, index)
			}
		} catch (Throwable e) {
			delegate.log.error(e)
			importLogger.logErrorRow(importLogId, row, index, e.toString())
		}
		if (delegate.canUseQueue(importLogId) && importLogger.isImportComplete(importLogId)) delegate.processComplete(params, importLogId)
    }

    static canUseQueue = {importLogId ->
    	return delegate.useQueue && (grails.util.Holders.pluginManager.getGrailsPlugin('rabbitmq') || grails.util.Holders.pluginManager.getGrailsPlugin('rabbit-amqp'))
    }

	static beforeBindRow = {row, index, columns, params, importLogId->
		delegate.log.debug('beforeBindRow')

	}

	static fetchObject = {row, index, columns, params, importLogId->
		delegate.log.debug('fetchObject')
	    def importLogger = getLogger(),
	        domainArtefact = grails.util.Holders.grailsApplication.getArtefactByLogicalPropertyName('Domain', params.entityName),
	        domainClassInstance = domainArtefact?.getClazz()?.newInstance(),
	        matchVals = [:],
	        existing,
	        matchProps = delegate.matchProperties instanceof List ? delegate.matchProperties : [delegate.matchProperties?.toString()]
	    matchProps.each {k->
	    	def col = columns.find {entry-> entry.value == k},
	    		colName = col?.key ?: k, 
	    	    prop = col?.value ?: k,
	    	    val = delegate.marshall(colName, prop, row[colName], importLogId) ?: delegate.defaultValue(colName, prop, importLogId)
	    	matchVals[k.toString()] = val
	    }
	    existing = domainClassInstance.findWhere(matchVals) 
	    if (existing) {
			importLogger.logUpdateRow(importLogId, row, index)    		
			domainClassInstance = existing
    	} else {
			importLogger.logInsertRow(importLogId, row, index)    		
    	}
		return domainClassInstance
	}

	static bindRow = {row, index, columns, params, importLogId->
		delegate.beforeBindRow(row, index, columns, params, importLogId)
		delegate.log.debug('bindRow')
	    def bindingMap = new org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap([:], null),
		    domainArtefact = grails.util.Holders.grailsApplication.getArtefactByLogicalPropertyName('Domain', params.entityName),
	        object = delegate.fetchObject(row, index, columns, params, importLogId)
	    columns.each {k, v->
			def property = columns[k] ?: k,
				value = delegate.marshall(k, property, row[k], importLogId) ?: delegate.defaultValue(k, property, importLogId)
			bindingMap.put(property, value)
	    }
		DataBindingUtils.bindObjectToDomainInstance(domainArtefact, object, bindingMap)
		DataBindingUtils.assignBidirectionalAssociations(object, bindingMap, domainArtefact) 
		delegate.afterBindRow(object, row, index, columns, params, importLogId)
		return object
    }

    static defaultValue = {columnName, propertyName, importLogId->
		delegate.log.debug('defaultValue:'+columnName)
		return null
    }

	static marshall = {columnName,  propertyName, value, importLogId->
		delegate.log.debug('marshall:'+columnName)
		return value
	}

	static validateHeaders = {headers, params, importLogId->
		delegate.log.debug('validateHeaders')
		return true
    }

	static afterBindRow = {obj, row, index, columns, params, importLogId->
		delegate.log.debug('afterBindRow')

    }

	static validateRow = {obj, row, index, columns, params, importLogId->
		return true
	}

	static saveRow = {obj, row, index, columns, params, importLogId->
		delegate.log.debug('saveRow')
		def success = delegate.validateRow(obj, row, index, columns, params, importLogId)
		success = success && obj.save()
		delegate.afterSaveRow(success, obj, row, index, columns, params, importLogId)
		return success
    }

    static formatErrors = {obj, importLogId->
    	def msg = null,
		    ms = grails.util.Holders.grailsApplication.mainContext.messageSource
		obj.errors.allErrors.each {
			if (!msg) msg = ''
			else msg += ';'
			msg += ms.getMessage(it, null)
		}
		return msg
    }

	static afterSaveRow = {success, obj, row, index, columns, params, importLogId->
		delegate.log.debug('afterSaveRow')

    }

    static processComplete = {params, importLogId->
    	def importLogger = getLogger()
		delegate.log.debug('processComplete')
		if (delegate.doSummaryEmail) {
			try {
				delegate.sendSummaryEmail(params, importLogId)
			} catch(e) {
				importLogger.logMessage(importLogId, [text:'Unable to send summary email', trace: org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e)])
			}
		}
		importLogger.logMessage(importLogId,[text:'Processing Complete', ts:new Date()])
		importLogger.setImportLogValue(importLogId, 'processing', false)
		importLogger.setImportLogValue(importLogId, 'finishedAt', new Date())
    }


    // START SUMMARY EMAIL STUFF

    static summaryEmailContentTemplate = {params, importLogId->
    	return delegate.summaryEmailContentTemplate
    }


    static summaryEmailSubjectTemplate = {params, importLogId->
    	return delegate.summaryEmailSubjectTemplate
    }

    static summaryEmailBindVariables = {binding, params, importLogId->
    	return binding
    }

    static summaryEmailContent = {params, importLogId->
    	def importLogger = getLogger(),
    	    importLog = importLogger.getImportLog(importLogId),
    	    engine = new SimpleTemplateEngine(),
    		binding = [:],
			template = engine.createTemplate(delegate.summaryEmailContentTemplate(params, importLogId))
		params?.putAll(binding)
		binding += importLog
		return template.make(binding).toString()

    }

    static summaryEmailSubject = {params, importLogId->
    	def importLogger = getLogger(),
    	    importLog = importLogger.getImportLog(importLogId),
    	    engine = new SimpleTemplateEngine(),
    		binding = [:],
			template = engine.createTemplate(delegate.summaryEmailSubjectTemplate(params, importLogId))
		params?.putAll(binding)
		binding += importLog
		delegate.summaryEmailBindVariables(binding, params, importLogId)
		return template.make(binding).toString()
    }

    static summaryEmailAddress = {params, importLogId->
		def rtn = params.email
		return rtn
    }

	static sendSummaryEmail = {params, importLogId->
		delegate.log.debug('sendSummaryEmail')
		def email = delegate.summaryEmailAddress(params, importLogId),
			subj = delegate.summaryEmailSubject(params, importLogId),
			content = delegate.summaryEmailContent(params, importLogId),
			fromEmail = delegate.fromEmailAddress,
			importLogger = getLogger(),
			errorInfo = importLogger.getImportLogErrorInfo(importLogId),
			includeErrs = delegate.doIncludeErrorsInSummary

		delegate.log.debug("Now sending email (with content) to : $email --> " + content )
		try {
			if (!email) throw new Exception('Summary email address not found')
			if (!subj) throw new Exception('Summary email subject not found')
			if (!content) throw new Exception('Summary email content not found')
			if (!fromEmail) throw new Exception('Summary email from address not found')
			importLogger.setImportLogValue(importLogId, 'summaryEmail', [recipient:email, subject:subj, content:content, from:fromEmail])
			grails.util.Holders.grailsApplication.mainContext['mailService'].sendMail {->
				multipart true
				to email
				from fromEmail
				subject subj
				text content
				if (includeErrs && errorInfo?.errorRows?.size() >  1) {
					def csv = ''
					errorInfo.errorRows.each {err->
						csv+=err.join(',')
						csv+= '\n'
					}
					attach errorInfo.fileName, "text/csv", csv.toString() as byte[]
				}
			}
			delegate.log.debug("Import email successfully sent to $email")
		} catch(e) {
			delegate.log.error("Import Email exception for email $email : $e")
			throw e
		}
    }

    // END SUMMARY EMAIL STUFF

    // START CONFIRMATION EMAIL STUFF

    static confirmationEmailContentTemplate = {params, importLogId->
    	return delegate.confirmationEmailContentTemplate
    }

    static confirmationEmailSubjectTemplate = {params, importLogId->
    	return delegate.confirmationEmailSubjectTemplate
    }

    static confirmationEmailBindVariables = {binding, params, importLogId->
    	return binding
    }

    static confirmationEmailContent = {params, importLogId->
    	def importLog = getLogger().getImportLog(importLogId),
    	    engine = new SimpleTemplateEngine(),
    		binding = [:],
			template = engine.createTemplate(delegate.confirmationEmailContentTemplate(params, importLogId))
		params?.putAll(binding)
		binding += importLog
		delegate.confirmationEmailBindVariables(binding, params, importLogId)
		return template.make(binding).toString()
    }

    static confirmationEmailSubject = {params, importLogId->
    	def importLog = getLogger().getImportLog(importLogId),
    	    engine = new SimpleTemplateEngine(),
    		binding = [:],
			template = engine.createTemplate(delegate.confirmationEmailSubjectTemplate(params, importLogId))
		params?.putAll(binding)
		binding += importLog
		return template.make(binding).toString()
    }

    static confirmationEmailAddress = {params, importLogId->
		def rtn = params.email
		return rtn
    }

	static sendConfirmationEmail = {params, importLogId->
		delegate.log.debug('sendConfirmationEmail')
		def email = delegate.confirmationEmailAddress(params, importLogId),
			subj = delegate.confirmationEmailSubject(params, importLogId),
			content = delegate.confirmationEmailContent(params, importLogId),
			fromEmail = delegate.fromEmailAddress,
			importLogger = getLogger()

		delegate.log.debug("Now sending email (with content) to : $email --> " + content )
		try {
			if (!email) throw new Exception('Confirmation email address not found')
			if (!subj) throw new Exception('Confirmation email subject not found')
			if (!content) throw new Exception('Confirmation email content not found')
			if (!fromEmail) throw new Exception('Confirmation email from address not found')
			importLogger.setImportLogValue(importLogId, 'confirmationEmail', [recipient:email, subject:subj, content:content, from:fromEmail])

			def cls =  {->
				to email
				from fromEmail
				subject subj
				text content
			}
			grails.util.Holders.grailsApplication.mainContext['mailService'].sendMail(cls)
			delegate.log.debug("Import email successfully sent to $email")
		} catch(e) {
			delegate.log.error("Import Email exception for email $email : $e")
			throw e
		}
    }

    // END CONFIRMATION EMAIL STUFF
}
