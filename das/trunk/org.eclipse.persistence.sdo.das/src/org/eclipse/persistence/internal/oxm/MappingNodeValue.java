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
* bdoughan - January 7/2009 - 1.1 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.internal.oxm;

import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * A node value corresponding to mapping. 
 */
public abstract class MappingNodeValue extends NodeValue {

    /**
     * Return the mapping associated with this node value. 
     */
    public abstract DatabaseMapping getMapping();
    
    public boolean isMappingNodeValue() {
        return true;
    }
    
}