/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.opengroup.archimate.xmlexchange;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;


/**
 * Utils for XML Exchange
 * 
 * @author Phillip Beauvoir
 */
public final class XMLValidator {
    
    public void validateXML(File xmlInstance) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        // Resolver for xsd import
        factory.setResourceResolver(new ResourceResolver());
        
        // Local XSDs
        Schema schema = factory.newSchema(new Source[]{
                new StreamSource(XMLExchangePlugin.INSTANCE.getBundleInputStream(XMLExchangePlugin.XSD_FOLDER
                        + XMLExchangePlugin.ARCHIMATE_XSD)),
                new StreamSource(XMLExchangePlugin.INSTANCE.getBundleInputStream(XMLExchangePlugin.XSD_FOLDER
                        + XMLExchangePlugin.DUBLINCORE_XSD))});

        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlInstance));
    }

    /**
     * From http://stackoverflow.com/questions/2342808/problem-validating-an-xml-file-using-java-with-an-xsd-having-an-include
     */
    static class ResourceResolver implements LSResourceResolver {
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            // Resolve <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="http://www.w3.org/2001/xml.xsd"/>
            // in the main XSD file so that we don't have to go online to get it (takes ages)
            if("http://www.w3.org/2001/xml.xsd".equals(systemId)) { //$NON-NLS-1$
                try {
                    InputStream is = XMLExchangePlugin.INSTANCE.getBundleInputStream(XMLExchangePlugin.XSD_FOLDER + XMLExchangePlugin.XML_XSD);
                    return new Input(publicId, systemId, is);
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            return null;
        }
    }
    
    static class Input implements LSInput {
        private String publicId;
        private String systemId;
        private BufferedInputStream inputStream;

        public Input(String publicId, String sysId, InputStream input) {
            this.publicId = publicId;
            this.systemId = sysId;
            this.inputStream = new BufferedInputStream(input);
        }

        public String getPublicId() {
            return publicId;
        }

        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        public String getBaseURI() {
            return null;
        }

        public InputStream getByteStream() {
            return null;
        }

        public boolean getCertifiedText() {
            return false;
        }

        public Reader getCharacterStream() {
            return null;
        }

        public String getEncoding() {
            return null;
        }

        public String getStringData() {
            synchronized(inputStream) {
                try {
                    byte[] input = new byte[inputStream.available()];
                    inputStream.read(input);
                    String contents = new String(input);
                    return contents;
                }
                catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        public void setBaseURI(String baseURI) {
        }

        public void setByteStream(InputStream byteStream) {
        }

        public void setCertifiedText(boolean certifiedText) {
        }

        public void setCharacterStream(Reader characterStream) {
        }

        public void setEncoding(String encoding) {
        }

        public void setStringData(String stringData) {
        }

        public String getSystemId() {
            return systemId;
        }

        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        public BufferedInputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(BufferedInputStream inputStream) {
            this.inputStream = inputStream;
        }
    }
}
