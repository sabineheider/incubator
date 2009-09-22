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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.simple.sequencing;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.UnaryTableSequence;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.junit.AfterClass;

public class UnaryTableSequencing extends BaseSequencingTest {

    @Override
    protected void configureSequencing(DatabaseSession session, EntityTypeBuilder typeBuilder) {
        UnaryTableSequence sequence = new UnaryTableSequence("TEST_SEQ");
        sequence.setCounterFieldName("SEQ_VALUE");
        sequence.setPreallocationSize(5);
        ((AbstractSession) session).getProject().getLogin().setDefaultSequence(sequence);
        sequence.onConnect(session.getPlatform());
        typeBuilder.configureSequencing(sequence, "TEST_SEQ", "SID");
    }

    @Override
    protected void verifySequencingConfig(Session session, ClassDescriptor descriptor) {
        // TODO Auto-generated method stub
    }

    protected void resetSequence(DatabaseSession session) {
        session.executeNonSelectingSQL("UPDATE TEST_SEQ SET SEQ_VALUE = 0");
    }

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE TEST_SEQ CASCADE CONSTRAINTS");
        BaseSequencingTest.shutdown();

    }

}
