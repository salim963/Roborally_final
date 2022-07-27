
CREATE SCHEMA IF NOT EXISTS `roborally` DEFAULT CHARACTER SET utf8 ;;


CREATE TABLE IF NOT EXISTS `game` (
                                      `id` INT NOT NULL AUTO_INCREMENT,
                                      `name` VARCHAR(100) NOT NULL,
    `phase` ENUM('INITIALISATION', 'PROGRAMMING', 'ACTIVATION', 'PLAYER_INTERACTION') NOT NULL,
    `step` INT NOT NULL,
    `board` JSON NOT NULL,
    PRIMARY KEY (`id`)
    )
    ENGINE = InnoDB;;

CREATE TABLE IF NOT EXISTS player (
                                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      gameId INT NOT NULL,
                                      name VARCHAR(50) NOT NULL,
    posX INT NOT NULL,
    posY INT NOT NULL,
    heading ENUM('NORTH', 'EAST', 'SOUTH', 'WEST') NOT NULL,
    colour VARCHAR(50) NOT NULL,
    `order` INT NOT NULL,
    FOREIGN KEY `fk_player_gameId_game_id` (gameId) REFERENCES game(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    INDEX `ix_gameId` (gameId ASC)
    )
    ENGINE = InnoDB;;

CREATE TABLE IF NOT EXISTS gameState (
                                         gameId INT NOT NULL PRIMARY KEY,
                                         currentPlayerId INT NOT NULL,
                                         FOREIGN KEY `fk_gameState_game_id` (gameId) REFERENCES game(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    FOREIGN KEY `fk_gameState_player_id` (currentPlayerId) REFERENCES player(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB;;

CREATE TABLE IF NOT EXISTS playerHand (
                                          id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                          playerId INT NOT NULL,
                                          card ENUM('FORWARD', 'RIGHT', 'LEFT', 'FAST_FORWARD', 'OPTION_LEFT_RIGHT', 'Move_Back', 'Move_3', 'U_Turn') NOT NULL,
    FOREIGN KEY `fk_playerHand_player_id` (playerId) REFERENCES player(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    INDEX `ix_playerId` (playerId ASC)
    )
    ENGINE = InnoDB;;

CREATE TABLE IF NOT EXISTS playerRegister (
                                              id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                              playerId INT NOT NULL,
                                              card ENUM('FORWARD', 'RIGHT', 'LEFT', 'FAST_FORWARD', 'OPTION_LEFT_RIGHT', 'Move_Back', 'Move_3', 'U_Turn') NULL,
    FOREIGN KEY `fk_playerRegister_player_id` (playerId) REFERENCES player(id),
    INDEX `ix_playerId` (playerId ASC)
    )
    ENGINE = InnoDB;;
