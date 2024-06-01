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


-- "bigtop_manager"."audit_log" definition

CREATE TABLE "bigtop_manager"."audit_log" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"args" CLOB NULL,
	"operation_desc" VARCHAR(255) NULL,
	"operation_summary" VARCHAR(255) NULL,
	"tag_desc" VARCHAR(255) NULL,
	"tag_name" VARCHAR(255) NULL,
	"uri" VARCHAR(255) NULL,
	"user_id" BIGINT NULL,
	CONSTRAINT CONS134218863 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555665 ON "bigtop_manager"."audit_log" ("id");


-- "bigtop_manager"."cluster" definition

CREATE TABLE "bigtop_manager"."cluster" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"cluster_name" VARCHAR(255) NULL,
	"cluster_type" INTEGER NULL,
	"packages" VARCHAR(255) NULL,
	"repo_template" VARCHAR(255) NULL,
	"root" VARCHAR(255) NULL,
	"selected" BIT NULL,
	"state" VARCHAR(255) NULL,
	"user_group" VARCHAR(255) NULL,
	"stack_id" BIGINT NULL,
	CONSTRAINT CONS134218864 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555667 ON "bigtop_manager"."cluster" ("id");
CREATE UNIQUE INDEX INDEX33555701 ON "bigtop_manager"."cluster" ("cluster_name");
CREATE INDEX "idx_cluster_stack_id" ON "bigtop_manager"."cluster" ("stack_id");


-- "bigtop_manager"."command_log" definition

CREATE TABLE "bigtop_manager"."command_log" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"hostname" VARCHAR(255) NULL,
	"result" CLOB NULL,
	"job_id" BIGINT NULL,
	"stage_id" BIGINT NULL,
	"task_id" BIGINT NULL,
	CONSTRAINT CONS134218865 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555669 ON "bigtop_manager"."command_log" ("id");
CREATE INDEX "idx_cl_job_id" ON "bigtop_manager"."command_log" ("job_id");
CREATE INDEX "idx_cl_stage_id" ON "bigtop_manager"."command_log" ("stage_id");
CREATE INDEX "idx_cl_task_id" ON "bigtop_manager"."command_log" ("task_id");


-- "bigtop_manager"."component" definition

CREATE TABLE "bigtop_manager"."component" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"category" VARCHAR(255) NULL,
	"command_script" VARCHAR(255) NULL,
	"component_name" VARCHAR(255) NULL,
	"custom_commands" CLOB NULL,
	"display_name" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	"service_id" BIGINT NULL,
	CONSTRAINT CONS134218866 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555671 ON "bigtop_manager"."component" ("id");
CREATE UNIQUE INDEX INDEX33555707 ON "bigtop_manager"."component" ("component_name","cluster_id");
CREATE INDEX "idx_component_cluster_id" ON "bigtop_manager"."component" ("cluster_id");
CREATE INDEX "idx_component_service_id" ON "bigtop_manager"."component" ("service_id");


-- "bigtop_manager"."host" definition

CREATE TABLE "bigtop_manager"."host" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"arch" VARCHAR(255) NULL,
	"available_processors" INTEGER NULL,
	"free_disk" BIGINT NULL,
	"free_memory_size" BIGINT NULL,
	"hostname" VARCHAR(255) NULL,
	"ipv4" VARCHAR(255) NULL,
	"ipv6" VARCHAR(255) NULL,
	"os" VARCHAR(255) NULL,
	"state" VARCHAR(255) NULL,
	"total_disk" BIGINT NULL,
	"total_memory_size" BIGINT NULL,
	"cluster_id" BIGINT NULL,
	CONSTRAINT CONS134218867 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555673 ON "bigtop_manager"."host" ("id");
CREATE UNIQUE INDEX INDEX33555709 ON "bigtop_manager"."host" ("hostname","cluster_id");
CREATE INDEX "idx_host_cluster_id" ON "bigtop_manager"."host" ("cluster_id");


-- "bigtop_manager"."host_component" definition

CREATE TABLE "bigtop_manager"."host_component" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"state" VARCHAR(255) NULL,
	"component_id" BIGINT NULL,
	"host_id" BIGINT NULL,
	CONSTRAINT CONS134218868 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555675 ON "bigtop_manager"."host_component" ("id");
CREATE INDEX "idx_hc_component_id" ON "bigtop_manager"."host_component" ("component_id");
CREATE INDEX "idx_hc_host_id" ON "bigtop_manager"."host_component" ("host_id");


-- "bigtop_manager"."job" definition

CREATE TABLE "bigtop_manager"."job" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"context" CLOB NULL,
	"name" VARCHAR(255) NULL,
	"state" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	CONSTRAINT CONS134218869 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555677 ON "bigtop_manager"."job" ("id");
CREATE INDEX "idx_job_cluster_id" ON "bigtop_manager"."job" ("cluster_id");


-- "bigtop_manager"."repo" definition

CREATE TABLE "bigtop_manager"."repo" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"arch" VARCHAR(255) NULL,
	"base_url" VARCHAR(255) NULL,
	"os" VARCHAR(255) NULL,
	"repo_id" VARCHAR(255) NULL,
	"repo_name" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	CONSTRAINT CONS134218870 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555679 ON "bigtop_manager"."repo" ("id");
CREATE UNIQUE INDEX INDEX33555714 ON "bigtop_manager"."repo" ("repo_id","os","arch","cluster_id");
CREATE INDEX "idx_repo_cluster_id" ON "bigtop_manager"."repo" ("cluster_id");


-- "bigtop_manager"."SEQUENCE" definition

CREATE TABLE "bigtop_manager"."SEQUENCE" (
	"seq_name" VARCHAR(255) NOT NULL,
	"seq_count" BIGINT NULL,
	CONSTRAINT CONS134218871 PRIMARY KEY ("seq_name")
);
CREATE UNIQUE INDEX INDEX33555681 ON "bigtop_manager"."SEQUENCE" ("seq_name");


-- "bigtop_manager"."service" definition

CREATE TABLE "bigtop_manager"."service" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"display_name" VARCHAR(255) NULL,
	"os_specifics" VARCHAR(255) NULL,
	"required_service" VARCHAR(255) NULL,
	"service_desc" VARCHAR(255) NULL,
	"service_group" VARCHAR(255) NULL,
	"service_name" VARCHAR(255) NULL,
	"service_user" VARCHAR(255) NULL,
	"service_version" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	CONSTRAINT CONS134218872 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555683 ON "bigtop_manager"."service" ("id");
CREATE UNIQUE INDEX INDEX33555716 ON "bigtop_manager"."service" ("service_name","cluster_id");
CREATE INDEX "idx_service_cluster_id" ON "bigtop_manager"."service" ("cluster_id");


-- "bigtop_manager"."service_config" definition

CREATE TABLE "bigtop_manager"."service_config" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"properties_json" CLOB NULL,
	"type_name" VARCHAR(255) NULL,
	"version" INTEGER NULL,
	"cluster_id" BIGINT NULL,
	"service_id" BIGINT NULL,
	CONSTRAINT CONS134218873 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555685 ON "bigtop_manager"."service_config" ("id");
CREATE UNIQUE INDEX INDEX33555719 ON "bigtop_manager"."service_config" ("type_name","version","service_id","cluster_id");
CREATE INDEX "idx_sc_cluster_id" ON "bigtop_manager"."service_config" ("cluster_id");
CREATE INDEX "idx_sc_service_id" ON "bigtop_manager"."service_config" ("service_id");


-- "bigtop_manager"."service_config_mapping" definition

CREATE TABLE "bigtop_manager"."service_config_mapping" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"service_config_id" BIGINT NULL,
	"service_config_record_id" BIGINT NULL,
	CONSTRAINT CONS134218874 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555687 ON "bigtop_manager"."service_config_mapping" ("id");
CREATE INDEX "idx_scm_sc_id" ON "bigtop_manager"."service_config_mapping" ("service_config_id");
CREATE INDEX "idx_scm_scr_id" ON "bigtop_manager"."service_config_mapping" ("service_config_record_id");


-- "bigtop_manager"."service_config_record" definition

CREATE TABLE "bigtop_manager"."service_config_record" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"config_desc" CLOB NULL,
	"version" INTEGER NULL,
	"version_group" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	"service_id" BIGINT NULL,
	CONSTRAINT CONS134218875 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555689 ON "bigtop_manager"."service_config_record" ("id");
CREATE UNIQUE INDEX INDEX33555724 ON "bigtop_manager"."service_config_record" ("version","service_id");
CREATE INDEX "idx_scr_cluster_id" ON "bigtop_manager"."service_config_record" ("cluster_id");
CREATE INDEX "idx_scr_service_id" ON "bigtop_manager"."service_config_record" ("service_id");


-- "bigtop_manager"."setting" definition

CREATE TABLE "bigtop_manager"."setting" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"config_data" CLOB NULL,
	"type_name" VARCHAR(255) NULL,
	CONSTRAINT CONS134218876 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555691 ON "bigtop_manager"."setting" ("id");


-- "bigtop_manager"."stack" definition

CREATE TABLE "bigtop_manager"."stack" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"stack_name" VARCHAR(255) NULL,
	"stack_version" VARCHAR(255) NULL,
	CONSTRAINT CONS134218877 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555693 ON "bigtop_manager"."stack" ("id");
CREATE UNIQUE INDEX INDEX33555725 ON "bigtop_manager"."stack" ("stack_name","stack_version");


-- "bigtop_manager"."stage" definition

CREATE TABLE "bigtop_manager"."stage" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"component_name" VARCHAR(255) NULL,
	"context" CLOB NULL,
	"name" VARCHAR(255) NULL,
	"service_name" VARCHAR(255) NULL,
	"stage_order" INTEGER NULL,
	"state" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	"job_id" BIGINT NULL,
	CONSTRAINT CONS134218878 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555695 ON "bigtop_manager"."stage" ("id");
CREATE INDEX "idx_stage_cluster_id" ON "bigtop_manager"."stage" ("cluster_id");
CREATE INDEX "idx_stage_job_id" ON "bigtop_manager"."stage" ("job_id");


-- "bigtop_manager"."task" definition

CREATE TABLE "bigtop_manager"."task" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"command" VARCHAR(255) NULL,
	"command_script" VARCHAR(255) NULL,
	"component_name" VARCHAR(255) NULL,
	"content" CLOB NULL,
	"custom_command" VARCHAR(255) NULL,
	"custom_commands" CLOB NULL,
	"hostname" VARCHAR(255) NULL,
	"message_id" VARCHAR(255) NULL,
	"name" VARCHAR(255) NULL,
	"service_group" VARCHAR(255) NULL,
	"service_name" VARCHAR(255) NULL,
	"service_user" VARCHAR(255) NULL,
	"stack_name" VARCHAR(255) NULL,
	"stack_version" VARCHAR(255) NULL,
	"state" VARCHAR(255) NULL,
	"cluster_id" BIGINT NULL,
	"job_id" BIGINT NULL,
	"stage_id" BIGINT NULL,
	CONSTRAINT CONS134218879 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555697 ON "bigtop_manager"."task" ("id");
CREATE INDEX "idx_task_cluster_id" ON "bigtop_manager"."task" ("cluster_id");
CREATE INDEX "idx_task_job_id" ON "bigtop_manager"."task" ("job_id");
CREATE INDEX "idx_task_stage_id" ON "bigtop_manager"."task" ("stage_id");


-- "bigtop_manager"."user" definition

CREATE TABLE "bigtop_manager"."user" (
	"id" BIGINT NOT NULL,
	"create_by" BIGINT NULL,
	"create_time" DATETIME NULL,
	"update_by" BIGINT NULL,
	"update_time" DATETIME NULL,
	"nickname" VARCHAR(255) NULL,
	"password" VARCHAR(255) NULL,
	"status" BIT NULL,
	"username" VARCHAR(255) NULL,
	CONSTRAINT CONS134218880 PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX INDEX33555699 ON "bigtop_manager"."user" ("id");