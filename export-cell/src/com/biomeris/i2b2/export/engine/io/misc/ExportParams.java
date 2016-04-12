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

import java.util.Calendar;

public class ExportParams {
	private String exportType;
	private Calendar startDate, endDate;
	private boolean patientSheet, maskPatientIds;
	private boolean zip;
	private PatientSheetElements patientSheetElements;

	public PatientSheetElements getPatientSheetElements() {
		return patientSheetElements;
	}

	public void setPatientSheetElements(PatientSheetElements patientSheetElements) {
		this.patientSheetElements = patientSheetElements;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public String getExportType() {
		return exportType;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public boolean isZip() {
		return zip;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}

	public boolean isPatientSheet() {
		return patientSheet;
	}

	public void setPatientSheet(boolean patientSheet) {
		this.patientSheet = patientSheet;
	}

	public boolean isMaskPatientIds() {
		return maskPatientIds;
	}

	public void setMaskPatientIds(boolean maskPatientIds) {
		this.maskPatientIds = maskPatientIds;
	}

	
}
