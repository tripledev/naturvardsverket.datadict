/*
	Script for importing CDDA Site codes from CSV file. 
	NB! deletes data from T_SITE_CODE table and clears sitecode vocabulary concepts.

	FIXME!
	SET correct VOCABULARY_ID before executing the script.
	SET csv file name
*/

/*SET @CSV_FILE = "C:/Projects/EEA/datadict/CDDA_SiteCodes.csv";*/
SET @CSV_FILE = "./CDDA_SiteCodes.csv";
SET @VOCABULARY_ID = 2;

/* Create temporary table for import */
DROP TABLE IF EXISTS TMP_CDDA_SITE_CODE_IMPORT;
create table TMP_CDDA_SITE_CODE_IMPORT (
    SITE_CODE varchar(100) default null,
    CC char(2) default null,
    ALLOCATED_DATE datetime null default null,
    ALLOCATED_NAME varchar(50),
    PARENT_ISO char(3) default null,
	SITE_CODE_NAT varchar(30) default null,
    SITE_NAME varchar(120) default null,
	CREATED_YEAR datetime null default null,
	SITE_CODE_REPORTED_YEAR year null default null,
	SITE_DELETED_YEAR year default null,
	SITE_DISAPEARED_YEAR year default null,
	SITE_CODE_ERROR varchar(255) default null,
	SITE_NAME_CHANGED_YEAR year default null,
	SITE_CODE_NAT_CHANGED_YEAR year default null

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Load data */
LOAD DATA INFILE "./CDDA_SiteCodes.csv" IGNORE INTO TABLE TMP_CDDA_SITE_CODE_IMPORT CHARACTER SET 'utf8' 
FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n'  IGNORE 1 LINES
(SITE_CODE, CC, @field_ALLOCATED_DATE, ALLOCATED_NAME, PARENT_ISO, SITE_CODE_NAT, SITE_NAME, @field_CREATED_YEAR)
SET ALLOCATED_DATE = IF(@field_ALLOCATED_DATE='',null,str_to_date(IF(LENGTH(@field_ALLOCATED_DATE)=4,concat(@field_ALLOCATED_DATE,'-01-01'),@field_ALLOCATED_DATE), '%Y-%m-%d')),
 CREATED_YEAR = IF(@field_CREATED_YEAR='',null,str_to_date(IF(LENGTH(@field_CREATED_YEAR)=4,concat(@field_CREATED_YEAR,'-01-01'),@field_CREATED_YEAR), '%Y-%m-%d'));
/*
DELETE FROM TMP_CDDA_SITE_CODE_IMPORT;
SELECT * FROM TMP_CDDA_SITE_CODE_IMPORT LIMIT 0, 100;
*/

/* Delete existing site codes */
DELETE FROM T_VOCABULARY_CONCEPT WHERE VOCABULARY_FOLDER_ID=@VOCABULARY_ID;
DELETE FROM T_SITE_CODE;

/* Insert vocabulary concepts */
INSERT INTO T_VOCABULARY_CONCEPT  (VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL,  NOTATION) SELECT @VOCABULARY_ID, SITE_CODE, IF(SITE_NAME IS NULL or SITE_NAME='','reserved',SITE_NAME), SITE_CODE FROM  TMP_CDDA_SITE_CODE_IMPORT;

/* Insert site codes */
INSERT INTO T_SITE_CODE  (VOCABULARY_CONCEPT_ID, SITE_CODE_NAT, STATUS, CC_ISO2, PARENT_ISO, DATE_CREATED, USER_CREATED, DATE_ALLOCATED, USER_ALLOCATED) 
	SELECT VOCABULARY_CONCEPT_ID, SITE_CODE_NAT,
		IF (SITE_NAME IS NULL OR SITE_NAME='', 'ALLOCATED', 'ASSIGNED'), CC, PARENT_ISO, CREATED_YEAR, ALLOCATED_NAME, ALLOCATED_DATE, ALLOCATED_NAME
	FROM  TMP_CDDA_SITE_CODE_IMPORT i, T_VOCABULARY_CONCEPT c WHERE i.SITE_CODE=c.IDENTIFIER AND c.VOCABULARY_FOLDER_ID=@VOCABULARY_ID;

/* remove temporary table */
DROP TABLE IF EXISTS TMP_CDDA_SITE_CODE_IMPORT;
