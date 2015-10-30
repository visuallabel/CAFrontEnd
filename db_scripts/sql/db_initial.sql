--
-- Copyright 2015 Tampere University of Technology, Pori Department
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


-- Dumping structure for table ca_frontend.users
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_external_ids
CREATE TABLE IF NOT EXISTS `users_external_ids` (
  `user_id` bigint(20) NOT NULL,
  `user_service_id` int(11) NOT NULL,
  `external_id` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `user_id_google_id_UNIQUE` (`external_id`,`user_service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_facebook
CREATE TABLE IF NOT EXISTS `users_facebook` (
  `user_id` bigint(20) NOT NULL,
  `access_token` varchar(255) DEFAULT NULL,
  `access_token_expires` bigint(20) DEFAULT NULL,
  `nonce` varchar(255) DEFAULT NULL,
  `nonce_expires` bigint(20) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `nonce_UNIQUE` (`nonce`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_google
CREATE TABLE IF NOT EXISTS `users_google` (
  `user_id` bigint(20) NOT NULL,
  `access_token` varchar(255) DEFAULT NULL,
  `access_token_expires` bigint(20) DEFAULT NULL,
  `refresh_token` varchar(255) DEFAULT NULL,
  `token_type` varchar(45) DEFAULT NULL,
  `nonce` varchar(255) DEFAULT NULL,
  `nonce_expires` bigint(20) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `nonce_UNIQUE` (`nonce`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_roles
CREATE TABLE IF NOT EXISTS `users_roles` (
  `user_id` bigint(20) NOT NULL,
  `role` varchar(45) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  KEY `user_id_INDEX` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_twitter
CREATE TABLE IF NOT EXISTS `users_twitter` (
  `user_id` bigint(20) NOT NULL,
  `access_token` varchar(255) NOT NULL,
  `access_token_secret` varchar(255) NOT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `access_token_UNIQUE` (`access_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table ca_frontend.users_twitter_request_tokens
CREATE TABLE IF NOT EXISTS `users_twitter_request_tokens` (
  `user_id` bigint(20) DEFAULT NULL,
  `request_token` varchar(255) NOT NULL,
  `request_token_secret` varchar(255) NOT NULL,
  `redirect_uri` varchar(2000) DEFAULT NULL,
  `row_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `row_created` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  UNIQUE KEY `request_token_UNIQUE` (`request_token`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
