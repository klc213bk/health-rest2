package com.transglobe.streamingetl.health.rest2.bean;

public enum HealthTopicEnum {
	HEARTBEAT("EBAOPRD1.TGLMINER.TM2_HEARTBEAT"),
	DDL("EBAOPRD1.TGLMINER._GENERIC_DDL2");
			
	private String topic;
	

	HealthTopicEnum(String topic) {
		this.topic = topic;
	}


	public String getTopic() {
		return topic;
	}


	public void setTopic(String topic) {
		this.topic = topic;
	}

	
	
}
