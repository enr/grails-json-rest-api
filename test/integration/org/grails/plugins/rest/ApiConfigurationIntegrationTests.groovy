package org.grails.plugins.rest

import static org.junit.Assert.*;
import grails.converters.JSON

public class ApiConfigurationIntegrationTests extends GroovyTestCase {

    def controller
    
    protected void setUp() {
        super.setUp()
        controller = new JsonRestApiController() 
    }
    
    protected void tearDown() {
        super.tearDown()
    }
    
    public void testHasManyWithEagerFields() {
        def a1 = new JsonRestApiAddress(street:'The street, 9', city:'London').save(flush:true, failOnError:true)
        def a2 = new JsonRestApiAddress(street:'Another road, 2', city:'Manchester').save(flush:true, failOnError:true)
        new WithEagerFieldsDomain(name:'With Eager')
            .addToAddresses(a1)
            .addToAddresses(a2)
            .save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.WithEagerFieldsDomain'
        controller.list()
        // expected something similar to:
        // {"success":true,"data":[{"addresses":[
        // {"attached":true,"city":"London","errors":{"errors":[]},"id":1},
        // {"attached":true,"city":"Manchester","errors":{"errors":[]},"id":2}],
        // "attached":true,"errors":{"errors":[]},"id":1,"name":"With Eager"}],"count":1}'
        def actual = controller.response.text
        def jsonResult = JSON.parse(actual)
        assertTrue "Success", jsonResult.success
        assertEquals 2, jsonResult.data.addresses[0].size()
        // order and id are random, so we need a little workaround
        // to be cleaned
        def a1Id = a1.id
        def a2Id = a2.id
        def a3 = jsonResult.data.addresses[0][0]
        def a4 = jsonResult.data.addresses[0][1]
        assert ('Manchester' == a3.city || 'Manchester' == a4.city)
        assert ('London' == a3.city || 'London' == a4.city)
        assert (a1Id == a3.id || a1Id == a4.id)
        assert (a2Id == a3.id || a2Id == a4.id)
    }
    
    public void testHasManyWithoutEagerFields() {
        def a1 = new JsonRestApiAddress(street:'New street, 19', city:'Liverpool').save(flush:true, failOnError:true)
        def a2 = new JsonRestApiAddress(street:'Beautiful road, 200', city:'Leeds').save(flush:true, failOnError:true)
        new WithoutEagerFieldsDomain(name:'Without Eager')
            .addToAddresses(a1)
            .addToAddresses(a2)
            .save(flush:true, failOnError:true)
        controller.params.entity = 'org.grails.plugins.rest.WithoutEagerFieldsDomain'
        controller.list()
        // expected something similar to:
        // {"success":true,"data":[{"addresses":[3,4],"attached":true,"errors":{"errors":[]},
        // "id":1,"name":"Without Eager"}],"count":1}'
        def actual = controller.response.text
        def jsonResult = JSON.parse(actual)
        assertTrue jsonResult.success
        assertEquals 2, jsonResult.data.addresses[0].size()
        // order and id are random, so we need a little workaround
        // to be cleaned
        def a1Id = a1.id
        def a2Id = a2.id
        def id1 = jsonResult.data.addresses[0][0]
        def id2 = jsonResult.data.addresses[0][1]
        assert (a1Id == id1 || a1Id == id2)
        assert (a2Id == id1 || a2Id == id2)
    }

}

