
import grails.util.Environment
import mypkg.*

class BootStrap {

    def localeResolver

    def init = { servletContext ->
    
        // functional tests against messages in 'en'
        def defaultLocale = Locale.ENGLISH
        println "locale initialized to         ${defaultLocale}"
        localeResolver.defaultLocale = defaultLocale
        println "localeResolver ${localeResolver.getClass().getName()} ${localeResolver}"
        java.util.Locale.setDefault(defaultLocale)
    
        switch (Environment.current) {
            case Environment.DEVELOPMENT:
                new Address(street:'Grails Road', city:'London').save(flush:true, failOnError:true)
                new Address(street:'Groovier Street', city:'London').save(flush:true, failOnError:true)
        
                new Person(firstName:'Milla', lastName:'Booo').save(flush:true, failOnError:true)
                new Person(firstName:'Hellen', lastName:'Mhaaa').save(flush:true, failOnError:true)
        }
    }

    def destroy = {
    }
}
