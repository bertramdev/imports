package grails.plugins.imports

import grails.converters.*

class ImportsController {
	def importsService
    def grailsApplication

	static allowedMethods = [showInfo:'GET',
                             process:'POST' // get for testing
                             ]

	def noSupport() {
		response.sendError(405)
	}

    def showInfo() { 
        def infoUrl = importsService.getInfoUrl(params.entityName)
        println infoUrl
        if (infoUrl) {
            redirect uri:infoUrl
            return
        } else {
            render view:'showInfo'
        }
    }

    def showLog() {
        def rtn = [success:true]
        rtn.log = importsService.getLog(request, params)
        withFormat {
            xml {
                render rtn as XML
            }
            json {
                render rtn as JSON
            }
            html {
                // allow config for redirect thank you page
                def view = 'showLog'
                if (grailsApplication.config.grails.plugins.imports.containsKey('logView')) {
                    view = grailsApplication.config.grails.plugins.imports.logView
                }
                render view: view, model:rtn
            }
        }
    }

    def cancel() {
        def rtn = [importLogId:(params.importLogId ?: params.id)]
        rtn.success = importsService.cancel(request, params)
        withFormat {
            xml {
                render rtn as XML
            }
            json {
                render rtn as JSON
            }
            html {
                // allow config for redirect thank you page
                def view = 'cancel'
                if (grailsApplication.config.grails.plugins.imports.containsKey('cancelView')) {
                    view = grailsApplication.config.grails.plugins.imports.cancelView
                }
                render view: view, model:rtn
            }
        }
    }

    def showErrorRows() {
        def rtn = importsService.getErrorRowInfo(request, params) ?: [:]
        rtn.success = true
        withFormat {
            xml {
                render rtn as XML
            }
            json {
                render rtn as JSON
            }
            csv {
                def csv = null
                rtn.errorRows?.each {row->
                    if (csv == null) csv = ''
                    else csv += '\n'
                    row.eachWithIndex {col,idx->
                        col = col.toString()
                        if (idx > 0) csv += ','
                        if (col.contains(',')) col = '"'+col+'"'
                        csv += col.replaceAll('\n','')
                    }
                }
                response.setHeader("Content-disposition", "attachment;filename=${rtn.fileName ?: 'NO_ERRORS.csv'}")
                response.setContentType('text/csv')
                response.outputStream << (csv ?: '').bytes
            }
            html {
                // allow config for redirect thank you page
                def view = 'showErrorRows'
                if (grailsApplication.config.grails.plugins.imports.containsKey('errorRowView')) {
                    view = grailsApplication.config.grails.plugins.imports.errorRowView
                }
                render view: view, model:rtn
            }
        }
    }

    def process() {
        def rtn = [success:false],
            parameterName
        try {
            importsService.validateEntityName(params.entityName)
            rtn.importLogId = importsService.processRequest(request, params)
            rtn.success = true
        } catch (ImportsException pie) {
            log.error(pie)
            if (grailsApplication.config.grails.plugins.imports.throwError == true || params.boolean('throwError') == true) throw pie
            rtn.msg = pie.message
            rtn.status = pie.responseCode
            rtn.importLogId = pie.importLogId
            response.status = pie.responseCode
        } catch (Exception e) {
            log.error(e)
            if (grailsApplication.config.grails.plugins.imports.throwError == true || params.boolean('throwError') == true) throw pie
            rtn.msg = e.message
            rtn.status = 500
            response.status = 500
            rtn.trace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e)
        }
        withFormat {
            xml {
                render rtn as XML
            }
            json {
                render rtn as JSON
            }
            html {
                // allow config for redirect thank you page
                def view = 'process'
                if (grailsApplication.config.grails.plugins.imports.containsKey('confirmationView')) {
                    view = grailsApplication.config.grails.plugins.imports.confirmationView
                }
                render view: view, model:rtn
            }
        }
    }
}
