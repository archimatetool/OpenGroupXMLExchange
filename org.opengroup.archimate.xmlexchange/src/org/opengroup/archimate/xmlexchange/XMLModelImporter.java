/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.model.IArchimateComponent;
import com.archimatetool.model.IArchimateDiagramModel;
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
        
        // Parse Views
        parseViews(doc.getRootElement());
        
        // Parse Organization - Not implemented
        // parseOrganization(doc.getRootElement());
        
        return fModel;
    }
    
    // ========================================= Property Definitions ======================================

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
    
    // ========================================= Root Element ======================================

    void parseRootElement(Element rootElement) {
        // Identifier
        String id = rootElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
        if(id != null) {
            fModel.setId(id);
        }
        
        // Name
        String name = getChildElementText(rootElement, ELEMENT_NAME, true);
        if(name != null) {
            fModel.setName(name);
        }
        
        // Documentation
        String documentation = getChildElementText(rootElement, ELEMENT_DOCUMENTATION, false);
        if(documentation != null) {
            fModel.setPurpose(documentation);
        }
        
        // Properties
        addProperties(rootElement, fModel);
    }
    
    // ========================================= Properties ======================================

    void addProperties(Element parentElement, IProperties propertiesModel) {
        Element propertiesElement = parentElement.getChild(ELEMENT_PROPERTIES, OPEN_GROUP_NAMESPACE);
        if(propertiesElement != null) {
            for(Element propertyElement : propertiesElement.getChildren(ELEMENT_PROPERTY, OPEN_GROUP_NAMESPACE)) {
                String idref = propertyElement.getAttributeValue(ATTRIBUTE_IDENTIFIERREF);
                if(idref != null) {
                    String propertyName = fPropertyDefsList.get(idref);
                    if(propertyName != null) {
                        String propertyValue = getChildElementText(propertyElement, ELEMENT_VALUE, true);
                        IProperty property = IArchimateFactory.eINSTANCE.createProperty();
                        property.setKey(propertyName);
                        property.setValue(propertyValue);
                        propertiesModel.getProperties().add(property);
                    }
                }
            }
        }
    }
    
    // ========================================= Elements ======================================

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
            
            IArchimateElement element = (IArchimateElement)XMLTypeMapper.createArchimateComponent(type);
            // If element is null throw exception
            if(element == null) {
                throw new IOException("Element for type: " + type + " not found.");
            }
                    
            fModel.getDefaultFolderForElement(element).getElements().add(element);
            
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                element.setId(id);
            }

            String name = getChildElementText(childElement, ELEMENT_LABEL, true);
            if(name != null) {
                element.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                element.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(childElement, element);
        }
    }
    
    // ========================================= Relations ======================================

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
            
            IRelationship relation = (IRelationship)XMLTypeMapper.createArchimateComponent(type);
            // If relation is null throw exception
            if(relation == null) {
                throw new IOException("Relation for type: " + type + " not found.");
            }
            
            // Source and target
            String sourceID = childElement.getAttributeValue(ATTRIBUTE_SOURCE);
            String targetID = childElement.getAttributeValue(ATTRIBUTE_TARGET);
            
            EObject eObjectSrc = ArchimateModelUtils.getObjectByID(fModel, sourceID);
            if(!(eObjectSrc instanceof IArchimateElement)) {
                throw new IOException("Source Element not found for id: " + sourceID);
            }
            
            EObject eObjectTgt = ArchimateModelUtils.getObjectByID(fModel, targetID);
            if(!(eObjectTgt instanceof IArchimateElement)) {
                throw new IOException("Target Element not found for id: " + targetID);
            }
            
            relation.setSource((IArchimateElement)eObjectSrc);
            relation.setTarget((IArchimateElement)eObjectTgt);
            
            fModel.getDefaultFolderForElement(relation).getElements().add(relation);
            
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                relation.setId(id);
            }

            String name = getChildElementText(childElement, ELEMENT_LABEL, true);
            if(name != null) {
                relation.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                relation.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(childElement, relation);
        }
    }
    
    // ========================================= Organization ======================================

    void parseOrganization(Element rootElement) {
        Element organizationElement = rootElement.getChild(ELEMENT_ORGANIZATION, OPEN_GROUP_NAMESPACE);
        if(organizationElement == null) { // Optional
            return;
        }

        for(Element childElement : organizationElement.getChildren(ELEMENT_ITEM, OPEN_GROUP_NAMESPACE)) {
            parseItem(childElement);
        }
    }
    
    void parseItem(Element itemElement) {
        // The idea is to see if we can match any referenced elements/relations into a suitable folder
        // and then move them to that folder. At this stage, it's not worth it.
        
        String idref = itemElement.getAttributeValue(ATTRIBUTE_IDENTIFIERREF);
        
        if(idref != null) {
            EObject eObject = ArchimateModelUtils.getObjectByID(fModel, idref);
            if(eObject instanceof IArchimateComponent) {
                
            }
        }
        // Folder?
        else {
            
        }

        for(Element childElement : itemElement.getChildren(ELEMENT_ITEM, OPEN_GROUP_NAMESPACE)) {
            parseItem(childElement);
        }
    }
    
    // ========================================= Views ======================================

    void parseViews(Element rootElement) {
        Element viewsElement = rootElement.getChild(ELEMENT_VIEWS, OPEN_GROUP_NAMESPACE);
        if(viewsElement == null) { // Optional
            return;
        }
        
        for(Element viewElement : viewsElement.getChildren(ELEMENT_VIEW, OPEN_GROUP_NAMESPACE)) {
            IArchimateDiagramModel dm = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
            fModel.getDefaultFolderForElement(dm).getElements().add(dm);
            
            String id = viewElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                dm.setId(id);
            }
            
            String viewPointName = viewElement.getAttributeValue(ATTRIBUTE_VIEWPOINT);
            if(viewPointName != null) {
                int viewPointID = XMLTypeMapper.getViewpointID(viewPointName);
                dm.setViewpoint(viewPointID);
            }

            String name = getChildElementText(viewElement, ELEMENT_LABEL, true);
            if(name != null) {
                dm.setName(name);
            }
            
            String documentation = getChildElementText(viewElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                dm.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(viewElement, dm);
        }
    }

    // ========================================= Helpers ======================================

    String getChildElementText(Element parentElement, String childElementName, boolean normalise) {
        //Check for localised element according to the system's locale
        String code = Locale.getDefault().getLanguage();
        if(code == null) {
            code = "en";
        }
        
        for(Element childElement : parentElement.getChildren(childElementName, OPEN_GROUP_NAMESPACE)) {
            String lang = childElement.getAttributeValue(ATTRIBUTE_LANG, Namespace.XML_NAMESPACE);
            if(code.equals(lang)) {
                return normalise ? childElement.getTextNormalize() : childElement.getText();
            }
        }
        
        // Default to first element found
        Element element = parentElement.getChild(childElementName, OPEN_GROUP_NAMESPACE);
        return element == null ? null : normalise ? element.getTextNormalize() : element.getText();
    }
}
