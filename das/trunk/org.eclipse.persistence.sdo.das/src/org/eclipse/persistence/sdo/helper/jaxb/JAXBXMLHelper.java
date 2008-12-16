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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

import org.eclipse.persistence.sdo.helper.delegates.SDOXMLHelperDelegate;
import org.xml.sax.InputSource;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

/**
 * This implementation of commonj.sdo.helper.XMLHelper is responsible for 
 * ensuring that newly unmarshalled DataObjects are assigned a JPA aware 
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
        try {
            Unmarshaller unmarshaller = getHelperContext().getJAXBContext().createUnmarshaller();
            unmarshaller.unmarshal(inputSource);
            throw new UnsupportedOperationException();
        } catch(JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLDocument load(InputStream inputStream, String locationURI, Object options) throws IOException {
        return load(inputStream);
    }

    @Override
    public XMLDocument load(InputStream inputStream) throws IOException {
        try {
            Unmarshaller unmarshaller = getHelperContext().getJAXBContext().createUnmarshaller();
            JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(inputStream);
            return wrap(jaxbElement);
        } catch(JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLDocument load(Reader inputReader, String locationURI, Object options) throws IOException {
        try {
            Unmarshaller unmarshaller = getHelperContext().getJAXBContext().createUnmarshaller();
            JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(inputReader);
            return wrap(jaxbElement);
        } catch(JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLDocument load(Source source, String locationURI, Object options) throws IOException {
        try {
            Unmarshaller unmarshaller = getHelperContext().getJAXBContext().createUnmarshaller();
            JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(source);
            return wrap(jaxbElement);
        } catch(JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLDocument load(String inputString) {
        try {
            StringReader reader = new StringReader(inputString);
            return load(reader, null, null);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private XMLDocument wrap(JAXBElement jaxbElement) {
        DataObject dataObject = getHelperContext().wrap(jaxbElement.getValue());
        return getHelperContext().getXMLHelper().createDocument(dataObject, jaxbElement.getName().getNamespaceURI(), jaxbElement.getName().getLocalPart());
    }

}