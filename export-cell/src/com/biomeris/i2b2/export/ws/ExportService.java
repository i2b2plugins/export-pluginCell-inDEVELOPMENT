/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.service.Lifecycle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.xerces.dom.ElementNSImpl;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

import com.biomeris.i2b2.export.datavo.i2b2message.BodyType;
import com.biomeris.i2b2.export.datavo.i2b2message.MessageHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResponseHeaderType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResponseMessageType;
import com.biomeris.i2b2.export.datavo.i2b2message.ResultStatusType;
import com.biomeris.i2b2.export.datavo.pdo.BlobType;
import com.biomeris.i2b2.export.datavo.pdo.ObservationSet;
import com.biomeris.i2b2.export.datavo.pdo.ObservationType;
import com.biomeris.i2b2.export.datavo.pm.ConfigureType;
import com.biomeris.i2b2.export.datavo.pm.ProjectType;
import com.biomeris.i2b2.export.datavo.pm.UserType;
import com.biomeris.i2b2.export.engine.ExportCellException;
import com.biomeris.i2b2.export.engine.io.input.ExportListRequest;
import com.biomeris.i2b2.export.engine.io.input.ExportRequest;
import com.biomeris.i2b2.export.engine.io.input.OpenNewSessionRequest;
import com.biomeris.i2b2.export.engine.io.input.TestSessionRequest;
import com.biomeris.i2b2.export.engine.io.misc.Export;
import com.biomeris.i2b2.export.engine.io.misc.Network;
import com.biomeris.i2b2.export.engine.io.misc.Session;
import com.biomeris.i2b2.export.engine.io.output.ExportListResponse;
import com.biomeris.i2b2.export.engine.io.output.ExportResponse;
import com.biomeris.i2b2.export.engine.io.output.OpenNewSessionResponse;
import com.biomeris.i2b2.export.engine.io.output.TestSessionResponse;
import com.biomeris.i2b2.export.engine.session.WExport;
import com.biomeris.i2b2.export.engine.session.WSession;
import com.biomeris.i2b2.export.engine.session.WSessionManager;
import com.biomeris.i2b2.export.ws.messages.MessageBuilder;
import com.biomeris.i2b2.export.ws.messages.MessageFactory;
import com.biomeris.i2b2.export.ws.messages.MessageManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public class ExportService implements ServiceLifeCycle, Lifecycle {
	private static WSessionManager sessionManager;
	private static Log log = LogFactory.getLog(ExportService.class);
	private static int authStrategy = -1;
	private static int authLevel = -1;
	private static int sessionLifespan;
	
	private final static int MAX_AUTH_STRATEGY = 2;
	private final static int MAX_AUTH_LEVEL = 5;
	
	public OMElement testSession(OMElement inputElement) throws I2B2Exception, JsonSyntaxException {
		Assert.notNull(inputElement, "Export Cell: Incoming request is null");

		MessageHeaderType messageHeaderType = MessageManager.extractReqMess(inputElement).getMessageHeader();
		String requestJsonStr = MessageManager.extractObsBlob(inputElement);

		Gson gson = new Gson();
		TestSessionRequest request = gson.fromJson(requestJsonStr, TestSessionRequest.class);
		
		TestSessionResponse response = new TestSessionResponse();
		
		WSession wSession = sessionManager.getSession(request.getSessionId());
		if(wSession!=null){
			response.setValid(true);
		} else{
			response.setValid(false);
		}
		response.setLifeSpan(sessionLifespan);
		
		String tsrString = new GsonBuilder().create().toJson(response);
		
		ObservationSet observationSet = new ObservationSet();
		ObservationType observationType = new ObservationType();
		BlobType blobType = new BlobType();
		blobType.getContent().add(tsrString);
		observationType.setObservationBlob(blobType);
		observationSet.getObservation().add(observationType);
		
		ResponseMessageType responseMessageType = MessageFactory.createBuildResponse(messageHeaderType, observationSet);
		String rmtString = MessageFactory.convertToXMLString(responseMessageType);

		OMElement out = null;
		try {
			out = MessageFactory.createResponseOMElementFromString(rmtString);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		log.debug(rmtString);

		return out;
	}

	
	public OMElement openNewSession(OMElement inputElement) throws I2B2Exception, JsonSyntaxException {
		Assert.notNull(inputElement, "Export Cell: Incoming request is null");

		MessageHeaderType messageHeaderType = MessageManager.extractReqMess(inputElement).getMessageHeader();
		String requestJsonStr = MessageManager.extractObsBlob(inputElement);

		Gson gson = new Gson();
		OpenNewSessionRequest request = gson.fromJson(requestJsonStr, OpenNewSessionRequest.class);

		boolean validUser = false;
		if (authStrategy > 0) {
			try {
				validUser = validateUser(request.getNetwork());
			} catch (JAXBException | IOException | TransformerException e1) {
				// do nothing
			}
		} else {
			validUser = true;
		}

		OpenNewSessionResponse response = new OpenNewSessionResponse();
		if (!validUser) {
			String error = "You are not allowed to use export functions";
			response.setError(error);
		} else {
			WSession wSession = sessionManager.createNewSession();
			wSession.setNetwork(request.getNetwork());
			Session session = new Session();
			session.setId(wSession.getId());
			response.setSession(session);
		}

		String onsrString = new GsonBuilder().create().toJson(response);

		ObservationSet observationSet = new ObservationSet();
		ObservationType observationType = new ObservationType();
		BlobType blobType = new BlobType();
		blobType.getContent().add(onsrString);
		observationType.setObservationBlob(blobType);
		observationSet.getObservation().add(observationType);

		ResponseMessageType responseMessageType = MessageFactory.createBuildResponse(messageHeaderType, observationSet);
		String rmtString = MessageFactory.convertToXMLString(responseMessageType);

		OMElement out = null;
		try {
			out = MessageFactory.createResponseOMElementFromString(rmtString);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		log.debug(rmtString);

		return out;
	}

	public OMElement export(OMElement inputElement) throws I2B2Exception, JsonSyntaxException, ExportCellException {
		Assert.notNull(inputElement, "Export Cell: Incoming request is null");

		OMElement returnElement = null;
		MessageHeaderType messageHeaderType = MessageManager.extractReqMess(inputElement).getMessageHeader();
		String requestJsonStr = MessageManager.extractObsBlob(inputElement);

		Gson gson = new Gson();
		ExportRequest request = gson.fromJson(requestJsonStr, ExportRequest.class);

		String sessionId = request.getSessionId();
		WSession wSession = sessionManager.getSession(sessionId);

		if (wSession == null) {
			throw new ExportCellException("Session does not exist");
		}

		if (request.getNewPassword() != null) {
			wSession.getNetwork().setPassword(request.getNewPassword());
		} else {
			if (authStrategy > 1) {
				throw new ExportCellException("Password not provided.");
			}
		}

		boolean validUser = false;
		if (authStrategy > 1) {
			try {
				validUser = validateUser(wSession.getNetwork());
			} catch (JAXBException | IOException | TransformerException e1) {
				// do nothing
			}
		} else {
			validUser = true;
		}

		ExportResponse response = new ExportResponse();

		if (validUser) {
			response.setSessionId(sessionId);

			WExport wExport = null;
			wExport = wSession.addNewExport(request.getName());
			wExport.setExportParams(request.getExportParams());
			wExport.setConcepts(request.getConcepts());
			wExport.setPatSetId(request.getPatientSetId());
			wExport.setPatSetName(request.getPatientSetName());

			response.setExportId(wExport.getId());

			ExportRunnable erun = new ExportRunnable(wSession, wExport);
			Thread t = new Thread(erun);
			t.start();
		} else {
			String error = "You're not allowed to use export functions";
			response.setError(error);
		}

		String jsonRespStr = new GsonBuilder().create().toJson(response);

		ObservationSet observationSet = new ObservationSet();
		ObservationType observationType = new ObservationType();
		BlobType blobType = new BlobType();
		blobType.getContent().add(jsonRespStr);
		observationType.setObservationBlob(blobType);
		observationSet.getObservation().add(observationType);
		ResponseMessageType responseMessageType = MessageFactory.createBuildResponse(messageHeaderType, observationSet);
		String rmtStr = MessageFactory.convertToXMLString(responseMessageType);

		try {
			returnElement = MessageFactory.createResponseOMElementFromString(rmtStr);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		log.debug(rmtStr);

		return returnElement;
	}

	public OMElement exportList(OMElement inputElement) throws I2B2Exception, ExportCellException {
		Assert.notNull(inputElement, "Export Cell: Incoming request is null");

		OMElement returnElement = null;
		MessageHeaderType messageHeaderType = MessageManager.extractReqMess(inputElement).getMessageHeader();
		String requestJsonStr = MessageManager.extractObsBlob(inputElement);

		Gson gson = new Gson();
		ExportListRequest request = gson.fromJson(requestJsonStr, ExportListRequest.class);

		String sessionId = request.getSessionId();
		WSession wSession = sessionManager.getSession(sessionId);

		if (wSession == null) {
			throw new ExportCellException("Session does not exist");
		}

		if (request.getNewPassword() != null) {
			wSession.getNetwork().setPassword(request.getNewPassword());
		}

		boolean validUser = false;
		if (authStrategy > 1) {
			try {
				validUser = validateUser(wSession.getNetwork());
			} catch (JAXBException | IOException | TransformerException e1) {
				// do nothing
			}
		} else {
			validUser = true;
		}

		ExportListResponse response = new ExportListResponse();

		if (validUser) {
			response.setSessionId(sessionId);

			List<Export> exports = new ArrayList<>();
			for (WExport we : wSession.getExports()) {
				Export e = we.makeIOExport();
				exports.add(e);
			}
			response.setExports(exports);
		} else {
			String error = "You're not allowed to use export functions";
			response.setError(error);
		}

		String jsonRespStr = new GsonBuilder().create().toJson(response);

		ObservationSet observationSet = new ObservationSet();
		ObservationType observationType = new ObservationType();
		BlobType blobType = new BlobType();
		blobType.getContent().add(jsonRespStr);
		observationType.setObservationBlob(blobType);
		observationSet.getObservation().add(observationType);
		ResponseMessageType responseMessageType = MessageFactory.createBuildResponse(messageHeaderType, observationSet);
		String rmtStr = MessageFactory.convertToXMLString(responseMessageType);

		try {
			returnElement = MessageFactory.createResponseOMElementFromString(rmtStr);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		log.debug(rmtStr);

		return returnElement;
	}

	private boolean validateUser(Network network) throws JAXBException, IOException, TransformerException {
		boolean output = false;

		String pmRequest = MessageBuilder.buildPMGetServiceRequest(network);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(network.getProxyAddress());
		StringEntity entity1A = new StringEntity(pmRequest, Consts.UTF_8);

		entity1A.setContentType("text/xml");
		entity1A.setChunked(true);
		httpPost.setEntity(entity1A);

		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpPost);
			log.debug("Proxy: "+network.getProxyAddress());
		} catch (HttpHostConnectException hhce) {
			try {
				if(network.getStaticProxyAddress() != null){
					httpPost = new HttpPost(network.getStaticProxyAddress());
					httpPost.setEntity(entity1A);

					response = httpclient.execute(httpPost);
					log.debug("Proxy: " + network.getStaticProxyAddress());
				}
			} catch (HttpHostConnectException e) {
				throw e;
			}
		}

		try {
			HttpEntity entity1R = response.getEntity();

			String responseString = EntityUtils.toString(entity1R);

			ResponseMessageType rmt = JAXB.unmarshal(new StringReader(responseString), ResponseMessageType.class);

			ResponseHeaderType responseHeader = rmt.getResponseHeader();
			ResultStatusType responseStatus = responseHeader.getResultStatus();
			if (responseStatus.getStatus().getType().equals("ERROR")) {
				return false;
			}

			BodyType body = rmt.getMessageBody();

			ElementNSImpl bodyEl = (ElementNSImpl) body.getAny().get(0);
			String bodyStr = elementToString(bodyEl);

			ConfigureType configureType = JAXB.unmarshal(new StringReader(bodyStr), ConfigureType.class);
			UserType userType = configureType.getUser();

			if (userType.isIsAdmin()) {
				output = true;
			} else {
				List<ProjectType> userProjects = userType.getProject();
				for (ProjectType pt : userProjects) {
					if (pt.getId().equals(network.getProject())) {
						if (userAccessLevel(pt.getRole()) >= authLevel) {
							output = true;
						}
					}
				}
			}

			EntityUtils.consume(entity1R);
		} finally {
			response.close();
		}

		return output;
	}

	private int userAccessLevel(List<String> roles) {
		int output = 0;
		for (String role : roles) {
			int roleLevel = dataAccessLevel(role);
			if (roleLevel > output) {
				output = roleLevel;
			}
		}
		return output;
	}

	private int dataAccessLevel(String dataAccess) {
		switch (dataAccess.toUpperCase()) {
		case "DATA_OBFSC":
			return 1;
		case "DATA_AGG":
			return 2;
		case "DATA_LDS":
			return 3;
		case "DATA_DEID":
			return 4;
		case "DATA_PROT":
			return 5;
		default:
			return 0;
		}
	}

	private String elementToString(ElementNSImpl element) throws TransformerException {

		Document doc = element.getOwnerDocument();
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);

		return writer.toString();
	}

	private void authStrategy(){
		Properties exportCellProperties;
		InputStream is_i2b2 = ExportService.class.getClassLoader().getResourceAsStream("conf/exportcell.properties");
		exportCellProperties = new Properties();
		try {
			exportCellProperties.load(is_i2b2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		switch (exportCellProperties.getProperty("exportcell.auth.strategy").toUpperCase()) {
		case "NONE":
			authStrategy = 0;
			break;
		case "SINGLE":
			authStrategy = 1;
			break;
		case "MULTI":
			authStrategy = 2;
			break;
		default:
			authStrategy = MAX_AUTH_STRATEGY;
		}
	}
	
	private void authLevel(){
		Properties exportCellProperties;
		InputStream is_i2b2 = ExportService.class.getClassLoader().getResourceAsStream("conf/exportcell.properties");
		exportCellProperties = new Properties();
		try {
			exportCellProperties.load(is_i2b2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		authLevel = dataAccessLevel(exportCellProperties.getProperty("exportcell.auth.level"));
		if(authLevel==0){
			authLevel = MAX_AUTH_LEVEL;
		}
	}
	
	private void sessionLifespan(){
		Properties exportCellProperties;
		InputStream is_i2b2 = ExportService.class.getClassLoader().getResourceAsStream("conf/exportcell.properties");
		exportCellProperties = new Properties();
		try {
			exportCellProperties.load(is_i2b2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sessionLifespan = Integer.parseInt(exportCellProperties.getProperty("exportcell.session.timeout.minutes"));
	}

	// This method will be called before each call
	public synchronized void init(ServiceContext serviceContext){
	}

	// This method will be called after each call
	public synchronized void destroy(ServiceContext serviceContext) {
	}

	//Called at startup
	public void startUp(ConfigurationContext configctx, AxisService service) {
		authStrategy();
		authLevel();
		sessionLifespan();
		
		sessionManager = new WSessionManager();
		
		CleanerRunnable cleanRun = new CleanerRunnable(sessionManager);
		Thread t = new Thread(cleanRun);
		t.start();
	}

	//Should be called at shutdown
	public void shutDown(ConfigurationContext configctx, AxisService service) {
	}
}
