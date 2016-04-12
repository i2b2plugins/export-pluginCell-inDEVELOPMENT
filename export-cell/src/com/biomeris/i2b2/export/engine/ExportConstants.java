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

public class ExportConstants {
	// Concept type
	public static final String LEAF = "L";
	public static final String FOLDER = "F";

	// Observation type
	public static final String VALTYPE_N = "N";
	public static final String VALTYPE_T = "T";
	public static final String VALTYPE_NULL = "NULL";
	public static final String VALTYPE_BLOB = "B";
	public static final String VALTYPE_AT = "@";

	// Export status
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_WORKING = "WORKING";
	public static final String STATUS_DONE = "DONE";

	// DB type
	public static final String POSTGRESQL_DB = "POSTGRESQL";
	public static final String ORACLE_DB = "ORACLE";

	// Export type
	public static final String ALL_OBSERVATIONS = "OBS";
	public static final String PATIENT_AGGREGATE = "PAT";
}
