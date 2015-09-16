/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.archimatetool.editor.model.DiagramModelUtils;
import com.archimatetool.editor.ui.ColorFactory;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IAndJunction;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelBendpoint;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelGroup;
import com.archimatetool.model.IDiagramModelNote;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IDiagramModelReference;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IFontAttribute;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.ILineObject;
import com.archimatetool.model.IOrJunction;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.IRelationship;



/**
 * Export Archi Model to Open Exchange XML Format using JDOM
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLModelExporter implements IXMLExchangeGlobals {
    
    // ArchiMate model
    private IArchimateModel fModel;
    
    // Properties
    private Map<String, String> fPropertyDefsList;

    /**
     * A map of DC metadata element tags mapped to values
     */
    private Map<String, String> fMetadata;
    
    /**
     * Whether to save organisation of folders
     */
    private boolean fDoSaveOrganisation;
    
    /**
     * Whether to copy XSD files
     */
    private boolean fIncludeXSD;

    /**
     * The language code
     */
    private String fLanguageCode;

    public void exportModel(IArchimateModel model, File outputFile) throws IOException {
        fModel = model;
        
        // JDOM Document
        Document doc = createDocument();
        
        // Root Element
        Element rootElement = createRootElement(doc);

        // Persist model
        writeModel(rootElement);
        
        // Save
        JDOMUtils.write2XMLFile(doc, outputFile);
        
        // XSD
        if(fIncludeXSD) {
            File out = new File(outputFile.getParentFile(), XMLExchangePlugin.ARCHIMATE_XSD);
            XMLExchangePlugin.INSTANCE.copyXSDFile(XMLExchangePlugin.ARCHIMATE_XSD, out);
        }
    }
    
    /**
     * Set DC Metadata
     * @param metadata A map of DC metadata element tags mapped to values
     */
    public void setMetadata(Map<String, String> metadata) {
        fMetadata = metadata;
    }
    
    boolean hasMetadata() {
        if(fMetadata != null) {
            for(String value : fMetadata.values()) {
                if(StringUtils.isSet(value)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Set whether to save organisation of folders
     * @param set
     */
    public void setSaveOrganisation(boolean set) {
        fDoSaveOrganisation = set;
    }
    
    /**
     * Set whether to copy XSD files to target
     * @param set
     */
    public void setIncludeXSD(boolean set) {
        fIncludeXSD = set;
    }
    
    /**
     * Set the language code to use
     * @param languageCode
     */
    public void setLanguageCode(String languageCode) {
        fLanguageCode = languageCode;
    }

    /**
     * @return A JDOM Document
     */
    Document createDocument() {
        return new Document(); 
    }
    
    /**
     * @param doc
     * @return The Root JDOM Element
     */
    Element createRootElement(Document doc) {
        Element rootElement = new Element(ELEMENT_MODEL, OPEN_GROUP_NAMESPACE);
        doc.setRootElement(rootElement);

        rootElement.addNamespaceDeclaration(JDOMUtils.XSI_Namespace);
        // rootElement.addNamespaceDeclaration(OPEN_GROUP_NAMESPACE_EMBEDDED); // Don't include this
        
        // DC Namespace
        if(hasMetadata()) {
            rootElement.addNamespaceDeclaration(DC_NAMESPACE);
        }

        /* 
         * Add Schema Location Attribute which is constructed from Target Namespaces and file names of Schemas
         */
        StringBuffer schemaLocationURI = new StringBuffer();
        
        // Open Group Schema Location
        schemaLocationURI.append(rootElement.getNamespace().getURI());
        schemaLocationURI.append(" ");  //$NON-NLS-1$
        schemaLocationURI.append(OPEN_GROUP_SCHEMA_LOCATION);
        
        // DC Schema Location
        if(hasMetadata()) {
            schemaLocationURI.append(" ");  //$NON-NLS-1$
            schemaLocationURI.append(DC_NAMESPACE.getURI());
            schemaLocationURI.append(" ");  //$NON-NLS-1$
            schemaLocationURI.append(DC_SCHEMA_LOCATION);
        }
        
        rootElement.setAttribute(JDOMUtils.XSI_SchemaLocation, schemaLocationURI.toString(), JDOMUtils.XSI_Namespace);

        return rootElement;
    }
    
    /**
     * Write the model
     */
    private void writeModel(Element rootElement) {
        rootElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(fModel)); //$NON-NLS-1$
        
        // Gather all properties now
        fPropertyDefsList = getAllUniquePropertyKeysForModel();
        
        // Metadata
        writeMetadata(rootElement);
        
        // Name
        writeTextToElement(fModel.getName(), rootElement, ELEMENT_NAME);
        
        // Documentation (Purpose)
        writeTextToElement(fModel.getPurpose(), rootElement, ELEMENT_DOCUMENTATION);

        // Model Properties
        writeProperties(fModel, rootElement);
        
        // Model Elements
        writeModelElements(rootElement);
        
        // Relationships
        writeModelRelationships(rootElement);
        
        // Organization
        if(fDoSaveOrganisation) {
            writeOrganization(rootElement);
        }
        
        // Properties Definitions
        writeModelPropertiesDefinitions(rootElement);
        
        // Views
        writeViews(rootElement);
    }
    
    // ========================================= Metadata ======================================
    
    /**
     * Write any DC Metadata
     */
    Element writeMetadata(Element rootElement) {
        if(!hasMetadata()) {
            return null;
        }
        
        Element mdElement = new Element(ELEMENT_METADATA, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(mdElement);
        
        Element schemaElement = new Element(ELEMENT_SCHEMA, OPEN_GROUP_NAMESPACE);
        schemaElement.setText("Dublin Core");
        mdElement.addContent(schemaElement);
        
        Element schemaVersionElement = new Element(ELEMENT_SCHEMAVERSION, OPEN_GROUP_NAMESPACE);
        schemaVersionElement.setText("1.1");
        mdElement.addContent(schemaVersionElement);
        
        for(Entry<String, String> entry : fMetadata.entrySet()) {
            if(StringUtils.isSet(entry.getKey()) && StringUtils.isSet(entry.getValue())) {
                Element element = new Element(entry.getKey(), DC_NAMESPACE);
                element.setText(entry.getValue());
                mdElement.addContent(element);
            }
        }
        
        return null;
    }
    
    // ========================================= Model Elements ======================================

    /**
     * Write the elements from the layers and extensions
     */
    Element writeModelElements(Element rootElement) {
        Element elementsElement = new Element(ELEMENT_ELEMENTS, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(elementsElement);
        
        writeModelElementsFolder(fModel.getFolder(FolderType.BUSINESS), elementsElement);
        writeModelElementsFolder(fModel.getFolder(FolderType.APPLICATION), elementsElement);
        writeModelElementsFolder(fModel.getFolder(FolderType.TECHNOLOGY), elementsElement);
        writeModelElementsFolder(fModel.getFolder(FolderType.MOTIVATION), elementsElement);
        writeModelElementsFolder(fModel.getFolder(FolderType.IMPLEMENTATION_MIGRATION), elementsElement);
        writeModelElementsFolder(fModel.getFolder(FolderType.CONNECTORS), elementsElement);
        
        return elementsElement;
    }
    
    /**
     * Write the elements from an Archi folder
     */
    private void writeModelElementsFolder(IFolder folder, Element elementsElement) {
        if(folder == null) {
            return;
        }

        List<EObject> list = new ArrayList<EObject>();
        getElements(folder, list);
        for(EObject eObject : list) {
            if(eObject instanceof IArchimateElement) {
                writeModelElement((IArchimateElement)eObject, elementsElement);
             }
        }
    }
    
    /**
     * Write an element
     */
    Element writeModelElement(IArchimateElement element, Element elementsElement) { 
        Element elementElement = new Element(ELEMENT_ELEMENT, OPEN_GROUP_NAMESPACE);
        elementsElement.addContent(elementElement);
        
        // Identifier
        elementElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(element));
        
        // Type
        elementElement.setAttribute(ATTRIBUTE_TYPE, XMLTypeMapper.getArchimateComponentName(element), JDOMUtils.XSI_Namespace);
        
        // Name
        writeTextToElement(element.getName(), elementElement, ELEMENT_LABEL);
        
        // Documentation
        writeTextToElement(element.getDocumentation(), elementElement, ELEMENT_DOCUMENTATION);
        
        // Properties
        writeProperties(element, elementElement);
        
        return elementElement;
    }

    /**
     * Return all elements in an Archi folder and its sub-folders
     */
    private void getElements(IFolder folder, List<EObject> list) {
        if(folder == null) {
            return;
        }
        
        for(EObject object : folder.getElements()) {
            list.add(object);
        }
        
        for(IFolder f : folder.getFolders()) {
            getElements(f, list);
        }
    }
    
    // ========================================= Model Relationships ======================================

    /**
     * Write the relationships
     */
    Element writeModelRelationships(Element rootElement) {
        Element relationshipsElement = new Element(ELEMENT_RELATIONSHIPS, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(relationshipsElement);
        writeModelRelationshipsFolder(fModel.getFolder(FolderType.RELATIONS), relationshipsElement);
        writeModelRelationshipsFolder(fModel.getFolder(FolderType.DERIVED), relationshipsElement);
        return relationshipsElement;
    }
    
    /**
     * Write the relationships from an Archi folder
     */
    private void writeModelRelationshipsFolder(IFolder folder, Element relationshipsElement) {
        if(folder == null) {
            return;
        }

        List<EObject> list = new ArrayList<EObject>();
        getElements(folder, list);
        for(EObject eObject : list) {
            if(eObject instanceof IRelationship) {
                writeModelRelationship((IRelationship)eObject, relationshipsElement);
             }
        }
    }

    /**
     * Write a relationship
     */
    Element writeModelRelationship(IRelationship relationship, Element relationshipsElement) { 
        Element relationshipElement = new Element(ELEMENT_RELATIONSHIP, OPEN_GROUP_NAMESPACE);
        relationshipsElement.addContent(relationshipElement);
        
        // Identifier
        relationshipElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(relationship));
        
        // Source ID
        relationshipElement.setAttribute(ATTRIBUTE_SOURCE, createID(relationship.getSource()));
        
        // Target ID
        relationshipElement.setAttribute(ATTRIBUTE_TARGET, createID(relationship.getTarget()));

        // Type
        relationshipElement.setAttribute(ATTRIBUTE_TYPE, XMLTypeMapper.getArchimateComponentName(relationship), JDOMUtils.XSI_Namespace);

        // Name
        writeTextToElement(relationship.getName(), relationshipElement, ELEMENT_LABEL);
        
        // Documentation
        writeTextToElement(relationship.getDocumentation(), relationshipElement, ELEMENT_DOCUMENTATION);
        
        // Properties
        writeProperties(relationship, relationshipElement);

        return relationshipElement;
    }
    
    // ========================================= Organization ======================================

    Element writeOrganization(Element rootElement) {
        Element organizationElement = new Element(ELEMENT_ORGANIZATION, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(organizationElement);
        
        for(IFolder folder : fModel.getFolders()) {
            writeFolder(folder, organizationElement);
        }
        
        return organizationElement;
    }
    
    Element writeFolder(IFolder folder, Element parentElement) {
        if(folder.getFolders().isEmpty() && folder.getElements().isEmpty()) {
            return null;
        }
        
        Element itemElement = new Element(ELEMENT_ITEM, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(itemElement);
        
        // Name
        writeTextToElement(folder.getName(), itemElement, ELEMENT_LABEL);
        
        // Documentation
        writeTextToElement(folder.getDocumentation(), itemElement, ELEMENT_DOCUMENTATION);

        for(IFolder subFolder : folder.getFolders()) {
            writeFolder(subFolder, itemElement);
        }
        
        for(EObject eObject : folder.getElements()) {
            if(eObject instanceof IIdentifier) {
                // Don't write Sketch or Canvas Views
                if(eObject instanceof IDiagramModel && !(eObject instanceof IArchimateDiagramModel)) {
                    continue;
                }
                
                IIdentifier component = (IIdentifier)eObject;
                Element itemChildElement = new Element(ELEMENT_ITEM, OPEN_GROUP_NAMESPACE);
                itemElement.addContent(itemChildElement);
                itemChildElement.setAttribute(ATTRIBUTE_IDENTIFIERREF, createID(component));
            }
        }
        
        return itemElement;
    }
    
    // ========================================= Properties ======================================

    Element writeModelPropertiesDefinitions(Element rootElement) {
        if(fPropertyDefsList.isEmpty()) {
            return null;
        }
        
        Element propertiesDefinitionsElement = new Element(ELEMENT_PROPERTYDEFS, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(propertiesDefinitionsElement);

        for(Entry<String, String> entry : fPropertyDefsList.entrySet()) {
            Element propertyDefElement = new Element(ELEMENT_PROPERTYDEF, OPEN_GROUP_NAMESPACE);
            propertiesDefinitionsElement.addContent(propertyDefElement);
            propertyDefElement.setAttribute(ATTRIBUTE_IDENTIFIER, entry.getValue());
            propertyDefElement.setAttribute(ATTRIBUTE_NAME, entry.getKey());
            propertyDefElement.setAttribute(ATTRIBUTE_TYPE, "string"); //$NON-NLS-1$
        }
        
        return propertiesDefinitionsElement;
    }
    
    /**
     * @return All unique property types in the model
     */
    Map<String, String> getAllUniquePropertyKeysForModel() {
        Map<String, String> list = new TreeMap<String, String>();
        
        String id = "propid-"; //$NON-NLS-1$
        int idCount = 1;
        
        for(Iterator<EObject> iter = fModel.eAllContents(); iter.hasNext();) {
            EObject element = iter.next();
            if(element instanceof IProperty) {
                String name = ((IProperty)element).getKey();
                if(name != null && !list.containsKey(name)) {
                    list.put(name, id + (idCount++));
                }
            }
        }
        
        // Add a special property definition for Junctions so we can declare junction types
        list.put(PROPERTY_JUNCTION_TYPE, PROPERTY_JUNCTION_ID);
        
        return list;
    }
    
    /**
     * Write all property values for a given element
     * @param properties
     * @param parentElement
     * @return The Element or null
     */
    Element writeProperties(IProperties properties, Element parentElement) {
        Element propertiesElement = new Element(ELEMENT_PROPERTIES, OPEN_GROUP_NAMESPACE);
        
        // If this element is an AND or OR Junction, add a property for its type
        if(properties instanceof IAndJunction || properties instanceof IOrJunction) {
            String value = (properties instanceof IAndJunction) ? PROPERTY_JUNCTION_AND : PROPERTY_JUNCTION_OR;
            writePropertyValue(propertiesElement, PROPERTY_JUNCTION_ID, value);
        }

        // Other properties
        for(IProperty property : properties.getProperties()) {
            String name = property.getKey();
            String value = property.getValue();
            if(hasSomeText(name)) {
                String propertyRefID = fPropertyDefsList.get(name);
                if(propertyRefID != null) {
                    writePropertyValue(propertiesElement, propertyRefID, value);
                }
            }
        }
        
        if(propertiesElement.getChildren().size() > 0) {
            parentElement.addContent(propertiesElement);
        }
        
        return propertiesElement;
    }
    
    /**
     * Write a Property value referencing a property ref id
     */
    Element writePropertyValue(Element propertiesElement, String propertyRefID, String propertyValue) {
        Element propertyElement = new Element(ELEMENT_PROPERTY, OPEN_GROUP_NAMESPACE);
        propertiesElement.addContent(propertyElement);
        propertyElement.setAttribute(ATTRIBUTE_IDENTIFIERREF, propertyRefID);
        
        Element valueElement = new Element(ELEMENT_VALUE, OPEN_GROUP_NAMESPACE);
        propertyElement.addContent(valueElement);
        writeElementTextWithLanguageCode(valueElement, propertyValue);

        return propertyElement;
    }
    
    // ========================================= Views ======================================
    
    /**
     * The negative offset for the current diagram.
     * The exchange format diagram starts at origin 0,0 with no negative coordinates allowed.
     * Archi diagram nodes can have negative coordinates, so this is the offset to apply to nodes and bendpoints.
     * We calculate it once for each diagram.
     */
    private Point fCurrentDiagramNegativeOffset;
    
    Element writeViews(Element rootElement) {
        // Do we have any views?
        EList<IDiagramModel> views = fModel.getDiagramModels();
        if(views.isEmpty()) {
            return null;
        }
        
        Element viewsElement = new Element(ELEMENT_VIEWS, OPEN_GROUP_NAMESPACE);
        rootElement.addContent(viewsElement);
        
        for(IDiagramModel dm : views) {
            if(dm instanceof IArchimateDiagramModel) {
                // Calculate negative offset for this diagram
                fCurrentDiagramNegativeOffset = XMLExchangeUtils.getNegativeOffsetForDiagram(dm);
                
                writeView((IArchimateDiagramModel)dm, viewsElement);
            }
        }
        
        return viewsElement;
    }
    
    Element writeView(IArchimateDiagramModel dm, Element viewsElement) {
        Element viewElement = new Element(ELEMENT_VIEW, OPEN_GROUP_NAMESPACE);
        viewsElement.addContent(viewElement);

        // Identifier
        viewElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(dm));

        // Viewpoint
        String viewPointName = XMLTypeMapper.getViewpointName(dm.getViewpoint());
        if(StringUtils.isSet(viewPointName)) {
            viewElement.setAttribute(ATTRIBUTE_VIEWPOINT, viewPointName);
        }

        // Name
        writeTextToElement(dm.getName(), viewElement, ELEMENT_LABEL);
        
        // Documentation
        writeTextToElement(dm.getDocumentation(), viewElement, ELEMENT_DOCUMENTATION);

        // Properties
        writeProperties(dm, viewElement);
        
        // Nodes
        writeNodes(dm, viewElement);
        
        // Connections
        writeConnections(dm, viewElement);
        
        return viewElement;
    }
    
    // ========================================= Nodes ======================================
    
    /**
     * Write all diagram nodes
     */
    void writeNodes(IDiagramModel dm, Element viewElement) {
        for(IDiagramModelObject child : dm.getChildren()) {
            writeNode(child, viewElement);
        }
    }
    
    /**
     * Write a diagram node
     */
    void writeNode(IDiagramModelObject dmo, Element parentElement) {
        if(dmo instanceof IDiagramModelArchimateObject) {
            writeArchimateNode((IDiagramModelArchimateObject)dmo, parentElement);
        }
        // Group
        else if(dmo instanceof IDiagramModelGroup) {
            writeGroupNode((IDiagramModelGroup)dmo, parentElement);
        }
        // Note
        else if(dmo instanceof IDiagramModelNote) {
            writeNoteNode((IDiagramModelNote)dmo, parentElement);
        }
        // TODO Diagram Model Reference type
        else if(dmo instanceof IDiagramModelReference) {
            //writeReferenceNode((IDiagramModelReference)dmo, parentElement);
        }
    }
    
    /**
     * Write an ArchiMate node
     */
    Element writeArchimateNode(IDiagramModelArchimateObject dmo, Element parentElement) {
        Element nodeElement = new Element(ELEMENT_NODE, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(nodeElement);
        
        // ID
        nodeElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(dmo));
        
        // Element Ref
        IArchimateElement element = dmo.getArchimateElement();
        nodeElement.setAttribute(ATTRIBUTE_ELEMENTREF, createID(element));
        
        // Bounds
        writeAbsoluteBounds(dmo, nodeElement);
        
        // Style
        writeNodeStyle(dmo, nodeElement);

        // Children
        for(IDiagramModelObject child : dmo.getChildren()) {
            writeNode(child, nodeElement);
        }
        
        return nodeElement;
    }
    
    /**
     * Write a Group node
     */
    Element writeGroupNode(IDiagramModelGroup group, Element parentElement) {
        Element nodeElement = new Element(ELEMENT_NODE, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(nodeElement);
        
        // ID
        nodeElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(group));

        // Bounds
        writeAbsoluteBounds(group, nodeElement);
        
        // Type
        nodeElement.setAttribute(ATTRIBUTE_TYPE, NODE_TYPE_GROUP);
        
        // Name
        writeTextToElement(group.getName(), nodeElement, ELEMENT_LABEL);
        
        // Documentation
        writeTextToElement(group.getDocumentation(), nodeElement, ELEMENT_DOCUMENTATION);

        // Properties
        writeProperties(group, nodeElement);
        
        // Style
        writeNodeStyle(group, nodeElement);
        
        // Children
        for(IDiagramModelObject child : group.getChildren()) {
            writeNode(child, nodeElement);
        }
        
        return nodeElement;
    }
    
    /**
     * Write a Note node
     */
    Element writeNoteNode(IDiagramModelNote note, Element parentElement) {
        Element nodeElement = new Element(ELEMENT_NODE, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(nodeElement);
        
        // ID
        nodeElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(note));

        // Bounds
        writeAbsoluteBounds(note, nodeElement);
        
        // Text
        writeTextToElement(note.getContent(), nodeElement, ELEMENT_LABEL);
        
        // Style
        writeNodeStyle(note, nodeElement);
        
        return nodeElement;
    }

    /**
     * Write a node style
     */
    Element writeNodeStyle(IDiagramModelObject dmo, Element nodeElement) {
        Element styleElement = new Element(ELEMENT_STYLE, OPEN_GROUP_NAMESPACE);
        nodeElement.addContent(styleElement);
        
        // Fill Color
        writeFillColor(dmo, styleElement);
        
        // Line color
        writeLineColor(dmo, styleElement);

        // Font
        writeFont(dmo, styleElement);
        
        return styleElement;
    }
    
    /**
     * Write fill colour of a diagram object
     */
    Element writeFillColor(IDiagramModelObject dmo, Element parentElement) {
        Element fillColorElement = null;
        
        RGB rgb = ColorFactory.convertStringToRGB(dmo.getFillColor());
        if(rgb == null) {
            Color color = ColorFactory.getDefaultFillColor(dmo);
            if(color != null) {
                rgb = color.getRGB();
            }
        }
        
        if(rgb != null) {
            fillColorElement = new Element(ELEMENT_FILLCOLOR, OPEN_GROUP_NAMESPACE);
            parentElement.addContent(fillColorElement);
            writeRGBAttributes(rgb, fillColorElement);
        }
        
        return fillColorElement;
    }
    
    // ========================================= Connections ======================================
    
    /**
     * Write all connections
     */
    void writeConnections(IDiagramModel dm, Element parentElement) {
        for(IDiagramModelObject child : dm.getChildren()) {
            writeConnections(child, parentElement);
        }
    }
    
    /**
     * Write connections from a diagram model object
     */
    void writeConnections(IDiagramModelObject dmo, Element parentElement) {
        for(IDiagramModelConnection connection : dmo.getSourceConnections()) {
            // ArchiMate connection
            if(connection instanceof IDiagramModelArchimateConnection) {
                // If it's nested don't write a connection
                if(!isNestedConnection((IDiagramModelArchimateConnection)connection)) {
                    writeConnection(connection, parentElement);
                }
            }
            // Other connection
            else {
                writeConnection(connection, parentElement);
            }
        }
        
        // Children
        if(dmo instanceof IDiagramModelContainer) {
            for(IDiagramModelObject child : ((IDiagramModelContainer)dmo).getChildren()) {
                writeConnections(child, parentElement);
            }
        }
    }
    
    /**
     * Check whether this is a nested connection
     */
    boolean isNestedConnection(IDiagramModelArchimateConnection connection) {
        if(connection.getSource() instanceof IDiagramModelArchimateObject && connection.getTarget() instanceof IDiagramModelArchimateObject) {
            IDiagramModelArchimateObject src = (IDiagramModelArchimateObject)connection.getSource();
            IDiagramModelArchimateObject tgt = (IDiagramModelArchimateObject)connection.getTarget();
            return src.getChildren().contains(tgt) && DiagramModelUtils.isNestedConnectionTypeRelationship(connection.getRelationship());
        }
        return false;
    }
    
    /**
     * Write a connection
     */
    Element writeConnection(IDiagramModelConnection connection, Element parentElement) {
        Element connectionElement = new Element(ELEMENT_CONNECTION, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(connectionElement);
        
        // ID
        connectionElement.setAttribute(ATTRIBUTE_IDENTIFIER, createID(connection));

        // ArchiMate connection has a Relationship ref
        if(connection instanceof IDiagramModelArchimateConnection) {
            connectionElement.setAttribute(ATTRIBUTE_RELATIONSHIPREF, createID(((IDiagramModelArchimateConnection)connection).getRelationship()));
        }
        
        // Source node
        connectionElement.setAttribute(ATTRIBUTE_SOURCE, createID(connection.getSource()));
        
        // Target node
        connectionElement.setAttribute(ATTRIBUTE_TARGET, createID(connection.getTarget()));
        
        // Bendpoints
        writeConnectionBendpoints(connection, connectionElement);
        
        // Style
        writeConnectionStyle(connection, connectionElement);

        return connectionElement;
    }
    
    /**
     * Write connection bendpoints
     */
    void writeConnectionBendpoints(IDiagramModelConnection connection, Element connectionElement) {
        double bpindex = 1; // index count + 1
    	double bpcount = connection.getBendpoints().size() + 1; // number of bendpoints + 1
    	
        for(IDiagramModelBendpoint bendpoint : connection.getBendpoints()) {
        	// The weight of this Bendpoint should use to calculate its location.
        	// The weight should be between 0.0 and 1.0. A weight of 0.0 will
        	// cause the Bendpoint to follow the start point, while a weight
        	// of 1.0 will cause the Bendpoint to follow the end point
        	double bpweight = bpindex / bpcount;
        	
            Element bendpointElement = new Element(ELEMENT_BENDPOINT, OPEN_GROUP_NAMESPACE);
            connectionElement.addContent(bendpointElement);
            
            IBounds srcBounds = XMLExchangeUtils.getAbsoluteBounds(connection.getSource()); // get bounds of source node
            double startX = (srcBounds.getX() + (srcBounds.getWidth() / 2)) + bendpoint.getStartX();
            startX *= (1.0 - bpweight);
            double startY = (srcBounds.getY() + (srcBounds.getHeight() / 2)) + bendpoint.getStartY();
            startY *= (1.0 - bpweight);
            
            IBounds tgtBounds = XMLExchangeUtils.getAbsoluteBounds(connection.getTarget()); // get bounds of target node
            double endX = (tgtBounds.getX() + (tgtBounds.getWidth() / 2)) + bendpoint.getEndX();
            endX *= bpweight;
            double endY = (tgtBounds.getY() + (tgtBounds.getHeight() / 2)) + bendpoint.getEndY();
            endY *= bpweight;
            
            int x = (int)(startX + endX);
            x -= fCurrentDiagramNegativeOffset.x; // compensate for negative space
            int y = (int)(startY + endY);
            y -= fCurrentDiagramNegativeOffset.y; // compensate for negative space
            
            bendpointElement.setAttribute(ATTRIBUTE_X, Integer.toString(x));
            bendpointElement.setAttribute(ATTRIBUTE_Y, Integer.toString(y));
            
            bpindex++;
        }
    }
    
    /**
     * Write a connection style
     */
    Element writeConnectionStyle(IDiagramModelConnection connection, Element parentElement) {
        Element styleElement = new Element(ELEMENT_STYLE, OPEN_GROUP_NAMESPACE);
        parentElement.addContent(styleElement);
        
        // Line Width
        int lineWidth = connection.getLineWidth();
        if(lineWidth != 1) {
            styleElement.setAttribute(ATTRIBUTE_LINEWIDTH, Integer.toString(lineWidth));
        }
        
        // Line color
        writeLineColor(connection, styleElement);
        
        // Font
        writeFont(connection, styleElement);

        return styleElement;
    }

    // ========================================= Helpers ======================================
    
    /**
     * Write line colour of a diagram object
     * TODO: Should we export connection line color if it is the default black?
     */
    Element writeLineColor(ILineObject lineObject, Element parentElement) {
        Element lineColorElement = null;
        
        RGB rgb = ColorFactory.convertStringToRGB(lineObject.getLineColor());
        if(rgb == null) {
            Color color = ColorFactory.getDefaultLineColor(lineObject);
            if(color != null) {
                rgb = color.getRGB();
            }
        }
        
        if(rgb != null) {
            lineColorElement = new Element(ELEMENT_LINECOLOR, OPEN_GROUP_NAMESPACE);
            parentElement.addContent(lineColorElement);
            writeRGBAttributes(rgb, lineColorElement);
        }
        
        return lineColorElement;
    }

    /**
     * Write font of a diagram component
     */
    Element writeFont(IFontAttribute fontObject, Element styleElement) {
        Element fontElement = new Element(ELEMENT_FONT, OPEN_GROUP_NAMESPACE);
        
        String fontString = fontObject.getFont();
        if(fontString != null) {
            try {
                FontData fontData = new FontData(fontString);
                
                fontElement.setAttribute(ATTRIBUTE_FONTNAME, fontData.getName());
                fontElement.setAttribute(ATTRIBUTE_FONTSIZE, Integer.toString(fontData.getHeight()));
                
                int style = fontData.getStyle();
                String styleString = "";
                
                if((style & SWT.BOLD) == SWT.BOLD) {
                    styleString += "bold";
                }
                if((style & SWT.ITALIC) == SWT.ITALIC) {
                    if(StringUtils.isSet(styleString)) {
                        styleString += "|";
                    }
                    styleString += "italic";
                }
                
                if(hasSomeText(styleString)) {
                    fontElement.setAttribute(ATTRIBUTE_FONTSTYLE, styleString);
                }
            }
            catch(Exception ex) {
                //ex.printStackTrace();
            }
        }
        
        String fontColorString = fontObject.getFontColor();
        if(fontColorString != null) {
            RGB rgb = ColorFactory.convertStringToRGB(fontColorString);
            if(rgb != null) {
                Element fontColorElement = new Element(ELEMENT_FONTCOLOR, OPEN_GROUP_NAMESPACE);
                fontElement.addContent(fontColorElement);
                writeRGBAttributes(rgb, fontColorElement);
            }
        }
        
        if(hasElementContent(fontElement)) {
            styleElement.addContent(fontElement);
        }

        return fontElement;
    }
    
    /**
     * Write RGB attribute on an Element 
     */
    void writeRGBAttributes(RGB rgb, Element colorElement) {
        colorElement.setAttribute(ATTRIBUTE_R, Integer.toString(rgb.red));
        colorElement.setAttribute(ATTRIBUTE_G, Integer.toString(rgb.green));
        colorElement.setAttribute(ATTRIBUTE_B, Integer.toString(rgb.blue));
    }

    /**
     * Write absolute bounds of a diagram object
     */
    void writeAbsoluteBounds(IDiagramModelObject dmo, Element element) {
        IBounds bounds = XMLExchangeUtils.getAbsoluteBounds(dmo);
        
        int x = bounds.getX() - fCurrentDiagramNegativeOffset.x; // compensate for negative space
        int y = bounds.getY() - fCurrentDiagramNegativeOffset.y; // compensate for negative space
        
        element.setAttribute(ATTRIBUTE_X, Integer.toString(x));
        element.setAttribute(ATTRIBUTE_Y, Integer.toString(y));
        element.setAttribute(ATTRIBUTE_WIDTH, Integer.toString(bounds.getWidth()));
        element.setAttribute(ATTRIBUTE_HEIGHT, Integer.toString(bounds.getHeight()));
    }

    Element writeTextToElement(String text, Element parentElement, String childElementName) {
        Element element = null;
        
        if(hasSomeText(text)) {
            element = new Element(childElementName, OPEN_GROUP_NAMESPACE);
            parentElement.addContent(element);
            writeElementTextWithLanguageCode(element, text);
        }
        
        return element;
    }

    private void writeElementTextWithLanguageCode(Element element, String text) {
        element.setText(text);
        
        if(fLanguageCode != null) {
            element.setAttribute(ATTRIBUTE_LANG, fLanguageCode, Namespace.XML_NAMESPACE);
        }
    }

    /**
     * Return true if string has at least some text
     */
    private boolean hasSomeText(String string) {
        return string != null && !string.isEmpty();
    }
    
    /**
     * @return true if element has attributes or a child element
     */
    private boolean hasElementContent(Element element) {
        return element != null && (element.hasAttributes() || !element.getChildren().isEmpty());
    }

    /**
     * Create a uniform id
     */
    private String createID(IIdentifier identifier) {
        if(identifier.getId() != null && identifier.getId().startsWith("id-")) {
            return identifier.getId();
        }
        return "id-" + identifier.getId();
    }
}
