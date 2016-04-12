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

public class Network {
	private String proxyAddress, pmServiceAddress, username, password, domain, project;
	private String staticProxyAddress;
	
	//proxyAddress da JavaScript lo trovi in location.href (globale) (e.g. "http://192.168.0.187/webclient/index.php")
	//pmServiceAddress da JS si trova in i2b2.PM.cfg.cellUrl (globale) (e.g. "http://127.0.0.1:9090/i2b2/rest/PMService/")
	//	o anche in i2b2.hive.cfg.lstDomains[0].urlCellPM
	//pmServiceName va messo hardcoded in JS (getServices)
	//username da JS : i2b2.h.getUser()
	//password da JS : i2b2.h.getPass()
	//domain da JS : i2b2.h.getDomain()
	//project da JS : i2b2.h.getProject()
	
	public String getPmServiceAddress() {
		return pmServiceAddress;
	}

	public void setPmServiceAddress(String pmServiceAddress) {
		this.pmServiceAddress = pmServiceAddress;
	}

	public String getProxyAddress() {
		return proxyAddress;
	}

	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}

	public String getStaticProxyAddress() {
		return staticProxyAddress;
	}

	public void setStaticProxyAddress(String staticProxyAddress) {
		this.staticProxyAddress = staticProxyAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
}
