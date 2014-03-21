package grails.plugins.imports

import grails.plugins.imports.logging.*
import grails.converters.JSON
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.web.binding.DataBindingUtils

class ImportsService {
    static rabbitQueue = "${grails.util.Holders.grailsApplication.metadata['app.name']}ImportRows"

	def grailsApplication
	def importsLogger
	static IMPORT_CONFIGURATIONS = [:]

	def handleMessage(msg) {
		def args = JSON.parse(msg)
		def serviceName = IMPORT_CONFIGURATIONS[args.params.entityName],
		    service = grailsApplication.mainContext[serviceName]
		service.processRow(args.row, args.rowIndex, args.columns, args.params, args.importLogId)
		return
	}

	def publishRow(row, index, columns, params, importLogId) {
		def msgContent = [row:row, index:index, columns:columns, importLogId:importLogId, params:params]
		rabbitSend rabbitQueue, new JSON(msgContent).toString()
	}

	def validateEntityName(entityName) throws Exception {
		if (entityName == null || !IMPORT_CONFIGURATIONS.containsKey(entityName)) throw new ImportsException('Import service not found for '+entityName, 405)
	}

	def getImportType(params) {
		params.importType = params.importType ?: 'csv'
		return params.importType
	}

	def cancel(request, params) {
		def canceled = importsLogger.cancel(params.importLogId ?: params.id)
		return canceled
	}

	def getLog(request, params) {
		def log = importsLogger.getImportLog(params.importLogId ?: params.id)
		return log
	}


	def getErrorRowInfo(request, params) {
		def errorRowInfo = importsLogger.getImportLogErrorInfo(params.importLogId ?: params.id)
		return errorRowInfo
	}

	def findLogs(request, params) {
		def logs = importsLogger.findImportLogs(params)
		return logs
	}

	def processFile(uploadedFile, params) throws Exception {
		def serviceName = IMPORT_CONFIGURATIONS[params.entityName],
		    service,
		    importType,
		    importLogId
		try {
			if (!serviceName) throw new ImportsException('Import service not found for '+params.entityName, 405)
			service = grailsApplication.mainContext[serviceName]
			if (!service) throw new ImportsException('Import service not found for '+params.entityName, 405)
			service.initParams(params)
			importType = getImportType(params)
			importLogId = importsLogger.createImportLog(params)
			if (!service.metaClass.respondsTo(service, "process${importType.capitalize()}", Object, Object, Object) ) throw new ImportsException('Import type '+ importType+' not found for '+params.entityName, 405)
			importsLogger.setImportLogValue(importLogId, 'processing', true)
			importsLogger.logMessage(importLogId,[text:'Starting file processing', ts:new Date()])
			if (service.doArchiveFile) service.archiveFile(uploadedFile, params, importLogId)
			importsLogger.setImportLogValue(importLogId, 'fileName', uploadedFile.getOriginalFilename())
			service."process${importType.capitalize()}"(uploadedFile, params, importLogId)
			importsLogger.logMessage(importLogId,[text:'Process request complete (async processing may still be occurring)', ts:new Date()])
		} catch(Exception e) {
			importsLogger?.setImportLogValue(importLogId, 'status', 'error')
			throw e
		}
		return importLogId
	}


	def processRequest(request, params) throws Exception {
		def serviceName = IMPORT_CONFIGURATIONS[params.entityName],
		    service,
		    importType,
		    importLogId,
		    uploadedFile 
		try {
			if (!serviceName) throw new ImportsException('Import service not found for '+params.entityName, 405)
			service = grailsApplication.mainContext[serviceName]
			if (!service) throw new ImportsException('Import service not found for '+params.entityName, 405)
			service.initParams(params)
			importType = getImportType(params)
			importLogId = importsLogger.createImportLog(params)
			if (!service.metaClass.respondsTo(service, "process${importType.capitalize()}", Object, Object, Object) ) throw new ImportsException('Import type '+ importType+' not found for '+params.entityName, 405)
			importsLogger.setImportLogValue(importLogId, 'processing', true)
			importsLogger.logMessage(importLogId,[text:'Starting file processing', ts:new Date()])
			try {
				uploadedFile = request.getFile(service.parameterName)
			} catch (e) {
				service.log.error(e)
				throw new ImportsException('Unable to find uploaded file parameter '+ service.parameterName+' not found for '+params.entityName, 400)			
			}
			params.remove(service.parameterName)
			if (service.doArchiveFile) service.archiveFile(uploadedFile, params, importLogId)
			importsLogger?.setImportLogValue(importLogId, 'fileName', uploadedFile.getOriginalFilename())
			service."process${importType.capitalize()}"(uploadedFile, params, importLogId)
			importsLogger.logMessage(importLogId,[text:'Process request complete (async processing may still be occurring)', ts:new Date()])
		} catch(Exception e) {
			importsLogger.setImportLogValue(importLogId, 'status', 'error')
			throw e
		}
		return importLogId
	}

	def getInfoUrl(entityName) {
		def rtn = null,
			serviceName = IMPORT_CONFIGURATIONS[entityName]
		if (serviceName) rtn = grailsApplication.mainContext[serviceName]?.infoUrl
		return rtn
	}



}
