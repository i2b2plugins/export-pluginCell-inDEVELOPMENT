/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.ws.messages;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.datavo.i2b2message.ApplicationType;
import com.biomeris.i2b2.export.datavo.i2b2message.BodyType;
import com.biomeris.i2b2.export.datavo.i2b2message.MessageHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.PasswordType;
import com.biomeris.i2b2.export.datavo.i2b2message.RequestHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.SecurityType;
import com.biomeris.i2b2.export.datavo.pm.GetUserConfigurationType;
import com.biomeris.i2b2.export.engine.io.misc.Network;
import com.biomeris.i2b2.export.ws.ExportService;
import com.biomeris.i2b2.export.ws.messages.extensions.Proxy;
import com.biomeris.i2b2.export.ws.messages.extensions.ProxyedRequestMessageType;

public class MessageBuilder {
	private final static String PMSERVICE_NAME = "getServices";
	private static Log log = LogFactory.getLog(MessageBuilder.class);

	public static String buildPMGetServiceRequest(Network network) throws JAXBException{
		com.biomeris.i2b2.export.datavo.pm.ObjectFactory pmObjectFactory = new com.biomeris.i2b2.export.datavo.pm.ObjectFactory();
		com.biomeris.i2b2.export.ws.messages.extensions.ObjectFactory extObjectFactory = new com.biomeris.i2b2.export.ws.messages.extensions.ObjectFactory();

		ProxyedRequestMessageType proxyedRequestMessageType = new ProxyedRequestMessageType();

		Proxy proxy = new Proxy();
		proxy.setRedirectUrl(network.getPmServiceAddress() + PMSERVICE_NAME);
		proxyedRequestMessageType.setProxy(proxy);

		MessageHeaderType messageHeaderType = new MessageHeaderType();
		SecurityType securityType = new SecurityType();
		securityType.setDomain(network.getDomain());
		securityType.setUsername(network.getUsername());
		PasswordType passwordType = JAXB.unmarshal(new StringReader(network.getPassword()), PasswordType.class);
		securityType.setPassword(passwordType);
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(network.getProject());
		ApplicationType applicationType = new ApplicationType();
		applicationType.setApplicationName("Export Cell");
		applicationType.setApplicationVersion("1.0");
		messageHeaderType.setSendingApplication(applicationType);
		proxyedRequestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeaderType = new RequestHeaderType();
		requestHeaderType.setResultWaittimeMs(1800000);
		proxyedRequestMessageType.setRequestHeader(requestHeaderType);
		
		BodyType bodyType = new BodyType();
				
		GetUserConfigurationType g = pmObjectFactory.createGetUserConfigurationType();
		g.getProject().add("undefined");
		JAXBElement<GetUserConfigurationType> any1 = pmObjectFactory.createGetUserConfiguration(g);
		
		bodyType.getAny().add(any1);
		proxyedRequestMessageType.setMessageBody(bodyType);
		
		JAXBContext jc = JAXBContext.newInstance(ProxyedRequestMessageType.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		JAXBElement<ProxyedRequestMessageType> xxx = extObjectFactory.createRequestPM(proxyedRequestMessageType);
		m.marshal(xxx, sw);

		return sw.toString();
	}
}
