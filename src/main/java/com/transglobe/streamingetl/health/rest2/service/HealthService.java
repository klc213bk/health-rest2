package com.transglobe.streamingetl.health.rest2.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transglobe.streamingetl.health.rest2.bean.HealthSPEnum;
import com.transglobe.streamingetl.health.rest2.bean.HealthTableEnum;
import com.transglobe.streamingetl.health.rest2.bean.HealthTopicEnum;
import com.transglobe.streamingetl.health.rest2.common.CreateTopic;


@Service
public class HealthService {
	static final Logger LOG = LoggerFactory.getLogger(HealthService.class);
	
	static Integer HEALTH_NUM_PARTITIONS = 1; 
	static Short HEALTH_REPLICATION_FACTOR = 2;
	
	@Value("${tglminer.db.driver}")
	private String tglminerDbDriver;

	@Value("${tglminer.db.url}")
	private String tglminerDbUrl;

	@Value("${tglminer.db.username}")
	private String tglminerDbUsername;

	@Value("${tglminer.db.password}")
	private String tglminerDbPassword;
	
	@Value("${kafka.rest.url}")
	private String kafkaRestUrl;
	
	public void cleanup() throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			Class.forName(tglminerDbDriver);
			conn = DriverManager.getConnection(tglminerDbUrl, tglminerDbUsername, tglminerDbPassword);

			// drop health SP
			// drop user SP
			Set<String> spSet = new HashSet<>();
			for (HealthSPEnum e : HealthSPEnum.values()) {
				spSet.add(e.getSpName());
			}
			sql = "select OBJECT_NAME from dba_objects where object_type = 'PROCEDURE' and owner = 'TGLMINER'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String sp = rs.getString("OBJECT_NAME");
				if (spSet.contains(sp)) {
					executeScript(conn, "DROP PROCEDURE " + sp);
					LOG.info(">>> SP={} dropped", sp);
				}
			}
			rs.close();
			pstmt.close();

			// drop user tables
			LOG.info(">>> drop user tables");
			Set<String> tbSet = new HashSet<>();
			for (HealthTableEnum tableEnum : HealthTableEnum.values()) {
				tbSet.add(tableEnum.getTableName());
			}
			sql = "select TABLE_NAME from USER_TABLES";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String table = rs.getString("TABLE_NAME");
				if (tbSet.contains(table)) {
					executeScript(conn, "DROP TABLE " + table);
					LOG.info(">>> table={} dropped", table); 
				}
			}
			pstmt.close();

			LOG.info(">>> delete kafka topic");
			Set<String> topicSet = listTopics();
			for (HealthTopicEnum e : HealthTopicEnum.values()) {
				if (topicSet.contains(e.getTopic())) {
					deleteTopic(e.getTopic());
					LOG.info(">>>>>>>>>>>> deleteTopic ={}done ", e.getTopic());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (pstmt != null) pstmt.close();
			if (conn != null) conn.close();
		}

	}
	public void initialize() throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			Class.forName(tglminerDbDriver);
			conn = DriverManager.getConnection(tglminerDbUrl, tglminerDbUsername, tglminerDbPassword);
			conn.setAutoCommit(false);

			// create tables
			for (HealthTableEnum e : HealthTableEnum.values()) {
				LOG.info(">>>>>>> create TABLE file {}",e.getScriptFile());
				executeSqlScriptFromFile(conn, e.getScriptFile());
			}
			conn.commit();

			for (HealthSPEnum e : HealthSPEnum.values()) {
				LOG.info(">>>>>>> create SP file {}",e.getScriptFile());
				executeSqlScriptFromFile(conn, e.getScriptFile());
			}

			conn.commit();

			LOG.info(">>> insert kafka topic");
			Set<String> topicSet = listTopics();
			for (HealthTopicEnum e : HealthTopicEnum.values()) {
				if (topicSet.contains(e.getTopic())) {
					deleteTopic(e.getTopic());
					LOG.info(">>>>>>>>>>>> deleteTopic:{} done", e.getTopic());
				}
				createTopic(e.getTopic(), HEALTH_NUM_PARTITIONS, HEALTH_REPLICATION_FACTOR);
				LOG.info(">>>>>>>>>>>> createTopic:{} done ", e.getTopic());
			}

			conn.close();
		} finally {
			if (pstmt != null) pstmt.close();
			if (conn != null) conn.close();
		}


	}
	private void executeScript(Connection conn, String script) throws Exception {

		Statement stmt = null;
		try {

			stmt = conn.createStatement();
			stmt.executeUpdate(script);
			stmt.close();

		} finally {
			if (stmt != null) stmt.close();
		}

	}
	private void createTopic(String topic, Integer numPartitions, Short replicationFactor) throws Exception {

		CreateTopic createTopic = new CreateTopic();
		createTopic.setNumPartitions(numPartitions);
		createTopic.setReplicationFactor(replicationFactor);
		createTopic.setTopic(topic);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(createTopic);

		String url = kafkaRestUrl + "/createTopic";
		LOG.info(">>>>>>> url={}, jsonStr={}", url, jsonStr); 
		String response = restPostService(url, jsonStr);


	}
	private Set<String> listTopics() throws Exception {
		String url = kafkaRestUrl + "/listTopics";
		String response = restService(url, "GET");

//		LOG.info(">>>>>>>>>>>> response={} ", response);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(response);
		String topicStr = jsonNode.get("topics").asText();
		List<String> topicList = mapper.readValue(topicStr, new TypeReference<List<String>>() {});
		LOG.info(">>>>>>>>>>>> topics={} ", String.join(",", topicList));

		return new HashSet<>(topicList);
	}
	private void executeSqlScriptFromFile(Connection conn, String file) throws Exception {
		
		Statement stmt = null;
		try {

			ClassLoader loader = Thread.currentThread().getContextClassLoader();	
			try (InputStream inputStream = loader.getResourceAsStream(file)) {
				String createScript = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				stmt = conn.createStatement();
				stmt.executeUpdate(createScript);
				stmt.close();
			} catch (SQLException | IOException e) {
				if (stmt != null) stmt.close();
				throw e;
			}

		
		} finally {
			if (stmt != null) stmt.close();
		}
	}
	private void deleteTopic(String topic) throws Exception {

		String deleteUrl = String.format(kafkaRestUrl + "/deleteTopic/%s", topic);
		LOG.info(">>>>>>>>>>>> deleteUrl={} ", deleteUrl);

		String response = restService(deleteUrl, "POST");

		LOG.info(">>>>>>>>>>>> response={} ", response);


	}
	private String restService(String urlStr, String requestMethod) throws Exception {
		
		HttpURLConnection httpConn = null;
		URL url = null;
		try {
			url = new URL(urlStr);
			httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestMethod(requestMethod);
			int responseCode = httpConn.getResponseCode();
			//			LOG.info(">>>>>  responseCode={}",responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			StringBuffer response = new StringBuffer();
			String readLine = null;
			while ((readLine = in.readLine()) != null) {
				response.append(readLine);
			}
			in.close();

			return response.toString();
		} finally {
			if (httpConn != null ) httpConn.disconnect();
		}
	}
	public static String restPostService(String urlStr, String jsonStr) throws Exception {

		HttpURLConnection httpConn = null;
		URL url = null;
		OutputStream os = null;
		BufferedReader in = null;
		try {
			url = new URL(urlStr);
			httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/json;utf-8" );
			httpConn.setRequestProperty("Accept", "application/json" );
			httpConn.setDoOutput(true);

			os = httpConn.getOutputStream();
			byte[] input = jsonStr.getBytes("utf-8");
			os.write(input, 0, input.length);
			
			
//			httpConn.setRequestMethod(requestMethod);
			int responseCode = httpConn.getResponseCode();
			
			in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
			StringBuffer response = new StringBuffer();
			String readLine = null;
			while ((readLine = in.readLine()) != null) {
				response.append(readLine.trim());
			}
			in.close();

			return response.toString();
		} finally {
			if (os != null) os.close();
			if (in != null) in.close();
			if (httpConn != null ) httpConn.disconnect();
		}
	}
}
