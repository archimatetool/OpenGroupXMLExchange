/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelGroup;
import com.archimatetool.model.util.ArchimateResourceFactory;


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


    @Test
    public void testGetAbsoluteBounds() {
        XMLModelExporter exporter = new XMLModelExporter();
        IArchimateDiagramModel dm = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
        
        IDiagramModelGroup dmo1 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dmo1.setBounds(10, 15, 500, 500);
        dm.getChildren().add(dmo1);
        
        IBounds bounds = exporter.getAbsoluteBounds(dmo1);
        assertEquals(10, bounds.getX());
        assertEquals(15, bounds.getY());
        
        IDiagramModelGroup dmo2 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dmo2.setBounds(10, 15, 400, 400);
        dmo1.getChildren().add(dmo2);

        bounds = exporter.getAbsoluteBounds(dmo2);
        assertEquals(20, bounds.getX());
        assertEquals(30, bounds.getY());
        
        IDiagramModelGroup dmo3 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dmo3.setBounds(10, 15, 300, 300);
        dmo2.getChildren().add(dmo3);

        bounds = exporter.getAbsoluteBounds(dmo3);
        assertEquals(30, bounds.getX());
        assertEquals(45, bounds.getY());
    }
    
    @Test
    public void testExportModel() throws IOException {
        File inputFile = new File(TestSupport.getTestDataFolder(), "archisurance.archimate");
        Resource resource = ArchimateResourceFactory.createNewResource(inputFile);
        resource.load(null);
        
        IArchimateModel model = (IArchimateModel)resource.getContents().get(0);
        
        XMLModelExporter exporter = new XMLModelExporter();
        
        // Language code
        exporter.setLanguageCode("en");
        
        // Metadata
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("creator", "Phil Beauvoir");
        metadata.put("date", "2015-01-21 17:50");
        metadata.put("description", "Test the Archisurance Exchange Model");
        metadata.put("language", "en");
        metadata.put("subject", "ArchiMate, Testing");
        metadata.put("title", "Archisurance Test Exchange Model");
        exporter.setMetadata(metadata);
        
        // Organization
        exporter.setSaveOrganisation(true);
        
        File outputFile = new File(TestSupport.getTestDataFolder(), "archisurance.xml");
        exporter.exportModel(model, outputFile);
    }

}
