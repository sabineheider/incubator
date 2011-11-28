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

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.ProtocolHandler;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketEngine;
import com.sun.grizzly.websockets.WebSocketListener;

@Singleton
@Startup
public class JPARSWebSocketApplication extends WebSocketApplication {
	static final Logger logger = Logger.getLogger("JPARSWebSocketApplication");
	protected JAXBContext jaxbContext;	
	protected PersistenceFactory factory;
	protected Map<PersistenceContext, ApplicationListener> applicationToListener = new ConcurrentHashMap<PersistenceContext, ApplicationListener>();

	public JPARSWebSocketApplication() {
    	logger.info("JPARSWebSocketApplication created: " + this);
//    	initialize();
		try {
			jaxbContext = JAXBContext.newInstance(Registration.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@PostConstruct
	public void initialize() {
    	logger.info(this + " Registering JPARSWebSocket Application with WebSocketEngine");
        WebSocketEngine.getEngine().register(this);
	}
	
	@PreDestroy
	public void cleanup() {
    	logger.info(this + " Unregistering JPARSWebSocket Application from WebSocketEngine");
		WebSocketEngine.getEngine().unregister(this);
	}
	
    public PersistenceFactory getFactory() {
        return this.factory;
    }

    @EJB
    public void setRepository(PersistenceFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public WebSocket createWebSocket(ProtocolHandler protocolHandler, WebSocketListener... listeners) {
        return new JPARSWebSocket(protocolHandler, listeners);
    }

    @Override
    public boolean isApplicationRequest(Request request) {
        String uri = request.requestURI().toString();
        return uri.endsWith("/ws");
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
    	logger.info(socket + " message received: " + text);
		JPARSWebSocket jparsSocket = (JPARSWebSocket) socket;
		try {
			Registration message = unmarshallMessage(text);
			jparsSocket.setRegistration(message);
			PersistenceContext application = this.factory.getPersistenceContext(message.getAppName());
			if (application != null){
			    jparsSocket.setApplication(application);
			} else {
			    logger.info("Message recieved for unavailable application: " + message.getAppName() + " closing socket.");
		        closeSocket(jparsSocket);
			}
		} catch (Exception e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
	        logger.info(jparsSocket + " exception processing message, closing socket");
			closeSocket(jparsSocket);
		}
    }

	@Override
	public boolean add(WebSocket socket) {
		logger.info(socket + " add");
		boolean success = super.add(socket);
		logger.info("After add now managing sockets: " + getWebSockets().size());
		return success;
	}

	@Override
	public boolean remove(WebSocket socket) {
		JPARSWebSocket chatSocket = (JPARSWebSocket) socket;
		logger.info(socket + " remove: " + chatSocket.getRegistration());
		boolean success = super.remove(socket);
		logger.info("After remove now managing sockets: " + getWebSockets().size());
		return success;
	}


	protected void closeSocket(WebSocket socket) {
		try {
			socket.close(WebSocket.INVALID_DATA);
		} catch (Exception e2) {
			logger.info(socket + " exception closing socket: " + e2.getMessage());
		}
	}

	public Registration unmarshallMessage(String text) throws JAXBException,
			PropertyException {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_JSON);
		StringReader reader = new StringReader(text);
		StreamSource inSource = new StreamSource(reader);
		JAXBElement<Registration> jaxbElement = unmarshaller.unmarshal(
				inSource, Registration.class);
		Registration message = jaxbElement.getValue();
		return message;
	}


    
    
}
