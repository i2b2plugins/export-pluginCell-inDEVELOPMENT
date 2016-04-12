/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.ws.messages.extensions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.biomeris.i2b2.export.datavo.i2b2message.RequestMessageType;
import com.biomeris.i2b2.export.datavo.pm.GetUserConfigurationType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "request_messageType", propOrder = {
		"proxy"
})
@XmlSeeAlso(GetUserConfigurationType.class)

public class ProxyedRequestMessageType extends RequestMessageType {
	
	@XmlElement(name = "proxy", required = true)
    private Proxy proxy;

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}
}
