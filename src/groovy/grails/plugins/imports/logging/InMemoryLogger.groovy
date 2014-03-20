package grails.plugins.imports.logging

import grails.plugins.imports.*


class InMemoryLogger implements ImportLogger{
	static internalData = [:]
	
	static getImportLogMap(importLogId) {
		return internalData[importLogId]
	}

	def createImportLog(params) {
        def doc = DefaultLogger.getLogTemplate(params)
            
        if (params.logValues instanceof Map) {
            doc += params.logValues
        }
        internalData[doc._id] = doc
        return doc._id
	}

    def incrementImportCounter(importLogId) {
    	def map = getImportLogMap(importLogId)
    	map.counter = map.counter ?: 0i
    	map.counter++
		return
    }

    def setImportTotal(importLogId, total) {
    	def map = getImportLogMap(importLogId)
    	map.total = total
    }

    def setImportLogValue(importLogId, name, value) {
    	def map = getImportLogMap(importLogId)
    	map[name] = value
    }

    def cancel(importLogId) {
    	def map = getImportLogMap(importLogId)
    	map.canceled = true
		return true
    }
    def isCanceled(importLogId) {
    	def map = getImportLogMap(importLogId)
		return map.canceled == true
    }

    def isImportComplete(importLogId) {
    	def map = getImportLogMap(importLogId)
    	if (map.total && map.processed) {
    		return map.total == map.processed
    	}
    	return false
    }

    def logMessage(importLogId, valuesMap) {
 	  	def map = getImportLogMap(importLogId)
 	  	map.messages << valuesMap
     }

    def logSuccessRow(importLogId, row, index) {
        def map = getImportLogMap(importLogId)
        map.successCount = map.successCount ?: 0i
        map.successCount++
        return

    }

    def logErrorRow(importLogId, row, index, msg) {
        def map = getImportLogMap(importLogId)
        map.errorCount = map.errorCount ?: 0i
        delegate.log.debug('logErrorRow')
        if (msg) row[DefaultImporter.IMPORT_ERROR] = msg
        if (index) row[DefaultImporter.IMPORT_INDEX] = index
        map.errorCount++
        map.errorRows << row

        return
    }

    def logCancelRow(importLogId, row, index) {
        def map = getImportLogMap(importLogId)
        map.cancelCount = map.cancelCount ?: 0i
        map.cancelCount++
        return
    }

    def logInsertRow(importLogId, row, index) {
        def map = getImportLogMap(importLogId)
        map.insertCount = map.insertCount ?: 0i
        map.insertCount++
        return
    }

    def logUpdateRow(importLogId, row, index) {
        def map = getImportLogMap(importLogId)
        map.updateCount = map.updateCount ?: 0i
        map.updateCount++
        return
    }

    def getImportLog(importLogId) {
		getImportLogMap(importLogId)
    }

    def getImportLogErrorInfo(importLogId) {
        def doc = getImportLogMap(importLogId),
            headers = doc?.headers,
            errorRows = doc?.errorRows,
            rtn = [errorRows:[]]
        if (headers) {
            headers << DefaultImporter.IMPORT_INDEX
            headers << DefaultImporter.IMPORT_ERROR
            rtn.errorRows << headers
            if (errorRows) {
                errorRows.each {row ->
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
		return internalData.values()	    
    }

}
