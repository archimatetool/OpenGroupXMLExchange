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
import com.archimatetool.model.IJunction;


@SuppressWarnings("nls")
public class XMLTypeMapper implements IXMLExchangeGlobals {
    
    // Mapping of Type strings to Element Eobjects
    private static Map<String, EClass> ElementsMapping = new Hashtable<String, EClass>();
    
    static {
        // Strategy Elements
        ElementsMapping.put("Capability", IArchimatePackage.eINSTANCE.getCapability());
        ElementsMapping.put("CourseOfAction", IArchimatePackage.eINSTANCE.getCourseOfAction());
        ElementsMapping.put("Resource", IArchimatePackage.eINSTANCE.getResource());
        
        // Business Elements
        ElementsMapping.put("BusinessActor", IArchimatePackage.eINSTANCE.getBusinessActor());
        ElementsMapping.put("BusinessCollaboration", IArchimatePackage.eINSTANCE.getBusinessCollaboration());
        ElementsMapping.put("BusinessEvent", IArchimatePackage.eINSTANCE.getBusinessEvent());
        ElementsMapping.put("BusinessFunction", IArchimatePackage.eINSTANCE.getBusinessFunction());
        ElementsMapping.put("BusinessInteraction", IArchimatePackage.eINSTANCE.getBusinessInteraction());
        ElementsMapping.put("BusinessInterface", IArchimatePackage.eINSTANCE.getBusinessInterface());
        ElementsMapping.put("BusinessObject", IArchimatePackage.eINSTANCE.getBusinessObject());
        ElementsMapping.put("BusinessProcess", IArchimatePackage.eINSTANCE.getBusinessProcess());
        ElementsMapping.put("BusinessRole", IArchimatePackage.eINSTANCE.getBusinessRole());
        ElementsMapping.put("BusinessService", IArchimatePackage.eINSTANCE.getBusinessService());
        ElementsMapping.put("Contract", IArchimatePackage.eINSTANCE.getContract());
        ElementsMapping.put("Product", IArchimatePackage.eINSTANCE.getProduct());
        ElementsMapping.put("Representation", IArchimatePackage.eINSTANCE.getRepresentation());
        
        // Application Elements
        ElementsMapping.put("ApplicationCollaboration", IArchimatePackage.eINSTANCE.getApplicationCollaboration());
        ElementsMapping.put("ApplicationComponent", IArchimatePackage.eINSTANCE.getApplicationComponent());
        ElementsMapping.put("ApplicationEvent", IArchimatePackage.eINSTANCE.getApplicationEvent());
        ElementsMapping.put("ApplicationFunction", IArchimatePackage.eINSTANCE.getApplicationFunction());
        ElementsMapping.put("ApplicationInteraction", IArchimatePackage.eINSTANCE.getApplicationInteraction());
        ElementsMapping.put("ApplicationInterface", IArchimatePackage.eINSTANCE.getApplicationInterface());
        ElementsMapping.put("ApplicationProcess", IArchimatePackage.eINSTANCE.getApplicationProcess());
        ElementsMapping.put("ApplicationService", IArchimatePackage.eINSTANCE.getApplicationService());
        ElementsMapping.put("DataObject", IArchimatePackage.eINSTANCE.getDataObject());
        
        // Technology Elements
        ElementsMapping.put("Artifact", IArchimatePackage.eINSTANCE.getArtifact());
        ElementsMapping.put("CommunicationNetwork", IArchimatePackage.eINSTANCE.getCommunicationNetwork());
        ElementsMapping.put("Device", IArchimatePackage.eINSTANCE.getDevice());
        ElementsMapping.put("Node", IArchimatePackage.eINSTANCE.getNode());
        ElementsMapping.put("Path", IArchimatePackage.eINSTANCE.getPath());
        ElementsMapping.put("SystemSoftware", IArchimatePackage.eINSTANCE.getSystemSoftware());
        ElementsMapping.put("TechnologyCollaboration", IArchimatePackage.eINSTANCE.getTechnologyCollaboration());
        ElementsMapping.put("TechnologyInteraction", IArchimatePackage.eINSTANCE.getTechnologyInteraction());
        ElementsMapping.put("TechnologyInterface", IArchimatePackage.eINSTANCE.getTechnologyInterface());
        ElementsMapping.put("TechnologyEvent", IArchimatePackage.eINSTANCE.getTechnologyEvent());
        ElementsMapping.put("TechnologyFunction", IArchimatePackage.eINSTANCE.getTechnologyFunction());
        ElementsMapping.put("TechnologyProcess", IArchimatePackage.eINSTANCE.getTechnologyProcess());
        ElementsMapping.put("TechnologyService", IArchimatePackage.eINSTANCE.getTechnologyService());
        
        // Physical Elements
        ElementsMapping.put("DistributionNetwork", IArchimatePackage.eINSTANCE.getDistributionNetwork());
        ElementsMapping.put("Equipment", IArchimatePackage.eINSTANCE.getEquipment());
        ElementsMapping.put("Facility", IArchimatePackage.eINSTANCE.getFacility());
        ElementsMapping.put("Material", IArchimatePackage.eINSTANCE.getMaterial());

        // Motivation
        ElementsMapping.put("Assessment", IArchimatePackage.eINSTANCE.getAssessment());
        ElementsMapping.put("Constraint", IArchimatePackage.eINSTANCE.getConstraint());
        ElementsMapping.put("Driver", IArchimatePackage.eINSTANCE.getDriver());
        ElementsMapping.put("Goal", IArchimatePackage.eINSTANCE.getGoal());
        ElementsMapping.put("Outcome", IArchimatePackage.eINSTANCE.getOutcome());
        ElementsMapping.put("Meaning", IArchimatePackage.eINSTANCE.getMeaning());
        ElementsMapping.put("Principle", IArchimatePackage.eINSTANCE.getPrinciple());
        ElementsMapping.put("Requirement", IArchimatePackage.eINSTANCE.getRequirement());
        ElementsMapping.put("Stakeholder", IArchimatePackage.eINSTANCE.getStakeholder());
        ElementsMapping.put("Value", IArchimatePackage.eINSTANCE.getValue());

        // Impl/Migration
        ElementsMapping.put("Deliverable", IArchimatePackage.eINSTANCE.getDeliverable());
        ElementsMapping.put("Gap", IArchimatePackage.eINSTANCE.getGap());
        ElementsMapping.put("ImplementationEvent", IArchimatePackage.eINSTANCE.getImplementationEvent());
        ElementsMapping.put("Plateau", IArchimatePackage.eINSTANCE.getPlateau());
        ElementsMapping.put("WorkPackage", IArchimatePackage.eINSTANCE.getWorkPackage());

        // Other
        ElementsMapping.put("Grouping", IArchimatePackage.eINSTANCE.getGrouping());
        ElementsMapping.put("Location", IArchimatePackage.eINSTANCE.getLocation());
        
        // Relations
        ElementsMapping.put("Assignment", IArchimatePackage.eINSTANCE.getAssignmentRelationship());
        ElementsMapping.put("Access", IArchimatePackage.eINSTANCE.getAccessRelationship());
        ElementsMapping.put("Association", IArchimatePackage.eINSTANCE.getAssociationRelationship());
        ElementsMapping.put("Composition", IArchimatePackage.eINSTANCE.getCompositionRelationship());
        ElementsMapping.put("Aggregation", IArchimatePackage.eINSTANCE.getAggregationRelationship());
        ElementsMapping.put("Serving", IArchimatePackage.eINSTANCE.getServingRelationship());
        ElementsMapping.put("Triggering", IArchimatePackage.eINSTANCE.getTriggeringRelationship());
        ElementsMapping.put("Flow", IArchimatePackage.eINSTANCE.getFlowRelationship());
        ElementsMapping.put("Realization", IArchimatePackage.eINSTANCE.getRealizationRelationship());
        ElementsMapping.put("Specialization", IArchimatePackage.eINSTANCE.getSpecializationRelationship());
        ElementsMapping.put("Influence", IArchimatePackage.eINSTANCE.getInfluenceRelationship());
        
        // Junctions
        ElementsMapping.put("AndJunction", IArchimatePackage.eINSTANCE.getJunction());
        ElementsMapping.put("OrJunction", IArchimatePackage.eINSTANCE.getJunction());
    }

    public static IArchimateConcept createArchimateConcept(String type) {
        EClass eClass = ElementsMapping.get(type);
        
        if(eClass == null) {
            System.err.println("Concept was null for: " + type);
            return null;
        }
        
        IArchimateConcept archimateConcept = (IArchimateConcept)IArchimateFactory.eINSTANCE.create(eClass);
        
        // Junction is a special case
        if(eClass == IArchimatePackage.eINSTANCE.getJunction()) {
            if("OrJunction".equals(type)) {
                ((IJunction)archimateConcept).setType(IJunction.OR_JUNCTION_TYPE);
            }
        }

        return archimateConcept;
    }
    
    public static String getArchimateConceptName(IArchimateConcept archimateConcept) {
        // Junction is a special case
        if(archimateConcept.eClass() == IArchimatePackage.eINSTANCE.getJunction()) {
            String type = ((IJunction)archimateConcept).getType();
            if(IJunction.OR_JUNCTION_TYPE.equals(type)) {
                return "OrJunction";
            }
            return "AndJunction";
        }
        
        for(Entry<String, EClass> entry : ElementsMapping.entrySet()) {
            if(entry.getValue().equals(archimateConcept.eClass())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    // Mapping of Viewpoint Names
    private static Map<String, String> ViewPointsMapping = new Hashtable<String, String>();
    
    static {
        ViewPointsMapping.put("", ""); // This means no Viewpoint
        ViewPointsMapping.put("organization", "Organization");
//        ViewPointsMapping.put("", "Application Platform");
//        ViewPointsMapping.put("", "Information Structure");
//        ViewPointsMapping.put("", "Technology");
//        ViewPointsMapping.put("", "Layered");
//        ViewPointsMapping.put("", "Physical");
//        ViewPointsMapping.put("", "Product");
//        ViewPointsMapping.put("", "Application Usage");
//        ViewPointsMapping.put("", "Technology Usage");
//        ViewPointsMapping.put("", "Business Process Cooperation");
//        ViewPointsMapping.put("", "Application Cooperation");
//        ViewPointsMapping.put("", "Service Realization");
//        ViewPointsMapping.put("", "Implementation and Deployment");
//        ViewPointsMapping.put("", "Goal Realization");
//        ViewPointsMapping.put("", "Goal Contribution");
//        ViewPointsMapping.put("", "Principles");
//        ViewPointsMapping.put("", "Requirements Realization");
//        ViewPointsMapping.put("", "Motivation");
//        ViewPointsMapping.put("", "Capability Map");
//        ViewPointsMapping.put("", "Outcome Realization");
//        ViewPointsMapping.put("", "Resource Map");
//        ViewPointsMapping.put("", "Project");
//        ViewPointsMapping.put("", "Migration");
//        ViewPointsMapping.put("", "Implementation and Migration");
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
