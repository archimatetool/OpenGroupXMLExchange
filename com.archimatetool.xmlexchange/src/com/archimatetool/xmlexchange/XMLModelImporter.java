/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.IRelationship;
import com.archimatetool.model.util.ArchimateModelUtils;




/**
 * XML Model Importer
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLModelImporter implements IXMLExchangeGlobals {
    
    private IArchimateModel fModel;
    
    // Properties
    private Map<String, String> fPropertyDefsList;
    
    public IArchimateModel createArchiMateModel(File instanceFile) throws IOException, JDOMException {
        // Create a new Archimate Model and set its defaults
        fModel = IArchimateFactory.eINSTANCE.createArchimateModel();
        fModel.setDefaults();
        
        // Read file without Schema validation
        Document doc = JDOMUtils.readXMLFile(instanceFile);
        
        // Parse Property Definitions first
        parsePropertyDefinitions(doc.getRootElement());
        
        // Parse Root Element
        parseRootElement(doc.getRootElement());
        
        // Parse ArchiMate Elements
        parseArchiMateElements(doc.getRootElement());
        
        // Parse ArchiMate Relations
        parseArchiMateRelations(doc.getRootElement());
        
        // TODO Organization

        return fModel;
    }
    
    void parsePropertyDefinitions(Element rootElement) {
        fPropertyDefsList = null;
        
        Element propertydefsElement = rootElement.getChild(ELEMENT_PROPERTYDEFS, OPEN_GROUP_NAMESPACE);
        if(propertydefsElement == null) {
            return;
        }
        
        fPropertyDefsList = new HashMap<String, String>();
        
        // Archi only supports String types so we can ignor the data type
        for(Element propertyDefElement : propertydefsElement.getChildren(ELEMENT_PROPERTYDEF, OPEN_GROUP_NAMESPACE)) {
            String identifier = propertyDefElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            String name = propertyDefElement.getAttributeValue(ATTRIBUTE_NAME);
            if(identifier != null && name != null) {
                fPropertyDefsList.put(identifier, name);
            }
        }
    }
    
    void parseRootElement(Element rootElement) {
        // Identifier
        String id = rootElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
        if(id != null) {
            fModel.setId(id);
        }
        
        // Name
        String name = getChildElementTextNormalized(rootElement, ELEMENT_NAME);
        if(name != null) {
            fModel.setName(name);
        }
        
        // Documentation
        String documentation = getChildElementText(rootElement, ELEMENT_DOCUMENTATION);
        if(documentation != null) {
            fModel.setPurpose(documentation);
        }
        
        // Properties
        addProperties(rootElement, fModel);
    }
    
    void addProperties(Element parentElement, IProperties propertiesModel) {
        Element propertiesElement = parentElement.getChild(ELEMENT_PROPERTIES, OPEN_GROUP_NAMESPACE);
        if(propertiesElement != null) {
            for(Element propertyElement : propertiesElement.getChildren(ELEMENT_PROPERTY, OPEN_GROUP_NAMESPACE)) {
                String idref = propertyElement.getAttributeValue(ATTRIBUTE_IDENTIFIERREF);
                if(idref != null) {
                    String propertyName = fPropertyDefsList.get(idref);
                    if(propertyName != null) {
                        String propertyValue = getChildElementTextNormalized(propertyElement, ELEMENT_VALUE);
                        IProperty property = IArchimateFactory.eINSTANCE.createProperty();
                        property.setKey(propertyName);
                        property.setValue(propertyValue);
                        propertiesModel.getProperties().add(property);
                    }
                }
            }
        }
    }
    
    void parseArchiMateElements(Element rootElement) throws IOException {
        Element elementsElement = rootElement.getChild(ELEMENT_ELEMENTS, OPEN_GROUP_NAMESPACE);
        if(elementsElement == null) {
            throw new IOException("No Elements found");
        }
        
        for(Element childElement : elementsElement.getChildren(ELEMENT_ELEMENT, OPEN_GROUP_NAMESPACE)) {
            String type = childElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE);
            // If type is bogus ignore
            if(type == null) {
                continue;
            }
            
            IArchimateElement element = XMLTypeMapper.createElement(type);
            // If element is null throw exception
            if(element == null) {
                throw new IOException("Element for type: " + type + " not found.");
            }
                    
            fModel.getDefaultFolderForElement(element).getElements().add(element);
            
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                element.setId(id);
            }

            String name = getChildElementTextNormalized(childElement, ELEMENT_LABEL);
            if(name != null) {
                element.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION);
            if(documentation != null) {
                element.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(childElement, element);
        }
    }
    
    void parseArchiMateRelations(Element rootElement) throws IOException {
        Element relationsElement = rootElement.getChild(ELEMENT_RELATIONSHIPS, OPEN_GROUP_NAMESPACE);
        if(relationsElement == null) { // Optional
            return;
        }
        
        for(Element childElement : relationsElement.getChildren(ELEMENT_RELATIONSHIP, OPEN_GROUP_NAMESPACE)) {
            String type = childElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE);
            // If type is bogus ignore
            if(type == null) {
                continue;
            }
            
            IRelationship relation = (IRelationship)XMLTypeMapper.createElement(type);
            // If relation is null throw exception
            if(relation == null) {
                throw new IOException("Relation for type: " + type + " not found.");
            }
            
            // Source and target
            String sourceID = childElement.getAttributeValue(ATTRIBUTE_SOURCE);
            String targetID = childElement.getAttributeValue(ATTRIBUTE_TARGET);
            
            IArchimateElement src = (IArchimateElement)ArchimateModelUtils.getObjectByID(fModel, sourceID);

            // If not found throw exception
            if(src == null) {
                throw new IOException("Source Element not found for id: " + sourceID);
            }
            
            IArchimateElement tgt = (IArchimateElement)ArchimateModelUtils.getObjectByID(fModel, targetID);
            
            // If not found throw exception
            if(tgt == null) {
                throw new IOException("Target Element not found for id: " + targetID);
            }
            
            relation.setSource(src);
            relation.setTarget(tgt);
            
            fModel.getDefaultFolderForElement(relation).getElements().add(relation);
            
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                relation.setId(id);
            }

            String name = getChildElementTextNormalized(childElement, ELEMENT_LABEL);
            if(name != null) {
                relation.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION);
            if(documentation != null) {
                relation.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(childElement, relation);
        }
    }
    
    String getChildElementText(Element parentElement, String childElementName) {
        // TODO Check for localised element according to system locale
        Element element = parentElement.getChild(childElementName, OPEN_GROUP_NAMESPACE);
        return element == null ? null : element.getText();
    }
    
    String getChildElementTextNormalized(Element parentElement, String childElementName) {
        // TODO Check for localised element according to system locale
        Element element = parentElement.getChild(childElementName, OPEN_GROUP_NAMESPACE);
        return element == null ? null : element.getTextNormalize();
    }
}
