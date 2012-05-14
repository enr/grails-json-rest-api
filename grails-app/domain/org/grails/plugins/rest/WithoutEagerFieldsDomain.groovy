package org.grails.plugins.rest

class WithoutEagerFieldsDomain {

    String name
    
    static hasMany = [addresses: JsonRestApiAddress]

    static expose = 'withouteagerfields'

}
