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

public class TestSessionResponse {
	private Boolean valid;
	private Integer lifeSpan;

	public TestSessionResponse() {
		super();
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Integer getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(Integer lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

}
