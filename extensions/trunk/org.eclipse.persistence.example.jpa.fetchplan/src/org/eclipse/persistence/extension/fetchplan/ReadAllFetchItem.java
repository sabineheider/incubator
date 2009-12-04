/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Bug 288307: FetchPlan Example
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import org.eclipse.persistence.queries.ReadAllQuery;

public class ReadAllFetchItem extends FetchItem {

    protected ReadAllFetchItem(ReadAllQuery query, String... attributePaths) {
        super(attributePaths);
    }

}
