CREATE SCHEMA OpenFDA

USE SCHEMA OpenFDA

CREATE TABLE `drug_event_pair_table_one` (
  `drug_name` varchar(200) DEFAULT NULL,
  `event_name` varchar(200) DEFAULT NULL,
  `literature_source` varchar(500) DEFAULT NULL
) ;

CREATE TABLE `drug_event_pair_table_test` (
  `drug_name` varchar(200) DEFAULT NULL,
  `event_name` varchar(200) DEFAULT NULL,
  `literature_source` varchar(500) DEFAULT NULL
) ;