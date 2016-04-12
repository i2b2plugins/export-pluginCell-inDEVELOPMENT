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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
	private final static QName _Request_QNAME = new QName("http://www.i2b2.org/xsd/hive/msg/1.1/", "request");
	
    public JAXBElement<ProxyedRequestMessageType> createRequestPM(ProxyedRequestMessageType value) {
        return new JAXBElement<ProxyedRequestMessageType>(_Request_QNAME, ProxyedRequestMessageType.class, null, value);
    }
}
