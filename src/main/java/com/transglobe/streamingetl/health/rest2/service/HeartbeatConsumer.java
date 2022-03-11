package com.transglobe.streamingetl.health.rest2.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HeartbeatConsumer implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(HeartbeatConsumer.class);

	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final KafkaConsumer<String, String> consumer;

	private BasicDataSource tglminerConnPool;

	private String clientId;
	
	private List<String> topicList;
	
	private Boolean consumerStarted = Boolean.FALSE; 
	
	public HeartbeatConsumer(String clientId,
			String groupId,  
			String bootstrapServers,
			List<String> topicList,
			BasicDataSource tglminerConnPool
			) {
		this.tglminerConnPool = tglminerConnPool;
		this.clientId = clientId;
		this.topicList = topicList;
		
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrapServers);
		props.put("group.id", groupId);
		props.put("client.id", clientId);
		props.put("group.instance.id", groupId + "-mygid" );
		props.put("enable.auto.commit", "false");
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		props.put("session.timeout.ms", 60000 ); // 60 seconds
		props.put("max.poll.records", 50 );
		props.put("auto.offset.reset", "earliest" );
		this.consumer = new KafkaConsumer<>(props);

	}
	public boolean consumerStarted() {
		return consumerStarted.booleanValue();
	}
	@Override
	public void run() {
		try {
			consumer.subscribe(topicList);

			consumerStarted = Boolean.TRUE;
			
			logger.info("   >>>>>>>>>>>>>>>>>>>>>>>> run ........closed={}",closed.get());

			List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
			while (!closed.get()) {

				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
				for (ConsumerRecord<String, String> record : records) {
					buffer.add(record);
				}

				if (buffer.size() > 0) {

					int tries = 0;
					while (tglminerConnPool.isClosed()) {
						tries++;
						try {
							tglminerConnPool.restart();

							logger.info("   >>> tglminerConnPool restart, try {} times", tries);

							Thread.sleep(10000);
						} catch (Exception e) {
							logger.error(">>> message={}, stack trace={}, record str={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
						}

					}

					process(buffer);

					consumer.commitSync();

					buffer.clear();
				}
			}
		} catch (WakeupException e) {
			// ignore excepton if closing 
			if (!closed.get()) throw e;

			logger.info(">>>ignore excepton if closing, message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));

		} finally {
			consumer.close();
			
			if (tglminerConnPool != null) {
				try {
					tglminerConnPool.close();
				} catch (SQLException e) {
					logger.error(">>>logminerConnPool error, finally message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}
	public void process(List<ConsumerRecord<String, String>> buffer) {

		Connection conn = null;
		CallableStatement cstmt = null;
		PreparedStatement pstmt = null;
		ConsumerRecord<String, String> recordEx = null;
		try {
			conn = tglminerConnPool.getConnection();
			for (ConsumerRecord<String, String> record : buffer) {
				recordEx = record;
//				logger.info("   >>>record topic={}, key={},value={},offset={}", record.topic(), record.key(), record.value(), record.offset());
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				JsonNode jsonNodeKey = objectMapper.readTree(record.key());
				String connectorKey = jsonNodeKey.get("payload").asText();
				
				objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

				JsonNode jsonNode = objectMapper.readTree(record.value());
				JsonNode payload = jsonNode.get("payload");
				Long scn = Long.valueOf(payload.get("SCN").asText());
				Long commitScn = Long.valueOf(payload.get("COMMIT_SCN").asText());
				String rowId = payload.get("ROW_ID").asText();
				JsonNode payLoadData = payload.get("data");
				long hartBeatTimeMs = Long.valueOf(payLoadData.get("HEARTBEAT_TIME").asText());
				Timestamp heartbeatTime = new Timestamp(hartBeatTimeMs);
				
				Timestamp currTs = new Timestamp(System.currentTimeMillis());
				
				String consumerClient = String.format("%s(%s)", clientId, connectorKey);
				cstmt = conn.prepareCall("{call SP2_UPD_CONSUMER_RECEIVED(?,?,?)}");
				cstmt.setString(1,  consumerClient);
				cstmt.setTimestamp(2,  heartbeatTime);
				cstmt.setTimestamp(3,  currTs);
				cstmt.execute();
				
				logger.info("   >>>Done !!! heartbeatTime={},scn={},commitscn={],rowid={}",heartbeatTime, scn,commitScn,rowId);
				
			}
			conn.close();
		}  catch(Exception e) {
			if (recordEx != null) {
				Map<String, Object> data = new HashMap<>();
				data.put("topic", recordEx.topic());
				data.put("partition", recordEx.partition());
				data.put("offset", recordEx.offset());
				data.put("value", recordEx.value());
				logger.error(">>>record error, message={}, stack trace={}, record str={}", e.getMessage(), ExceptionUtils.getStackTrace(e), data);
			} else {
				logger.error(">>>record error, message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					logger.error(">>>record error, message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					logger.error(">>>record error, message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error(">>>record error, message={}, stack trace={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public void shutdown() {
		closed.set(true);
		consumer.wakeup();
	}
	public boolean isConsumerClosed() {
		return closed.get();
	}
	
	
	

}