/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.xmlexchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimatePackage;
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

    private File testFolder = TestUtils.getLocalBundleFolder("com.archimatetool.xmlexchange.tests", "testdata");
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
    
}
