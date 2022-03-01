package com.transglobe.streamingetl.health.rest2.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTopic {

	@JsonProperty("topic")
	private String topic;
	
	@JsonProperty("numPartitions")
	private Integer numPartitions;
	
	@JsonProperty("replicationFactor")
	private Short replicationFactor;

	public CreateTopic() { }

	
	public String getTopic() {
		return topic;
	}


	public void setTopic(String topic) {
		this.topic = topic;
	}


	public Integer getNumPartitions() {
		return numPartitions;
	}

	public void setNumPartitions(Integer numPartitions) {
		this.numPartitions = numPartitions;
	}

	public Short getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(Short replicationFactor) {
		this.replicationFactor = replicationFactor;
	}
	
	
}
