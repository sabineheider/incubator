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
 *      ssmith - initial 
 ******************************************************************************/
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