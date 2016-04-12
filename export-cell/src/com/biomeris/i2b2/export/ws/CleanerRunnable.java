/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biomeris.i2b2.export.engine.session.WSessionManager;

class CleanerRunnable implements Runnable{
	
	private WSessionManager sessionManager;
	private static Log log = LogFactory.getLog(CleanerRunnable.class);

	CleanerRunnable(WSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void run() {
		while(true){
			try {
				sessionManager.cleanSessionsBut(null);
				Thread.sleep(15*1000);
			} catch (InterruptedException e) {
				log.debug("Problem with cleaner");
				break;
			}
		}
	}

}
