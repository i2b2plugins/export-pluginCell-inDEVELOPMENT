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

public class Concept {
	//from the plugin (metadata)
	private String itemKey, name;
	private Integer hlevel;
	private String tableName, columnName, dimCode;
	
	//from plugin (almost metadata)
	private String type;
	
	//modifier
	private Modifier modifier;
	
	//while processing
	private Map<String, String> childrenMap;

	public Map<String, String> getChildrenMap() {
		if (childrenMap == null) {
			childrenMap = new HashMap<>();
		}
		return childrenMap;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getDimCode() {
		return dimCode;
	}

	public Integer getHlevel() {
		return hlevel;
	}

	public String getItemKey() {
		return itemKey;
	}

	public Modifier getModifier() {
		return modifier;
	}

	public String getName() {
		return name;
	}

	public String getTableName() {
		return tableName;
	}

	public String getType() {
		return type;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setDimCode(String dimCode) {
		this.dimCode = dimCode;
	}

	public void setHlevel(Integer hlevel) {
		this.hlevel = hlevel;
	}

	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}

	public void setModifier(Modifier modifier) {
		this.modifier = modifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setType(String type) {
		this.type = type;
	}
}
