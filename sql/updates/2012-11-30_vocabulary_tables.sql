
create table T_VOCABULARY_FOLDER (
    VOCABULARY_FOLDER_ID int(10) unsigned not null auto_increment,
    CONTINUITY_ID varchar(36) not null,
    IDENTIFIER varchar(100) not null,
    VOCABULARY_TYPE varchar(50) not null default 'COMMON',
    LABEL varchar(255) not null,
    REG_STATUS enum('Draft','Public draft','Released') not null default 'Draft',
    WORKING_COPY bool not null default false,
    CHECKEDOUT_COPY_ID int(10) unsigned default null,
    WORKING_USER varchar(50) default null,
    DATE_MODIFIED timestamp not null default now(),
    USER_MODIFIED varchar(50) default null,
    CONCEPT_IDENTIFIER_NUMERIC boolean not null default false,
    BASE_URI varchar(255) default null,
    primary key (VOCABULARY_FOLDER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table T_VOCABULARY_FOLDER add index (IDENTIFIER);
ALTER TABLE T_VOCABULARY_FOLDER ADD CONSTRAINT UNIQUE_VOCABULARY UNIQUE (IDENTIFIER, WORKING_COPY);

create table T_VOCABULARY_CONCEPT (
    VOCABULARY_CONCEPT_ID int(10) unsigned not null auto_increment,
    VOCABULARY_FOLDER_ID int(10) unsigned not null,
    IDENTIFIER varchar(100) not null,
    LABEL varchar(255) not null,
    DEFINITION text default null,
    NOTATION varchar(50) default null,
    primary key (VOCABULARY_CONCEPT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE T_VOCABULARY_CONCEPT ADD CONSTRAINT FK_VOCABULARY_FOLDER_ID FOREIGN KEY (VOCABULARY_FOLDER_ID) REFERENCES T_VOCABULARY_FOLDER(VOCABULARY_FOLDER_ID) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE T_VOCABULARY_CONCEPT ADD CONSTRAINT UNIQUE_CONCEPT UNIQUE (VOCABULARY_FOLDER_ID, IDENTIFIER);
