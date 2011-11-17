/******************************************************************************
 * ORACLE CONFIDENTIAL
 * Copyright (c) 2011 Oracle. All rights reserved.
 *
 * Contributors:
 * 		 - shaun.smith
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.websockets;

import java.util.logging.Logger;

import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.util.ChangeListener;;

public class ApplicationListener implements ChangeListener {
	static final Logger logger = Logger.getLogger("ApplicationListener");
	
	private JPARSWebSocket socket;
	private PersistenceContext context;

	public ApplicationListener(JPARSWebSocket webSocket, PersistenceContext context) {
		this.socket = webSocket;
		this.context = context;
	}

	@Override
	public void objectUpdated(Object object) {
		this.socket.sendUpdate(this.context, object);
	}

	@Override
	public void objectInserted(Object object) {
		this.socket.sendInsert(this.context, object);
	}

	public void register() {
		logger.info(this + " registering with application");
		this.context.addListener(this);
	}
	
	public void unregister() {
		logger.info(this + " unregistering from application");
		this.context.remove(this);
	}
	
	public JPARSWebSocket getSocket() {
		return socket;
	}

	public PersistenceContext getApplication() {
		return context;
	}




}
