/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import com.archimatetool.model.IArchimateModel;



/**
 * Export to XML Wizard
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class ExportToXMLWizard extends Wizard {
    
    private IArchimateModel fModel;
    
    private ExportToXMLPage fPage;
    private ExportToXMLPageMetadata fPageMetadata;
    
    public ExportToXMLWizard(IArchimateModel model) {
        fModel = model;
        setWindowTitle("Export Model");
    }
    
    @Override
    public void addPages() {
        fPage = new ExportToXMLPage(fModel);
        addPage(fPage);
        
        fPageMetadata = new ExportToXMLPageMetadata();
        addPage(fPageMetadata);
    }
    
    @Override
    public boolean performFinish() {
        final File file = new File(fPage.getFileName());
        
        // Check valid file name
        try {
            file.getCanonicalPath();
        }
        catch(IOException ex) {
            MessageDialog.openError(getShell(), "File error", "The file name is incorrect");
            return false;
        }
        
        // Make sure the file does not already exist
        if(file.exists()) {
            boolean result = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    "Export Model",
                    "'" + file + "' already exists. Are you sure you want to overwrite it?");
            if(!result) {
                return false;
            }
        }

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            @Override
            public void run() {
                try {
                    XMLModelExporter xmlModelExporter = new XMLModelExporter();
                    xmlModelExporter.setMetadata(fPageMetadata.getMetadata());
                    xmlModelExporter.setSaveOrganisation(fPage.doSaveOrganisation());
                    xmlModelExporter.setIncludeXSD(fPage.doIncludeXSD());
                    xmlModelExporter.exportModel(fModel, file);

                    fPage.storePreferences();
                    fPageMetadata.storePreferences();
                }
                catch(Throwable ex) {
                    ex.printStackTrace();
                    MessageDialog.openError(Display.getCurrent().getActiveShell(),
                            "Export Model",
                            "Error Exporting" + " " + ex.getMessage()); //$NON-NLS-1$
                }
            }
        });
        
        return true;
    }

}
