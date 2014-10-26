/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.xmlexchange;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;

import com.archimatetool.model.IArchimateComponent;
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
        ElementsMapping.put("CommunicationPath", IArchimatePackage.eINSTANCE.getCommunicationPath());
        ElementsMapping.put("Network", IArchimatePackage.eINSTANCE.getNetwork());
        ElementsMapping.put("InfrastructureInterface", IArchimatePackage.eINSTANCE.getInfrastructureInterface());
        ElementsMapping.put("InfrastructureFunction", IArchimatePackage.eINSTANCE.getInfrastructureFunction());
        ElementsMapping.put("InfrastructureService", IArchimatePackage.eINSTANCE.getInfrastructureService());
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
        ElementsMapping.put("UsedByRelationship", IArchimatePackage.eINSTANCE.getUsedByRelationship());
        ElementsMapping.put("TriggeringRelationship", IArchimatePackage.eINSTANCE.getTriggeringRelationship());
        ElementsMapping.put("FlowRelationship", IArchimatePackage.eINSTANCE.getFlowRelationship());
        ElementsMapping.put("RealisationRelationship", IArchimatePackage.eINSTANCE.getRealisationRelationship());
        ElementsMapping.put("SpecialisationRelationship", IArchimatePackage.eINSTANCE.getSpecialisationRelationship());
        ElementsMapping.put("InfluenceRelationship", IArchimatePackage.eINSTANCE.getInfluenceRelationship());
        
        // Junctions
        //ElementsMapping.put("Junction", IArchimatePackage.eINSTANCE.getJunction());
        //ElementsMapping.put("AndJunction", IArchimatePackage.eINSTANCE.getAndJunction());
        //ElementsMapping.put("OrJunction", IArchimatePackage.eINSTANCE.getOrJunction());
    }

    public static IArchimateComponent createArchimateComponent(String type) {
        // Junction is a special case
        if("Junction".equals(type)) {
            // TODO: ascertain from Property if AND/OR
            return IArchimateFactory.eINSTANCE.createJunction();
        }

        EClass eClass = ElementsMapping.get(type);
        return (IArchimateComponent)(eClass == null ? null : IArchimateFactory.eINSTANCE.create(eClass));
    }
    
    public static String getArchimateComponentName(IArchimateComponent archimateComponent) {
        // Junctions are a special case
        if(archimateComponent.eClass() == IArchimatePackage.eINSTANCE.getJunction() || archimateComponent.eClass() == IArchimatePackage.eINSTANCE.getAndJunction()
                || archimateComponent.eClass() == IArchimatePackage.eINSTANCE.getOrJunction()) {
            return "Junction";
        }

        for(Entry<String, EClass> entry : ElementsMapping.entrySet()) {
            if(entry.getValue().equals(archimateComponent.eClass())) {
                return entry.getKey();
            }
        }
        
        return null;
    }
}
