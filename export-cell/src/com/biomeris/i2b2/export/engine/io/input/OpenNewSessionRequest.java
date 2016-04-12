/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.io.input;

import com.biomeris.i2b2.export.engine.io.misc.Network;

public class OpenNewSessionRequest {
	private Network network;

	public OpenNewSessionRequest() {
		super();
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}
}
