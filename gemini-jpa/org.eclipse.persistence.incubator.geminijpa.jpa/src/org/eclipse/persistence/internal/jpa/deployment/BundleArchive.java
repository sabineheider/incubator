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
 *     mkeith - Gemini JPA work (INCUBATION) 
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.internal.jpa.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.gemini.jpa.GeminiUtil;

public class BundleArchive extends URLArchive {
    
    URL descriptorUrl;
    
    public BundleArchive(URL rootUrl, URL descriptorUrl) { 
        super(rootUrl);
        this.descriptorUrl = descriptorUrl;
    }
    
    public InputStream getDescriptorStream() {
        GeminiUtil.debug("BundleArchive - getDescriptorStream, url: ", descriptorUrl);
        InputStream is = null;
        try {
            is = descriptorUrl.openStream();
        } catch (IOException ioe) {
            // we return null when entry does not exist
        }
        GeminiUtil.debug("BundleArchive - getDescriptorStream, returning: ", is);
        return is;        
    }
    
    public InputStream getEntry(String entryPath) throws IOException {
        GeminiUtil.debug("BundleArchive - getEntry, path: ", entryPath);
        URL subEntry = new URL(url, entryPath);
        GeminiUtil.debug("BundleArchive - getEntry, new url: ", subEntry);
        InputStream is = null;
        try {
            is = subEntry.openStream();
        } catch (IOException ioe) {
            // we return null when entry does not exist
        }
        GeminiUtil.debug("BundleArchive - getEntry, returning: ", is);
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        GeminiUtil.debug("BundleArchive - getEntryAsURL, path: ", entryPath);
        URL subEntry = new URL(url, entryPath);
        try {
            InputStream is = subEntry.openStream();
            if (is == null){
                return null;
            }
            is.close();
        } catch (IOException ioe) {
            return null; // return null when entry does not exist
        }
        GeminiUtil.debug("BundleArchive - getEntryAsURL, returning: ", subEntry);
        return subEntry;
    }
}
