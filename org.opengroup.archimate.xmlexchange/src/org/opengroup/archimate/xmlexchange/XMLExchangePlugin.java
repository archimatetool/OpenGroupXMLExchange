package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Activitor
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class XMLExchangePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.opengroup.archimate.xmlexchange";
    
    public static final String XSD_FOLDER = "xsd/";
    public static final String ARCHIMATE_XSD = "archimate_v2p1.xsd";
    public static final String DUBLINCORE_XSD = "dc.xsd";
    public static final String XML_XSD = "xml.xsd";

    /**
     * The shared instance
     */
    public static XMLExchangePlugin INSTANCE;

    public XMLExchangePlugin() {
        INSTANCE = this;
    }
    
    public void copyXSDFile(String xsdFile, File outputFile) throws IOException {
        InputStream in = getBundleInputStream(XSD_FOLDER + xsdFile);
        Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }

    public InputStream getBundleInputStream(String bundleFileName) throws IOException {
        URL url = getBundle().getResource(bundleFileName);
        return url.openStream();
    }
}
