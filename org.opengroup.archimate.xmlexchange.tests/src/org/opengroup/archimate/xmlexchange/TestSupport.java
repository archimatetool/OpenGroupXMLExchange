/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;


/**
 * Testing Support
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
public class TestSupport {
    
    private static File testFolder;
    
    public static File getTestDataFolder() {
        if(testFolder == null) {
            testFolder = getLocalBundleFolder("org.opengroup.archimate.xmlexchange.tests", "testdata");
        }
        return testFolder;
    }

    /**
     * @param bundleName
     * @param path
     * @return A File folder path relative to bundle
     */
    public static File getLocalBundleFolder(String bundleName, String path) {
        URL url = Platform.getBundle(bundleName).getEntry("/");
        try {
            url = FileLocator.resolve(url);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return new File(url.getPath(), path);
    }
    
}
