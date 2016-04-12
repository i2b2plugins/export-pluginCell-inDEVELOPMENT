/*
 * Copyright (c) 2006-2010 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */

package com.biomeris.i2b2.export.ws.messages;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.datavo.i2b2message.BodyType;
import com.biomeris.i2b2.export.datavo.i2b2message.MessageHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResponseHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResponseMessageType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResultStatusType;
import com.biomeris.i2b2.export.datavo.i2b2message.StatusType;
import com.biomeris.i2b2.export.datavo.pdo.ObjectFactory;
import com.biomeris.i2b2.export.datavo.pdo.ObservationSet;
import com.biomeris.i2b2.export.datavo.pdo.PatientDataType;
import com.biomeris.i2b2.export.ws.JAXBConstant;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

/**
 * Factory class to create request/response message objects.
 * 
 */
public class MessageFactory {
	private static Log log = LogFactory.getLog(MessageFactory.class);

	/**
	 * Function creates tutorial response OMElement from xml string
	 * 
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
	public static OMElement createResponseOMElementFromString(String xmlString) throws XMLStreamException {
		OMElement returnElement = null;

		try {
			StringReader strReader = new StringReader(xmlString);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			returnElement = builder.getDocumentElement();
		} catch (XMLStreamException xmlStreamEx) {
			log.error("Error while converting tutorial response PDO to OMElement");
			throw xmlStreamEx;
		}

		return returnElement;
	}

	/**
	 * Function to build patientData body type
	 * 
	 * @param obsSet
	 *            Observation fact set to be returned to requester
	 * @return BodyType object
	 */
	private static BodyType createBodyType(ObservationSet obsSet) {
		PatientDataType patientData = new PatientDataType();
		patientData.getObservationSet().add(obsSet);

		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPatientData(patientData));

		return bodyType;

		// BodyType bodyType = new BodyType();
		// bodyType.getAny().add(obsSet);
		//
		// return bodyType;
	}

	/**
	 * Function to create response message header based on request message
	 * header
	 * 
	 * @return MessageHeader object
	 */
	private static MessageHeaderType createResponseMessageHeader(MessageHeaderType messageHeaderType) {
		MessageHeaderType messageHeader = new MessageHeaderType();

		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.6"));

		if (messageHeaderType != null) {
			if (messageHeaderType.getSendingApplication() != null) {
				messageHeader.setReceivingApplication(messageHeaderType.getSendingApplication());
			}

			messageHeader.setReceivingFacility(messageHeaderType.getSendingFacility());

		}

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));

		if (messageHeaderType != null) {
			if (messageHeaderType.getProjectId() != null) {
				messageHeader.setProjectId(messageHeaderType.getProjectId());
			}
		}

		return messageHeader;
	}

	/**
	 * Function to create response message type
	 * 
	 * @param messageHeader
	 * @param respHeader
	 * @param bodyType
	 * @return ResponseMessageType
	 */
	private static ResponseMessageType createResponseMessageType(MessageHeaderType messageHeader, ResponseHeaderType respHeader, BodyType bodyType) {
		ResponseMessageType respMsgType = new ResponseMessageType();
		respMsgType.setMessageHeader(messageHeader);
		respMsgType.setMessageBody(bodyType);
		respMsgType.setResponseHeader(respHeader);

		return respMsgType;
	}

	/**
	 * Function to convert ResponseMessageType to string
	 * 
	 * @param respMessageType
	 * @return String
	 * @throws Exception
	 */
	public static String convertToXMLString(ResponseMessageType respMessageType) throws I2B2Exception {
		StringWriter strWriter = null;

		try {
			JAXBUtil jaxbUtil = new JAXBUtil(JAXBConstant.DEFAULT_PACKAGE_NAME);
			strWriter = new StringWriter();

			com.biomeris.i2b2.export.datavo.i2b2message.ObjectFactory objectFactory = new com.biomeris.i2b2.export.datavo.i2b2message.ObjectFactory();
			jaxbUtil.marshaller(objectFactory.createResponse(respMessageType), strWriter);
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("Error converting response message type to string " + e.getMessage(), e);
		}

		return strWriter.toString();
	}

	/**
	 * Function to build Response message type and return it as an XML string
	 * 
	 * @param obsSet
	 *            observation fact set to be included in response PDO
	 * 
	 * @return A String data type containing the ResponseMessage in XML format
	 * @throws Exception
	 */
	public static ResponseMessageType createBuildResponse(MessageHeaderType messageHeaderType, ObservationSet obsSet) {
		ResponseMessageType respMessageType = null;

		MessageHeaderType messageHeader = createResponseMessageHeader(messageHeaderType);

		ResponseHeaderType respHeader = createResponseHeader("DONE", "Export Cell processing completed");

		BodyType bodyType = createBodyType(obsSet);
		respMessageType = createResponseMessageType(messageHeader, respHeader, bodyType);

		return respMessageType;
	}

	/**
	 * Creates ResponseHeader for the given type and value
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	private static ResponseHeaderType createResponseHeader(String type, String value) {
		ResponseHeaderType respHeader = new ResponseHeaderType();
		StatusType status = new StatusType();
		status.setType(type);
		status.setValue(value);

		ResultStatusType resStat = new ResultStatusType();
		resStat.setStatus(status);
		respHeader.setResultStatus(resStat);

		return respHeader;
	}
}
