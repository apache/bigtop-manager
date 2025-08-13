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

CREATE TABLE `audit_log`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `args`              TEXT,
    `create_by`         BIGINT,
    `create_time`       DATETIME,
    `operation_desc`    VARCHAR(255),
    `operation_summary` VARCHAR(255),
    `tag_desc`          VARCHAR(255),
    `tag_name`          VARCHAR(255),
    `update_by`         BIGINT,
    `update_time`       DATETIME,
    `uri`               VARCHAR(255),
    `user_id`           BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(32) DEFAULT NULL,
    `password`    VARCHAR(255) DEFAULT NULL,
    `nickname`    VARCHAR(32) DEFAULT NULL,
    `status`      BIT(1)      DEFAULT 1 COMMENT '0-Disable, 1-Enable',
    `create_time` DATETIME    DEFAULT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT,
    `update_by`   BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cluster`
(
    `id`                   BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`                 VARCHAR(255) DEFAULT NULL COMMENT 'Unique Name',
    `display_name`         VARCHAR(255) DEFAULT NULL COMMENT 'Display Name',
    `desc`                 VARCHAR(255) DEFAULT NULL COMMENT 'Cluster Description',
    `type`                 INTEGER DEFAULT 1 COMMENT '1-Physical Machine, 2-Kubernetes',
    `user_group`           VARCHAR(255),
    `root_dir`             VARCHAR(255),
    `status`               INTEGER DEFAULT NULL COMMENT '1-healthy, 2-unhealthy, 3-unknown',
    `create_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`            BIGINT,
    `update_by`            BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `host`
(
    `id`                   BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`           BIGINT(20) UNSIGNED DEFAULT NULL,
    `hostname`             VARCHAR(255) DEFAULT NULL,
    `agent_dir`            VARCHAR(255) DEFAULT NULL,
    `ssh_user`             VARCHAR(255) DEFAULT NULL,
    `ssh_port`             INTEGER DEFAULT NULL,
    `auth_type`            INTEGER DEFAULT NULL COMMENT '1-password, 2-key, 3-no_auth',
    `ssh_password`         VARCHAR(255) DEFAULT NULL,
    `ssh_key_string`       TEXT DEFAULT NULL,
    `ssh_key_filename`     VARCHAR(255) DEFAULT NULL,
    `ssh_key_password`     VARCHAR(255) DEFAULT NULL,
    `grpc_port`            INTEGER DEFAULT NULL,
    `ipv4`                 VARCHAR(32)  DEFAULT NULL,
    `ipv6`                 VARCHAR(32)  DEFAULT NULL,
    `arch`                 VARCHAR(32)  DEFAULT NULL,
    `os`                   VARCHAR(32)  DEFAULT NULL,
    `available_processors` INTEGER,
    `free_disk`            BIGINT,
    `free_memory_size`     BIGINT,
    `total_disk`           BIGINT,
    `total_memory_size`    BIGINT,
    `desc`                 VARCHAR(255) DEFAULT NULL,
    `status`               INTEGER DEFAULT NULL COMMENT '1-healthy, 2-unhealthy, 3-unknown',
    `err_info`             TEXT DEFAULT NULL,
    `create_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`            BIGINT,
    `update_by`            BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hostname` (`hostname`, `cluster_id`),
    KEY            `idx_host_cluster_id` (cluster_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `repo`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(32) DEFAULT NULL,
    `arch`        VARCHAR(32) DEFAULT NULL,
    `base_url`    VARCHAR(255) DEFAULT NULL,
    `pkg_name`    VARCHAR(64)   DEFAULT NULL,
    `checksum`    VARCHAR(255)  DEFAULT NULL,
    `type`        INTEGER  DEFAULT NULL COMMENT '1-service, 2-dependency',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT,
    `update_by`   BIGINT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `service`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `name`              VARCHAR(255) DEFAULT NULL,
    `display_name`      VARCHAR(255) DEFAULT NULL,
    `desc`              VARCHAR(1024) DEFAULT NULL,
    `user`              VARCHAR(255) DEFAULT NULL,
    `version`           VARCHAR(255) DEFAULT NULL,
    `stack`             VARCHAR(255) DEFAULT NULL,
    `restart_flag`      BOOLEAN DEFAULT FALSE,
    `cluster_id`        BIGINT,
    `status`            INTEGER DEFAULT NULL COMMENT '1-healthy, 2-unhealthy, 3-unknown',
    `create_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         BIGINT,
    `update_by`         BIGINT,
    PRIMARY KEY (id),
    KEY                 idx_service_cluster_id (cluster_id),
    UNIQUE KEY `uk_service_name` (`name`, `cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `service_config`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `name`              VARCHAR(255),
    `properties_json`   TEXT,
    `cluster_id`        BIGINT,
    `service_id`        BIGINT,
    `create_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         BIGINT,
    `update_by`         BIGINT,
    PRIMARY KEY (id),
    KEY           idx_sc_cluster_id (cluster_id),
    KEY           idx_sc_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `service_config_snapshot`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `name`              VARCHAR(255),
    `desc`              VARCHAR(255),
    `config_json`       LONGTEXT,
    `service_id`        BIGINT,
    `create_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         BIGINT,
    `update_by`         BIGINT,
    PRIMARY KEY (id),
    KEY           idx_scs_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `component`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(255),
    `display_name`    VARCHAR(255),
    `cluster_id`      BIGINT,
    `host_id`         BIGINT,
    `service_id`      BIGINT,
    `status`          INTEGER DEFAULT NULL COMMENT '1-healthy, 2-unhealthy, 3-unknown',
    `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT,
    `update_by`       BIGINT,
    PRIMARY KEY (id),
    KEY               `idx_component_cluster_id` (cluster_id),
    KEY               `idx_component_host_id` (host_id),
    KEY               `idx_component_service_id` (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `job`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(255),
    `context`     TEXT    NOT NULL,
    `state`       VARCHAR(32) NOT NULL,
    `cluster_id`  BIGINT(20) UNSIGNED DEFAULT NULL,
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT,
    `update_by`   BIGINT,
    PRIMARY KEY (`id`),
    KEY           `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stage`
(
    `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(32) NOT NULL,
    `service_name`   VARCHAR(255),
    `component_name` VARCHAR(255),
    `context`        TEXT,
    `order`          INTEGER,
    `state`          VARCHAR(32) NOT NULL,
    `cluster_id`     BIGINT(20) UNSIGNED DEFAULT NULL,
    `job_id`         BIGINT(20) UNSIGNED NOT NULL,
    `create_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT,
    `update_by`      BIGINT,
    PRIMARY KEY (`id`),
    KEY              `idx_cluster_id` (`cluster_id`),
    KEY              `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `task`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(255),
    `hostname`        VARCHAR(255),
    `service_name`    VARCHAR(255),
    `service_user`    VARCHAR(255),
    `component_name`  VARCHAR(255),
    `command`         VARCHAR(255),
    `custom_command`  VARCHAR(255),
    `content`         TEXT,
    `context`         TEXT NOT NULL,
    `state`           VARCHAR(255),
    `cluster_id`      BIGINT,
    `job_id`          BIGINT,
    `stage_id`        BIGINT,
    `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT,
    `update_by`       BIGINT,
    PRIMARY KEY (id),
    KEY               idx_task_cluster_id (cluster_id),
    KEY               idx_task_job_id (job_id),
    KEY               idx_task_stage_id (stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `llm_platform`
(
    `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(255)        NOT NULL,
    `credential`     TEXT                DEFAULT NULL,
    `desc`           TEXT                DEFAULT NULL,
    `support_models` VARCHAR(255)        DEFAULT NULL,
    `create_time`    DATETIME            DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT              DEFAULT NULL,
    `update_by`      BIGINT              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `llm_auth_platform`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `platform_id` BIGINT(20) UNSIGNED NOT NULL,
    `credentials` TEXT                NOT NULL,
    `is_deleted`  TINYINT(1)          DEFAULT 0 NULL,
    `status`      SMALLINT            DEFAULT 0 COMMENT '1-Active, 2-Available, 3-Unavailable',
    `model`       VARCHAR(255)        NOT NULL,
    `name`        VARCHAR(255)        NOT NULL,
    `desc`        VARCHAR(255)        NOT NULL,
    `create_time` DATETIME            DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT              DEFAULT NULL,
    `update_by`   BIGINT              DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_platform_id` (`platform_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `llm_chat_thread`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `auth_id`     BIGINT(20) UNSIGNED NOT NULL,
    `user_id`     BIGINT(20) UNSIGNED NOT NULL,
    `is_deleted`  TINYINT(1)          DEFAULT 0 NULL,
    `name`        VARCHAR(255)        DEFAULT NULL,
    `create_time` DATETIME            DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT              DEFAULT NULL,
    `update_by`   BIGINT              DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY             `idx_auth_id` (`auth_id`),
    KEY             `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `llm_chat_message`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `thread_id`   BIGINT(20) UNSIGNED NOT NULL,
    `user_id`     BIGINT(20) UNSIGNED NOT NULL,
    `message`     TEXT                NOT NULL,
    `sender`      VARCHAR(50)         NOT NULL,
    `is_deleted`  TINYINT(1)          DEFAULT 0 NULL,
    `create_time` DATETIME            DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT              DEFAULT NULL,
    `update_by`   BIGINT              DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY              `idx_thread_id` (`thread_id`),
    KEY              `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Adding default admin user
INSERT INTO user (username, password, nickname, status)
VALUES ('admin', '$2b$10$bdTvADKA0dSJYT3wMU3LFeIEnxzKQHeWN3XcHJ5jQpsIo7ju1U5Yi', 'Administrator', true);

ALTER TABLE user ADD COLUMN token_version INTEGER DEFAULT 1;

INSERT INTO repo (name, arch, base_url, pkg_name, checksum, type)
VALUES
('general', 'x86_64,aarch64', 'http://your-repo/', null, null, 1),
('mysql', 'x86_64,aarch64', 'https://dev.mysql.com/get/Downloads/MySQL-8.0/', null, null, 1),
('grafana', 'x86_64,aarch64', 'https://dl.grafana.com/oss/release/', null, null, 1),
('agent', 'x86_64,aarch64', 'http://your-repo/', 'bigtop-manager-agent.tar.gz', null, 2),
('jdk8', 'x86_64', 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u452-b09/', 'OpenJDK8U-jdk_x64_linux_hotspot_8u452b09.tar.gz', 'SHA-256:9448308a21841960a591b47927cf2d44fdc4c0533a5f8111a4b243a6bafb5d27', 2),
('jdk8', 'aarch64', 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u452-b09/', 'OpenJDK8U-jdk_aarch64_linux_hotspot_8u452b09.tar.gz', 'SHA-256:d8a1aecea0913b7a1e0d737ba6f7ea99059b3f6fd17813d4a24e8b3fc3aee278', 2),
('mysql-connector-j', 'x86_64,aarch64', 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/', 'mysql-connector-j-8.0.33.jar', 'SHA-256:e2a3b2fc726a1ac64e998585db86b30fa8bf3f706195b78bb77c5f99bf877bd9', 2);


-- Adding default llm platform
INSERT INTO llm_platform (credential, name, support_models)
VALUES
('{"apiKey": "API Key"}', 'OpenAI', 'gpt-3.5-turbo,gpt-4,gpt-4o,gpt-3.5-turbo-16k,gpt-4-turbo-preview,gpt-4-32k,gpt-4o-mini'),
('{"apiKey": "API Key"}', 'DashScope', 'qwen-1.8b-chat,qwen-max,qwen-plus,qwen-turbo,qwen3-235b-a22b,qwen3-30b-a3b,qwen-plus-latest,qwen-turbo-latest'),
('{"apiKey": "API Key", "secretKey": "Secret Key"}', 'QianFan','Yi-34B-Chat,ERNIE-4.0-8K,ERNIE-3.5-128K,ERNIE-Speed-8K,Llama-2-7B-Chat,Fuyu-8B'),
('{"apiKey": "API Key"}','DeepSeek','deepseek-chat,deepseek-reasoner');

UPDATE `llm_platform`
SET `desc` = 'Get your API Key in https://platform.openai.com/api-keys'
WHERE `name` = 'OpenAI';

UPDATE `llm_platform`
SET `desc` = 'Get your API Key in https://bailian.console.aliyun.com/?apiKey=1#/api-key'
WHERE `name` = 'DashScope';

UPDATE `llm_platform`
SET `desc` = 'Get API Key and Secret Key in https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application/v1'
WHERE `name` = 'QianFan';

UPDATE `llm_platform`
SET `desc` = 'Get your API Key in https://platform.deepseek.com'
WHERE `name` = 'DeepSeek';
