package com.archimatetool.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Activitor
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLExchangePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.archimatetool.xmlexchange";
    
    public static final File ARCHIMATE_XSD_FILE = new File("xsd", "archimate_v2p1.xsd");

    /**
     * The shared instance
     */
    public static XMLExchangePlugin INSTANCE;

    public XMLExchangePlugin() {
        INSTANCE = this;
    }

    /**
     * @return The ArchiMate XSD file
     */
    public File getArchiMateXSDFile() {
        return getAssetFile(ARCHIMATE_XSD_FILE);
    }

    /**
     * @return An asset file relative to this Bundle
     */
    public File getAssetFile(File file) {
        URL url = FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path(file.getPath()), null);
        
        try {
            url = FileLocator.resolve(url);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        
        return new File(url.getPath()); 
    }

}
