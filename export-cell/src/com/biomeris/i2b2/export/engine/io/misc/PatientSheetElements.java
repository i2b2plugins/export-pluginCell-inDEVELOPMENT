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

public class PatientSheetElements {
	private boolean vitalStatus, birthDate, deathDate, sex, language, race, maritalStatus, religion, zipCode, income;

	public boolean isVitalStatus() {
		return vitalStatus;
	}

	public void setVitalStatus(boolean vitalStatus) {
		this.vitalStatus = vitalStatus;
	}

	public boolean isBirthDate() {
		return birthDate;
	}

	public void setBirthDate(boolean birthDate) {
		this.birthDate = birthDate;
	}

	public boolean isDeathDate() {
		return deathDate;
	}

	public void setDeathDate(boolean deathDate) {
		this.deathDate = deathDate;
	}

	public boolean isSex() {
		return sex;
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public boolean isLanguage() {
		return language;
	}

	public void setLanguage(boolean language) {
		this.language = language;
	}

	public boolean isRace() {
		return race;
	}

	public void setRace(boolean race) {
		this.race = race;
	}

	public boolean isMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(boolean maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public boolean isReligion() {
		return religion;
	}

	public void setReligion(boolean religion) {
		this.religion = religion;
	}

	public boolean isZipCode() {
		return zipCode;
	}

	public void setZipCode(boolean zipCode) {
		this.zipCode = zipCode;
	}

	public boolean isIncome() {
		return income;
	}

	public void setIncome(boolean income) {
		this.income = income;
	}

	
}
