/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


--
-- DROP DATABASE IF EXISTS `ambari`;
-- DROP USER `ambari`;

# delimiter ;

# CREATE DATABASE `bigtop_manager` /*!40100 DEFAULT CHARACTER SET utf8 */;
#
# CREATE USER 'bigtop_manager' IDENTIFIED BY 'bigdata';

# USE @schema;

-- Set default_storage_engine to InnoDB
-- storage_engine variable should be used for versions prior to MySQL 5.6
set @version_short = substring_index(@@version, '.', 2);
set @major = cast(substring_index(@version_short, '.', 1) as SIGNED);
set @minor = cast(substring_index(@version_short, '.', -1) as SIGNED);
set @engine_stmt = IF((@major >= 5 AND @minor>=6) or @major >= 8, 'SET default_storage_engine=INNODB', 'SET storage_engine=INNODB');
prepare statement from @engine_stmt;
execute statement;
DEALLOCATE PREPARE statement;

CREATE TABLE `sequence`
(
    `id`        BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `seq_name`  VARCHAR(100) NOT NULL,
    `seq_count` BIGINT(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seq_name` (`seq_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(32) DEFAULT NULL,
    `password`    VARCHAR(32) DEFAULT NULL,
    `nickname`    VARCHAR(32) DEFAULT NULL,
    `status`      BIT(1)      DEFAULT 1 COMMENT '0-Disable, 1-Enable',
    `create_time` DATETIME    DEFAULT NULL,
    `update_time` DATETIME    DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cluster`
(
    `id`           BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_name` VARCHAR(255) DEFAULT NULL COMMENT 'Cluster Name',
    `cluster_desc` VARCHAR(255) DEFAULT NULL COMMENT 'Cluster Name',
    `cluster_type` SMALLINT UNSIGNED DEFAULT 1 COMMENT '1-Physical Machine, 2-Kubernetes',
    `selected`     BIT(1)      DEFAULT 1 COMMENT '0-Disable, 1-Enable',
    `create_time`  DATETIME     DEFAULT NULL,
    `update_time`  DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cluster_name` (`cluster_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `host`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`      BIGINT(20) UNSIGNED NOT NULL,
    `hostname`        VARCHAR(255) DEFAULT NULL,
    `ipv4`            VARCHAR(32)  DEFAULT NULL,
    `ipv6`            VARCHAR(32)  DEFAULT NULL,
    `arch`         VARCHAR(32) DEFAULT NULL,
    `os`         VARCHAR(32) DEFAULT NULL,
    `processor_count` INT         DEFAULT NULL,
    `physical_memory` BIGINT      DEFAULT NULL COMMENT 'Total Physical Memory(Bytes)',
    `state`           VARCHAR(32) DEFAULT NULL,
    `create_time`     DATETIME     DEFAULT NULL,
    `update_time`     DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hostname` (`hostname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `repo` (
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`  BIGINT(20) UNSIGNED NOT NULL,
    `os`          VARCHAR(32)  DEFAULT NULL,
    `arch`        VARCHAR(32)  DEFAULT NULL,
    `base_url`    VARCHAR(64)  DEFAULT NULL,
    `repo_id`     VARCHAR(32)  DEFAULT NULL,
    `repo_name`   VARCHAR(64)  DEFAULT NULL,
    `create_time` DATETIME     DEFAULT NULL,
    `update_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stack`
(
    `id`            BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `stack_name`    VARCHAR(32) NOT NULL,
    `stack_version` VARCHAR(32) NOT NULL,
    `create_time`   DATETIME DEFAULT NULL,
    `update_time`   DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stack_name` (`stack_name`,`stack_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `job`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`  BIGINT(20) UNSIGNED DEFAULT NULL,
    `state`       VARCHAR(32) NOT NULL,
    `context`     TEXT        NOT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY           `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stage`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(32) NOT NULL,
    `cluster_id`  BIGINT(20) UNSIGNED DEFAULT NULL,
    `job_id`      BIGINT(20) UNSIGNED NOT NULL,
    `state`       VARCHAR(32) NOT NULL,
    `stage_order` INT UNSIGNED DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY           `idx_cluster_id` (`cluster_id`),
    KEY           `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;