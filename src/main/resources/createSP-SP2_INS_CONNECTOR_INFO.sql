CREATE OR REPLACE PROCEDURE SP2_INS_CONNECTOR_INFO ( 
    i_connector VARCHAR2,
    i_op_time TIMESTAMP,
    i_operation VARCHAR2,
    i_configs VARCHAR2,
    i_reset_offset INT,
    i_start_scn NUMBER,
    i_connector_status VARCHAR2
) AS
BEGIN
  
  	INSERT INTO TM2_CONNECTOR_INFO
  	(CONNECTOR,OP_TIME,OPERATION,CONFIGS,RESET_OFFSET,START_SCN,CONNECTOR_STATUS) 
  	VALUES (i_connector,i_op_time,i_operation,i_configs,i_reset_offset,i_start_scn,i_connector_status);
  	
  
  
END SP2_INS_CONNECTOR_INFO;