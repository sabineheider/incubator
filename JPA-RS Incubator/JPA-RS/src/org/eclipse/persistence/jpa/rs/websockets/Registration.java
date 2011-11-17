/******************************************************************************
 * ORACLE CONFIDENTIAL
 * Copyright (c) 2011 Oracle. All rights reserved.
 *
 * Contributors:
 * 		 - shaun.smith
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.websockets;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Registration {

	private String appName;
	private String entityName;

	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	@Override
	public String toString() {
		return "Registration [appName=" + appName + ", entityName="
				+ entityName + "]";
	}
}
