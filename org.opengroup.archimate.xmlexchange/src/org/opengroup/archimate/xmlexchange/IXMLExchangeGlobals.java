/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import org.jdom2.Namespace;


/**
 * Global Element/Attribute/Namespace
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public interface IXMLExchangeGlobals {

    String FILE_EXTENSION = ".xml";
    String FILE_EXTENSION_WILDCARD = "*.xml";
    
    String OPEN_GROUP_NAMESPACE_PREFIX = "archimate";
    
    Namespace OPEN_GROUP_NAMESPACE = Namespace.getNamespace("http://www.opengroup.org/xsd/archimate");
    Namespace OPEN_GROUP_NAMESPACE_EMBEDDED = Namespace.getNamespace(OPEN_GROUP_NAMESPACE_PREFIX, OPEN_GROUP_NAMESPACE.getURI());
    //String OPEN_GROUP_SCHEMA_LOCATION = "http://www.opengroup.org/xsd/archimate_v2p1.xsd";
    String OPEN_GROUP_SCHEMA_LOCATION = "archimate_v2p1.xsd";
    
    Namespace XSI_NAMESPACE = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    
    Namespace DC_NAMESPACE = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
    String DC_SCHEMA_LOCATION = "dc.xsd";
    
    
    String ELEMENT_MODEL = "model";
    String ELEMENT_LABEL = "label";
    String ELEMENT_NAME = "name";
    String ELEMENT_DOCUMENTATION = "documentation";
    String ELEMENT_ELEMENT = "element";
    String ELEMENT_ELEMENTS = "elements";
    String ELEMENT_RELATIONSHIPS = "relationships";
    String ELEMENT_RELATIONSHIP = "relationship";
    String ELEMENT_PROPERTYDEFS = "propertydefs";
    String ELEMENT_PROPERTYDEF = "propertydef";
    String ELEMENT_PROPERTIES = "properties";
    String ELEMENT_PROPERTY = "property";
    String ELEMENT_VALUE = "value";
    String ELEMENT_ORGANIZATION = "organization";
    String ELEMENT_ITEM = "item";
    
    String ELEMENT_METADATA = "metadata";
    String ELEMENT_SCHEMA = "schema";
    String ELEMENT_SCHEMAVERSION = "schemaversion";
    
    String ATTRIBUTE_LABEL = "label";
    String ATTRIBUTE_NAME = "name";
    String ATTRIBUTE_IDENTIFIER = "identifier";
    String ATTRIBUTE_IDENTIFIERREF = "identifierref";
    String ATTRIBUTE_TYPE = "type";
    String ATTRIBUTE_SOURCE = "source";
    String ATTRIBUTE_TARGET = "target";
    
    
}
