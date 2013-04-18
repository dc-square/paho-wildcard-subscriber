DROP TABLE IF EXISTS `Messages`;

CREATE TABLE `Messages` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `message` longblob NOT NULL,
  `topic` text NOT NULL,
  `quality_of_service` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

