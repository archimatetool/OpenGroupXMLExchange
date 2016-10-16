/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;

import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimatePackage;


@SuppressWarnings("nls")
public class XMLTypeMapper implements IXMLExchangeGlobals {
    
    // Mapping of Type strings to Element Eobjects
    private static Map<String, EClass> ElementsMapping = new Hashtable<String, EClass>();
    
    static {
        // Business Elements
        ElementsMapping.put("BusinessActor", IArchimatePackage.eINSTANCE.getBusinessActor());
        ElementsMapping.put("BusinessRole", IArchimatePackage.eINSTANCE.getBusinessRole());
        ElementsMapping.put("BusinessCollaboration", IArchimatePackage.eINSTANCE.getBusinessCollaboration());
        ElementsMapping.put("BusinessInterface", IArchimatePackage.eINSTANCE.getBusinessInterface());
        ElementsMapping.put("BusinessFunction", IArchimatePackage.eINSTANCE.getBusinessFunction());
        ElementsMapping.put("BusinessProcess", IArchimatePackage.eINSTANCE.getBusinessProcess());
        ElementsMapping.put("BusinessEvent", IArchimatePackage.eINSTANCE.getBusinessEvent());
        ElementsMapping.put("BusinessInteraction", IArchimatePackage.eINSTANCE.getBusinessInteraction());
        ElementsMapping.put("Product", IArchimatePackage.eINSTANCE.getProduct());
        ElementsMapping.put("Contract", IArchimatePackage.eINSTANCE.getContract());
        ElementsMapping.put("BusinessService", IArchimatePackage.eINSTANCE.getBusinessService());
        ElementsMapping.put("Value", IArchimatePackage.eINSTANCE.getValue());
        ElementsMapping.put("Meaning", IArchimatePackage.eINSTANCE.getMeaning());
        ElementsMapping.put("Representation", IArchimatePackage.eINSTANCE.getRepresentation());
        ElementsMapping.put("BusinessObject", IArchimatePackage.eINSTANCE.getBusinessObject());
        ElementsMapping.put("Location", IArchimatePackage.eINSTANCE.getLocation());
        
        // Application Elements
        ElementsMapping.put("ApplicationComponent", IArchimatePackage.eINSTANCE.getApplicationComponent());
        ElementsMapping.put("ApplicationCollaboration", IArchimatePackage.eINSTANCE.getApplicationCollaboration());
        ElementsMapping.put("ApplicationInterface", IArchimatePackage.eINSTANCE.getApplicationInterface());
        ElementsMapping.put("ApplicationService", IArchimatePackage.eINSTANCE.getApplicationService());
        ElementsMapping.put("ApplicationFunction", IArchimatePackage.eINSTANCE.getApplicationFunction());
        ElementsMapping.put("ApplicationInteraction", IArchimatePackage.eINSTANCE.getApplicationInteraction());
        ElementsMapping.put("DataObject", IArchimatePackage.eINSTANCE.getDataObject());
        
        // Technology Elements
        ElementsMapping.put("Artifact", IArchimatePackage.eINSTANCE.getArtifact());
        ElementsMapping.put("Path", IArchimatePackage.eINSTANCE.getPath());
        ElementsMapping.put("CommunicationNetwork", IArchimatePackage.eINSTANCE.getCommunicationNetwork());
        ElementsMapping.put("TechnologyInterface", IArchimatePackage.eINSTANCE.getTechnologyInterface());
        ElementsMapping.put("TechnologyFunction", IArchimatePackage.eINSTANCE.getTechnologyFunction());
        ElementsMapping.put("TechnologyService", IArchimatePackage.eINSTANCE.getTechnologyService());
        ElementsMapping.put("Node", IArchimatePackage.eINSTANCE.getNode());
        ElementsMapping.put("SystemSoftware", IArchimatePackage.eINSTANCE.getSystemSoftware());
        ElementsMapping.put("Device", IArchimatePackage.eINSTANCE.getDevice());

        // Extensions
        ElementsMapping.put("Stakeholder", IArchimatePackage.eINSTANCE.getStakeholder());
        ElementsMapping.put("Driver", IArchimatePackage.eINSTANCE.getDriver());
        ElementsMapping.put("Assessment", IArchimatePackage.eINSTANCE.getAssessment());
        ElementsMapping.put("Goal", IArchimatePackage.eINSTANCE.getGoal());
        ElementsMapping.put("Principle", IArchimatePackage.eINSTANCE.getPrinciple());
        ElementsMapping.put("Requirement", IArchimatePackage.eINSTANCE.getRequirement());
        ElementsMapping.put("Constraint", IArchimatePackage.eINSTANCE.getConstraint());
        ElementsMapping.put("WorkPackage", IArchimatePackage.eINSTANCE.getWorkPackage());
        ElementsMapping.put("Deliverable", IArchimatePackage.eINSTANCE.getDeliverable());
        ElementsMapping.put("Plateau", IArchimatePackage.eINSTANCE.getPlateau());
        ElementsMapping.put("Gap", IArchimatePackage.eINSTANCE.getGap());
        
        // Relations
        ElementsMapping.put("AssignmentRelationship", IArchimatePackage.eINSTANCE.getAssignmentRelationship());
        ElementsMapping.put("AccessRelationship", IArchimatePackage.eINSTANCE.getAccessRelationship());
        ElementsMapping.put("AssociationRelationship", IArchimatePackage.eINSTANCE.getAssociationRelationship());
        ElementsMapping.put("CompositionRelationship", IArchimatePackage.eINSTANCE.getCompositionRelationship());
        ElementsMapping.put("AggregationRelationship", IArchimatePackage.eINSTANCE.getAggregationRelationship());
        ElementsMapping.put("ServingRelationship", IArchimatePackage.eINSTANCE.getServingRelationship());
        ElementsMapping.put("TriggeringRelationship", IArchimatePackage.eINSTANCE.getTriggeringRelationship());
        ElementsMapping.put("FlowRelationship", IArchimatePackage.eINSTANCE.getFlowRelationship());
        ElementsMapping.put("RealizationRelationship", IArchimatePackage.eINSTANCE.getRealizationRelationship());
        ElementsMapping.put("SpecializationRelationship", IArchimatePackage.eINSTANCE.getSpecializationRelationship());
        ElementsMapping.put("InfluenceRelationship", IArchimatePackage.eINSTANCE.getInfluenceRelationship());
        
        // Junctions
        ElementsMapping.put("Junction", IArchimatePackage.eINSTANCE.getJunction());
    }

    public static IArchimateConcept createArchimateConcept(String type) {
        EClass eClass = ElementsMapping.get(type);
        return (IArchimateConcept)(eClass == null ? null : IArchimateFactory.eINSTANCE.create(eClass));
    }
    
    public static String getArchimateComponentName(IArchimateConcept archimateComponent) {
        for(Entry<String, EClass> entry : ElementsMapping.entrySet()) {
            if(entry.getValue().equals(archimateComponent.eClass())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    // Mapping of Viewpoint Names
    private static Map<String, String> ViewPointsMapping = new Hashtable<String, String>();
    
    static {
        ViewPointsMapping.put("", ""); // This means no Viewpoint
        ViewPointsMapping.put("organization", "Actor Co-operation");
        // TODO Rest of them
    }

    public static String getViewpointName(String viewPointID) {
        return ViewPointsMapping.get(viewPointID);
    }
    
    public static String getViewpointID(String viewPointName) {
        for(Entry<String, String> entry : ViewPointsMapping.entrySet()) {
            if(entry.getValue().equals(viewPointName)) {
                return entry.getKey();
            }
        }
        
        return "";
    }

}
