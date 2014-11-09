/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.IArchimateImages;
import com.archimatetool.editor.ui.UIUtils;
import com.archimatetool.editor.utils.StringUtils;



/**
 * Export to XML Wizard Page for Metadata
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class ExportToXMLPageMetadata extends WizardPage {

    private static String HELP_ID = "org.opengroup.archimate.xmlexchange.help.ExportToXMLPageMetadata"; //$NON-NLS-1$
    
    private static final String PREFS_LAST_VALUE = "ExportXMLExchangeLastMD_"; //$NON-NLS-1$
    
    private String[] dcNames = {
            "title",
            "creator",
            "subject",
            "description",
            "date",
            "language"
    };
    
    private String[] dcTitles = {
            "Title",
            "Creator",
            "Subject",
            "Description",
            "Date",
            "Language"
    };
    
    private Text[] fTextControls;
    
    public ExportToXMLPageMetadata() {
        super("ExportToXMLPageMetadata"); //$NON-NLS-1$
        
        setTitle("Export model");
        setDescription("Add Metadata to describe the model");
        setImageDescriptor(IArchimateImages.ImageFactory.getImageDescriptor(IArchimateImages.ECLIPSE_IMAGE_EXPORT_DIR_WIZARD));
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());
        setControl(container);
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, HELP_ID);
        
        Group metadataGroup = new Group(container, SWT.NULL);
        metadataGroup.setText("Metadata");
        metadataGroup.setLayout(new GridLayout(2, false));
        metadataGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fTextControls = new Text[dcNames.length];
        
        for(int i = 0; i < dcNames.length; i++) {
            Label label = new Label(metadataGroup, SWT.NULL);
            label.setText(dcTitles[i] + ":");
            
            fTextControls[i] = new Text(metadataGroup, SWT.BORDER | SWT.SINGLE);
            fTextControls[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            // Single text control so strip CRLFs
            UIUtils.conformSingleTextControl(fTextControls[i]);

            IPreferenceStore store = XMLExchangePlugin.INSTANCE.getPreferenceStore();
            String lastValue = store.getString(PREFS_LAST_VALUE + dcNames[i]);
            if(StringUtils.isSet(lastValue)) {
                fTextControls[i].setText(lastValue);
            }
        }
    }
    
    Map<String, String> getMetadata() {
        Map<String, String> list = new TreeMap<String, String>();
        
        for(int i = 0; i < dcNames.length; i++) {
            if(StringUtils.isSet(fTextControls[i].getText())) {
                list.put(dcNames[i], fTextControls[i].getText());
            }
        }
        
        return list;
    }
    
    void storePreferences() {
        IPreferenceStore store = XMLExchangePlugin.INSTANCE.getPreferenceStore();
        for(int i = 0; i < dcNames.length; i++) {
            store.setValue(PREFS_LAST_VALUE + dcNames[i], fTextControls[i].getText());
        }
    }

}
