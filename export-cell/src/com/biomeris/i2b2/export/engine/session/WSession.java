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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.biomeris.i2b2.export.engine.ExportConstants;
import com.biomeris.i2b2.export.engine.io.misc.Network;

public class WSession {
	private String id;
	private Map<String,WExport> exports;
	private long lastAccess;
	private File folder;
	private Network network;

	public WSession() {
		id = UUID.randomUUID().toString();
		exports = new TreeMap<String, WExport>();
		updateLastAccess();
	}

	public synchronized void setNetwork(Network network) {
		this.network = network;
	}

	public Network getNetwork() {
		return network;
	}

	public synchronized void updateLastAccess(){
		lastAccess = System.currentTimeMillis();
	}
	
	public synchronized WExport addNewExport(String name){
		String eId = UUID.randomUUID().toString();

		WExport e = new WExport(eId, name, network.getUsername(), folder);
		exports.put(eId, e);

		return e;
	}
	
	public WExport getExport(String id){
		return exports.get(id);
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public String getId() {
		return id;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}
	
	public List<WExport> getExports(){
		List<WExport> output = new ArrayList<>();
		output.addAll(exports.values());
		
		Collections.sort(output, new Comparator<WExport>() {
			@Override
			public int compare(WExport e1, WExport e2) {
				return e1.getExportTime().compareTo(e2.getExportTime());
			}
		});
		
		return output;
	}

	public boolean isRunning() {
		for(WExport export : exports.values()){
			if(export.getStatus().equals(ExportConstants.STATUS_WORKING)){
				return true;
			}
		}
		return false;
	}
	
}
