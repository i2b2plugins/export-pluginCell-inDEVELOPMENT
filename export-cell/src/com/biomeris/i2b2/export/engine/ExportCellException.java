/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine;

public class ExportCellException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExportCellException() {
		super();
	}
	
	public ExportCellException(String message) {
		super(message);
	}
}
