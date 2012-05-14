package org.grails.plugins.rest

import static org.junit.Assert.*;
import grails.converters.JSON

public class JsonpIntegrationTests extends GroovyTestCase {

    def controller
    
    protected void setUp() {
        super.setUp()
        controller = new JsonRestApiController() 
    }
    
    protected void tearDown() {
        super.tearDown()
    }
    
    public void testNoEntity() {
        controller.params.entity = 'no.such.Domain'
        controller.params.callback = 'testcallback'
        controller.list()
        def expected = 'testcallback({"success":false,"message":"Entity no.such.Domain not found"})'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testEmptyList() {
        controller.params.entity = 'org.grails.plugins.rest.NeverSavedDomain'
        controller.params.callback = 'testcallback'
        controller.list()
        def expected = 'testcallback({"success":true,"data":[],"count":0})'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testSimpleDomain() {
        def one = new SimpleDomain(name:'one').save(flush:true, failOnError:true)
        def oneId = one.id
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.params.callback = 'testcallback'
        controller.list()
        def expected = 'testcallback({"success":true,"data":[{"attached":true,"errors":{"errors":[]},"id":'+oneId+',"name":"one"}],"count":1})'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testEdgeCaseDomain() {
        def one = new EdgeCaseDomain(name:'one').save(flush:true, failOnError:true)
        def oneId = one.id
        controller.params.entity = 'org.grails.plugins.rest.EdgeCaseDomain'
        controller.params.callback = 'testcallback'
        controller.list()
        def expected = 'testcallback({"success":true,"data":[{"attached":true,"errors":{"errors":[]},"id":'+oneId+',"name":"one"}],"count":1})'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testCreateWithErrors() {
        int before = SimpleDomain.count()
        controller.request.method = "POST"
        controller.request.JSON.data = '{}'
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.params.callback = 'testcallback'
        controller.create()
        def expected = 'testcallback({"success":false,"message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be null","errors":[{"field":"name","message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be null"}]})'
        def actual = controller.response.text
        assertEquals expected, actual
        int after = SimpleDomain.count()
        assertEquals before, after
    }
}

