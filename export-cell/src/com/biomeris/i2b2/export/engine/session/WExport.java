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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.io.misc.Export;
import com.biomeris.i2b2.export.engine.io.misc.ExportParams;
import com.biomeris.i2b2.export.engine.io.misc.Modifier;

public class WExport {
	private String id, name, username;
	private String status;
	private File exportDir, exportFile;
	private ExportParams exportParams;
	private List<Concept> concepts;
	private String patSetId, patSetName;
	private List<String> problems;
	private Exception exception;
	private Calendar exportTime;

	public WExport(String id, String name, String username, File sessionDir) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		concepts = new ArrayList<>();
		exportTime = Calendar.getInstance();
		
		exportDir = new File(sessionDir,id);
		exportDir.mkdir();
	}

	public void addProblem(String problem) {
		if(problems == null){
			problems = new ArrayList<>();
		}
		problems.add(problem);
	}

	public List<Concept> getConcepts() {
		return concepts;
	}

	public Exception getException() {
		return exception;
	}

	public File getExportFile() {
		return exportFile;
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

	public String getPatSetId() {
		return patSetId;
	}

	public List<String> getProblems() {
		return problems;
	}

	public String getStatus() {
		return status;
	}

	public String getUsername() {
		return username;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}

	public void setExportParams(ExportParams exportParams) {
		this.exportParams = exportParams;
	}

	public void setPatSetId(String patSetId) {
		this.patSetId = patSetId;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Calendar getExportTime() {
		return exportTime;
	}

	public File getExportDir() {
		return exportDir;
	}

	public String getPatSetName() {
		return patSetName;
	}

	public void setPatSetName(String patSetName) {
		this.patSetName = patSetName;
	}

	public Export makeIOExport(){
		Export output = new Export();
		output.setId(id);
		output.setName(name);
		output.setStatus(status);
		output.setPatientSetName(patSetName);
		output.setConcepts(cleanConcepts(concepts));
		output.setExportParams(exportParams);
		output.setProblems(problems);
		if(exportFile != null){
			output.setFileName(exportFile.getName());
		}
		if(exception!=null){
			output.setExceptionMessage(exception.getMessage());
		}
		return output;
	}
	
	private List<Concept> cleanConcepts(List<Concept> concepts){
		List<Concept> output = new ArrayList<>();
		for(Concept c : concepts){
			Concept newC = new Concept();
			
			newC.setColumnName(c.getColumnName());
			newC.setDimCode(c.getDimCode());
			newC.setHlevel(c.getHlevel());
			newC.setItemKey(c.getItemKey());
			newC.setName(c.getName());
			newC.setTableName(c.getTableName());
			newC.setType(c.getType());
			if(c.getModifier() != null){
				newC.setModifier(cleanModifier(c.getModifier()));
			}
			
			output.add(newC);
		}
		return output;
	}
	
	private Modifier cleanModifier(Modifier modifier){
		if(modifier == null){
			return null;
		}
		Modifier output = new Modifier();
		output.setBasecode(modifier.getBasecode());
		output.setColumnName(modifier.getColumnName());
		output.setDimCode(modifier.getDimCode());
		output.setModifierKey(modifier.getModifierKey());
		output.setName(modifier.getName());
		output.setTableName(modifier.getTableName());
		return output;
	}
}
