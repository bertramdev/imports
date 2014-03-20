package grails.plugins.imports.logging

import grails.plugins.imports.*

class DefaultLogger implements ImportLogger {
    static LOGGING_METHODS = [createImportLog:1, getImportLogErrorInfo:1,cancel:1,findImportLogs:1,
                                incrementImportCounter:1, setImportTotal:2, setImportLogValue:3,isCanceled:1,
                                isImportComplete:1, logMessage:2, getImportLog:1,logErrorRow:5,logSuccessRow:3,
                                logCancelRow:3,logInsertRow:3,logUpdateRow:3]


   static getLogTemplate(params) {
        [_id:UUID.randomUUID().toString().replaceAll("-", ""),
            created: new Date(),
            scope: (grails.util.Holders.grailsApplication.config.grails.plugins.imports.containsKey('scope') ? grails.util.Holders.grailsApplication.config.grails.plugins.imports.scope : grails.util.Holders.grailsApplication.metadata['app.name']),
            userId: params.sUserId,
            accountId: params.sAccountId,
            entityName: params.entityName,
            importType: params.importType,
            total:null,
            processing:false,
            fileName:null,
            canceled:false,
            archivedFileLocator:null,
            processed:0,
            successCount:0,
            updateCount:0,
            insertCount:0,
            cancelCount:0,
            errorCount:0,
            canceled:false,
            headers:[],
            errorRows:[],
            messages:[]
        ]       
    }

	def createImportLog(params) {
		return UUID.randomUUID().toString().replaceAll("-", "")
	}

    def incrementImportCounter(importLogId) {
    }

    def setImportTotal(importLogId, total) {
    }

    def setImportLogValue(importLogId, name, value) {
    }

    def cancel(importLogId) {
		return false
    }

    def isCanceled(importLogId) {
    	return false
    }

    def isImportComplete(importLogId) {
    	return false
    }

    def logMessage(importLogId, valuesMap) {
    	return
    }

    def logSuccessRow(importLogId, row, index) {
    	return
    }

    def logCancelRow(importLogId, row, index) {
    	return
    }

    def logInsertRow(importLogId, row, index) {
    	return
    }

    def logUpdateRow(importLogId, row, index) {
    	return
    }

    def logErrorRow(importLogId, row, index, msg) {
    	return
    }

    def getImportLog(importLogId) {
    	return null
    }

    def getImportLogErrorInfo(importLogId) {
		return null
    }

    def findImportLogs(params) {
    	return []
    }

}
