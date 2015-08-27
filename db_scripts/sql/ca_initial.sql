--
-- Copyright 2015 Tampere University of Technology, Pori Unit
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--   http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- --------------------------------------------------------
-- Host:                         otula.pori.tut.fi
-- Server version:               5.5.38-0+wheezy1 - (Debian)
-- Server OS:                    debian-linux-gnu
-- HeidiSQL Version:             8.3.0.4694
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for ca_frontend
CREATE DATABASE IF NOT EXISTS `ca_frontend` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `ca_frontend`;


-- Dumping structure for table ca_frontend.ca_backends
CREATE TABLE IF NOT EXISTS `ca_backends` (
  `backend_id` int(11) NOT NULL AUTO_INCREMENT,
  `analysis_uri` varchar(2000) NOT NULL,
  `enabled` tinyint(4) NOT NULL,
  `description` varchar(1024) NOT NULL,
  `default_task_datagroups` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`backend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_backend_capabilities
CREATE TABLE IF NOT EXISTS `ca_backend_capabilities` (
  `backend_id` int(11) NOT NULL,
  `capability` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `backend_id_capability_UNIQUE` (`backend_id`,`capability`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_facebook_entries
CREATE TABLE IF NOT EXISTS `ca_facebook_entries` (
  `guid` varchar(255) NOT NULL,
  `static_url` varchar(2000) NOT NULL,
  `object_id` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`guid`),
  UNIQUE KEY `object_id_UNIQUE` (`object_id`),
  KEY `user_id_INDEX` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_photo_friendly_keywords
CREATE TABLE IF NOT EXISTS `ca_photo_friendly_keywords` (
  `value` varchar(255) NOT NULL,
  `friendly_value` varchar(255) DEFAULT NULL,
  `backend_id` int(11) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `value_INDEX` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_picasa_entries
CREATE TABLE IF NOT EXISTS `ca_picasa_entries` (
  `guid` varchar(255) NOT NULL,
  `album_id` varchar(255) NOT NULL,
  `photo_id` varchar(255) NOT NULL,
  `google_user_id` varchar(255) NOT NULL,
  `static_url` varchar(2000) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks
CREATE TABLE IF NOT EXISTS `ca_tasks` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_type` int(11) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`task_id`),
  KEY `task_type_INDEX` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks_backends
CREATE TABLE IF NOT EXISTS `ca_tasks_backends` (
  `task_id` bigint(20) NOT NULL,
  `backend_id` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `message` varchar(1024) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_backend_id_UNIQUE` (`task_id`,`backend_id`),
  KEY `status_INDEX` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks_guids
CREATE TABLE IF NOT EXISTS `ca_tasks_guids` (
  `task_id` bigint(20) NOT NULL,
  `guid` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_guid_UNIQUE` (`task_id`,`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks_guids_status
CREATE TABLE IF NOT EXISTS `ca_tasks_guids_status` (
  `backend_id` int(11) NOT NULL,
  `guid` varchar(255) NOT NULL,
  `task_id` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `message` varchar(1024) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `backend_id_guid_unique` (`backend_id`,`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks_metadata
CREATE TABLE IF NOT EXISTS `ca_tasks_metadata` (
  `task_id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(512) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_name_UNIQUE` (`task_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_tasks_media_objects
CREATE TABLE IF NOT EXISTS `ca_tasks_media_objects` (
  `task_id` bigint(20) NOT NULL,
  `guid` varchar(255) DEFAULT NULL,
  `media_object_id` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `task_id_guid_media_object_id_UNIQUE` (`task_id`,`guid`,`media_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_twitter_entries
CREATE TABLE IF NOT EXISTS `ca_twitter_entries` (
  `guid` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `entity_url` varchar(2000) NOT NULL,
  `entity_id` varchar(255) NOT NULL,
  `screen_name` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`guid`),
  UNIQUE KEY `entity_id_user_id_UNIQUE` (`user_id`,`entity_id`),
  KEY `user_id_INDEX` (`user_id`),
  KEY `screen_name_INDEX` (`screen_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_url_storage_entries
CREATE TABLE IF NOT EXISTS `ca_url_storage_entries` (
  `guid` varchar(255) NOT NULL,
  `media_type` int(11) NOT NULL,
  `url` varchar(2000) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`guid`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.ca_media_object_associations
CREATE TABLE IF NOT EXISTS `ca_media_object_associations` (
  `media_object_id` varchar(255) NOT NULL,
  `guid` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `media_object_id_INDEX` (`media_object_id`),
  KEY `guid_index` (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_ip
CREATE TABLE IF NOT EXISTS `users_ip` (
  `ip_address` varchar(40) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.fbj_weight_modifiers
CREATE TABLE IF NOT EXISTS `fbj_weight_modifiers` (
  `user_id` bigint(20) DEFAULT NULL,
  `value` int(11) NOT NULL,
  `modifier_type` int(11) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `user_id_modifier_type_UNIQUE` (`user_id`,`modifier_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.fs_files
CREATE TABLE IF NOT EXISTS `fs_files` (
  `file_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `original_name` varchar(255) DEFAULT NULL,
  `saved_name` varchar(55) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`file_id`),
  UNIQUE KEY `saved_name_UNIQUE` (`saved_name`),
  KEY `user_id_INDEX` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
