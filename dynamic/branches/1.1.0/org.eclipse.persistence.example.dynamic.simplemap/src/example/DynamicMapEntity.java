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
 *     dclarke - SimpleDynamicMap Example - Bug 277731
 *               http://wiki.eclipse.org/EclipseLink/Examples/JPA/Dynamic/SimpleDynamicMap
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package example;

import java.util.HashMap;

/**
 * 
 * 
 * This DynamicEntity class does not support:
 * <ul>
 * <li>Features typically introduced through weaving: Change Tracking, Fetch
 * Groups, PK caching, transparent 1:1/M:1 lazy loading
 * <li>Additional helper methods to simplify using indirect relationships and
 * collections. (including initialization)
 * <li>Metamodel/Mapping access
 * </ul>
 * 
 * @author dclarke
 * @since EclipseLink 1.1.1
 */
public abstract class DynamicMapEntity extends HashMap<String, Object> {
}
