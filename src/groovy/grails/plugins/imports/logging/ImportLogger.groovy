package grails.plugins.imports.logging


interface ImportLogger {
	def createImportLog(params)
    def incrementImportCounter(importLogId)
    def setImportTotal(importLogId, total)
    def setImportLogValue(importLogId, name, value)
    def cancel(importLogId)
    def isCanceled(importLogId)
    def isImportComplete(importLogId)
    def logMessage(importLogId, valuesMap)
    def logSuccessRow(importLogId, row, index)
    def logCancelRow(importLogId, row, index)
    def logInsertRow(importLogId, row, index)
    def logUpdateRow(importLogId, row, index)
    def logErrorRow(importLogId, row, index, msg)
    def getImportLog(importLogId)
    def getImportLogErrorInfo(importLogId)
    def findImportLogs(params)
}
