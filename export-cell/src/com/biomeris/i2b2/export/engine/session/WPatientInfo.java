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

public class WPatientInfo {
	private String patientId, sex, language, race, maritalStatus, religion, zipCode, income, vitalStatus;
	private Calendar dateOfBirth, dateOfDeath;

	public Calendar getDateOfBirth() {
		return dateOfBirth;
	}

	public Calendar getDateOfDeath() {
		return dateOfDeath;
	}

	public String getIncome() {
		return income;
	}

	public String getLanguage() {
		return language;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public String getPatientId() {
		return patientId;
	}

	public String getRace() {
		return race;
	}

	public String getReligion() {
		return religion;
	}

	public String getSex() {
		return sex;
	}

	public String getVitalStatus() {
		return vitalStatus;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setDateOfBirth(Calendar dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setDateOfDeath(Calendar dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}

	public void setIncome(String income) {
		this.income = income;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public void setRace(String race) {
		this.race = race;
	}

	public void setReligion(String religion) {
		this.religion = religion;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setVitalStatus(String vitalStatus) {
		this.vitalStatus = vitalStatus;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

}
