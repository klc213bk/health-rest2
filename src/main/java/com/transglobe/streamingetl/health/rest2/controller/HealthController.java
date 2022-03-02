package com.transglobe.streamingetl.health.rest2.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.transglobe.streamingetl.health.rest2.service.HealthService;

@RestController
@RequestMapping("/")
public class HealthController {
	static final Logger LOG = LoggerFactory.getLogger(HealthController.class);

	@Autowired
	private HealthService healthService;
	
//	@Autowired
//	private HealthMonitorService healthMonitorService;
	
	@Autowired
	private ObjectMapper mapper;

	@GetMapping(path="/ok")
	@ResponseBody
	public ResponseEntity<String> ok() {
		LOG.info(">>>>controller ok is called");
		
		
		
		LOG.info(">>>>controller ok finished ");
		
		return new ResponseEntity<String>("OK", HttpStatus.OK);
	}
	
	@PostMapping(path="/cleanup", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> cleanup() {
		LOG.info(">>>>controller cleanup is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			healthService.cleanup();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller cleanup finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	
	@PostMapping(path="/initialize", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> initialize() {
		LOG.info(">>>>controller initialize is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			healthService.initialize();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller initialize finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	@PostMapping(path="/startHeartbeat", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> startHeartbeat() {
		LOG.info(">>>>controller startHeartbeat is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			healthService.startHeartbeat();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller startHeartbeat finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	@PostMapping(path="/startHeartbeatConsumer", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> startHeartbeatConsumer() {
		LOG.info(">>>>controller startHeartbeatConsumer is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			healthService.startHeartbeatConsumer();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller startHeartbeatConsumer finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
	@PostMapping(path="/stopHeartbeatConsumer", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> stopHeartbeatConsumer() {
		LOG.info(">>>>controller stopHeartbeatConsumer is called");
		
		ObjectNode objectNode = mapper.createObjectNode();
		
		try {
			healthService.stopHeartbeatConsumer();
			objectNode.put("returnCode", "0000");
		} catch (Exception e) {
			String errMsg = ExceptionUtils.getMessage(e);
			String stackTrace = ExceptionUtils.getStackTrace(e);
			objectNode.put("returnCode", "-9999");
			objectNode.put("errMsg", errMsg);
			objectNode.put("returnCode", stackTrace);
			LOG.error(">>> errMsg={}, stacktrace={}",errMsg,stackTrace);
		}
		
		LOG.info(">>>>controller stopHeartbeatConsumer finished ");
		
		return new ResponseEntity<Object>(objectNode, HttpStatus.OK);
	}
}
