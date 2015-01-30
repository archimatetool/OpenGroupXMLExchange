/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelObject;


/**
 * Utils for XML Exchange
 * 
 * @author Phillip Beauvoir
 */
public final class XMLExchangeUtils {

    /**
     * Return the absolute bounds of a diagram model object
     * @param dmo The DiagramModelObject
     * @return The absolute bounds of a diagram model object
     */
    public static final IBounds getAbsoluteBounds(IDiagramModelObject dmo) {
        IBounds bounds = dmo.getBounds().getCopy();
        
        EObject container = dmo.eContainer();
        while(container instanceof IDiagramModelObject) {
            IDiagramModelObject parent = (IDiagramModelObject)container;
            IBounds parentBounds = parent.getBounds().getCopy();
            
            bounds.setX(bounds.getX() + parentBounds.getX());
            bounds.setY(bounds.getY() + parentBounds.getY());
            
            container = container.eContainer();
        }

        return bounds;
    }

    /**
     * Convert the given absolute bounds to the relative bounds of a diagram model object
     * @param absoluteBounds The absolute bounds as imported from the XML file
     * @param dmo The DiagramModelObject that should be already contained in its parent
     * @return the relative bounds of a diagram model object
     */
    public static final IBounds convertAbsoluteToRelativeBounds(IBounds absoluteBounds, IDiagramModelObject dmo) {
        IBounds bounds = absoluteBounds.getCopy();
        
        EObject container = dmo.eContainer();
        while(container instanceof IDiagramModelObject) {
            IDiagramModelObject parent = (IDiagramModelObject)container;
            IBounds parentBounds = parent.getBounds().getCopy();
            
            bounds.setX(bounds.getX() - parentBounds.getX());
            bounds.setY(bounds.getY() - parentBounds.getY());
            
            container = container.eContainer();
        }

        return bounds;
    }

}
