package org.grails.plugins.rest

import static org.junit.Assert.*;
import grails.converters.JSON

public class MarshallingIntegrationTests extends GroovyTestCase {

    def controller
    
    protected void setUp() {
        super.setUp()
        controller = new JsonRestApiController()
    }
    
    protected void tearDown() {
        super.tearDown()
    }
    
    public void testDomainWithInjectedGrailsApplication() {
        def one = new EdgeCaseDomain(name:'one').save(flush:true, failOnError:true)
        def oneId = one.id
        controller.params.entity = 'org.grails.plugins.rest.EdgeCaseDomain'
        controller.list()
        def expected = '{"success":true,"data":[{"errors":{"errors":[]},"id":'+oneId+',"name":"one"}],"count":1}'
        def actual = controller.response.text
        assertEquals expected, actual
    }

}

