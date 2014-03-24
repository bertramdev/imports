import grails.util.GrailsNameUtils as NU
import grails.plugins.imports.DefaultImporter as DF
import grails.plugins.imports.ImportsService as IS
import grails.plugins.imports.logging.DefaultLogger 
import grails.plugins.imports.logging.MongoLogger
import grails.plugins.imports.logging.InMemoryLogger

class BulkDataImportsGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.0 > *"
    def title = "Bulk Data Imports Plugin"
    def author = "Jeremy Leng"
    def authorEmail = "jleng@bcap.com"
    def description = '''\
Bulk Data Imports Plugin simplifies importing of bulk data via file uploads
Default support for CSV and domain classes
'''
    def pluginExcludes = [
        "grails-app/controllers/grails/plugins/imports/test/*",
        "grails-app/domain/grails/plugins/imports/test/*",
        "grails-app/services/grails/plugins/imports/test/*",
        "grails-app/views/importsTest/index.gsp"
    ]
    def loadBefore = ['rabbitmq','rabbit-amqp']
    def documentation = "http://grails.org/plugin/procon-import-plugin"
    def watchedResources = "file:./grails-app/services/*Service.groovy"
    def organization = [ name: "BertramLabs", url: "http://www.bertramlabs.com/" ]
    def doWithSpring = {
        def loggingProvider = application.config.grails.plugins.imports.containsKey('loggingProvider') ? application.config.grails.plugins.imports.loggingProvider : 'default'
        if (loggingProvider == 'mongo') {
            importsLogger(MongoLogger)
        } else if (loggingProvider == 'mem') {
            importsLogger(InMemoryLogger)
        } else if (loggingProvider == 'default') {
            importsLogger(DefaultLogger)
        } else {
            Class clazz = Class.forName(loggingProvider, true, Thread.currentThread().contextClassLoader)
            importsLogger(clazz)
        }

        for(service in application.serviceClasses) {
            if (service.hasProperty('imports')) {
                def entityName,
                    imports = service.getPropertyValue('imports')
                if (imports instanceof Class || imports instanceof String) {
                    entityName = NU.getPropertyName(imports)
                }
                if (entityName) {
                    def found = application.domainClasses?.find { NU.getPropertyName(it.name) == entityName} != null
                    if (!found  && !service.hasMetaMethod('processRow', getArgs(3)) ) {
                        log.warn('\n    BulkDataImports: could not configure importer '+service.shortName+'... no domain class found and missing processRow method')
                    } else {
                        DF.SERVICE_METHODS.each {k,v-> 
                            if (!service.hasMetaMethod(k, getArgs(v))) service.metaClass."${k}" = DF."${k}"
                        }
                        def props = DF.SERVICE_PROPERTIES.clone()
                        props.entityName = entityName
                        props.each {k, v->
                            if (!service.hasMetaMethod(k)) service.metaClass."${k}" = { ->  v } 
                        }
                        IS.IMPORT_CONFIGURATIONS[entityName] = NU.getPropertyNameRepresentation(service.shortName)
                    }
                } else {
                    log.warn('\n    BulkDataImports: invalid imports configuration for '+service.shortName+' :'+imports)
                }
            }
        }
        IS.IMPORT_CONFIGURATIONS.each {k,v-> log.info('\n    BulkDataImports:'+ k + ' imported by '+v) }
    }
    def onChange = { event -> if (event.source) doWithSpring() }
    private getArgs(ct) {
        (1..ct).collect { Object.class }.toArray()
    }
}
