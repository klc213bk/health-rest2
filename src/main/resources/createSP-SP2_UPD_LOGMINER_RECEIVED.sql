CREATE OR REPLACE PROCEDURE SP2_UPD_LOGMINER_RECEIVED (
    i_heartbeat_time TIMESTAMP,
    i_scn NUMBER,
    i_received TIMESTAMP,
    i_logminer_client VARCHAR2
) AS 
BEGIN
  
    update TM2_HEALTH 
    set LOGMINER_SCN=i_scn, 
    LOGMINER_RECEIVED=i_received, 
    LOGMINER_CLIENT=(LOGMINER_CLIENT || i_logminer_client || ',') 
    where HEARTBEAT_TIME = i_heartbeat_time;
                
END SP2_UPD_LOGMINER_RECEIVED;