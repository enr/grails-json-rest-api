package org.grails.plugins.rest

class JsonRestApiAddress {

    String street
    
    String city
    
    WithEagerFieldsDomain owner

    static expose = 'jsonrestapiaddress'
    
    static api = [
        excludedFields: [ "street" ]
    ]
    
    static constraints = {
        owner nullable:true
    }
}
