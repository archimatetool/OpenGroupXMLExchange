/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;

import com.archimatetool.editor.diagram.ArchimateDiagramModelFactory;
import com.archimatetool.editor.diagram.ICreationFactory;
import com.archimatetool.editor.model.DiagramModelUtils;
import com.archimatetool.editor.ui.ColorFactory;
import com.archimatetool.editor.ui.FontFactory;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.jdom.JDOMUtils;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IAccessRelationship;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IConnectable;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateComponent;
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
import com.archimatetool.model.IInfluenceRelationship;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;




/**
 * XML Model Importer
 * 
 * @author Phillip Beauvoir
 */
public class XMLModelImporter implements IXMLExchangeGlobals {
    
    private IArchimateModel fModel;
    
    // Properties
    private Map<String, String> fPropertyDefinitionsList;
    
    // Concept lookup
    private Map<String, IArchimateConcept> fConceptsLookup;
    
    // Connection/Node lookup
    private Map<String, IConnectable> fConnectionsNodesLookup;
    
    
    public IArchimateModel createArchiMateModel(File instanceFile) throws IOException, JDOMException, XMLModelParserException {
        // Create a new Archimate Model and set its defaults
        fModel = IArchimateFactory.eINSTANCE.createArchimateModel();
        fModel.setDefaults();
        
        // Read file without Schema validation
        Document doc = JDOMUtils.readXMLFile(instanceFile);
        
        Element rootElement = doc.getRootElement();
        
        // Parse Property Definitions first
        parsePropertyDefinitions(rootElement.getChild(ELEMENT_PROPERTYDEFINITIONS, ARCHIMATE3_NAMESPACE));
        
        // Parse Root Element
        parseRootElement(rootElement);
        
        // New lookup tables
        fConceptsLookup = new HashMap<>();
        fConnectionsNodesLookup = new HashMap<>();
        
        // Parse ArchiMate Elements
        parseArchiMateElements(rootElement.getChild(ELEMENT_ELEMENTS, ARCHIMATE3_NAMESPACE));
        
        // Parse ArchiMate Relations
        parseArchiMateRelations(rootElement.getChild(ELEMENT_RELATIONSHIPS, ARCHIMATE3_NAMESPACE));
        
        // Parse Views
        Element viewsElement = rootElement.getChild(ELEMENT_VIEWS, ARCHIMATE3_NAMESPACE);
        if(viewsElement != null) {
            parseViews(viewsElement.getChild(ELEMENT_DIAGRAMS, ARCHIMATE3_NAMESPACE));
        }
        
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
        for(Element propertyDefElement : propertydefsElement.getChildren(ELEMENT_PROPERTYDEFINITION, ARCHIMATE3_NAMESPACE)) {
            String identifier = propertyDefElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            String name = getChildElementText(propertyDefElement, ELEMENT_NAME, false);
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
        Element propertiesElement = parentElement.getChild(ELEMENT_PROPERTIES, ARCHIMATE3_NAMESPACE);
        if(propertiesElement != null) {
            for(Element propertyElement : propertiesElement.getChildren(ELEMENT_PROPERTY, ARCHIMATE3_NAMESPACE)) {
                String idref = propertyElement.getAttributeValue(ATTRIBUTE_PROPERTY_IDENTIFIERREF);
                
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
            throw new XMLModelParserException(Messages.XMLModelImporter_0);
        }
        
        for(Element childElement : elementsElement.getChildren(ELEMENT_ELEMENT, ARCHIMATE3_NAMESPACE)) {
            String type = childElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE);
            // If type is bogus ignore
            if(type == null) {
                continue;
            }
            
            IArchimateElement element = (IArchimateElement)XMLTypeMapper.createArchimateConcept(type);
            // If element is null throw exception
            if(element == null) {
                throw new XMLModelParserException(NLS.bind(Messages.XMLModelImporter_1, type));
            }
                    
            // Identifier first
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                element.setId(id);
            }

            // Add to model
            fModel.getDefaultFolderForObject(element).getElements().add(element);
            
            String name = getChildElementText(childElement, ELEMENT_NAME, true);
            if(name != null) {
                element.setName(name);
            }
            
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                element.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(element, childElement);
            
            // Add to lookup
            fConceptsLookup.put(element.getId(), element);
        }
    }
    
    // ========================================= Relations ======================================

    private void parseArchiMateRelations(Element relationsElement) throws IOException {
        if(relationsElement == null) { // Optional
            return;
        }
        
        class RelationInfo {
            IArchimateRelationship relation;
            String sourceID;
            String targetID;
        }
        
        List<RelationInfo> relationInfoList = new ArrayList<RelationInfo>();
        IFolder relationshipFolder = fModel.getFolder(FolderType.RELATIONS);
        
        for(Element childElement : relationsElement.getChildren(ELEMENT_RELATIONSHIP, ARCHIMATE3_NAMESPACE)) {
            String type = childElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE);
            // If type is bogus ignore
            if(type == null) {
                continue;
            }
            
            IArchimateRelationship relation = (IArchimateRelationship)XMLTypeMapper.createArchimateConcept(type);
            // If relation is null throw exception
            if(relation == null) {
                throw new IOException(NLS.bind(Messages.XMLModelImporter_2, type));
            }
            
            // Identifier first
            String id = childElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                relation.setId(id);
            }

            // Add to model
            relationshipFolder.getElements().add(relation);
            
            // Name
            String name = getChildElementText(childElement, ELEMENT_NAME, true);
            if(name != null) {
                relation.setName(name);
            }
            
            // Documentation
            String documentation = getChildElementText(childElement, ELEMENT_DOCUMENTATION, false);
            if(documentation != null) {
                relation.setDocumentation(documentation);
            }
            
            // Properties
            addProperties(relation, childElement);
            
            // Source and target
            String sourceID = childElement.getAttributeValue(ATTRIBUTE_SOURCE);
            String targetID = childElement.getAttributeValue(ATTRIBUTE_TARGET);
            
            // Access type
            if(relation instanceof IAccessRelationship) {
                String accessType = childElement.getAttributeValue(ATTRIBUTE_ACCESS_TYPE);
                if(accessType != null) {
                    IAccessRelationship accessRelationship = (IAccessRelationship)relation;
                    
                    switch(accessType) {
                        case ACCESS_TYPE_ACCESS:
                            accessRelationship.setAccessType(IAccessRelationship.UNSPECIFIED_ACCESS);
                            break;

                        case ACCESS_TYPE_READ:
                            accessRelationship.setAccessType(IAccessRelationship.READ_ACCESS);
                            break;

                        case ACCESS_TYPE_READ_WRITE:
                            accessRelationship.setAccessType(IAccessRelationship.READ_WRITE_ACCESS);
                            break;

                        default:
                            accessRelationship.setAccessType(IAccessRelationship.WRITE_ACCESS);
                            break;
                    }
                }
            }
            
            // Influence type
            if(relation instanceof IInfluenceRelationship) {
                String influenceStrength = childElement.getAttributeValue(ATTRIBUTE_INFLUENCE_MODIFIER);
                if(influenceStrength != null) {
                    ((IInfluenceRelationship)relation).setStrength(influenceStrength);
                }
            }
            
            // Add to lookup table
            fConceptsLookup.put(relation.getId(), relation);
            
            // Add to relations list for 2nd pass
            RelationInfo rInfo = new RelationInfo();
            rInfo.relation = relation;
            rInfo.sourceID = sourceID;
            rInfo.targetID = targetID;
            relationInfoList.add(rInfo);
        }
        
        // 2nd pass, add source and target concepts
        for(RelationInfo rInfo : relationInfoList) {
            IArchimateConcept source = fConceptsLookup.get(rInfo.sourceID);
            if(source == null) {
                throw new IOException(Messages.XMLModelImporter_3 + rInfo.sourceID);
            }

            IArchimateConcept target = fConceptsLookup.get(rInfo.targetID);
            if(target == null) {
                throw new IOException(Messages.XMLModelImporter_4 + rInfo.targetID);
            }

            rInfo.relation.setSource(source);
            rInfo.relation.setTarget(target);
        }
    }
    
    // ========================================= Organization ======================================

    @SuppressWarnings("unused")
    private void parseOrganization(Element organizationElement) {
        if(organizationElement == null) { // Optional
            return;
        }

        for(Element childElement : organizationElement.getChildren(ELEMENT_ITEM, ARCHIMATE3_NAMESPACE)) {
            parseItem(childElement);
        }
    }
    
    private void parseItem(Element itemElement) {
        // The idea is to see if we can match any referenced elements/relations into a suitable folder
        // and then move them to that folder. At this stage, it's not worth it.
        
        String idref = itemElement.getAttributeValue(ATTRIBUTE_IDENTIFIERREF);
        
        if(idref != null) {
            IArchimateConcept concept = fConceptsLookup.get(idref);
            if(concept != null) {
                
            }
        }
        // Folder?
        else {
            
        }

        for(Element childElement : itemElement.getChildren(ELEMENT_ITEM, ARCHIMATE3_NAMESPACE)) {
            parseItem(childElement);
        }
    }
    
    // ========================================= Views ======================================

    private void parseViews(Element viewsElement) throws XMLModelParserException {
        if(viewsElement == null) { // Optional
            return;
        }
        
        Map<String, IArchimateDiagramModel> diagramModels = new Hashtable<String, IArchimateDiagramModel>();
        
        // Add the views first because there may be child node view references
        for(Element viewElement : viewsElement.getChildren(ELEMENT_VIEW, ARCHIMATE3_NAMESPACE)) {
            IArchimateDiagramModel dm = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
            fModel.getDefaultFolderForObject(dm).getElements().add(dm);
            
            // Identifier first
            String id = viewElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
            if(id != null) {
                dm.setId(id);

                // Store it
                diagramModels.put(id, dm);
            }
            
            // Viewpoint
            String viewPointName = viewElement.getAttributeValue(ATTRIBUTE_VIEWPOINT);
            if(viewPointName != null) {
                String viewPointID = XMLTypeMapper.getViewpointID(viewPointName);
                dm.setViewpoint(viewPointID);
            }

            // Name
            String name = getChildElementText(viewElement, ELEMENT_NAME, true);
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
        
        // Now add any pending view diagram references
        for(Iterator<EObject> iter = fModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            if(eObject instanceof IDiagramModelReference) {
                IDiagramModelReference dmRef = (IDiagramModelReference)eObject;
                String refID = (String)dmRef.getAdapter(ATTRIBUTE_REF);
                if(refID != null) {
                    IArchimateDiagramModel dm = diagramModels.get(refID);
                    dmRef.setReferencedModel(dm);
                }
            }
        }
    }
    
    // ========================================= Nodes ======================================

    private void addNodes(IDiagramModelContainer parentContainer, Element parentElement) throws XMLModelParserException {
        for(Element nodeElement : parentElement.getChildren(ELEMENT_NODE, ARCHIMATE3_NAMESPACE)) {
            IDiagramModelObject dmo = null;
            
            // This has an element ref so it's an ArchiMate element node
            String elementRef = nodeElement.getAttributeValue(ATTRIBUTE_ELEMENTREF);
            if(hasValue(elementRef) ) {
                IArchimateConcept concept = fConceptsLookup.get(elementRef);
                
                if(!(concept instanceof IArchimateElement)) {
                    throw new XMLModelParserException(Messages.XMLModelImporter_5 + elementRef);
                }
                
                // Create new diagram node object
                dmo = ArchimateDiagramModelFactory.createDiagramModelArchimateObject((IArchimateElement)concept);
            }
            
            // No element ref so this is another type of node, but what is it?
            else {
                boolean isGroup = ATTRIBUTE_CONTAINER_TYPE.equals(nodeElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE));
                boolean isLabel = ATTRIBUTE_LABEL_TYPE.equals(nodeElement.getAttributeValue(ATTRIBUTE_TYPE, XSI_NAMESPACE));
                
                // Does the graphical node have children?
                // Our notes cannot contain children, so if it does contain children it has to be a Group.
                boolean hasChildren = nodeElement.getChildren(ELEMENT_NODE, ARCHIMATE3_NAMESPACE).size() > 0;
                
                // Is it a label with view ref?
                boolean isViewRef = isLabel && nodeElement.getChild(ELEMENT_VIEWREF, ARCHIMATE3_NAMESPACE) != null;
                
                if(isGroup || hasChildren) {
                    ICreationFactory factory = new ArchimateDiagramModelFactory(IArchimatePackage.eINSTANCE.getDiagramModelGroup());
                    IDiagramModelGroup group = (IDiagramModelGroup)factory.getNewObject();
                    dmo = group;

                    // Label
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
                // View Ref
                else if(isViewRef) {
                    IDiagramModelReference ref = IArchimateFactory.eINSTANCE.createDiagramModelReference();
                    dmo = ref;
                    
                    // View reference
                    Element viewRefElement = nodeElement.getChild(ELEMENT_VIEWREF, ARCHIMATE3_NAMESPACE);
                    String viewRefID = viewRefElement.getAttributeValue(ATTRIBUTE_REF);
                    // Note - the referenced diagram model will have to be set afterwards since we may not have created it yet
                    // so we use this a temp store
                    ref.setAdapter(ATTRIBUTE_REF, viewRefID);
                }
                // A Note is our only other option
                else {
                    ICreationFactory factory = new ArchimateDiagramModelFactory(IArchimatePackage.eINSTANCE.getDiagramModelNote());
                    IDiagramModelNote note = (IDiagramModelNote)factory.getNewObject();
                    
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
                addNodeStyle(dmo, nodeElement.getChild(ELEMENT_STYLE, ARCHIMATE3_NAMESPACE));
                
                // Add to lookup
                fConnectionsNodesLookup.put(dmo.getId(), dmo);

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
            throw new XMLModelParserException(Messages.XMLModelImporter_6);
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
        dmo.setFillColor(getRGBColorString(styleElement.getChild(ELEMENT_FILLCOLOR, ARCHIMATE3_NAMESPACE)));
        
        // Fill Alpha
        dmo.setAlpha(getRGBAlphaValue(styleElement.getChild(ELEMENT_FILLCOLOR, ARCHIMATE3_NAMESPACE)));

        // Line Color
        dmo.setLineColor(getRGBColorString(styleElement.getChild(ELEMENT_LINECOLOR, ARCHIMATE3_NAMESPACE)));

        // Font
        addFont(dmo, styleElement.getChild(ELEMENT_FONT, ARCHIMATE3_NAMESPACE));
    }
    
    // ======================================= Connections ====================================
    
    private void addConnections(Element viewElement) throws XMLModelParserException {
        class ConnectionInfo {
            IDiagramModelConnection connection;
            Element connectionElement;
        }
        
        List<ConnectionInfo> connectionInfoList = new ArrayList<>();
        
        // 1st pass - Create all connections
        for(Element connectionElement : viewElement.getChildren(ELEMENT_CONNECTION, ARCHIMATE3_NAMESPACE)) {
            IDiagramModelConnection connection = null;
        
            // An ArchiMate relationship connection
            String relationshipRef = connectionElement.getAttributeValue(ATTRIBUTE_RELATIONSHIPREF);
            if(hasValue(relationshipRef)) {
                // Get relationship
                IArchimateConcept concept = fConceptsLookup.get(relationshipRef);
                if(!(concept instanceof IArchimateRelationship)) {
                    throw new XMLModelParserException(Messages.XMLModelImporter_7 + relationshipRef);
                }
                
                // Create new ArchiMate connection with relationship
                connection = ArchimateDiagramModelFactory.createDiagramModelArchimateConnection((IArchimateRelationship)concept);
            }
            // Create new ordinary connection
            else {
                connection = IArchimateFactory.eINSTANCE.createDiagramModelConnection();
            }
            
            // Add id and add to lookup table
            if(connection != null) {
                // Add Identifier before adding to model
                String identifier = connectionElement.getAttributeValue(ATTRIBUTE_IDENTIFIER);
                connection.setId(identifier);
                
                // Add to connection list for 2nd pass
                ConnectionInfo cInfo = new ConnectionInfo();
                cInfo.connection = connection;
                cInfo.connectionElement = connectionElement;
                connectionInfoList.add(cInfo);
                
                // Add to lookup
                fConnectionsNodesLookup.put(connection.getId(), connection);
            }
        }
        
        // 2nd pass
        for(ConnectionInfo cInfo : connectionInfoList) {
            // Get connection source node/connection
            String sourceRef = cInfo.connectionElement.getAttributeValue(ATTRIBUTE_SOURCE);
            IConnectable connectableSource = fConnectionsNodesLookup.get(sourceRef);
            if(connectableSource == null) {
                throw new XMLModelParserException(Messages.XMLModelImporter_9 + sourceRef);
            }
            
            // Get connection target node/connection
            String targetRef = cInfo.connectionElement.getAttributeValue(ATTRIBUTE_TARGET);
            IConnectable connectableTarget = fConnectionsNodesLookup.get(targetRef);
            if(connectableTarget == null) {
                throw new XMLModelParserException(Messages.XMLModelImporter_10 + targetRef);
            }
            
            // If an ArchiMate connection, source and target must be also
            if(cInfo.connection instanceof IDiagramModelArchimateConnection) {
                // Must be ArchiMate type source
                if(!(connectableSource instanceof IDiagramModelArchimateComponent)) {
                    throw new XMLModelParserException(Messages.XMLModelImporter_11 + sourceRef);
                }

                // Must be ArchiMate type target
                if(!(connectableTarget instanceof IDiagramModelArchimateComponent)) {
                    throw new XMLModelParserException(Messages.XMLModelImporter_12 + targetRef);
                }
            }
            // Another connection type
            else {
                // Only connect between notes and groups
                if(connectableSource instanceof IDiagramModelArchimateComponent && connectableTarget instanceof IDiagramModelArchimateComponent) {
                    continue;
                }
                // Don't connect to other connections
                if(connectableSource instanceof IDiagramModelConnection || connectableTarget instanceof IDiagramModelConnection) {
                    continue;
                }
            }
            
            // Connect
            cInfo.connection.connect(connectableSource, connectableTarget);
                
            // Bendpoints
            addBendpoints(cInfo.connection, cInfo.connectionElement);
                
            // Style
            addConnectionStyle(cInfo.connection, cInfo.connectionElement.getChild(ELEMENT_STYLE, ARCHIMATE3_NAMESPACE));
        }
        
        // Add implicit nested connections
        addNestedConnections();
    }
    
    /**
     * Add implicit nested connections
     * 1. Iterate through all diagram ArchiMate nodes and look for nested nodes
     * 2. If there is a relationship between the ArchiMate elements of the nodes and no existing connection, add one
     */
    private void addNestedConnections() {
        for(IDiagramModel dm : fModel.getDiagramModels()) { // All diagrams
            for(Iterator<EObject> iter = dm.eAllContents(); iter.hasNext();) { // Contents of a diagram
                EObject eObject = iter.next();
                
                if(eObject instanceof IDiagramModelArchimateObject) { // ArchiMate node
                    IDiagramModelArchimateObject parent = (IDiagramModelArchimateObject)eObject;
                    
                    for(IDiagramModelObject dmo : parent.getChildren()) {
                        if(dmo instanceof IDiagramModelArchimateObject) { // ArchiMate child node
                            IDiagramModelArchimateObject child = (IDiagramModelArchimateObject)dmo;
                            IArchimateElement parentElement = parent.getArchimateElement();
                            IArchimateElement childElement = child.getArchimateElement();
                            
                            // Parent -> Child
                            for(IArchimateRelationship relation : parentElement.getSourceRelationships()) {
                                if(relation.getTarget() == childElement && !DiagramModelUtils.hasDiagramModelArchimateConnection(parent, child, relation)) {
                                    IDiagramModelArchimateConnection connection = ArchimateDiagramModelFactory.createDiagramModelArchimateConnection(relation);
                                    connection.connect(parent, child);
                                }
                            }
                            
                            // Child -> Parent
                            for(IArchimateRelationship relation : childElement.getSourceRelationships()) {
                                if(relation.getTarget() == parentElement && !DiagramModelUtils.hasDiagramModelArchimateConnection(child, parent, relation)) {
                                    IDiagramModelArchimateConnection connection = ArchimateDiagramModelFactory.createDiagramModelArchimateConnection(relation);
                                    connection.connect(child, parent);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        
    /**
     * Add bendpoints
     */
    private void addBendpoints(IDiagramModelConnection connection, Element connectionElement) throws XMLModelParserException {
        // TODO: Doesn't work for connection->connection
        if(connection.getSource() instanceof IDiagramModelConnection || connection.getTarget() instanceof IDiagramModelConnection) {
            return;
        }

        for(Element bendpointElement : connectionElement.getChildren(ELEMENT_BENDPOINT, ARCHIMATE3_NAMESPACE)) {
            String xString = bendpointElement.getAttributeValue(ATTRIBUTE_X);
            String yString = bendpointElement.getAttributeValue(ATTRIBUTE_Y);
            if(!hasValue(xString) || !hasValue(yString)) {
                throw new XMLModelParserException(Messages.XMLModelImporter_13);
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
        connection.setLineColor(getRGBColorString(styleElement.getChild(ELEMENT_LINECOLOR, ARCHIMATE3_NAMESPACE)));

        // Font
        addFont(connection, styleElement.getChild(ELEMENT_FONT, ARCHIMATE3_NAMESPACE));
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
            int val = Double.valueOf(fontSize).intValue();
            newFontData.setHeight(val);
        }
        
        String fontStyle = fontElement.getAttributeValue(ATTRIBUTE_FONTSTYLE);
        if(hasValue(fontStyle)) {
            int styleValue = SWT.NORMAL;
            if(fontStyle.contains("bold")) { //$NON-NLS-1$
                styleValue |= SWT.BOLD;
            }
            if(fontStyle.contains("italic")) { //$NON-NLS-1$
                styleValue |= SWT.ITALIC;
            }
            newFontData.setStyle(styleValue);
        }
        
        fontObject.setFont(newFontData.toString());
        
        // Font color
        fontObject.setFontColor(getRGBColorString(fontElement.getChild(ELEMENT_FONTCOLOR, ARCHIMATE3_NAMESPACE)));
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
                throw new XMLModelParserException(Messages.XMLModelImporter_14);
            }
            
            int red = Integer.valueOf(rString);
            int green = Integer.valueOf(gString);
            int blue = Integer.valueOf(bString);
            
            colorStr = ColorFactory.convertRGBToString(new RGB(red, green, blue));
        }
        
        return colorStr;
    }
    
    /**
     * @return The Alpha value for an element, converted to 0-255
     */
    int getRGBAlphaValue(Element rgbElement) {
        if(rgbElement != null) {
            String alphaString = rgbElement.getAttributeValue(ATTRIBUTE_A);
            if(hasValue(alphaString)) {
                int alpha = Integer.valueOf(alphaString);
                return Math.round(((float)alpha * 255) / 100);
            }
        }
        
        return 255;
    }
    
    String getChildElementText(Element parentElement, String childElementName, boolean normalise) {
        //Check for localised element according to the system's locale
        String code = Locale.getDefault().getLanguage();
        if(code == null) {
            code = "en"; //$NON-NLS-1$
        }
        
        for(Element childElement : parentElement.getChildren(childElementName, ARCHIMATE3_NAMESPACE)) {
            String lang = childElement.getAttributeValue(ATTRIBUTE_LANG, Namespace.XML_NAMESPACE);
            if(code.equals(lang)) {
                return normalise ? childElement.getTextNormalize() : childElement.getText();
            }
        }
        
        // Default to first element found
        Element element = parentElement.getChild(childElementName, ARCHIMATE3_NAMESPACE);
        return element == null ? null : normalise ? element.getTextNormalize() : element.getText();
    }
    
    boolean hasValue(String val) {
        return StringUtils.isSet(val);
    }
}
