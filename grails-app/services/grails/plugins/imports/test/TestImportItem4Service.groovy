package grails.plugins.imports.test

class TestImportItem4Service {
	static imports = TestImportItem4
	static transactional = false

	static async = true
    def columns(params, importLogId) {
    	return [name:'name',item5:'testImportItem5.id']
	}

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
