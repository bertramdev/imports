package grails.plugins.imports.test

class TestImportItem4Service {
	static imports = TestImportItem4
	static transactional = false

	def async = true

	def doValidation = true
    def columns(params, importLogId) {
    	return [name:'name',item5:'testImportItem5.id', item5name:'lookupItem5Name']
	}
	def validateFile(uploadedFile, params, importLogId) {
		def testItem = TestImportItem5.get(1l)
		if (!testItem) {
			testItem = new TestImportItem5(name:'poop')
			println '>>>>>'+testItem.save()
		}
	}

	def afterBindRow(obj, row, index, columns, params, importLogId) {
		println('afterBindRow:'+row)
		if (row['item5name']) {
			obj.testImportItem5= TestImportItem5.findByName(row['item5name'])
		}

    }

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
