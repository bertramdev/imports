package grails.plugins.imports.test

class TestImportItem1Service {
	static imports = TestImportItem1
	static transactional = false

	def async = true
	def matchProperties = ['id']
	def doValidation = true
	def cancelCheckIncrement = 5

    def columns(params, importLogId) {
		def cols = [emailValue:'emailValue',longValue:'longValue',doubleValue:'doubleValue',stringValueDefault:'stringValueDefault',
                  stringValue:'stringValue',listValue:'listValue',integerValueMarshalled:'integerValueMarshalled',
                  stringValueBeforeBind:'stringValueBeforeBind',stringValueAfterBind:'stringValueAfterBind',
                  integerValue:'integerValue',friendlyName:'aliasValue']
    	return cols
	}

	def confirmationEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}
	def summaryEmailAddress(params, importLogId) { return 'sefiaconsulting@gmail.com'}

	def beforeBindRow(row, index, columns, params, importLogId) {
		log.info('beforeBindRow')
		row.stringValueBeforeBind = 'beforeBind value'
	}

	def afterBindRow(obj, row, index, columns,  params, importLogId) {
		log.info('afterBindRow')
		obj.stringValueAfterBind = 'afterBind value'
	}

	def defaultValue (columnName, propertyName, importLogId) {
		def rtn = null
		if (propertyName == 'stringValueDefault') {
			rtn = new Date().format('HH:mm:ss')
			log.info('defaultValue:'+columnName+'->'+rtn)
		}
		return rtn
	}

	def marshall(columnName,  propertyName, value, importLogId) {
		def rtn = value
		if (propertyName == 'integerValueMarshalled') {
			rtn = new Integer((rtn ?: '0').toString()) + 10
			log.info('marshall:'+columnName+','+value+'->'+rtn)
		}
		return rtn
	}

}
