dataSource {
    pooled = true
    url = System.getProperty('db.url', "jdbc:mysql://localhost/imports")
    driverClassName = System.getProperty('db.driverClassName', "com.mysql.jdbc.Driver")
    username = System.getProperty('db.username', "root")
    password = System.getProperty('db.password', "")
    dbCreate = "update"
    logSql = System.getProperty('db.logSql') == 'true'
    dialect = org.hibernate.dialect.MySQL5InnoDBDialect
    println "db.url: "+ url
    println "db.username: "+username
    println "db.password: "+password
    println "dbCreate: "+dbCreate
    println "db.logSql: "+logSql
    properties {
      validationQuery = 'select 1'
      testOnBorrow = true
      testOnReturn = false
      testWhileIdle = true
      timeBetweenEvictionRunsMillis = 300000
      numTestsPerEvictionRun = 3
      minEvictableIdleTimeMillis = 600000
      initialSize = 1
      minIdle = 1
      maxActive = 10
      maxIdle = 10000
      maxWait = 90000
      removeAbandoned = true
      removeAbandonedTimeout = 6000
      logAbandoned = true
    }
}


grails {
  mongo {
    host = '127.0.0.1'
    port = 27017
    databaseName = "Import"
    options {
      autoConnectRetry = true
      connectTimeout = 300
    }
  }
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
        }
    }
    test {
        dataSource {
        }
    }
    production {
        dataSource {
        }
    }
}


