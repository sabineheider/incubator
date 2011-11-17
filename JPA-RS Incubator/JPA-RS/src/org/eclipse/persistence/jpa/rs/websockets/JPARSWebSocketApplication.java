/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
	static final Logger logger = Logger.getLogger("AvatarWebSocketApplication");
	protected JAXBContext jaxbContext;	
	protected PersistenceFactory factory;
	protected Map<PersistenceContext, ApplicationListener> applicationToListener = new ConcurrentHashMap<PersistenceContext, ApplicationListener>();

	public JPARSWebSocketApplication() {
    	logger.info("AvatarWebSocketApplication created: " + this);
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
		JPARSWebSocket avatarSocket = (JPARSWebSocket) socket;
		try {
			Registration message = unmarshallMessage(text);
			avatarSocket.setRegistration(message);
			PersistenceContext application = this.factory.getPersistenceContext(message.getAppName());
			avatarSocket.setApplication(application);
		} catch (Exception e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
	        logger.info(avatarSocket + " exception processing message, closing socket");
			closeSocket(avatarSocket);
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
