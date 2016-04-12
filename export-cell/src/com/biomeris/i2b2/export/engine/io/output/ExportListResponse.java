/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.io.output;

import java.util.List;

import com.biomeris.i2b2.export.engine.io.misc.Export;

public class ExportListResponse {
	private String sessionId;
	private List<Export> exports;
	private String error;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<Export> getExports() {
		return exports;
	}

	public void setExports(List<Export> exports) {
		this.exports = exports;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
