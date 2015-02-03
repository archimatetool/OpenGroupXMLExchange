/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;


/**
 * XML Model Exporter Tests
 * 
 * @author Phillip Beauvoir
 */
public class XMLValidatorTests {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(XMLValidatorTests.class);
    }

    @Test
    public void testValidate() throws Exception {
        XMLValidator validator = new XMLValidator();
        validator.validateXML(TestSupport.xmlFile2);
    }
    
}
