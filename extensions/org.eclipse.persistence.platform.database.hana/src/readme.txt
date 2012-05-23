This jar contains a contributed version of HANAPlatform from the following bug:

https://bugs.eclipse.org/bugs/show_bug.cgi?id=380226

When filing bugs against this platform, please reference the bug above.

It is being made available for community use and testing and has been compiled against an EclipseLink trunk interim build.

To make use of HANAPlatform, put this jar in your classpath and indicate to EclipseLink to use this platform.  (e.g. Set the eclipselink.target-database persistence unit property to org.eclipse.persistence.platform.database.HANAPlatform)

Below is the licensing information:

/*******************************************************************************
 * Copyright (c) 2012 SAP. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * SAP AG - Initial implementation, enhancement bug 380226
 *
 * This code is being developed under INCUBATION and is not currently included
 * in the automated EclipseLink build. The API in this code may change, or
 * may never be included in the product. Please provide feedback through mailing
 * lists or the bug database.
 ******************************************************************************/