class ImportUrlMappings {
	static mappings = {
		"/imports/$action?/$id?"(controller:"imports")
		"/imports/test"(controller:"importsTest")
        "/api/import/csv/$entityName?"(controller:"imports"){
        	importType = 'csv'
            action = [GET:"showInfo", PUT:"noSupport", DELETE:"noSupport", POST:"process"]
        }	
        "/api/import/$entityName?"(controller:"imports") {
            action = [GET:"showInfo", PUT:"noSupport", DELETE:"noSupport", POST:"process"]
        }	
	}
}
