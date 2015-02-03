/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange.wizard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.opengroup.archimate.xmlexchange.XMLExchangePlugin;

import com.archimatetool.editor.ui.IArchimateImages;
import com.archimatetool.editor.utils.PlatformUtils;



/**
 * Export to XML Wizard Page for Metadata
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class ExportToXMLPageMetadata extends WizardPage {

    private static String HELP_ID = "org.opengroup.archimate.xmlexchange.help.ExportToXMLPageMetadata"; //$NON-NLS-1$
    
    private static final String PREFS_LAST_VALUE = "ExportXMLExchangeLastMD_"; //$NON-NLS-1$
    
    private static String[] dcNames = {
            "title",
            "creator",
            "subject",
            "description",
            "publisher",
            "contributor",
            "date",
            "type",
            "format",
            "identifier",
            "source",
            "language",
            "relation",
            "coverage",
            "rights"
    };
    
    private static String[] dcTitles = {
            "Title",
            "Creator",
            "Subject",
            "Description",
            "Publisher",
            "Contributor",
            "Date",
            "Type",
            "Format",
            "Identifier",
            "Source",
            "Language",
            "Relation",
            "Coverage",
            "Rights"
    };
    
    private TableViewer fTableViewer;
    private Button fClearAllButton;
    
    private Map<String, String> fNames;
    private Map<String, String> fData;
    
    public ExportToXMLPageMetadata() {
        super("ExportToXMLPageMetadata"); //$NON-NLS-1$
        
        setTitle("Export model");
        setDescription("Add Metadata to describe the model");
        setImageDescriptor(IArchimateImages.ImageFactory.getImageDescriptor(IArchimateImages.ECLIPSE_IMAGE_EXPORT_DIR_WIZARD));
        
        fNames = new LinkedHashMap<String, String>();
        fData = new LinkedHashMap<String, String>();
        
        for(int i = 0; i < dcNames.length; i++) {
            fNames.put(dcNames[i], dcTitles[i]);
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());
        setControl(container);
        
        Composite tableComp = new Composite(container, SWT.BORDER);
        TableColumnLayout tableLayout = new TableColumnLayout();
        tableComp.setLayout(tableLayout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 300; // stop overstretch
        tableComp.setLayoutData(gd);
        
        fTableViewer = new TableViewer(tableComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        fTableViewer.getTable().setHeaderVisible(true);
        fTableViewer.getTable().setLinesVisible(true);
        fTableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // Help ID on table
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fTableViewer.getTable(), HELP_ID);

        // Content Provider
        fTableViewer.setContentProvider(new IStructuredContentProvider() {
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return fNames.keySet().toArray();
            }
        });

        // Columns
        TableViewerColumn columnName = new TableViewerColumn(fTableViewer, SWT.NONE, 0);
        columnName.getColumn().setText("Name");
        tableLayout.setColumnData(columnName.getColumn(), new ColumnWeightData(20, true));

        TableViewerColumn columnValue = new TableViewerColumn(fTableViewer, SWT.NONE, 1);
        columnValue.getColumn().setText("Value");
        tableLayout.setColumnData(columnValue.getColumn(), new ColumnWeightData(80, true));
        columnValue.setEditingSupport(new ValueEditingSupport(fTableViewer));
        
        fTableViewer.setLabelProvider(new LabelCellProvider());
        
        for(int i = 0; i < dcNames.length; i++) {
            IPreferenceStore store = XMLExchangePlugin.INSTANCE.getPreferenceStore();
            String lastValue = store.getString(PREFS_LAST_VALUE + dcNames[i]);
            fData.put(dcNames[i], lastValue);
        }
        
        fClearAllButton = new Button(container, SWT.PUSH);
        fClearAllButton.setText("Clear All");
        fClearAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for(Entry<String, String> entry : fData.entrySet()) {
                    entry.setValue("");
                }
                fTableViewer.setInput("");
            }
        });

        // Table row bug on Yosemite https://bugs.eclipse.org/bugs/show_bug.cgi?id=446534
        if(PlatformUtils.isMac() && System.getProperty("os.version").startsWith("10.10")) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    fTableViewer.setInput("");
                }
            });
        }
        else {
            fTableViewer.setInput("");
        }
    }
    
    Map<String, String> getMetadata() {
        return fData;
    }
    
    void storePreferences() {
        IPreferenceStore store = XMLExchangePlugin.INSTANCE.getPreferenceStore();
        for(Entry<String, String> entry : fData.entrySet()) {
            store.setValue(PREFS_LAST_VALUE + entry.getKey(), entry.getValue());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    // Table functions
    //
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Label Provider
     */
    private class LabelCellProvider extends LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return fNames.get(element);

                case 1:
                    return fData.get(element);

                default:
                    return null;
            }
        }

    }

    /**
     * Key Editor
     */
    private class ValueEditingSupport extends EditingSupport {
        TextCellEditor cellEditor;

        public ValueEditingSupport(ColumnViewer viewer) {
            super(viewer);
            cellEditor = new TextCellEditor((Composite)viewer.getControl());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return fData.get(element);
        }

        @Override
        protected void setValue(Object element, Object value) {
            fData.put((String)element, (String)value);
            fTableViewer.update(element, null);
        }
    }

}
