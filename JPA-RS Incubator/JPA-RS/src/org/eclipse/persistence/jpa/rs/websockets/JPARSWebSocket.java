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

import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.helper.Helper;

import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.util.LinkAdapter;

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

	public void sendInsert(PersistenceContext application, Object entity) {
		send(application, entity);
	}

	public void sendUpdate(PersistenceContext application, Object entity) {
		send(application, entity);
	}
	
	protected void send(PersistenceContext application, Object entity) {
		String entityName = Helper.getShortClassName(entity.getClass());
		if (ofInterest(application, entity, entityName)) {
			String json = marshallEntity(application, entity);
			if (json == null) {
				logger.severe("Entity did not marshall, not sending: " + entity);
			} else {
				String wrappedJson = 
						"{" +
						"\"url\" : \"persistence/" + application.getName() + "/" + entityName + "\", " +
						"\"data\" : " + json +
						"}";
				logger.info(this + " sending: " + wrappedJson);
				send(wrappedJson);
			}
		}
	}

	/**
	 * Answer whether the socket should send the update.  The application name must
	 * match but the entity name is optional and will only be used if not null.  This
	 * allows clients to register interest in changes to all entities in an application.
	 * 
	 * @param application
	 * @param entity
	 * @param entityName
	 * @return
	 */
	private boolean ofInterest(PersistenceContext application, Object entity, String entityName) {
		String appName = application.getName();
		if ((appName == null) || (getRegistration() == null)) {
			return false;
		}
		
		boolean appNameMatches = appName.equals(getRegistration().getAppName());
		boolean entityNameMatches = (getRegistration().getEntityName() == null) 
				|| (getRegistration().getEntityName().equals(entityName));
		return appNameMatches && entityNameMatches;
	}

	protected String marshallEntity(PersistenceContext context, Object entity) {
		try {
			JAXBContext jaxbContext = context.getJAXBContext();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_JSON);
			marshaller.setProperty(org.eclipse.persistence.jaxb.JAXBContext.INCLUDE_ROOT, false);
            marshaller.setAdapter(new LinkAdapter("http://localhost:8080/JPA-RS/auction/entity/", context));
            marshaller.setListener(new Marshaller.Listener() {
                @Override
                public void beforeMarshal(Object source) {
                    DynamicEntityImpl sourceImpl = (DynamicEntityImpl)source;
                    PropertyChangeListener listener = sourceImpl._persistence_getPropertyChangeListener();
                    sourceImpl._persistence_setPropertyChangeListener(null);
                    ((DynamicEntity)source).set("self", source);
                    sourceImpl._persistence_setPropertyChangeListener(listener);
                }
            });
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