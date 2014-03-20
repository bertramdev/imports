package grails.plugins.imports.test

class TestImportNoItemService {
	static imports = 'NoItem'

 	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
