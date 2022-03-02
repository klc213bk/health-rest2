package com.transglobe.streamingetl.health.rest2.bean;

public enum HealthTableEnum {
	TM_HEALTH("TGLMINER", "TM2_HEALTH", "createtable-TM2_HEALTH.sql"),
	TM_HEARTBEAT("TGLMINER", "TM2_HEARTBEAT", "createtable-TM2_HEARTBEAT.sql"),
	TM2_LOGMINER_OFFSET("TGLMINER", "TM2_LOGMINER_OFFSET", "createtable-TM2_LOGMINER_OFFSET.sql"),
	;
	
	private String schema;
	
	private String tableName;
	
	private String scriptFile;

	HealthTableEnum(String schema, String tableName, String scriptFile) {
		this.schema = schema;
		this.tableName = tableName;
		this.scriptFile = scriptFile;
	}

	
	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}
	
	
}
