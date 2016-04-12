/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.io.misc;

import java.util.List;

public class Export {
	private String id, name, status, fileName, patientSetName;
	private List<Concept> concepts;
	private ExportParams exportParams;
	private List<String> problems;
	private String exceptionMessage;

	public List<Concept> getConcepts() {
		return concepts;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public ExportParams getExportParams() {
		return exportParams;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<String> getProblems() {
		return problems;
	}

	public String getStatus() {
		return status;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public void setExportParams(ExportParams exportParams) {
		this.exportParams = exportParams;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProblems(List<String> problems) {
		this.problems = problems;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPatientSetName() {
		return patientSetName;
	}

	public void setPatientSetName(String patientSetName) {
		this.patientSetName = patientSetName;
	}
}
