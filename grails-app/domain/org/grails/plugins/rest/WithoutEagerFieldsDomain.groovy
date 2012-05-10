package org.grails.plugins.rest

class WithoutEagerFieldsDomain {

    String name
    
    static hasMany = [addresses: Address]

    static expose = 'withouteagerfields'

}
