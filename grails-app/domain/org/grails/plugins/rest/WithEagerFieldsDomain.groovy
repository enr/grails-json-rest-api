package org.grails.plugins.rest

class WithEagerFieldsDomain {

    String name
    
    SimpleDomain simple
    
    static hasMany = [addresses: JsonRestApiAddress]

    static expose = 'witheagerfields'
    
    static api = [
        eagerFields: [ "addresses", "simple" ]
    ]
    
    static constraints = {
        simple nullable:true
    }
}
