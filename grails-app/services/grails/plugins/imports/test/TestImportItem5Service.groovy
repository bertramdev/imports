package grails.plugins.imports.test

class TestImportItem5Service {
	static imports = TestImportItem5
	static transactional = false
	def validateFile(uploadedFile, params, importLogId) {
		def testItem = TestImportItem5.get(1l)
		if (!testItem) new TestImportItem5(name:'poop').save()
	}
	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
