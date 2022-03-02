CREATE OR REPLACE PROCEDURE SP2_UPD_CONSUMER_RECEIVED (
	i_client_id VARCHAR2,
	i_heartbeat_time TIMESTAMP,
	i_consumer_received TIMESTAMP
) AS
BEGIN
  			
  update TM2_HEALTH 
   set CONSUMER_RECEIVED=i_consumer_received,
   CONSUMER_CLIENT=(CONSUMER_CLIENT || i_client_id || ',') 
   where HEARTBEAT_TIME = i_heartbeat_time;
   
END SP2_UPD_CONSUMER_RECEIVED;
