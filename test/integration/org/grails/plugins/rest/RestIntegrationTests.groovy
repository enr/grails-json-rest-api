package org.grails.plugins.rest

import static org.junit.Assert.*;
import grails.converters.JSON

public class RestIntegrationTests extends GroovyTestCase {

    def controller
    
    protected void setUp() {
        super.setUp()
        controller = new JsonRestApiController()
    }
    
    protected void tearDown() {
        super.tearDown()
    }
    
    public void testNonexistentEntity() {
        controller.params.entity = 'no.such.Domain'
        controller.list()
        def expected = '{"success":false,"message":"Entity no.such.Domain not found"}'
        def actual = controller.response.text
        assertEquals expected, actual
    }

    public void testNoEntityParam() {
        controller.list()
        def expected = '{"success":false,"message":"Entity null not found"}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testEmptyList() {
        controller.params.entity = 'org.grails.plugins.rest.NeverSavedDomain'
        controller.list()
        def expected = '{"success":true,"data":[],"count":0}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testSingleElementList() {
        def one = new SimpleDomain(name:'one').save(flush:true, failOnError:true)
        def oneId = one.id
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.list()
        def expected = '{"success":true,"data":[{"errors":{"errors":[]},"id":'+oneId+',"name":"one"}],"count":1}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testList() {
        (1..10).collect {
            new SimpleDomain(name:"domain_${it}").save(flush:true, failOnError:true)
        }
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.list()
        def actual = controller.response.text
        def jsonResult = JSON.parse(actual)
        assertTrue jsonResult.success
        assertEquals 10, jsonResult.count
        assertEquals 10, jsonResult.data.size()
        (1..10).collect {
            assertEquals "domain_${it}" as String, jsonResult.data[(it - 1)].name
        } 
    }

    public void testCreate() {
        int before = SimpleDomain.count()
        controller.request.method = "POST"
        controller.request.JSON = '{"data":{"name":"simple test"}}'
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.create()
        def responseText = controller.response.text
        assertNotNull responseText
        def jsonResult = JSON.parse(responseText)
        assertTrue jsonResult.success
        assertEquals "simple test", jsonResult.data.name
        int after = SimpleDomain.count()
        assertEquals "number of records after creation", before+1, after
    }
    
    public void testCreateWithErrors() {
        int before = SimpleDomain.count()
        controller.request.method = "POST"
        controller.request.JSON = '{"data":{}}'
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.create()
        def expected = '{"success":false,"message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be null","errors":[{"field":"name","message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be null"}]}'
        def actual = controller.response.text
        assertEquals expected, actual
        int after = SimpleDomain.count()
        assertEquals before, after
    }

    public void testUpdate() {
        def simple = new SimpleDomain(name:'simple one')
        simple.eagerFieldsAllowed = true
        simple.save(flush:true, failOnError:true)
        def withEager = new WithEagerFieldsDomain(name:'With Eager S', simple:simple)
        withEager.eagerFieldsAllowed = true
        withEager.save(flush:true, failOnError:true)
        def domainId = withEager.id
        def simpleId = simple.id

        def jsonData = '{"id":'+domainId+',"name":"brand new name","simple":{"errors":{"errors":[]},"id":'+simpleId+',"name":"nneeww"}}'

        controller.request.method = "PUT"
        controller.request.JSON = '{"data":'+jsonData+'}'

        controller.params.id = domainId
        controller.params.entity = 'org.grails.plugins.rest.WithEagerFieldsDomain'
        controller.update()
        // {"success":true,"data":{"addresses":null,"attached":true,"errors":{"errors":[]},"id":6,"name":"brand new name","simple":5}}
        def responseText = controller.response.text
        assertNotNull responseText
        def jsonResult = JSON.parse(responseText)
        
        assertTrue jsonResult.success
        assertEquals "brand new name", jsonResult.data.name
        assertEquals domainId, jsonResult.data.id
        assertEquals simpleId, jsonResult.data.simple
    }
    
    public void testUpdateDomainWithHasManyField() {
        def rome = new JsonRestApiAddress(street:'La strada, 8', city:'Rome').save(flush:true, failOnError:true)
        def milan = new JsonRestApiAddress(street:'Via MI, 8', city:'Milan').save(flush:true, failOnError:true)
        def liverpool = new JsonRestApiAddress(street:'New street, 11', city:'Liverpool').save(flush:true, failOnError:true)
        def leeds = new JsonRestApiAddress(street:'Beautiful road, 11', city:'Leeds').save(flush:true, failOnError:true)
        def withEagerLiverpoolAndLeeds = new WithEagerFieldsDomain(name:'With Eager L+L')
            .addToAddresses(liverpool)
            .addToAddresses(leeds)
            .save(flush:true, failOnError:true)
        def domainId = withEagerLiverpoolAndLeeds.id
        def jsonData = '{"addresses":['+rome.id+','+milan.id+',],"id":'+domainId+',"name":"UPDATED_NAME"}'
        controller.request.method = "PUT"
        controller.request.JSON = '{"data":'+jsonData+'}'
        controller.params.id = domainId
        controller.params.entity = 'org.grails.plugins.rest.WithEagerFieldsDomain'
        controller.update()
        def expected = '{"success":true,"data":{"addresses":['+rome.id+','+milan.id+'],"errors":{"errors":[]},"id":'+domainId+',"name":"UPDATED_NAME","simple":null}}'
        def actual = controller.response.text
        assertEquals expected, actual
    }
    
    public void testUpdateWithErrors() {
        def simple = new SimpleDomain(name:'simple one').save(flush:true, failOnError:true)
        def simpleId = simple.id
        controller.request.method = "PUT"
        controller.request.JSON = '{"data":{"name":""}}'
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.params.id = simpleId
        controller.update()
        def expectedErrormessage = "Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be blank"
        // {"success":false,"[data":{"attached":true,"errors":{"errors":[{"object":"org.grails.plugins.rest.SimpleDomain","field":"name","rejected-value":"",
        //  "message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be blank"}]},"id":15,"name":""},
        //  "message":"Property [name] of class [class org.grails.plugins.rest.SimpleDomain] cannot be blank"]}
        def responseText = controller.response.text
        assertNotNull responseText
        def jsonResult = JSON.parse(responseText)
        assertFalse jsonResult.success
        assertEquals expectedErrormessage, jsonResult.message
        assertEquals "errors size", 1, jsonResult.data.errors.errors.size()
        assertEquals "error field", "name", jsonResult.data.errors.errors[0].field
        assertEquals "error message", expectedErrormessage, jsonResult.data.errors.errors[0].message
    }
    
    public void testDelete() {
        def one = new SimpleDomain(name:'one').save(flush:true, failOnError:true)
        def oneId = one.id
        int before = SimpleDomain.count()
        controller.params.entity = 'org.grails.plugins.rest.SimpleDomain'
        controller.params.id = oneId
        controller.delete()

        def responseText = controller.response.text
        assertNotNull responseText
        def jsonResult = JSON.parse(responseText)
        assertTrue jsonResult.success
        assertNull jsonResult.message
        assertEquals "errors size", 0, jsonResult.data.errors.errors.size()
        
        int after = SimpleDomain.count()
        assertEquals before - 1, after
    }
}

