-- innospots_local.app_definition definition

CREATE TABLE `app_definition` (
  `app_key` varchar(16) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `icon` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `resources` mediumtext,
  `settings` text,
  `status` varchar(32) DEFAULT NULL,
  `app_path` varchar(32) DEFAULT NULL,
  `template_key` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.app_template definition

CREATE TABLE `app_template` (
  `template_key` varchar(16) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `icon` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `resources` mediumtext,
  `settings` text,
  `status` varchar(32) DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL,
  `publish_time` date DEFAULT NULL,
  PRIMARY KEY (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.dgm_page definition

CREATE TABLE `dgm_page` (
  `page_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `board_ext_config` text,
  `category_id` int(11) DEFAULT NULL,
  `config` text,
  `description` varchar(128) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `page_type` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `sub_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`page_id`)
) ENGINE=InnoDB AUTO_INCREMENT=422 DEFAULT CHARSET=utf8mb4;


-- innospots_local.dgm_widget definition

CREATE TABLE `dgm_widget` (
  `widget_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `config` text,
  `description` varchar(128) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `page_id` int(11) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `relations` varchar(2048) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `view_codes` varchar(2048) DEFAULT NULL,
  `view_ids` varchar(2048) DEFAULT NULL,
  `widget_key` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`widget_id`),
  KEY `idx_page_wdt` (`page_id`)
) ENGINE=InnoDB AUTO_INCREMENT=335 DEFAULT CHARSET=utf8mb4;


-- innospots_local.dgm_workspace definition

CREATE TABLE `dgm_workspace` (
  `workspace_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `page_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`workspace_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4;


-- innospots_local.ds_credential_info definition

CREATE TABLE `ds_credential_info` (
  `credential_key` varchar(8) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `credential_type_code` varchar(32) DEFAULT NULL,
  `encrypt_form_values` varchar(2048) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `props` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`credential_key`),
  KEY `credential_type` (`credential_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.ds_credential_type definition

CREATE TABLE `ds_credential_type` (
  `type_code` varchar(32) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `auth_option` varchar(32) DEFAULT NULL,
  `connector_name` varchar(32) DEFAULT NULL,
  `form_values` varchar(2048) DEFAULT NULL,
  `icon` varchar(128) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `props` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`type_code`),
  KEY `connector_name` (`connector_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.ds_schema_field definition

CREATE TABLE `ds_schema_field` (
  `field_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `code` varchar(64) DEFAULT NULL,
  `comment` varchar(128) DEFAULT NULL,
  `config` varchar(2048) DEFAULT NULL,
  `default_value` varchar(32) DEFAULT NULL,
  `field_scope` varchar(16) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `pkey` bit(1) DEFAULT NULL,
  `registry_code` varchar(64) DEFAULT NULL,
  `registry_id` varchar(16) DEFAULT NULL,
  `value_type` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`field_id`),
  KEY `idx_registry_fld` (`registry_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1084 DEFAULT CHARSET=utf8mb4;


-- innospots_local.ds_schema_registry definition

CREATE TABLE `ds_schema_registry` (
  `registry_id` varchar(16) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `app_key` varchar(16) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `code` varchar(64) DEFAULT NULL,
  `configs` text,
  `credential_key` varchar(8) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `registry_type` varchar(16) DEFAULT NULL,
  `scope` varchar(16) DEFAULT NULL,
  `script` text,
  PRIMARY KEY (`registry_id`),
  KEY `idx_credential_key` (`credential_key`),
  KEY `idx_app_key` (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.ext_definition definition

CREATE TABLE `ext_definition` (
  `ext_definition_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `author` varchar(32) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `ext_icon` varchar(128) DEFAULT NULL,
  `ext_key` varchar(64) DEFAULT NULL,
  `ext_name` varchar(128) DEFAULT NULL,
  `ext_version` varchar(16) DEFAULT NULL,
  `extension_source` varchar(16) DEFAULT NULL,
  `extension_status` varchar(16) DEFAULT NULL,
  `kernel_version` varchar(16) DEFAULT NULL,
  `last_app_version` varchar(16) DEFAULT NULL,
  `last_description` varchar(256) DEFAULT NULL,
  `last_publish_time` date DEFAULT NULL,
  `publish_time` date DEFAULT NULL,
  `signature` varchar(64) DEFAULT NULL,
  `vendor` varchar(32) DEFAULT NULL,
  `zip_file_names` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`ext_definition_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;


-- innospots_local.ext_installment definition

CREATE TABLE `ext_installment` (
  `ext_installment_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `ext_key` varchar(64) DEFAULT NULL,
  `extension_status` varchar(16) DEFAULT NULL,
  `install_person` varchar(32) DEFAULT NULL,
  `install_time` datetime(6) DEFAULT NULL,
  `install_version` varchar(16) DEFAULT NULL,
  `license_key` varchar(256) DEFAULT NULL,
  `license_time` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`ext_installment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_execution definition

CREATE TABLE `flow_execution` (
  `flow_execution_id` varchar(64) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `channel` varchar(16) DEFAULT NULL,
  `current_node_keys` varchar(64) DEFAULT NULL,
  `data_time` varchar(32) DEFAULT NULL,
  `end_time` varchar(32) DEFAULT NULL,
  `exec_mode` varchar(16) DEFAULT NULL,
  `execution_uri` varchar(128) DEFAULT NULL,
  `flow_instance_id` bigint(20) DEFAULT NULL,
  `hit_node_number` int(11) DEFAULT NULL,
  `location` varchar(32) DEFAULT NULL,
  `message` varchar(2048) DEFAULT NULL,
  `parent_flow_execution_id` varchar(64) DEFAULT NULL,
  `result` varchar(2048) DEFAULT NULL,
  `result_code` varchar(8) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `skip_flow_execution` bit(1) DEFAULT NULL,
  `skip_node_execution` bit(1) DEFAULT NULL,
  `source` varchar(128) DEFAULT NULL,
  `start_time` varchar(32) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `uuid` varchar(32) DEFAULT NULL,
  `uuid_type` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`flow_execution_id`),
  KEY `idx_flow_rev` (`flow_instance_id`,`revision`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_execution_context definition

CREATE TABLE `flow_execution_context` (
  `execution_id` varchar(64) NOT NULL,
  `context_type` varchar(8) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `flow_instance_id` bigint(20) DEFAULT NULL,
  `input_size` int(11) DEFAULT NULL,
  `inputs` longtext,
  `node_paths` text,
  `output_size` int(11) DEFAULT NULL,
  `outputs` longtext,
  PRIMARY KEY (`execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_execution_node definition

CREATE TABLE `flow_execution_node` (
  `node_execution_id` varchar(64) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `end_time` varchar(32) DEFAULT NULL,
  `exec_mode` varchar(16) DEFAULT NULL,
  `flow_execution_id` varchar(64) DEFAULT NULL,
  `flow_instance_id` bigint(20) DEFAULT NULL,
  `flow_start_time` varchar(255) DEFAULT NULL,
  `message` text,
  `next` bit(1) DEFAULT NULL,
  `next_node_keys` varchar(256) DEFAULT NULL,
  `node_code` varchar(32) DEFAULT NULL,
  `node_key` varchar(16) DEFAULT NULL,
  `node_name` varchar(32) DEFAULT NULL,
  `previous_node_keys` varchar(256) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `skip_node_execution` bit(1) DEFAULT NULL,
  `start_time` varchar(32) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`node_execution_id`),
  KEY `idx_flow_rev_ext_node` (`flow_instance_id`,`revision`),
  KEY `idx_flow_ext_node` (`flow_execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_execution_node_scheduled definition

CREATE TABLE `flow_execution_node_scheduled` (
  `node_execution_id` varchar(64) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `flow_execution_id` varchar(64) DEFAULT NULL,
  `flow_instance_id` bigint(20) DEFAULT NULL,
  `message` varchar(1024) DEFAULT NULL,
  `node_key` varchar(16) DEFAULT NULL,
  `scheduled_time` datetime(6) DEFAULT NULL,
  `server_key` varchar(64) DEFAULT NULL,
  `sharding_key` int(11) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`node_execution_id`),
  KEY `idx_server_sharding` (`scheduled_time`,`status`,`server_key`,`sharding_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_instance definition

CREATE TABLE `flow_instance` (
  `workflow_instance_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `datasource_code` varchar(8) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `flow_key` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `online_time` datetime(6) DEFAULT NULL,
  `page_id` int(11) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `table_name` varchar(32) DEFAULT NULL,
  `template_code` varchar(16) DEFAULT NULL,
  `trigger_code` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`workflow_instance_id`),
  KEY `idx_status_revision` (`status`,`updated_time`,`revision`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_instance_cache definition

CREATE TABLE `flow_instance_cache` (
  `workflow_instance_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `flow_instance` mediumtext,
  PRIMARY KEY (`workflow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_instance_edge definition

CREATE TABLE `flow_instance_edge` (
  `edge_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `data` text,
  `end_point` varchar(128) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `source` varchar(64) DEFAULT NULL,
  `source_anchor` varchar(64) DEFAULT NULL,
  `start_point` varchar(128) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `style` varchar(256) DEFAULT NULL,
  `target` varchar(64) DEFAULT NULL,
  `target_anchor` varchar(64) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`edge_id`),
  KEY `idx_flow_rev_edge` (`workflow_instance_id`,`revision`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_instance_node definition

CREATE TABLE `flow_instance_node` (
  `node_instance_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `action` text,
  `action_script_type` varchar(16) DEFAULT NULL,
  `continue_on_fail` bit(1) DEFAULT NULL,
  `data` text,
  `description` varchar(256) DEFAULT NULL,
  `display_name` varchar(32) DEFAULT NULL,
  `failure_branch` bit(1) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `input_fields` varchar(1024) DEFAULT NULL,
  `max_tries` int(11) DEFAULT NULL,
  `methods` text,
  `name` varchar(32) DEFAULT NULL,
  `node_definition_id` int(11) DEFAULT NULL,
  `node_key` varchar(64) DEFAULT NULL,
  `node_type` varchar(128) DEFAULT NULL,
  `output_fields` text,
  `pause_flag` bit(1) DEFAULT NULL,
  `ports` varchar(1024) DEFAULT NULL,
  `retry_on_fail` bit(1) DEFAULT NULL,
  `retry_wait_time_mills` int(11) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  PRIMARY KEY (`node_instance_id`),
  KEY `idx_flow_rev_node` (`workflow_instance_id`,`revision`)
) ENGINE=InnoDB AUTO_INCREMENT=218 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_instance_revision definition

CREATE TABLE `flow_instance_revision` (
  `flow_revision_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `node_number` bigint(20) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`flow_revision_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_node_definition definition

CREATE TABLE `flow_node_definition` (
  `node_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `code` varchar(32) DEFAULT NULL,
  `credential_type_code` varchar(32) DEFAULT NULL,
  `deletable` bit(1) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `flow_code` varchar(32) DEFAULT NULL,
  `icon` varchar(64) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `node_type` varchar(128) DEFAULT NULL,
  `primitive` varchar(16) DEFAULT NULL,
  `resources` mediumtext,
  `scripts` mediumtext,
  `settings` text,
  `status` varchar(32) DEFAULT NULL,
  `used` bit(1) DEFAULT NULL,
  `vendor` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`node_id`),
  KEY `idx_flow_code` (`flow_code`),
  KEY `idx_node_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_node_group definition

CREATE TABLE `flow_node_group` (
  `node_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `code` varchar(64) DEFAULT NULL,
  `flow_tpl_id` int(11) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `position` int(11) DEFAULT NULL,
  `scopes` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`node_group_id`),
  KEY `idx_flow_tpl_group` (`flow_tpl_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_node_group_node definition

CREATE TABLE `flow_node_group_node` (
  `node_group_node_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `flow_tpl_id` int(11) DEFAULT NULL,
  `node_group_id` int(11) DEFAULT NULL,
  `node_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`node_group_node_id`),
  KEY `idx_flow_tpl_node` (`flow_tpl_id`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4;


-- innospots_local.flow_template definition

CREATE TABLE `flow_template` (
  `flow_tpl_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `tpl_code` varchar(32) DEFAULT NULL,
  `tpl_name` varchar(32) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`flow_tpl_id`),
  KEY `idx_tpl_code` (`tpl_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;


-- innospots_local.i18n_currency definition

CREATE TABLE `i18n_currency` (
  `currency_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `code` varchar(16) DEFAULT NULL,
  `decimal_digits` int(11) DEFAULT NULL,
  `left_sign` varchar(4) DEFAULT NULL,
  `name` varchar(16) DEFAULT NULL,
  `right_sign` varchar(4) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`currency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4;


-- innospots_local.i18n_dictionary definition

CREATE TABLE `i18n_dictionary` (
  `dictionary_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `app` varchar(32) DEFAULT NULL,
  `code` varchar(64) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `module` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`dictionary_id`),
  KEY `idx_dict_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2488 DEFAULT CHARSET=utf8mb4;


-- innospots_local.i18n_language definition

CREATE TABLE `i18n_language` (
  `language_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `currency_id` int(11) DEFAULT NULL,
  `date_format` varchar(16) DEFAULT NULL,
  `decimal_separator` varchar(4) DEFAULT NULL,
  `default_lan` bit(1) DEFAULT NULL,
  `icon` varchar(16) DEFAULT NULL,
  `locale` varchar(16) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `thousand_separator` varchar(4) DEFAULT NULL,
  `time_format` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`language_id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4;


-- innospots_local.i18n_trans_message definition

CREATE TABLE `i18n_trans_message` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `dictionary_id` int(11) DEFAULT NULL,
  `locale` varchar(16) DEFAULT NULL,
  `message` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4911 DEFAULT CHARSET=utf8mb4;


-- innospots_local.schedule_job_execution definition

CREATE TABLE `schedule_job_execution` (
  `execution_id` varchar(32) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `context` text,
  `detail_uri` varchar(128) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `execution_status` varchar(16) DEFAULT NULL,
  `ext_execution_id` varchar(32) DEFAULT NULL,
  `fail_count` bigint(20) DEFAULT NULL,
  `instance_key` varchar(64) DEFAULT NULL,
  `job_class` varchar(64) DEFAULT NULL,
  `job_key` varchar(16) DEFAULT NULL,
  `job_name` varchar(32) DEFAULT NULL,
  `job_type` varchar(16) DEFAULT NULL,
  `key_type` varchar(16) DEFAULT NULL,
  `message` text,
  `origin_execution_id` varchar(32) DEFAULT NULL,
  `output` mediumtext,
  `parent_execution_id` varchar(32) DEFAULT NULL,
  `percent` int(11) DEFAULT NULL,
  `resource_key` varchar(32) DEFAULT NULL,
  `scopes` varchar(32) DEFAULT NULL,
  `self_end_time` datetime(6) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `server_key` varchar(32) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `sub_job_count` bigint(20) DEFAULT NULL,
  `success_count` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.schedule_job_info definition

CREATE TABLE `schedule_job_info` (
  `job_key` varchar(16) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `job_class` varchar(128) DEFAULT NULL,
  `job_name` varchar(32) DEFAULT NULL,
  `job_status` varchar(16) DEFAULT NULL,
  `job_type` varchar(16) DEFAULT NULL,
  `params` text,
  `resource_key` varchar(32) DEFAULT NULL,
  `schedule_mode` varchar(16) DEFAULT NULL,
  `scopes` varchar(32) DEFAULT NULL,
  `splitter` varchar(128) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `sub_job_count` int(11) DEFAULT NULL,
  `time_config` varchar(2056) DEFAULT NULL,
  PRIMARY KEY (`job_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.schedule_ready_queue definition

CREATE TABLE `schedule_ready_queue` (
  `job_ready_key` varchar(32) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `context` text,
  `ext_execution_id` varchar(32) DEFAULT NULL,
  `group_key` varchar(8) DEFAULT NULL,
  `instance_key` varchar(64) DEFAULT NULL,
  `job_class` varchar(64) DEFAULT NULL,
  `job_key` varchar(16) DEFAULT NULL,
  `job_name` varchar(32) DEFAULT NULL,
  `job_type` varchar(16) DEFAULT NULL,
  `key_type` varchar(16) DEFAULT NULL,
  `message_status` varchar(16) DEFAULT NULL,
  `origin_execution_id` varchar(32) DEFAULT NULL,
  `parent_execution_id` varchar(32) DEFAULT NULL,
  `resource_key` varchar(32) DEFAULT NULL,
  `scopes` varchar(32) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `server_key` varchar(32) DEFAULT NULL,
  `sub_job_count` int(11) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`job_ready_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_avatar_resource definition

CREATE TABLE `sys_avatar_resource` (
  `avatar_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `image_base64` mediumtext,
  `image_sort` int(11) DEFAULT NULL,
  `image_type` varchar(12) DEFAULT NULL,
  `resource_id` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`avatar_id`)
) ENGINE=InnoDB AUTO_INCREMENT=131 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_category definition

CREATE TABLE `sys_category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `category_code` varchar(128) DEFAULT NULL,
  `category_name` varchar(128) DEFAULT NULL,
  `category_type` varchar(16) DEFAULT NULL,
  `orders` int(11) DEFAULT NULL,
  `parent_category_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_config definition

CREATE TABLE `sys_config` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `config_code` varchar(32) DEFAULT NULL,
  `config_group` varchar(8) DEFAULT NULL,
  `config_name` varchar(32) DEFAULT NULL,
  `config_value` varchar(1024) DEFAULT NULL,
  `encrypt_type` varchar(8) DEFAULT NULL,
  `place_holder` varchar(32) DEFAULT NULL,
  `resource_id` int(11) DEFAULT NULL,
  `resource_type` varchar(16) DEFAULT NULL,
  `tips` varchar(32) DEFAULT NULL,
  `value_type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_function definition

CREATE TABLE `sys_function` (
  `function_id` int(11) NOT NULL,
  `cate_name` varchar(16) DEFAULT NULL,
  `cate_type` varchar(16) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `expression` varchar(2048) DEFAULT NULL,
  `function_type` varchar(16) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `param_types` varchar(128) DEFAULT NULL,
  `return_type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`function_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_login_log definition

CREATE TABLE `sys_login_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `browser` varchar(32) DEFAULT NULL,
  `city` varchar(32) DEFAULT NULL,
  `detail` varchar(128) DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `login_time` datetime(6) DEFAULT NULL,
  `os` varchar(32) DEFAULT NULL,
  `province` varchar(32) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `user_avatar` varchar(64) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `user_name` varchar(32) DEFAULT NULL,
  `user_role_name` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_menu_resource definition

CREATE TABLE `sys_menu_resource` (
  `resource_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `app_key` varchar(64) DEFAULT NULL,
  `app_name` varchar(128) DEFAULT NULL,
  `i18n_names` text,
  `icon` varchar(32) DEFAULT NULL,
  `item_key` varchar(64) DEFAULT NULL,
  `item_type` varchar(16) DEFAULT NULL,
  `load_mode` varchar(16) DEFAULT NULL,
  `menu_group` varchar(16) DEFAULT NULL,
  `method` varchar(32) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `open_mode` varchar(16) DEFAULT NULL,
  `orders` int(11) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `parent_item_keys` varchar(128) DEFAULT NULL,
  `show_menu` bit(1) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `uri` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=373 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_notification_channel definition

CREATE TABLE `sys_notification_channel` (
  `channel_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `channel_name` varchar(16) DEFAULT NULL,
  `channel_type` varchar(16) DEFAULT NULL,
  `credential_id` int(11) DEFAULT NULL,
  `params` varchar(256) DEFAULT NULL,
  `registry_id` varchar(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`channel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_notification_message definition

CREATE TABLE `sys_notification_message` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `channel_id` int(11) DEFAULT NULL,
  `channel_name` varchar(16) DEFAULT NULL,
  `event_code` varchar(16) DEFAULT NULL,
  `event_name` varchar(64) DEFAULT NULL,
  `ext_name` varchar(32) DEFAULT NULL,
  `message` varchar(2048) DEFAULT NULL,
  `module` varchar(16) DEFAULT NULL,
  `read_flag` bit(1) DEFAULT NULL,
  `read_time` datetime(6) DEFAULT NULL,
  `receive_user` varchar(32) DEFAULT NULL,
  `receive_user_id` int(11) DEFAULT NULL,
  `record_time` datetime(6) DEFAULT NULL,
  `send_result` varchar(128) DEFAULT NULL,
  `send_success` bit(1) DEFAULT NULL,
  `title` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3148 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_notification_setting definition

CREATE TABLE `sys_notification_setting` (
  `setting_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `channels` varchar(128) DEFAULT NULL,
  `event_code` varchar(16) DEFAULT NULL,
  `event_name` varchar(64) DEFAULT NULL,
  `ext_key` varchar(32) DEFAULT NULL,
  `module_key` varchar(32) DEFAULT NULL,
  `role_groups` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`setting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_operation_log definition

CREATE TABLE `sys_operation_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `browser` varchar(64) DEFAULT NULL,
  `city` varchar(32) DEFAULT NULL,
  `detail` varchar(256) DEFAULT NULL,
  `device` varchar(32) DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `language` varchar(128) DEFAULT NULL,
  `location` varchar(32) DEFAULT NULL,
  `manufacturer` varchar(32) DEFAULT NULL,
  `module` varchar(64) DEFAULT NULL,
  `operate_time` datetime(6) DEFAULT NULL,
  `operate_type` varchar(16) DEFAULT NULL,
  `os_system` varchar(32) DEFAULT NULL,
  `province` varchar(32) DEFAULT NULL,
  `request_path` varchar(128) DEFAULT NULL,
  `resolution` varchar(64) DEFAULT NULL,
  `resource_id` varchar(64) DEFAULT NULL,
  `resource_name` varchar(64) DEFAULT NULL,
  `resource_type` varchar(32) DEFAULT NULL,
  `roles` varchar(128) DEFAULT NULL,
  `user_avatar` varchar(64) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `username` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_role definition

CREATE TABLE `sys_role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `admin` bit(1) DEFAULT NULL,
  `role_code` varchar(32) DEFAULT NULL,
  `role_name` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_role_resource definition

CREATE TABLE `sys_role_resource` (
  `role_resource_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `item_key` varchar(64) DEFAULT NULL,
  `resource_type` varchar(16) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`role_resource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=762 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_service_registry definition

CREATE TABLE `sys_service_registry` (
  `server_id` bigint(20) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `domain_ip` varchar(16) DEFAULT NULL,
  `group_name` varchar(16) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `service_name` varchar(128) DEFAULT NULL,
  `service_role` varchar(255) NOT NULL,
  `service_status` varchar(255) NOT NULL,
  `service_type` varchar(255) NOT NULL,
  `tags` varchar(32) DEFAULT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`server_id`),
  UNIQUE KEY `idx_ip_port` (`domain_ip`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_task_execution definition

CREATE TABLE `sys_task_execution` (
  `task_execution_id` varchar(64) NOT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `app_key` varchar(32) DEFAULT NULL,
  `app_name` varchar(32) DEFAULT NULL,
  `detail_url` varchar(255) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `execution_status` varchar(16) DEFAULT NULL,
  `extension_key` varchar(64) DEFAULT NULL,
  `extension_type` varchar(16) DEFAULT NULL,
  `message` text,
  `param_context` text,
  `percent` int(11) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `task_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`task_execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_temp_cache definition

CREATE TABLE `sys_temp_cache` (
  `cache_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `cache_key` varchar(255) DEFAULT NULL,
  `cache_value` text,
  PRIMARY KEY (`cache_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_todo_task definition

CREATE TABLE `sys_todo_task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `description` text,
  `end_date` date DEFAULT NULL,
  `principal_user_id` int(11) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `task_name` varchar(128) DEFAULT NULL,
  `task_priority` varchar(16) DEFAULT NULL,
  `task_status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_todo_task_comment definition

CREATE TABLE `sys_todo_task_comment` (
  `comment_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `content` text,
  `task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_todo_task_tag definition

CREATE TABLE `sys_todo_task_tag` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `tag_name` varchar(32) DEFAULT NULL,
  `task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_user definition

CREATE TABLE `sys_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `avatar_key` varchar(64) DEFAULT NULL,
  `department` varchar(128) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `last_access_time` datetime(6) DEFAULT NULL,
  `last_org_id` int(11) DEFAULT NULL,
  `last_project_id` int(11) DEFAULT NULL,
  `login_times` int(11) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `on_off` varchar(16) DEFAULT NULL,
  `password` varchar(128) DEFAULT NULL,
  `real_name` varchar(128) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `user_name` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;


-- innospots_local.sys_user_role definition

CREATE TABLE `sys_user_role` (
  `user_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(32) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime(6) DEFAULT NULL,
  `project_id` int(11) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4;