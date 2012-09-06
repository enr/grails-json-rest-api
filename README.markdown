Grails Json Rest API Plugin
===========================

***

This is not the official repo for Grails Json Rest API Plugin.

The official repo is 
[padcom / grails-json-rest-api](https://github.com/padcom/grails-json-rest-api)

***

This version has been tested using Grails 2.1.0.

The main purpose is render domain objects to clients, so some property 
won't be rendered in json ( see `JSONDomainMarshaller.EXCLUDED` field):

 * metaClass
 * class
 * version
 * properties
 * attached
 * grailsApplication instance
 * service artefacts

You can see at integration tests for api usage and expected output.

Sample app
----------

In `test/apps` you can find a basic sample application, taken from
[padcom / grails-json-rest-api-examples](https://github.com/padcom/grails-json-rest-api-examples)

Run `grails test-app` from the sample directory to verify everything is working as expected.
