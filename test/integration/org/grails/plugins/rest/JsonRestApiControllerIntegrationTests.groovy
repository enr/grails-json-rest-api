package org.grails.plugins.rest

import static org.junit.Assert.*;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import grails.converters.JSON

public class JsonRestApiControllerIntegrationTests extends GroovyTestCase {

    GrailsApplication grailsApplication
    
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
        controller.list()
        def text = controller.modelAndView
        def expected = '{"success":false,"message":"Entity no.such.Domain not found"}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testEmptyList() {
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.list()
        def text = controller.modelAndView
        def expected = '{"success":true,"data":[],"count":0}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testSimpleDomain() {
        def one = new SimpleDomain(name:'one').save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.list()
        def text = controller.modelAndView
        def expected = '{"success":true,"data":[{"attached":true,"errors":{"errors":[]},"id":1,"name":"one"}],"count":1}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testEdgeCaseDomain() {
        def one = new EdgeCaseDomain(name:'one').save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.EdgeCaseDomain'
        controller.list()
        def text = controller.modelAndView
        def expected = '{"success":true,"data":[{"attached":true,"errors":{"errors":[]},"id":1,"name":"one"}],"count":1}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testCreateWithErrors() {
        int before = SimpleDomain.count()
        controller.request.method = "POST"
        controller.request.JSON.data = '{}'
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.create()
        def text = controller.modelAndView
        def expected = '{"success":false,"message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be null"}'
        def actual = controller.response.text
        assertEquals expected, actual
        int after = SimpleDomain.count()
        assertEquals before, after
    }

    public void testHasManyWithEagerFields() {
        def a1 = new Address(street:'The street, 9', city:'London').save(flush:true, failOnError:true)
        def a2 = new Address(street:'Another road, 2', city:'Manchester').save(flush:true, failOnError:true)
        new WithEagerFieldsDomain(name:'With Eager')
            .addToAddresses(a1)
            .addToAddresses(a2)
            .save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.WithEagerFieldsDomain'
        controller.list()
        // expected something similar to:
        // {"success":true,"data":[{"addresses":[
        //  {"attached":true,"city":"London","errors":{"errors":[]},"id":1},
        //  {"attached":true,"city":"Manchester","errors":{"errors":[]},"id":2}],
        //  "attached":true,"errors":{"errors":[]},"id":1,"name":"With Eager"}],"count":1}'
        def actual = controller.response.text
        def jsonResult = JSON.parse(actual)
        assertTrue "Success", jsonResult.success
        assertEquals 2, jsonResult.data.addresses[0].size()
        def a3 = jsonResult.data.addresses[0][0]
        def a4 = jsonResult.data.addresses[0][1]
        assert ('Manchester' == a3.city || 'Manchester' == a4.city)
        assert ('London' == a3.city || 'London' == a4.city)
        assert (1 == a3.id || 1 == a4.id)
        assert (2 == a3.id || 2 == a4.id)
    }
    
    public void testHasManyWithoutEagerFields() {
        def a1 = new Address(street:'New street, 19', city:'Liverpool').save(flush:true, failOnError:true)
        def a2 = new Address(street:'Beautiful road, 200', city:'Leeds').save(flush:true, failOnError:true)
        new WithoutEagerFieldsDomain(name:'Without Eager')
            .addToAddresses(a1)
            .addToAddresses(a2)
            .save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.WithoutEagerFieldsDomain'
        controller.list()
        // expected something similar to:
        //  {"success":true,"data":[{"addresses":[3,4],"attached":true,"errors":{"errors":[]},
        //  "id":1,"name":"Without Eager"}],"count":1}'
        def actual = controller.response.text
        def jsonResult = JSON.parse(actual)
        assertTrue jsonResult.success
        assertEquals 2, jsonResult.data.addresses[0].size()
        def id1 = jsonResult.data.addresses[0][0]
        def id2 = jsonResult.data.addresses[0][1]
        assert (3 == id1 || 3 == id2)
        assert (4 == id1 || 4 == id2)
    }
}
