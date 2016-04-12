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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.ws.ExportRunnable;

public class WSessionManager {
	private Map<String, WSession> sessions;
	private static Log log = LogFactory.getLog(WSessionManager.class);
	private long timeoutMillis;
	private File downloadDir;
	
	private final long TIMEOUT_MILLIS_DEFAULT = 180*60*1000;

	public WSessionManager() {
		sessions = new TreeMap<String, WSession>();

		// read temp dire location from cell properties
		InputStream is_i2b2 = ExportRunnable.class.getClassLoader().getResourceAsStream("conf/exportcell.properties");
		Properties exportCellProperties = new Properties();
		try {
			exportCellProperties.load(is_i2b2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String downloadtDirPath = exportCellProperties.getProperty("exportcell.download.dir");
		downloadDir = new File(downloadtDirPath);
		
		try {
			int timeoutMins = Integer.parseInt(exportCellProperties.getProperty("exportcell.session.timeout.minutes"));
			timeoutMillis = timeoutMins*60*1000;
		} catch (NumberFormatException e) {
			timeoutMillis = TIMEOUT_MILLIS_DEFAULT; 
		}
		
	}

	public synchronized WSession createNewSession() {
		WSession newSession = new WSession();
		sessions.put(newSession.getId(), newSession);

		// create session folder
		File sessionDir = new File(downloadDir, newSession.getId());
		sessionDir.mkdir();
		newSession.setFolder(sessionDir);

		return newSession;
	}

	public WSession getSession(String sessionId) {
		WSession returnSession = sessions.get(sessionId);

		if (returnSession != null) {
			returnSession.updateLastAccess();
		}

		return returnSession;
	}

	public synchronized void cleanSessionsBut(String sessionId) {
		long curentTime = System.currentTimeMillis();
		List<String> toBeRemovedKeys = new ArrayList<>();
		
		List<String> allSessionsFolderNames = new ArrayList<>();
		for(File sessionFolder : downloadDir.listFiles()){
			allSessionsFolderNames.add(sessionFolder.getName());
		}

		for (String key : sessions.keySet()) {
			allSessionsFolderNames.remove(key);
			
			if (sessionId != null && key.equals(sessionId)) {
				continue;
			}

			WSession session = sessions.get(key);
			long idleTime = curentTime - session.getLastAccess();

			if(!session.isRunning()){
				if (idleTime > timeoutMillis) {
					deleteSessionFilesAndFolder(session.getFolder());
					session = null;
					toBeRemovedKeys.add(key);
				}
			}
		}
		
		for(String oldSessionId : allSessionsFolderNames){
			File oldSessionFolder = new File(downloadDir,oldSessionId);
			deleteSessionFilesAndFolder(oldSessionFolder);
			log.info("Old session \"" + oldSessionId + "\" removed");
		}

		for (String k : toBeRemovedKeys) {
			sessions.remove(k);
			log.info("Session \"" + k + "\" removed");
		}

		System.gc();
	}

	private void deleteSessionFilesAndFolder(File sessionFolder) {
		// two level folder: session/export/file

		// delete files
		for (File exportDir : sessionFolder.listFiles()) {
			if(exportDir.isDirectory()){
				for(File f : exportDir.listFiles()){
					f.delete();
				}
			}
			exportDir.delete();
		}

		// delete session folder
		sessionFolder.delete();

	}
}
