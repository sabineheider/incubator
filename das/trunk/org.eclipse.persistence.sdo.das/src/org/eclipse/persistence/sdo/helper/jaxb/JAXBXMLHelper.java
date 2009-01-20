/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *               http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.sdo.helper.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

import org.eclipse.persistence.exceptions.SDOException;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.jaxb.JAXBUnmarshaller;
import org.eclipse.persistence.oxm.XMLRoot;
import org.eclipse.persistence.oxm.XMLUnmarshaller;
import org.eclipse.persistence.sdo.helper.delegates.SDOXMLHelperDelegate;
import org.xml.sax.InputSource;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

/**
 * This implementation of commonj.sdo.helper.XMLHelper is responsible for 
 * ensuring that newly unmarshalled DataObjects are assigned a JAXB aware 
 * value store. 
 */
public class JAXBXMLHelper extends SDOXMLHelperDelegate {

    public JAXBXMLHelper(HelperContext helperContext) {
        super(helperContext);
    }

    public JAXBXMLHelper(HelperContext helperContext, ClassLoader classLoader) {
        super(helperContext, classLoader);
    }

    public JAXBHelperContext getHelperContext() {
        return (JAXBHelperContext) super.getHelperContext();
    }

    @Override
    public XMLDocument load(InputSource inputSource, String locationURI, Object options) throws IOException {
        if(null == inputSource) {
            return super.load(inputSource, locationURI, options);
        }
        try {
            XMLRoot xmlRoot = (XMLRoot) createXMLUnmarshaller().unmarshal(inputSource);
            return wrap(xmlRoot);
        } catch (XMLMarshalException xmlMarshalException) {
            return handleLoadException(xmlMarshalException);
        }
    }

    @Override
    public XMLDocument load(InputStream inputStream, String locationURI, Object options) throws IOException {
        if(null == inputStream) {
            return super.load(inputStream, locationURI, options);
        }
        return load(inputStream);
    }

    @Override
    public XMLDocument load(InputStream inputStream) throws IOException {
        if(null == inputStream) {
            return super.load(inputStream);
        }
        try {
            XMLRoot xmlRoot = (XMLRoot) createXMLUnmarshaller().unmarshal(inputStream);
            return wrap(xmlRoot);
        } catch(XMLMarshalException xmlMarshalException) {
            return handleLoadException(xmlMarshalException);
        }
    }

    @Override
    public XMLDocument load(Reader inputReader, String locationURI, Object options) throws IOException {
        if(null == inputReader) {
            return super.load(inputReader, locationURI, options);
        }
        try {
            XMLRoot xmlRoot = (XMLRoot) createXMLUnmarshaller().unmarshal(inputReader);
            return wrap(xmlRoot);
        } catch(XMLMarshalException xmlMarshalException) {
            return handleLoadException(xmlMarshalException);
        }
    }

    @Override
    public XMLDocument load(Source source, String locationURI, Object options) throws IOException {
        if(null == source) {
            return super.load(source, locationURI, options);
        }
        try {
            XMLRoot xmlRoot = (XMLRoot) createXMLUnmarshaller().unmarshal(source);
            return wrap(xmlRoot);
        } catch(XMLMarshalException xmlMarshalException) {
            return handleLoadException(xmlMarshalException);
        }
    }

    @Override
    public XMLDocument load(String inputString) {
        if(null == inputString) {
            return super.load(inputString);
        }
        try {
            StringReader reader = new StringReader(inputString);
            return load(reader, null, null);
        } catch(IOException e) {
            return null;
        }
    }

    private XMLUnmarshaller createXMLUnmarshaller() {
        try {
            JAXBUnmarshaller unmarshaller = (JAXBUnmarshaller) getHelperContext().getJAXBContext().createUnmarshaller();
            XMLUnmarshaller xmlUnmarshaller = unmarshaller.getXMLUnmarshaller();
            xmlUnmarshaller.setResultAlwaysXMLRoot(true);
            return xmlUnmarshaller;
        } catch(JAXBException e) {
            throw SDOException.sdoJaxbErrorCreatingJAXBUnmarshaller(e);
        }
    }

    private XMLDocument wrap(XMLRoot xmlRoot) {
        DataObject dataObject = getHelperContext().wrap(xmlRoot.getObject());
        XMLDocument xmlDocument =  getHelperContext().getXMLHelper().createDocument(dataObject, xmlRoot.getNamespaceURI(), xmlRoot.getLocalName());
        xmlDocument.setEncoding(xmlRoot.getEncoding());
        xmlDocument.setXMLVersion(xmlRoot.getXMLVersion());
        xmlDocument.setSchemaLocation(xmlRoot.getSchemaLocation());
        xmlDocument.setNoNamespaceSchemaLocation(xmlRoot.getNoNamespaceSchemaLocation());
        return xmlDocument;
    }

    private XMLDocument handleLoadException(XMLMarshalException xmlMarshalException) throws IOException {
        if(xmlMarshalException.getCause() instanceof IOException) {
            throw (IOException) xmlMarshalException.getCause();
        } else {
            throw xmlMarshalException;
        }
    }

}