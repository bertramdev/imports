package grails.plugins.imports.test

class TestImportNoItemService {
	static imports = 'NoItem'
	def doConfirmationEmail = true 
	def doSummaryEmail = true

 	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
