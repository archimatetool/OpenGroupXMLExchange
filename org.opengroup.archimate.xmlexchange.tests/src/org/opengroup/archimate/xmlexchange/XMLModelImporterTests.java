/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.opengroup.archimate.xmlexchange.XMLModelImporter;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelGroup;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IRelationship;
import com.archimatetool.tests.TestUtils;

/**
 * XML Model Importer Tests
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLModelImporterTests {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(XMLModelImporterTests.class);
    }

    private File testFolder = TestUtils.getLocalBundleFolder("org.opengroup.archimate.xmlexchange.tests", "testdata");
    private File testFile1 = new File(testFolder, "sample1.xml");
    
    private XMLModelImporter importer;
    
    @Before
    public void runOnceBeforeEachTest() {
        importer = new XMLModelImporter();
    }


    @Test
    public void testArchimateModelExists() throws Exception {
        IArchimateModel model = importer.createArchiMateModel(testFile1);
        
        assertNotNull(model);
        
        // Model has default folders
        assertFalse(model.getFolders().isEmpty());
        
        // Model has name
        assertNotNull(model.getName());
    }
    
    @Test
    public void testArchimateModelHasCorrectElementsAndRelations() throws Exception {
        IArchimateModel model = importer.createArchiMateModel(testFile1);
        
        IFolder businessFolder = model.getFolder(FolderType.BUSINESS);
        IFolder relationsFolder = model.getFolder(FolderType.RELATIONS);
        
        assertEquals(2, businessFolder.getElements().size());
        assertEquals(1, relationsFolder.getElements().size());
        
        IArchimateElement element1 = (IArchimateElement)businessFolder.getElements().get(0);
        IArchimateElement element2 = (IArchimateElement)businessFolder.getElements().get(1);
        
        assertEquals(IArchimatePackage.eINSTANCE.getBusinessRole(), element1.eClass());
        assertEquals(IArchimatePackage.eINSTANCE.getBusinessProcess(), element2.eClass());
        
        IRelationship relation = (IRelationship)relationsFolder.getElements().get(0);
        assertEquals(IArchimatePackage.eINSTANCE.getAssignmentRelationship(), relation.eClass());
        assertEquals(element1, relation.getSource());
        assertEquals(element2, relation.getTarget());
    }
    
    @Test
    public void testGetRelativeBounds() {
        IArchimateDiagramModel dm = IArchimateFactory.eINSTANCE.createArchimateDiagramModel();
        
        IDiagramModelGroup dmo1 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dm.getChildren().add(dmo1);
        
        IBounds bounds = importer.getRelativeBounds(IArchimateFactory.eINSTANCE.createBounds(10, 15, 500, 500), dmo1);
        assertEquals(10, bounds.getX());
        assertEquals(15, bounds.getY());
        dmo1.setBounds(bounds);
        
        IDiagramModelGroup dmo2 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dmo1.getChildren().add(dmo2);

        bounds = importer.getRelativeBounds(IArchimateFactory.eINSTANCE.createBounds(20, 30, 500, 500), dmo2);
        assertEquals(10, bounds.getX());
        assertEquals(15, bounds.getY());
        dmo2.setBounds(bounds);
        
        IDiagramModelGroup dmo3 = IArchimateFactory.eINSTANCE.createDiagramModelGroup();
        dmo2.getChildren().add(dmo3);

        bounds = importer.getRelativeBounds(IArchimateFactory.eINSTANCE.createBounds(30, 45, 500, 500), dmo3);
        assertEquals(10, bounds.getX());
        assertEquals(15, bounds.getY());
        dmo3.setBounds(bounds);
    }

}
