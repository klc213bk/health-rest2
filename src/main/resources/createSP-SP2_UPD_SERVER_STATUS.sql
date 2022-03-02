CREATE OR REPLACE PROCEDURE SP2_UPD_SERVER_STATUS ( 
    i_kafka_status VARCHAR2,
    i_logminer_status VARCHAR2,
    i_connect_status VARCHAR2
) AS
BEGIN
  
    UPDATE TM2_LOGMINER_OFFSET 
    SET KAFKA_STATUS = CASE WHEN i_kafka_status IS NULL THEN KAFKA_STATUS ELSE i_kafka_status END
    , LOGMINER_STATUS = CASE WHEN i_logminer_status IS NULL THEN LOGMINER_STATUS ELSE i_logminer_status END
    , CONNECT_STATUS = CASE WHEN i_connect_status IS NULL THEN CONNECT_STATUS ELSE i_connect_status END
    , UPDATE_TIME = current_timestamp
    WHERE START_TIME = 
    (
        SELECT START_TIME
        FROM TM2_LOGMINER_OFFSET
        ORDER BY START_TIME DESC
        FETCH NEXT 1 ROW ONLY
    );
  
  
END SP2_UPD_SERVER_STATUS;