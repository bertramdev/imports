package grails.plugins.imports.test

class TestImportItem5Service {
	static imports = TestImportItem5
	static transactional = false

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
