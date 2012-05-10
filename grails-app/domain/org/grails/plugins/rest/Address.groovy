package org.grails.plugins.rest

class Address {

    String street
    
    String city

    static expose = 'address-expose'
    
    static api = [
        excludedFields: [ "street" ]
    ]
}
