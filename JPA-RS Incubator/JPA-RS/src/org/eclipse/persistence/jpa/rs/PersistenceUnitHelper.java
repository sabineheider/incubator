/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke - TODO
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * This repository is responsible for providing an application singleton map of
 * running applications and for creating new instances upon request based on
 * available definitions.
 * 
 * @author dclarke
 * @since Avatar POC
 */
@ApplicationScoped
public class PersistenceUnitHelper {

    private Map<String, PersistenceUnitWrapper> wrappers = new HashMap<String, PersistenceUnitWrapper>();

    public PersistenceUnitWrapper getPersistenceUnit(String unitName) {
        PersistenceUnitWrapper wrapper = wrappers.get(unitName);

        if (wrapper == null) {
            wrapper = new PersistenceUnitWrapper(unitName);
            wrappers.put(unitName, wrapper);
        }

        return wrapper;
    }

    public static List<String> getAvailablePersistenceUnitNames() {
        return null;
    }
}
