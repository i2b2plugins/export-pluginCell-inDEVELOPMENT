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

import java.util.HashMap;
import java.util.Map;

public class Modifier {
	// from the plugin (metadata)
	private String modifierKey, basecode, columnName, dimCode, name, tableName;

	// while processing
	private Map<String, String> childrenMap;

	public String getModifierKey() {
		return modifierKey;
	}

	public void setModifierKey(String modifierKey) {
		this.modifierKey = modifierKey;
	}

	public String getBasecode() {
		return basecode;
	}

	public void setBasecode(String basecode) {
		this.basecode = basecode;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDimCode() {
		return dimCode;
	}

	public void setDimCode(String dimCode) {
		this.dimCode = dimCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, String> getChildrenMap() {
		if (childrenMap == null) {
			childrenMap = new HashMap<>();
		}
		return childrenMap;
	}
}
