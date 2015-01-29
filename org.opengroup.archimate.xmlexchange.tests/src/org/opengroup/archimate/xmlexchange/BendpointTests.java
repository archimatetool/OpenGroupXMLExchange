/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelBendpoint;

/**
 * @author Phillip Beauvoir
 */
public class BendpointTests {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(BendpointTests.class);
    }

    @Test
    public void testBendpoint1() throws Exception {
        int startX = 120;
        int startY = -30;
        int endX = -192;
        int endY = -30;
        
        IBounds srcBounds =  IArchimateFactory.eINSTANCE.createBounds(768, 108, 120, 61);
        IBounds tgtBounds =  IArchimateFactory.eINSTANCE.createBounds(408, 108, 120, 61);
        
        // Export
        IDiagramModelBendpoint bendpoint = IArchimateFactory.eINSTANCE.createDiagramModelBendpoint();
        bendpoint.setStartX(startX);
        bendpoint.setStartY(startY);
        bendpoint.setEndX(endX);
        bendpoint.setEndY(endY);
        
        int x = (srcBounds.getX() + (srcBounds.getWidth() / 2)) + bendpoint.getStartX();
        int y = (srcBounds.getY() + (srcBounds.getHeight() / 2)) + bendpoint.getStartY();
        
        assertEquals(722, x);
        assertEquals(108, y);
        
        // Import
        startX = x - (srcBounds.getX() + (srcBounds.getWidth() / 2));
        startY = y - (srcBounds.getY() + (srcBounds.getHeight() / 2));
        endX = x - (tgtBounds.getX() + (tgtBounds.getWidth() / 2));
        endY = y - (tgtBounds.getY() + (tgtBounds.getHeight() / 2));
        
        assertEquals(180, startX);
        assertEquals(-51, startY);
        assertEquals(-240, endX);
        assertEquals(-75, endY);
    }
    
}
