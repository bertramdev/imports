package grails.plugins.imports.test

class TestImportItem3Service {
	static imports = TestImportItem3
	static transactional = false

	def useQueue = true

	def beforeBindRow(row, index,columns, params, importLogId) {
		//log.info('beforeBindRow')
		row.longValue = System.currentTimeMillis() - 1383600000000l
	}

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
}
