/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.session;

import java.util.Calendar;

public class WObservation {
	private String conceptCd;
	private Calendar startDate, endDate;
	private String instanceNum;
	private String modifierCd;
	private String valueTypeCd;
	private String tvalChar;
	private Double nvalNum;
	private String patientId;
	private String unitsCd;
	private String encounterId;

	public String getUnitsCd() {
		return unitsCd;
	}

	public void setUnitsCd(String unitsCd) {
		this.unitsCd = unitsCd;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public void setConceptCd(String conceptCd) {
		this.conceptCd = conceptCd;
	}

	public void setInstanceNum(String instanceNum) {
		this.instanceNum = instanceNum;
	}

	public void setModifierCd(String modifierCd) {
		this.modifierCd = modifierCd;
	}

	public void setValueTypeCd(String valueTypeCd) {
		this.valueTypeCd = valueTypeCd;
	}

	public void setTvalChar(String tvalChar) {
		this.tvalChar = tvalChar;
	}

	public void setNvalNum(Double nvalNum) {
		this.nvalNum = nvalNum;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public String getConceptCd() {
		return conceptCd;
	}

	public String getInstanceNum() {
		return instanceNum;
	}

	public String getModifierCd() {
		return modifierCd;
	}

	public String getValueTypeCd() {
		return valueTypeCd;
	}

	public String getTvalChar() {
		return tvalChar;
	}

	public Double getNvalNum() {
		return nvalNum;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public String getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(String encounterId) {
		this.encounterId = encounterId;
	}

}
