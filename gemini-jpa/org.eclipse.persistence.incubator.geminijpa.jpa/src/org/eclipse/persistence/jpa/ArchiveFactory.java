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
package org.eclipse.persistence.jpa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This interface should be implemented by users that want to provide a custom way
 * of dealing with archives that contain persistence units.  An implementer of this class
 * can be enabled by providing a System property
 * 
 * @see org.eclipse.persistence.config.SystemProperties
 * @author tware
 *
 */
public interface ArchiveFactory {

    /**
     * Return an instance of an implementer of Archive that can process the URL provided
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    public Archive createArchive(URL rootUrl) throws URISyntaxException, IOException;

    public Archive createArchive(URL rootUrl, URL descriptorUrl) throws URISyntaxException, IOException;
}
