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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import com.archimatetool.editor.ui.ColorFactory;
import com.archimatetool.editor.ui.FontFactory;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.model.IArchimateComponent;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelBendpoint;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelGroup;
import com.archimatetool.model.IDiagramModelNote;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IFontAttribute;
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
    private Map<String, String> fPropertyDefinitionsList;
    
    public IArchimateModel createArchiMateModel(File instanceFile) throws IOException, JDOMException, XMLModelParserException {
        // Create a new Archimate Model and set its defaults
        fModel = IArchimateFactory.eINSTANCE.createArchimateModel();
        fModel.setDefaults();
        
        // Read file without Schema validation
        Document doc = JDOMUtils.readXMLFile(instanceFile);
        
        Element rootElement = doc.getRootElement();
        
        // Parse Property Definitions first
        parsePropertyDefinitions(rootElement.getChild(ELEMENT_PROPERTYDEFS, OPEN_GROUP_NAMESPACE));
        
        // Parse Root Element
        parseRootElement(rootElement);
        
        // Parse ArchiMate Elements
        parseArchiMateElements(rootElement.getChild(ELEMENT_ELEMENTS, OPEN_GROUP_NAMESPACE));
        
        // Parse ArchiMate Relations
        parseArchiMateRelations(rootElement.getChild(ELEMENT_RELATIONSHIPS, OPEN_GROUP_NAMESPACE));
        
        // Parse Views
        parseViews(rootElement.getChild(ELEMENT_VIEWS, OPEN_GROUP_NAMESPACE));
        
        // TODO Parse Organization - not implemented as yet.
        // parseOrganization(rootElement.getChild(ELEMENT_ORGANIZATION, OPEN_GROUP_NAMESPACE));
        
        return fModel;
    }
    
    // ========================================= Property Definitions ======================================

    private void parsePropertyDefinitions(Element propertydefsElement) {
        if(propertydefsElement == null) {
            return;
        }

        fPropertyDefinitionsList = null;
        fPropertyDefinitionsList = new HashMap<String, String>();
        
        // Archi only supports String types so we can ignore the data type
        for(Element propertyDefElement : propertydefsElement.getChildren(ELEMENT_PROPERTYDEF, OPEN_GROUP_NAMESPACE)) {
            String identifier = propertyDefElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            String name = propertyDefElement.getAttributeValue(ATTRIBUTE_NAME);
            if(identifier != null && name != null) {
                fPropertyDefinitionsList.put(identifier, name);
            }
        }
    }
    
    // ========================================= Root Element ======================================

    private void parseRootElement(Element rootElement) {
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
        addProperties(fModel, rootElement);
    }
    
    // ========================================= Properties ======================================

    private void addProperties(IProperties propertiesModel, Element parentElement) {
        Element propertiesElement = parentElement.getChild(ELEMENT_PROPERTIES, OPEN_GROUP_NAMESPACE);
        if(propertiesElement != null) {
            for(Element propertyElement : propertiesElement.getChildren(ELEMENT_PROPERTY, OPEN_GROUP_NAMESPACE)) {
                String idref = propertyElement.getAttributeValue(ATTRIBUTE_IDENTIFIERREF);
                if(idref != null) {
                    String propertyName = fPropertyDefinitionsList.get(idref);
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

    private void parseArchiMateElements(Element elementsElement) throws XMLModelParserException {
        if(elementsElement == null) {
            throw new XMLModelParserException("No Elements found");
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
                throw new XMLModelParserException("Element for type: " + type + " not found.");
            }
                    
            // Identifier first
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                element.setId(id);
            }

            // Add to model
            fModel.getDefaultFolderForElement(element).getElements().add(element);
            
            String name = getChildElementText(childElement, ELEMENT_LABEL, true);
            if(name != null) {
                element.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                element.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(element, childElement);
        }
    }
    
    // ========================================= Relations ======================================

    private void parseArchiMateRelations(Element relationsElement) throws IOException {
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
            
            // Identifier first
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                relation.setId(id);
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
            
            // Add to model
            fModel.getDefaultFolderForElement(relation).getElements().add(relation);
            
            String name = getChildElementText(childElement, ELEMENT_LABEL, true);
            if(name != null) {
                relation.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                relation.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(relation, childElement);
        }
    }
    
    // ========================================= Organization ======================================

    @SuppressWarnings("unused")
    private void parseOrganization(Element organizationElement) {
        if(organizationElement == null) { // Optional
            return;
        }

        for(Element childElement : organizationElement.getChildren(ELEMENT_ITEM, OPEN_GROUP_NAMESPACE)) {
            parseItem(childElement);
        }
    }
    
    private void parseItem(Element itemElement) {
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

    private void parseViews(Element viewsElement) throws XMLModelParserException {
        if(viewsElement == null) { // Optional
            return;
        }
        
        for(Element viewElement : viewsElement.getChildren(ELEMENT_VIEW, OPEN_GROUP_NAMESPACE)) {
            IArchimateDiagramModel dm = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
            fModel.getDefaultFolderForElement(dm).getElements().add(dm);
            
            // Identifier first
            String id = viewElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                dm.setId(id);
            }
            
            // Viewpoint
            String viewPointName = viewElement.getAttributeValue(ATTRIBUTE_VIEWPOINT);
            if(viewPointName != null) {
                int viewPointID = XMLTypeMapper.getViewpointID(viewPointName);
                dm.setViewpoint(viewPointID);
            }

            // Name
            String name = getChildElementText(viewElement, ELEMENT_LABEL, true);
            if(name != null) {
                dm.setName(name);
            }
            
            // Documentation
            String documentation = getChildElementText(viewElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                dm.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(dm, viewElement);
            
            // Nodes
            addNodes(dm, viewElement);
            
            // Connections
            addConnections(viewElement);
        }
    }
    
    // ========================================= Nodes ======================================

    private void addNodes(IDiagramModelContainer parentContainer, Element parentElement) throws XMLModelParserException {
        for(Element nodeElement : parentElement.getChildren(ELEMENT_NODE, OPEN_GROUP_NAMESPACE)) {
            IDiagramModelObject dmo = null;
            
            // This has an element ref so it's an ArchiMate element node
            String elementRef = nodeElement.getAttributeValue(ATTRIBUTE_ELEMENTREF);
            if(hasValue(elementRef) ) {
                EObject eObject = ArchimateModelUtils.getObjectByID(fModel, elementRef);
                
                if(!(eObject instanceof IArchimateElement)) {
                    throw new XMLModelParserException("Element not found for id: " + elementRef);
                }
                
                // Create new diagram node object
                IArchimateElement element = (IArchimateElement)eObject;
                dmo = IArchimateFactory.eINSTANCE.createDiagramModelArchimateObject();
                ((IDiagramModelArchimateObject)dmo).setArchimateElement(element);
            }
            
            // No element ref so this is another type of node, but what is it?
            else {
                boolean isGroup = NODE_TYPE_GROUP.equals(nodeElement.getAttributeValue(ATTRIBUTE_TYPE));
                //boolean isNote = NODE_TYPE_TEXT.equals(nodeElement.getAttributeValue(ATTRIBUTE_TYPE));
                
                // Does the graphical node have children?
                // Our notes cannot contain children, so if it does contain children it has to be a Group.
                boolean hasChildren = nodeElement.getChildren(ELEMENT_NODE, OPEN_GROUP_NAMESPACE).size() > 0;
                
                if(isGroup || hasChildren) {
                    IDiagramModelGroup group = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
                    dmo = group;

                    // Name
                    String name = getChildElementText(nodeElement, ELEMENT_LABEL, true);
                    if(name != null) {
                        dmo.setName(name);
                    }

                    // Documentation
                    String documentation = getChildElementText(nodeElement, ELEMENT_DOCUMENTATION, false);
                    if(documentation != null) {
                        group.setDocumentation(documentation);
                    }
                    
                    // Properties
                    addProperties(group, nodeElement);
                }
                // A Note is our only other option
                else {
                    IDiagramModelNote note = IArchimateFactory.eINSTANCE.createDiagramModelNote();
                    note.setBorderType(IDiagramModelNote.BORDER_RECTANGLE);
                    dmo = note;
                    
                    // Text
                    String text = getChildElementText(nodeElement, ELEMENT_LABEL, false);
                    if(text != null) {
                        note.setContent(text);
                    }
                }
            }
            
            if(dmo != null) {
                // Add Identifier before adding to model
                String identifier = nodeElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
                dmo.setId(identifier);
                
                // Add the child first
                parentContainer.getChildren().add(dmo);
                
                // Get the absolute bounds as declared in the XML file
                IBounds absoluteBounds = getNodeBounds(nodeElement);
                
                // Now convert the given absolute bounds into relative bounds
                IBounds relativeBounds = XMLExchangeUtils.convertAbsoluteToRelativeBounds(absoluteBounds, dmo);
                dmo.setBounds(relativeBounds);
                
                // Style
                addNodeStyle(dmo, nodeElement.getChild(ELEMENT_STYLE, OPEN_GROUP_NAMESPACE));

                // Child nodes
                if(dmo instanceof IDiagramModelContainer) {
                    addNodes((IDiagramModelContainer)dmo, nodeElement);
                }
            }
        }
    }
    
    /**
     * Get the object bounds as declared in XML. The x, y will be absolute values.
     */
    IBounds getNodeBounds(Element nodeElement) throws XMLModelParserException {
        // Check for x, y, width and height
        String xString = nodeElement.getAttributeValue(ATTRIBUTE_X);
        String yString = nodeElement.getAttributeValue(ATTRIBUTE_Y);
        String wString = nodeElement.getAttributeValue(ATTRIBUTE_WIDTH);
        String hString = nodeElement.getAttributeValue(ATTRIBUTE_HEIGHT);
        
        if(!hasValue(xString) || !hasValue(yString) || !hasValue(wString) || !hasValue(hString)) {
            throw new XMLModelParserException("Co-ordinate value not found");
        }
        
        int x = Integer.valueOf(xString);
        int y = Integer.valueOf(yString);
        int width = Integer.valueOf(wString);
        int height = Integer.valueOf(hString);

        return IArchimateFactory.eINSTANCE.createBounds(x, y, width, height);
    }
    
    /**
     * Node Style
     */
    private void addNodeStyle(IDiagramModelObject dmo, Element styleElement) throws XMLModelParserException {
        if(styleElement == null) {
            return;
        }

        // Fill Color
        dmo.setFillColor(getRGBColorString(styleElement.getChild(ELEMENT_FILLCOLOR, OPEN_GROUP_NAMESPACE)));
        
        // Line Color
        dmo.setLineColor(getRGBColorString(styleElement.getChild(ELEMENT_LINECOLOR, OPEN_GROUP_NAMESPACE)));

        // Font
        addFont(dmo, styleElement.getChild(ELEMENT_FONT, OPEN_GROUP_NAMESPACE));
    }
    
    // ======================================= Connections ====================================
    
    private void addConnections(Element viewElement) throws XMLModelParserException {
        for(Element connectionElement : viewElement.getChildren(ELEMENT_CONNECTION, OPEN_GROUP_NAMESPACE)) {
            
            IDiagramModelConnection connection = null;
            
            // Get source node
            String sourceRef = connectionElement.getAttributeValue(ATTRIBUTE_SOURCE);
            EObject eObjectSourceNode = ArchimateModelUtils.getObjectByID(fModel, sourceRef);
            if(eObjectSourceNode == null) {
                throw new XMLModelParserException("Source node not found for id: " + sourceRef);
            }
            
            // Get target node
            String targetRef = connectionElement.getAttributeValue(ATTRIBUTE_TARGET);
            EObject eObjectTargetNode = ArchimateModelUtils.getObjectByID(fModel, targetRef);
            if(eObjectTargetNode == null) {
                throw new XMLModelParserException("Target node not found for id: " + targetRef);
            }
            
            // An ArchiMate relationship connection
            String relationshipRef = connectionElement.getAttributeValue(ATTRIBUTE_RELATIONSHIPREF);
            if(hasValue(relationshipRef)) {
                // Must be ArchiMate type source node
                if(!(eObjectSourceNode instanceof IDiagramModelArchimateObject)) {
                    throw new XMLModelParserException("Source node is not an ArchiMate node for id: " + sourceRef);
                }

                // Must be ArchiMate type target node
                if(!(eObjectTargetNode instanceof IDiagramModelArchimateObject)) {
                    throw new XMLModelParserException("Target node is not an ArchiMate node for id: " + targetRef);
                }

                // Get relationship
                EObject eObjectRelationship = ArchimateModelUtils.getObjectByID(fModel, relationshipRef);
                if(!(eObjectRelationship instanceof IRelationship)) {
                    throw new XMLModelParserException("Relationship not found for id: " + relationshipRef);
                }
                
                // Create new ArchiMate connection with relationship
                connection = IArchimateFactory.eINSTANCE.createDiagramModelArchimateConnection();
                ((IDiagramModelArchimateConnection)connection).setRelationship((IRelationship)eObjectRelationship);
            }
            // Another connection type
            else {
                // Only connect notes and groups
                if(eObjectTargetNode instanceof IDiagramModelArchimateObject && eObjectSourceNode instanceof IDiagramModelArchimateObject) {
                    continue;
                }
                
                
                // Create new ordinary connection
                connection = IArchimateFactory.eINSTANCE.createDiagramModelConnection();
            }
            
            if(connection != null) {
                // Add Identifier before adding to model
                String identifier = connectionElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
                connection.setId(identifier);
                
                // Connect
                connection.connect((IDiagramModelObject)eObjectSourceNode, (IDiagramModelObject)eObjectTargetNode);
                
                // Bendpoints
                addBendpoints(connection, connectionElement);
                
                // Style
                addConnectionStyle(connection, connectionElement.getChild(ELEMENT_STYLE, OPEN_GROUP_NAMESPACE));
            }
        }
    }
    
    /**
     * Add bendpoints
     */
    private void addBendpoints(IDiagramModelConnection connection, Element connectionElement) throws XMLModelParserException {
        for(Element bendpointElement : connectionElement.getChildren(ELEMENT_BENDPOINT, OPEN_GROUP_NAMESPACE)) {
            String xString = bendpointElement.getAttributeValue(ATTRIBUTE_X);
            String yString = bendpointElement.getAttributeValue(ATTRIBUTE_Y);
            if(!hasValue(xString) || !hasValue(yString)) {
                throw new XMLModelParserException("Bendpoint co-ordinate value not found");
            }
            
            int x = Integer.valueOf(xString);
            int y = Integer.valueOf(yString);
            
            IDiagramModelBendpoint bendpoint = IArchimateFactory.eINSTANCE.createDiagramModelBendpoint();
            connection.getBendpoints().add(bendpoint);

            IBounds srcBounds = XMLExchangeUtils.getAbsoluteBounds(connection.getSource());
            IBounds tgtBounds = XMLExchangeUtils.getAbsoluteBounds(connection.getTarget());
            
            int startX = x - (srcBounds.getX() + (srcBounds.getWidth() / 2));
            int startY = y - (srcBounds.getY() + (srcBounds.getHeight() / 2));
            bendpoint.setStartX(startX);
            bendpoint.setStartY(startY);

            int endX = x - (tgtBounds.getX() + (tgtBounds.getWidth() / 2));
            int endY = y - (tgtBounds.getY() + (tgtBounds.getHeight() / 2));
            bendpoint.setEndX(endX);
            bendpoint.setEndY(endY);
        }
    }
    
    /**
     * Connection Style
     */
    private void addConnectionStyle(IDiagramModelConnection connection, Element styleElement) throws XMLModelParserException {
        if(styleElement == null) {
            return;
        }
        
        // Line width
        String lineWidthString = styleElement.getAttributeValue(ATTRIBUTE_LINEWIDTH);
        if(hasValue(lineWidthString)) {
            int width = Integer.valueOf(lineWidthString);
            if(width < 0) {
                width = 1;
            }
            if(width > 3) {
                width = 3;
            }
            connection.setLineWidth(width);
        }
        
        // Line Color
        connection.setLineColor(getRGBColorString(styleElement.getChild(ELEMENT_LINECOLOR, OPEN_GROUP_NAMESPACE)));

        // Font
        addFont(connection, styleElement.getChild(ELEMENT_FONT, OPEN_GROUP_NAMESPACE));
    }

    // ========================================= Helpers ======================================

    private void addFont(IFontAttribute fontObject, Element fontElement) throws XMLModelParserException {
        if(fontElement == null) {
            return;
        }
        
        FontData newFontData = new FontData(FontFactory.getDefaultUserViewFontData().toString());

        String fontName = fontElement.getAttributeValue(ATTRIBUTE_FONTNAME);
        if(hasValue(fontName)) {
            newFontData.setName(fontName);
        }
        
        String fontSize = fontElement.getAttributeValue(ATTRIBUTE_FONTSIZE);
        if(hasValue(fontSize)) {
            newFontData.setHeight(Integer.valueOf(fontSize));
        }
        
        String fontStyle = fontElement.getAttributeValue(ATTRIBUTE_FONTSTYLE);
        if(hasValue(fontStyle)) {
            int styleValue = SWT.NORMAL;
            if(fontStyle.contains("bold")) {
                styleValue |= SWT.BOLD;
            }
            if(fontStyle.contains("italic")) {
                styleValue |= SWT.ITALIC;
            }
            newFontData.setStyle(styleValue);
        }
        
        fontObject.setFont(newFontData.toString());
        
        // Font color
        fontObject.setFontColor(getRGBColorString(fontElement.getChild(ELEMENT_FONTCOLOR, OPEN_GROUP_NAMESPACE)));
    }
    
    /**
     * Get the RGB String for an element, or null.
     */
    String getRGBColorString(Element rgbElement) throws XMLModelParserException {
        String colorStr = null;
        
        if(rgbElement != null) {
            String rString = rgbElement.getAttributeValue(ATTRIBUTE_R);
            String gString = rgbElement.getAttributeValue(ATTRIBUTE_G);
            String bString = rgbElement.getAttributeValue(ATTRIBUTE_B);
            
            if(!hasValue(rString) || !hasValue(gString) || !hasValue(bString)) {
                throw new XMLModelParserException("RGB value not found");
            }
            
            int red = Integer.valueOf(rString);
            int green = Integer.valueOf(gString);
            int blue = Integer.valueOf(bString);
            
            colorStr = ColorFactory.convertRGBToString(new RGB(red, green, blue));
        }
        
        return colorStr;
    }
    
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
    
    boolean hasValue(String val) {
        return StringUtils.isSet(val);
    }
}
