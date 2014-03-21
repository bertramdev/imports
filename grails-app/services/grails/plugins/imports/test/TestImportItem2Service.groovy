package grails.plugins.imports.test

class TestImportItem2Service {
	static imports = TestImportItem2
	static transactional = false

	def async = false
	def matchProperties = ['stringValue']
	def doValidation = true
	def doConfirmationEmail = true 
	def doSummaryEmail = true

	def beforeBindRow(row, index,columns, params, importLogId) {
		log.info('beforeBindRow')
		row.longValue = System.currentTimeMillis() - 1383600000000l
	}

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}


}
