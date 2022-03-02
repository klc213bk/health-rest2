package com.transglobe.streamingetl.health.rest2.bean;

public enum HealthSPEnum {
	SP2_INS_HEARTBEAT("SP2_INS_HEALTH_HEARTBEAT", "createSP-SP2_INS_HEARTBEAT.sql"),
	SP2_UPD_SERVER_STATUS("SP2_UPD_SERVER_STATUS", "createSP-SP2_UPD_SERVER_STATUS.sql"),
	SP2_UPD_LOGMINER_RECEIVED("SP2_UPD_LOGMINER_RECEIVED", "createSP-SP2_UPD_LOGMINER_RECEIVED.sql");
	
	private String spName;
	private String scriptFile;

	HealthSPEnum(String spName, String scriptFile) {
		this.spName = spName;
		this.scriptFile = scriptFile;
	}

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}
	
	
}
