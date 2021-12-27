CREATE DATABASE IF NOT EXISTS whiteboard;
use whiteboard;
DROP TABLE IF EXISTS `users`;
                         CREATE TABLE `users`
                         (
                             `id`             int(10) unsigned NOT NULL AUTO_INCREMENT,
                             `userId`         varchar(11)       NOT NULL DEFAULT '',
                             `region`         varchar(5)       NOT NULL,
                             `phone`          varchar(11)      NOT NULL,
                             `nickname`       varchar(32)      NOT NULL,
                             `portraitUri`    varchar(256)     NOT NULL DEFAULT '',
                             `passwordHash`   char(40)         NOT NULL,
                             `passwordSalt`   char(4)          NOT NULL,
                             `rongCloudToken` varchar(256)     NOT NULL DEFAULT '',
                             `gender`         varchar(32)      NOT NULL DEFAULT 'male',
                             `stAccount`      varchar(32)      NOT NULL DEFAULT '',
                             `phoneVerify`    int(10) unsigned NOT NULL DEFAULT '1',
                             `stSearchVerify` int(10) unsigned NOT NULL DEFAULT '1',
                             `friVerify`      int(10) unsigned NOT NULL DEFAULT '1',
                             `groupVerify`    int(10) unsigned NOT NULL DEFAULT '1',
                             `pokeStatus`     int(10) unsigned NOT NULL DEFAULT '1',
                             `groupCount`     int(10) unsigned NOT NULL DEFAULT '0',
                             `timestamp`      bigint(20)       NOT NULL DEFAULT '0',
                             `createdAt`      datetime         NOT NULL,
                             `updatedAt`      datetime         NOT NULL,
                             `deletedAt`      datetime                  DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `users_region_phone` (`region`, `phone`)
                         ) ENGINE = InnoDB
                           DEFAULT CHARSET = utf8;

                         DROP TABLE IF EXISTS `login_logs`;
                         CREATE TABLE `login_logs`
                         (
                             `id`           int(10) unsigned NOT NULL AUTO_INCREMENT,
                             `userId`       int(10) unsigned NOT NULL,
                             `ipAddress`    int(10) unsigned NOT NULL,
                             `os`           varchar(64)      NOT NULL,
                             `osVersion`    varchar(64)      NOT NULL,
                             `carrier`      varchar(64)      NOT NULL,
                             `device`       varchar(64)  DEFAULT NULL,
                             `manufacturer` varchar(64)  DEFAULT NULL,
                             `userAgent`    varchar(256) DEFAULT NULL,
                             `createdAt`    datetime         NOT NULL,
                             PRIMARY KEY (`id`)
                         ) ENGINE = InnoDB
                           DEFAULT CHARSET = utf8;

                         DROP TABLE IF EXISTS `verification_codes`;
                         CREATE TABLE `verification_codes`
                         (
                             `id`        int(10) unsigned                             NOT NULL AUTO_INCREMENT,
                             `region`    varchar(5)                                   NOT NULL,
                             `phone`     varchar(11)                                  NOT NULL,
                             `sessionId` varchar(32)                                  NOT NULL,
                             `token`     char(36) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
                             `createdAt` datetime                                     NOT NULL,
                             `updatedAt` datetime                                     NOT NULL,
                             `isUse`     int(1) NOT NULL DEFAULT '0',
                             PRIMARY KEY (`id`, `region`, `phone`),
                             UNIQUE KEY `token` (`token`),
                             UNIQUE KEY `verification_codes_region_phone` (`region`, `phone`)
                         ) ENGINE = InnoDB
                           DEFAULT CHARSET = utf8;


                         DROP TABLE IF EXISTS `verification_violations`;
                         CREATE TABLE `verification_violations`
                         (
                             `ip`    varchar(64) NOT NULL,
                             `time`  datetime    NOT NULL,
                             `count` int(10) unsigned DEFAULT NULL,
                             PRIMARY KEY (`ip`)
                         ) ENGINE = InnoDB
                           DEFAULT CHARSET = utf8;


