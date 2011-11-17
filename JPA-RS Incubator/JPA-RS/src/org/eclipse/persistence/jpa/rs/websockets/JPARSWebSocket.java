/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s): ssmith
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.eclipse.persistence.jpa.rs.websockets;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.internal.helper.Helper;

import org.eclipse.persistence.jpa.rs.PersistenceContext;

import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.DefaultWebSocket;
import com.sun.grizzly.websockets.ProtocolHandler;
import com.sun.grizzly.websockets.WebSocketListener;

public class JPARSWebSocket extends DefaultWebSocket {
	static final Logger logger = Logger.getLogger("JPARSWebSocket");
    private Registration registration;
    private ApplicationListener listener;
    
    public JPARSWebSocket(ProtocolHandler handler, WebSocketListener[] listeners) {
        super(handler, listeners);
    }

	public Registration getRegistration() {
		return registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public void setApplication(PersistenceContext context) {
		this.listener = new ApplicationListener(this, context);
		this.listener.register();
	}
	
	@Override
	public void onClose(DataFrame frame) {
		if (this.listener != null) {
			this.listener.unregister();
		}
		super.onClose(frame);
	}

	public void sendInsert(PersistenceContext context, Object entity) {
		if (ofInterest(context, entity)) {
			String json = marshallEntity(context, entity);
			if (json == null) {
				logger.severe("Entity did not marshall, not sending: " + entity);
			} else {
				logger.info(this + " sending: " + json);
				send(json);
			}
		}
	}

	public void sendUpdate(PersistenceContext context, Object entity) {
		if (ofInterest(context, entity)) {
			String json = marshallEntity(context, entity);
			if (json == null) {
				logger.severe("Entity did not marshall, not sending: " + entity);
			} else {
				logger.info(this + " sending: " + json);
				send(json);
			}
		}
	}
	
	private boolean ofInterest(PersistenceContext context, Object entity) {
		String appName = context.getName();
		String entityName = Helper.getShortClassName(entity.getClass());
		if ((appName == null) || (entityName == null) || (getRegistration() == null)) {
			return false;
		}
		return appName.equals(getRegistration().getAppName())
				&& entityName.equals(getRegistration().getEntityName());
	}

	protected String marshallEntity(PersistenceContext context, Object entity) {
		try {
			JAXBContext jaxbContext = context.getJAXBContext();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_JSON);
			marshaller.setProperty(org.eclipse.persistence.jaxb.JAXBContext.INCLUDE_ROOT, false);
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(entity, stringWriter);
			String jsonString = stringWriter.toString();
			return jsonString;
		} catch (Exception e) {
			logger.severe("Failed to marshall object: " + entity + ", exception: " + e.getMessage());
			return null;
		}
	}

}