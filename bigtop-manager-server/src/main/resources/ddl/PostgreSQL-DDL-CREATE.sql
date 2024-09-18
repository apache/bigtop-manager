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

CREATE TABLE audit_log
(
    id                BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    args              TEXT,
    create_by         BIGINT,
    create_time       TIMESTAMP(0),
    operation_desc    VARCHAR(255),
    operation_summary VARCHAR(255),
    tag_desc          VARCHAR(255),
    tag_name          VARCHAR(255),
    update_by         BIGINT,
    update_time       TIMESTAMP(0),
    uri               VARCHAR(255),
    user_id           BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id          BIGINT CHECK (id > 0) NOT NULL GENERATED ALWAYS AS IDENTITY,
    username    VARCHAR(32)  DEFAULT NULL,
    password    VARCHAR(32)  DEFAULT NULL,
    nickname    VARCHAR(32)  DEFAULT NULL,
    status      BOOLEAN          DEFAULT TRUE,
    create_time TIMESTAMP(0) DEFAULT NULL,
    update_time TIMESTAMP(0) DEFAULT NULL,
    create_by   BIGINT,
    update_by   BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_username UNIQUE (username)
);

COMMENT ON COLUMN "user".status IS '0-Disable, 1-Enable';

CREATE TABLE cluster
(
    id            BIGINT CHECK (id > 0) NOT NULL GENERATED ALWAYS AS IDENTITY,
    cluster_name  VARCHAR(255)                      DEFAULT NULL,
    cluster_desc  VARCHAR(255)                      DEFAULT NULL,
    cluster_type  SMALLINT CHECK (cluster_type > 0) DEFAULT 1,
    selected      BOOLEAN                               DEFAULT TRUE,
    create_time   TIMESTAMP(0)                      DEFAULT NULL,
    update_time   TIMESTAMP(0)                      DEFAULT NULL,
    create_by     BIGINT,
    packages      VARCHAR(255),
    repo_template VARCHAR(255),
    root          VARCHAR(255),
    state         VARCHAR(255),
    update_by     BIGINT,
    user_group    VARCHAR(255),
    stack_id      BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_cluster_name UNIQUE (cluster_name)
);

COMMENT ON COLUMN cluster.cluster_name IS 'Cluster Name';
COMMENT ON COLUMN cluster.cluster_desc IS 'Cluster Description';
COMMENT ON COLUMN cluster.cluster_type IS '1-Physical Machine, 2-Kubernetes';
COMMENT ON COLUMN cluster.selected IS '0-Disable, 1-Enable';

DROP INDEX IF EXISTS idx_cluster_stack_id;
CREATE INDEX idx_cluster_stack_id ON cluster (stack_id);

CREATE TABLE component
(
    id              BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    category        VARCHAR(255),
    command_script  VARCHAR(255),
    component_name  VARCHAR(255),
    create_by       BIGINT,
    create_time     TIMESTAMP(0),
    custom_commands TEXT,
    display_name    VARCHAR(255),
    quick_link      VARCHAR(255),
    cardinality     VARCHAR(255),
    update_by       BIGINT,
    update_time     TIMESTAMP(0),
    cluster_id      BIGINT,
    service_id      BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_component_name UNIQUE (component_name, cluster_id)
);

DROP INDEX IF EXISTS idx_component_cluster_id;
DROP INDEX IF EXISTS idx_component_service_id;
CREATE INDEX idx_component_cluster_id ON component (cluster_id);
CREATE INDEX idx_component_service_id ON component (service_id);

CREATE TABLE host_component
(
    id           BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    create_by    BIGINT,
    create_time  TIMESTAMP(0),
    state        VARCHAR(255),
    update_by    BIGINT,
    update_time  TIMESTAMP(0),
    component_id BIGINT,
    host_id      BIGINT,
    PRIMARY KEY (id)
);

DROP INDEX IF EXISTS idx_hc_component_id;
DROP INDEX IF EXISTS idx_hc_host_id;
CREATE INDEX idx_hc_component_id ON host_component (component_id);
CREATE INDEX idx_hc_host_id ON host_component (host_id);

CREATE TABLE host
(
    id                   BIGINT CHECK (id > 0)         NOT NULL GENERATED ALWAYS AS IDENTITY,
    cluster_id           BIGINT CHECK (cluster_id > 0) NOT NULL,
    hostname             VARCHAR(255) DEFAULT NULL,
    ipv4                 VARCHAR(32)  DEFAULT NULL,
    ipv6                 VARCHAR(32)  DEFAULT NULL,
    arch                 VARCHAR(32)  DEFAULT NULL,
    os                   VARCHAR(32)  DEFAULT NULL,
    processor_count      INT          DEFAULT NULL,
    physical_memory      BIGINT       DEFAULT NULL,
    state                VARCHAR(32)  DEFAULT NULL,
    create_time          TIMESTAMP(0) DEFAULT NULL,
    update_time          TIMESTAMP(0) DEFAULT NULL,
    available_processors INTEGER,
    create_by            BIGINT,
    free_disk            BIGINT,
    free_memory_size     BIGINT,
    total_disk           BIGINT,
    total_memory_size    BIGINT,
    update_by            BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_hostname UNIQUE (hostname, cluster_id)
);

COMMENT ON COLUMN host.physical_memory IS 'Total Physical Memory(Bytes)';

DROP INDEX IF EXISTS idx_host_cluster_id;
CREATE INDEX idx_host_cluster_id ON host (cluster_id);

CREATE TABLE repo
(
    id          BIGINT CHECK (id > 0)         NOT NULL GENERATED ALWAYS AS IDENTITY,
    cluster_id  BIGINT CHECK (cluster_id > 0) NOT NULL,
    os          VARCHAR(32)  DEFAULT NULL,
    arch        VARCHAR(32)  DEFAULT NULL,
    base_url    VARCHAR(64)  DEFAULT NULL,
    repo_id     VARCHAR(32)  DEFAULT NULL,
    repo_name   VARCHAR(64)  DEFAULT NULL,
    repo_type   VARCHAR(64)  DEFAULT NULL,
    create_time TIMESTAMP(0) DEFAULT NULL,
    update_time TIMESTAMP(0) DEFAULT NULL,
    create_by   BIGINT,
    update_by   BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_repo_id UNIQUE (repo_id, os, arch, cluster_id)
);

DROP INDEX IF EXISTS idx_cluster_id;
CREATE INDEX idx_cluster_id ON repo (cluster_id);

CREATE TABLE stack
(
    id            BIGINT CHECK (id > 0) NOT NULL GENERATED ALWAYS AS IDENTITY,
    stack_name    VARCHAR(32)           NOT NULL,
    stack_version VARCHAR(32)           NOT NULL,
    create_time   TIMESTAMP(0) DEFAULT NULL,
    update_time   TIMESTAMP(0) DEFAULT NULL,
    create_by     BIGINT,
    update_by     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_stack UNIQUE (stack_name, stack_version)
);

CREATE TABLE task
(
    id             BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    command        VARCHAR(255),
    component_name VARCHAR(255),
    content        TEXT,
    context        TEXT   NOT NULL,
    create_by      BIGINT,
    create_time    TIMESTAMP(0),
    custom_command VARCHAR(255),
    hostname       VARCHAR(255),
    name           VARCHAR(255),
    service_name   VARCHAR(255),
    service_user   VARCHAR(255),
    stack_name     VARCHAR(255),
    stack_version  VARCHAR(255),
    state          VARCHAR(255),
    update_by      BIGINT,
    update_time    TIMESTAMP(0),
    cluster_id     BIGINT,
    job_id         BIGINT,
    stage_id       BIGINT,
    PRIMARY KEY (id)
);

DROP INDEX IF EXISTS idx_task_cluster_id;
DROP INDEX IF EXISTS idx_task_job_id;
DROP INDEX IF EXISTS idx_task_stage_id;
CREATE INDEX idx_task_cluster_id ON task (cluster_id);
CREATE INDEX idx_task_job_id ON task (job_id);
CREATE INDEX idx_task_stage_id ON task (stage_id);

CREATE TABLE job
(
    id          BIGINT CHECK (id > 0) NOT NULL GENERATED ALWAYS AS IDENTITY,
    cluster_id  BIGINT CHECK (cluster_id > 0) DEFAULT NULL,
    state       VARCHAR(32)           NOT NULL,
    context     TEXT                  NOT NULL,
    create_time TIMESTAMP(0)                  DEFAULT NULL,
    update_time TIMESTAMP(0)                  DEFAULT NULL,
    create_by   BIGINT,
    name        VARCHAR(255),
    update_by   BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_job_cluster_id ON job (cluster_id);

CREATE TABLE type_config
(
    id                BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    create_by         BIGINT,
    create_time       TIMESTAMP(0),
    properties_json   TEXT,
    type_name         VARCHAR(255),
    update_by         BIGINT,
    update_time       TIMESTAMP(0),
    service_config_id BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE service
(
    id                BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    create_by         BIGINT,
    create_time       TIMESTAMP(0),
    display_name      VARCHAR(255),
    package_specifics VARCHAR(1024),
    required_services VARCHAR(255),
    service_desc      VARCHAR(1024),
    service_name      VARCHAR(255),
    service_user      VARCHAR(255),
    service_version   VARCHAR(255),
    update_by         BIGINT,
    update_time       TIMESTAMP(0),
    cluster_id        BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_service_name UNIQUE (service_name, cluster_id)
);

CREATE INDEX idx_service_cluster_id ON service (cluster_id);

CREATE TABLE service_config
(
    id          BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    config_desc VARCHAR(255),
    create_by   BIGINT,
    create_time TIMESTAMP(0),
    selected    BOOLEAN default FALSE,
    update_by   BIGINT,
    update_time TIMESTAMP(0),
    version     INTEGER,
    cluster_id  BIGINT,
    service_id  BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sc_cluster_id ON service_config (cluster_id);
CREATE INDEX idx_sc_service_id ON service_config (service_id);

CREATE TABLE setting
(
    id          BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    config_data TEXT,
    create_by   BIGINT,
    create_time TIMESTAMP(0),
    type_name   VARCHAR(255),
    update_by   BIGINT,
    update_time TIMESTAMP(0),
    PRIMARY KEY (id)
);

CREATE TABLE stage
(
    id             BIGINT CHECK (id > 0)     NOT NULL GENERATED ALWAYS AS IDENTITY,
    name           VARCHAR(32)               NOT NULL,
    cluster_id     BIGINT CHECK (cluster_id > 0) DEFAULT NULL,
    job_id         BIGINT CHECK (job_id > 0) NOT NULL,
    state          VARCHAR(32)               NOT NULL,
    create_time    TIMESTAMP(0)                  DEFAULT NULL,
    update_time    TIMESTAMP(0)                  DEFAULT NULL,
    component_name VARCHAR(255),
    context        TEXT,
    create_by      BIGINT,
    "order"        INTEGER,
    service_name   VARCHAR(255),
    update_by      BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_stage_cluster_id ON stage (cluster_id);
CREATE INDEX idx_job_id ON stage (job_id);

CREATE TABLE llm_platform
(
    id             BIGINT CHECK (id > 0) NOT NULL GENERATED ALWAYS AS IDENTITY,
    name           VARCHAR(255)          NOT NULL,
    credential     JSON         DEFAULT NULL,
    support_models VARCHAR(255) DEFAULT NULL,
    create_time    TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP /* ON UPDATE CURRENT_TIMESTAMP */,
    create_by      BIGINT       DEFAULT NULL,
    update_by      BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE llm_platform_authorized
(
    id          BIGINT CHECK (id > 0)          NOT NULL GENERATED ALWAYS AS IDENTITY,
    platform_id BIGINT CHECK (platform_id > 0) NOT NULL,
    credentials JSON                           NOT NULL,
    create_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP /* ON UPDATE CURRENT_TIMESTAMP */,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_authorized_platform_id ON llm_platform_authorized (platform_id);

CREATE TABLE llm_chat_thread
(
    id          BIGINT CHECK (id > 0)          NOT NULL GENERATED ALWAYS AS IDENTITY,
    platform_id BIGINT CHECK (platform_id > 0) NOT NULL,
    user_id     BIGINT CHECK (user_id > 0)     NOT NULL,
    model       VARCHAR(255)                   NOT NULL,
    thread_info JSON         DEFAULT NULL,
    create_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP /* ON UPDATE CURRENT_TIMESTAMP */,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_chatthread_platform_id ON llm_chat_thread (platform_id);
CREATE INDEX idx_chatthread_user_id ON llm_chat_thread (user_id);

CREATE TABLE llm_chat_message
(
    id          BIGINT CHECK (id > 0)        NOT NULL GENERATED ALWAYS AS IDENTITY,
    thread_id   BIGINT CHECK (thread_id > 0) NOT NULL,
    user_id     BIGINT CHECK (user_id > 0)   NOT NULL,
    message     TEXT                         NOT NULL,
    sender      VARCHAR(50)                  NOT NULL,
    create_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP /* ON UPDATE CURRENT_TIMESTAMP */,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_thread_id ON llm_chat_message (thread_id);
CREATE INDEX idx_message_user_id ON llm_chat_message (user_id);

INSERT INTO "user" (create_time, update_time, nickname, password, status, username)
VALUES (now(), now(), 'Administrator', '21232f297a57a5a743894a0e4a801fc3', true, 'admin');

INSERT INTO llm_platform (credential, NAME, support_models)
VALUES
('{"apiKey": "API Key"}','OpenAI','gpt-3.5-turbo,gpt-4,gpt-4o,gpt-3.5-turbo-16k,gpt-4-turbo-preview,gpt-4-32k,gpt-4o-mini'),
('{"apiKey": "API Key"}','DashScope','qwen-max,qwen-plus,qwen-turbo'),
('{"apiKey": "API Key", "secretKey": "Secret Key"}','QianFan','Yi-34B-Chat,ERNIE-4.0-8K,ERNIE-3.5-128K,ERNIE-Speed-8K,Llama-2-7B-Chat,Fuyu-8B');
