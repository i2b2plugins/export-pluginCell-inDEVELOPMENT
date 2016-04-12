/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.io.input;

import java.util.List;

import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.io.misc.ExportParams;

public class ExportRequest {
	private String sessionId, name, newPassword;
	private String patientSetId, patientSetName;
	private ExportParams exportParams;
	private List<Concept> concepts;
	
	public ExportRequest() {
		super();
	}

	public List<Concept> getConcepts() {
		return concepts;
	}

	public ExportParams getExportParams() {
		return exportParams;
	}

	public String getName() {
		return name;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public String getPatientSetId() {
		return patientSetId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	public void setExportParams(ExportParams exportParams) {
		this.exportParams = exportParams;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setPatientSetId(String patientSetId) {
		this.patientSetId = patientSetId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPatientSetName() {
		return patientSetName;
	}

	public void setPatientSetName(String patientSetName) {
		this.patientSetName = patientSetName;
	}
}
