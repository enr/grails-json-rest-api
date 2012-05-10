package org.grails.plugins.rest

class WithEagerFieldsDomain {

    String name
    
    static hasMany = [addresses: Address]

    static expose = 'witheagerfields'
    
    static api = [
        eagerFields: [ "addresses" ]
    ]
}
