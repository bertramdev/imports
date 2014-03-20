package grails.plugins.imports.logging

import grails.plugins.imports.*

class MongoLogger implements ImportLogger {
	static getLoggingMongoDb() {
		def mongo = grails.util.Holders.grailsApplication.mainContext.mongo,
		    grailsApp = grails.util.Holders.grailsApplication,
		    dbName = (grailsApp.config.grails.plugins.imports.containsKey('mongoDb') ? grailsApp.config.grails.plugins.imports.mongoDb : 'proconImports')
		return mongo.getDB(dbName)
	}

	def createImportLog(params) {
		delegate.log.debug('createImportLog')
		def doc = DefaultLogger.getLogTemplate(params),
			db = MongoLogger.getLoggingMongoDb()

		if (params.logValues instanceof Map) {
			doc += params.logValues
		}
		db.importLogs.insert(doc)
		return doc._id
	}

    def incrementImportCounter(importLogId) {
		delegate.log.debug('incrementImportCounter')
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[processed:1]])
    }

    def setImportTotal(importLogId, total) {
		delegate.log.debug('setImportTotal')
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$set':[total:total]])
    }

    def setImportLogValue(importLogId, name, value) {
		delegate.log.debug('setImportStatusValue')
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$set':["${name}":value]])
    }

    def cancel(importLogId) {
		delegate.log.debug('cancel')
		def doc = MongoLogger.getLoggingMongoDb().importLogs.findOne([_id:importLogId])
		if (doc?.processing == true) {
			MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$set':[canceled:true, canceledAt:new Date()]])
			delegate.logMessage(importLogId, [text:'Import canceled', ts:new Date()])
			return true
		} else {
			return false
		}
    }

    def isCanceled(importLogId) {
		delegate.log.debug('cancel')
		def ct = MongoLogger.getLoggingMongoDb().importLogs.count([_id:importLogId,canceled:true]) 
		return ct > 0
    }

    def isImportComplete(importLogId) {
		delegate.log.debug('isImportDone')
		def doc = MongoLogger.getLoggingMongoDb().importLogs.findOne([_id:importLogId])
		return doc.total == doc.processed
    }

    def logMessage(importLogId, valuesMap) {
		delegate.log.debug('logMessage')
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$push':[messages:valuesMap]])
    }

    def logSuccessRow(importLogId, row, index) {
		delegate.log.debug('logSuccessRow')
		// dont do anything by default
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[successCount:1]])
    }

    def logCancelRow(importLogId, row, index) {
		delegate.log.debug('logCancelRow')
		// dont do anything by default
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[cancelCount:1]])
    }

    def logInsertRow(importLogId, row, index) {
		delegate.log.debug('logInsertRow')
		// dont do anything by default
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[insertCount:1]])
    }

    def logUpdateRow(importLogId, row, index) {
		delegate.log.debug('logUpdateRow')
		// dont do anything by default
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[updateCount:1]])
    }

    def logErrorRow(importLogId, row, index, msg) {
		delegate.log.debug('logErrorRow')
		if (msg) row[DefaultImporter.IMPORT_ERROR] = msg
		if (index) row[DefaultImporter.IMPORT_INDEX] = index
		MongoLogger.getLoggingMongoDb().importLogs.update([_id:importLogId], ['$inc':[errorCount:1],'$push':[errorRows:row]])
    }

    def getImportLog(importLogId) {
		delegate.log.debug('logMessage')
		return MongoLogger.getLoggingMongoDb().importLogs.findOne([_id:importLogId])
    }

    def getImportLogErrorInfo(importLogId) {
		delegate.log.debug('getImportLogErrorInfo')
		def doc = MongoLogger.getLoggingMongoDb().importLogs.findOne([_id:importLogId]),
		    headers = doc?.headers,
		    errorRows = doc?.errorRows,
		    rtn = [errorRows:[]]
		if (headers) {
            if (!headers.contains(DefaultImporter.IMPORT_INDEX)) headers << DefaultImporter.IMPORT_INDEX
            if (!headers.contains(DefaultImporter.IMPORT_ERROR)) headers << DefaultImporter.IMPORT_ERROR
			rtn.errorRows << headers
			if (errorRows) {
				errorRows.each {row->
					def outputRow = []
					headers.each {hdr-> outputRow << row[hdr] }
					rtn.errorRows << outputRow
				}
			}	
			rtn.fileName = 'ERRORS_'+doc.fileName
			rtn.errorCount = doc.errorCount
			return rtn
		} else {
			return null
		}
    }

    def findImportLogs(params) {
    	def crit = params.criteris ?: [:],
    		options = params.options ?: [:]
		return MongoLogger.getLoggingMongoDb().importLogs.find(crit, options)
    }

}
