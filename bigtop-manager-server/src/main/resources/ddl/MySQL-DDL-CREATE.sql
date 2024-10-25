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
    `args`              LONGTEXT,
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
    `password`    VARCHAR(32) DEFAULT NULL,
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
    `id`            BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_name`  VARCHAR(255) DEFAULT NULL COMMENT 'Cluster Name',
    `cluster_desc`  VARCHAR(255) DEFAULT NULL COMMENT 'Cluster Description',
    `cluster_type`  SMALLINT UNSIGNED DEFAULT 1 COMMENT '1-Physical Machine, 2-Kubernetes',
    `selected`      BIT(1)       DEFAULT 1 COMMENT '0-Disable, 1-Enable',
    `create_time`   DATETIME     DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT NULL,
    `create_by`     BIGINT,
    `packages`      VARCHAR(255),
    `repo_template` VARCHAR(255),
    `root`          VARCHAR(255),
    `state`         VARCHAR(255),
    `update_by`     BIGINT,
    `user_group`    VARCHAR(255),
    `stack_id`      BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cluster_name` (`cluster_name`),
    KEY `idx_cluster_stack_id` (`stack_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `component`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `category`        VARCHAR(255),
    `command_script`  VARCHAR(255),
    `component_name`  VARCHAR(255),
    `create_by`       BIGINT,
    `create_time`     DATETIME,
    `custom_commands` LONGTEXT,
    `display_name`    VARCHAR(255),
    `quick_link`      VARCHAR(255),
    `cardinality`     VARCHAR(255),
    `update_by`       BIGINT,
    `update_time`     DATETIME,
    `cluster_id`      BIGINT,
    `service_id`      BIGINT,
    PRIMARY KEY (id),
    KEY               `idx_component_cluster_id` (cluster_id),
    KEY               `idx_component_service_id` (service_id),
    UNIQUE KEY `uk_component_name` (`component_name`, `cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `host_component`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `create_by`    BIGINT,
    `create_time`  DATETIME,
    `state`        VARCHAR(255),
    `update_by`    BIGINT,
    `update_time`  DATETIME,
    `component_id` BIGINT,
    `host_id`      BIGINT,
    PRIMARY KEY (id),
    KEY            `idx_hc_component_id` (component_id),
    KEY            `idx_hc_host_id` (host_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `host`
(
    `id`                   BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`           BIGINT(20) UNSIGNED NOT NULL,
    `hostname`             VARCHAR(255) DEFAULT NULL,
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
    `status`               INTEGER  DEFAULT NULL COMMENT '1-healthy, 2-unhealthy, 3-unknown',
    `err_info`             VARCHAR(255) DEFAULT NULL,
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
    `base_url`    VARCHAR(64) DEFAULT NULL,
    `type`        INT DEFAULT NULL COMMENT '1-services, 2-tools',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   BIGINT,
    `update_by`   BIGINT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stack`
(
    `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `stack_name`     VARCHAR(32) NOT NULL,
    `stack_version`  VARCHAR(32) NOT NULL,
    `create_time`    DATETIME DEFAULT NULL,
    `update_time`    DATETIME DEFAULT NULL,
    `create_by`      BIGINT,
    `update_by`      BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stack` (`stack_name`, `stack_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `task`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `command`         VARCHAR(255),
    `component_name`  VARCHAR(255),
    `content`         LONGTEXT,
    `context`         LONGTEXT NOT NULL,
    `create_by`       BIGINT,
    `create_time`     DATETIME,
    `custom_command`  VARCHAR(255),
    `hostname`        VARCHAR(255),
    `name`            VARCHAR(255),
    `service_name`    VARCHAR(255),
    `service_user`    VARCHAR(255),
    `stack_name`      VARCHAR(255),
    `stack_version`   VARCHAR(255),
    `state`           VARCHAR(255),
    `update_by`       BIGINT,
    `update_time`     DATETIME,
    `cluster_id`      BIGINT,
    `job_id`          BIGINT,
    `stage_id`        BIGINT,
    PRIMARY KEY (id),
    KEY               idx_task_cluster_id (cluster_id),
    KEY               idx_task_job_id (job_id),
    KEY               idx_task_stage_id (stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `job`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cluster_id`  BIGINT(20) UNSIGNED DEFAULT NULL,
    `state`       VARCHAR(32) NOT NULL,
    `context`     LONGTEXT    NOT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `create_by`   BIGINT,
    `name`        VARCHAR(255),
    `update_by`   BIGINT,
    PRIMARY KEY (`id`),
    KEY           `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `type_config`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `create_by`         BIGINT,
    `create_time`       DATETIME,
    `properties_json`   LONGTEXT,
    `type_name`         VARCHAR(255),
    `update_by`         BIGINT,
    `update_time`       DATETIME,
    `service_config_id` BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `service`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT,
    `create_by`         BIGINT,
    `create_time`       DATETIME,
    `display_name`      VARCHAR(255),
    `package_specifics` VARCHAR(1024),
    `required_services` VARCHAR(255),
    `service_desc`      VARCHAR(1024),
    `service_name`      VARCHAR(255),
    `service_user`      VARCHAR(255),
    `service_version`   VARCHAR(255),
    `update_by`         BIGINT,
    `update_time`       DATETIME,
    `cluster_id`        BIGINT,
    PRIMARY KEY (id),
    KEY                 idx_service_cluster_id (cluster_id),
    UNIQUE KEY `uk_service_name` (`service_name`, `cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `service_config`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `config_desc` VARCHAR(255),
    `create_by`   BIGINT,
    `create_time` DATETIME,
    `selected`    TINYINT(1) default 0,
    `update_by`   BIGINT,
    `update_time` DATETIME,
    `version`     INTEGER,
    `cluster_id`  BIGINT,
    `service_id`  BIGINT,
    PRIMARY KEY (id),
    KEY           idx_sc_cluster_id (cluster_id),
    KEY           idx_sc_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `setting`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `config_data` LONGTEXT,
    `create_by`   BIGINT,
    `create_time` DATETIME,
    `type_name`   VARCHAR(255),
    `update_by`   BIGINT,
    `update_time` DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stage`
(
    `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(32) NOT NULL,
    `cluster_id`     BIGINT(20) UNSIGNED DEFAULT NULL,
    `job_id`         BIGINT(20) UNSIGNED NOT NULL,
    `state`          VARCHAR(32) NOT NULL,
    `create_time`    DATETIME DEFAULT NULL,
    `update_time`    DATETIME DEFAULT NULL,
    `component_name` VARCHAR(255),
    `context`        LONGTEXT,
    `create_by`      BIGINT,
    `order`          INTEGER,
    `service_name`   VARCHAR(255),
    `update_by`      BIGINT,
    PRIMARY KEY (`id`),
    KEY              `idx_cluster_id` (`cluster_id`),
    KEY              `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `llm_platform`
(
    `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(255)        NOT NULL,
    `credential`     TEXT                DEFAULT NULL,
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
    `thread_info` TEXT                DEFAULT NULL,
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
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', true);

INSERT INTO repo (name, arch, base_url, type)
VALUES
('Service tarballs', 'x86_64', 'http://your-repo/', 1),
('Service tarballs', 'aarch64', 'http://your-repo/', 1),
('BM tools', 'x86_64', 'http://your-repo/', 2),
('BM tools', 'aarch64', 'http://your-repo/', 2);

-- Adding default llm platform
INSERT INTO llm_platform (credential, name, support_models)
VALUES
('{"apiKey": "API Key"}', 'OpenAI', 'gpt-3.5-turbo,gpt-4,gpt-4o,gpt-3.5-turbo-16k,gpt-4-turbo-preview,gpt-4-32k,gpt-4o-mini'),
('{"apiKey": "API Key"}', 'DashScope', 'qwen-1.8b-chat,qwen-max,qwen-plus,qwen-turbo'),
('{"apiKey": "API Key", "secretKey": "Secret Key"}', 'QianFan','Yi-34B-Chat,ERNIE-4.0-8K,ERNIE-3.5-128K,ERNIE-Speed-8K,Llama-2-7B-Chat,Fuyu-8B');
