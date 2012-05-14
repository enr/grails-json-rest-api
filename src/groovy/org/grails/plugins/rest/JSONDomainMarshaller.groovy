package org.grails.plugins.rest

//
// CustomDomainMarshaller.groovy by Siegfried Puchbauer
//
// http://stackoverflow.com/questions/1700668/grails-jsonp-callback-without-id-and-class-in-json-file/1701258#1701258
//

import grails.converters.JSON;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler;
import org.codehaus.groovy.grails.web.converters.ConverterUtil;
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException;
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller;
import org.codehaus.groovy.grails.web.json.JSONWriter;
import org.springframework.beans.BeanUtils;

public class JSONDomainMarshaller implements ObjectMarshaller<JSON> {

    static EXCLUDED = [
        'metaClass',
        'class',
        'version',
        'properties'
    ]

    private GrailsApplication application

    public JSONDomainMarshaller(GrailsApplication application) {
        this.application = application
    }

    public boolean supports(Object object) {
        return isDomainClass(object.getClass())
    }

    private getCustomApi(clazz) {
        clazz.declaredFields.name.contains('api') ? clazz.api : null
    }

    public void marshalObject(Object o, JSON json) throws ConverterException {
        JSONWriter writer = json.getWriter();
        try {
            writer.object();
            def properties = BeanUtils.getPropertyDescriptors(o.getClass());
            def excludedFields = getCustomApi(o.class)?.excludedFields

            /*
             * Taken from ryannorris/grails-json-rest-api:
             * This allows for a customization to allow for collections of child 
             * objects (*..n) to be explicitly marshalled with the entity that
             * was returned.  Useful when looking to optimize client library 
             * performance and foregoing frequent round trips to the api 
             */
            def eagerFields = getCustomApi(o.class)?.eagerFields
            def eagerFieldsAllowed = o.eagerFieldsAllowed
            for (property in properties) {
                String name = property.getName();
                if(!(EXCLUDED.contains(name) || excludedFields?.contains(name))) {
                    def readMethod = property.getReadMethod();
                    if (readMethod != null) {
                        def value = readMethod.invoke(o, (Object[]) null);
                        if (value instanceof List || value instanceof Set) {
                            writer.key(name);
                            writer.array()
                            value.each { item ->
                                if ((isDomainClass(item.getClass())) &&
                                    (!eagerFields?.contains(name) || !eagerFieldsAllowed)) {
                                    json.convertAnother(item.id);
                                } else {
                                    json.convertAnother(item);
                                }
                            }
                            writer.endArray()
                        } else if (isDomainClass(value.getClass())) {
                            writer.key(name);
                            json.convertAnother(value.id);
                        } else if (isServiceClass(value.getClass())) {
                            log.debug "skipping service ${name}"
                        } else if (isGrailsApplication(value)) {
                            log.debug "skipping grails application ${name}"
                        } else {
                            writer.key(name);
                            json.convertAnother(value);
                        }
                    }
                } else {
                    log.debug "skipping excluded property ${name}"
                }
            }
            writer.endObject();
        } catch (Exception e) {
            throw new ConverterException("Exception in JSONDomainMarshaller", e);
        }
    }

    private boolean isDomainClass(Class clazz) {
        String name = ConverterUtil.trimProxySuffix(clazz.getName());
        return application.isArtefactOfType(DomainClassArtefactHandler.TYPE, name);
    }
    
    private boolean isServiceClass(Class clazz) {
        String name = ConverterUtil.trimProxySuffix(clazz.getName());
        return application.isArtefactOfType(ServiceArtefactHandler.TYPE, name);
    }
        
    private boolean isGrailsApplication(Object o) {
        return (o instanceof DefaultGrailsApplication)
    }
}
