/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.xmlexchange;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.IRelationship;


/**
 * XML Model Exporter Tests
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLModelExporterTests {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(XMLModelExporterTests.class);
    }

    private XMLModelExporter exporter;
    private File outputFile;

    @Before
    public void runOnceBeforeEachTest() {
        exporter = new XMLModelExporter();
    }

    @Test
    public void testExportModel() throws Exception {
        IArchimateModel model = IArchimateFactory.eINSTANCE.createArchimateModel();
        model.setDefaults();
        
        IFolder businessFolder = model.getFolder(FolderType.BUSINESS);
        IFolder relationsFolder = model.getFolder(FolderType.RELATIONS);
        
        IArchimateElement element1 = IArchimateFactory.eINSTANCE.createBusinessRole();
        element1.setName("Sales Person");
        element1.setDocumentation("A description of a sales person");
        businessFolder.getElements().add(element1);
        
        element1.getProperties().add(createProperty("property1", "value1"));
        element1.getProperties().add(createProperty("property2", "value2"));
        element1.getProperties().add(createProperty("property3", "value3"));
        
        IArchimateElement element2 = IArchimateFactory.eINSTANCE.createBusinessProcess();
        element2.setName("Sell Product");
        element2.setDocumentation("A description of the sell product process");
        businessFolder.getElements().add(element2);
        
        element2.getProperties().add(createProperty("property1", "value1a"));
        
        IRelationship relation = IArchimateFactory.eINSTANCE.createAssignmentRelationship();
        relation.setSource(element1);
        relation.setTarget(element2);
        relationsFolder.getElements().add(relation);
        
        relation.getProperties().add(createProperty("property2", "value2a"));
        
        outputFile = new File("testdata", "Archi-Exported.xml");
        exporter.exportModel(model, outputFile);
    }
    
    @After
    public void deleteOutputFile() {
        outputFile.delete();
    }
    
    IProperty createProperty(String key, String name) {
        IProperty property = IArchimateFactory.eINSTANCE.createProperty();
        property.setKey(key);
        property.setValue(name);
        return property;
    }
}
