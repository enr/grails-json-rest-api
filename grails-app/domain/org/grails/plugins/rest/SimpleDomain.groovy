package org.grails.plugins.rest

class SimpleDomain {

    String name

    static expose = 'simple'
    
    static constraints = {
        name blank:false
    }
}
