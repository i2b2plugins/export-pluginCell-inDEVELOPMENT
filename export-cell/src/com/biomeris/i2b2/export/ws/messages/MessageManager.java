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

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.axiom.om.OMElement;

import com.biomeris.i2b2.export.datavo.i2b2message.BodyType;
import com.biomeris.i2b2.export.datavo.i2b2message.RequestMessageType;
import com.biomeris.i2b2.export.datavo.pdo.ObservationSet;
import com.biomeris.i2b2.export.datavo.pdo.ObservationType;
import com.biomeris.i2b2.export.datavo.pdo.PatientDataType;
import com.biomeris.i2b2.export.ws.JAXBConstant;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

public class MessageManager {
	public static RequestMessageType extractReqMess(OMElement element) throws I2B2Exception {
		JAXBUtil jaxbUtil = new JAXBUtil(JAXBConstant.DEFAULT_PACKAGE_NAME);
		try {
			@SuppressWarnings("rawtypes")
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(element.toString());
			if (jaxbElement == null) {
				throw new I2B2Exception("Null value from unmarshall for PDO xml : " + element.toString());
			}
			return (RequestMessageType) jaxbElement.getValue();
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("Umashaller error: " + e.getMessage() + e.getStackTrace()[0].toString()
					+ element.toString(), e);
		}
	}

	public static String extractObsBlob(OMElement element) throws I2B2Exception {
		RequestMessageType rmt = extractReqMess(element);
		BodyType bodyType = rmt.getMessageBody();
		List<ObservationSet> obsFactSet;
		try {
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
			PatientDataType patientDataType = (PatientDataType) helper.getObjectByClass(bodyType.getAny(),
					PatientDataType.class);
			
			obsFactSet = patientDataType.getObservationSet();
			if ((obsFactSet != null) && (obsFactSet.size() > 0)) {
				if (obsFactSet.get(0).getObservation().size() == 0) {
					throw new I2B2Exception("No Observation fact was found in requestPdo");
				} else {
					ObservationType obsFactType = obsFactSet.get(0).getObservation().get(0);
					return (String) obsFactType.getObservationBlob().getContent().get(0);
				}
			} else {
				throw new I2B2Exception("No Observation fact set was found in requestPdo");
			}
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("Something went wrong with the requestPdo");
		}
	}
}
