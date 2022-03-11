create table TM2_CONNECTOR_INFO
(
	CONNECTOR VARCHAR2(30 BYTE),
	OP_TIME	TIMESTAMP,
	OPERATION VARCHAR2(30 BYTE),
	CONFIGS VARCHAR2(500 BYTE),
	RESET_OFFSET	NUMBER(1,0),
	START_SCN	NUMBER(19,0),
	CONNECTOR_STATUS VARCHAR2(30 BYTE),
	PRIMARY KEY (CONNECTOR, OP_TIME)
)
