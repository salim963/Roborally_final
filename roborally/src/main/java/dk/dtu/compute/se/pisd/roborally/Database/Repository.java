/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.Database;

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
 class Repository implements IRepository {

    private static final String GAME_GAMEID = "id";
    private static final String GAME_NAME = "name";
    private static final String GAME_PHASE = "phase";
    private static final String GAME_STEP = "step";
    private static final String GAME_BOARD = "board";

    private static final String PLAYER_ID = "id";
    private static final String PLAYER_GAMEID = "gameId";
    private static final String PLAYER_ORDER = "order";
    private static final String PLAYER_NAME = "name";
    private static final String PLAYER_COLOUR = "colour";
    private static final String PLAYER_POSITION_X = "posX";
    private static final String PLAYER_POSITION_Y = "posY";
    private static final String PLAYER_HEADING = "heading";

    private static final String GAME_STATE_CURRENT_PLAYERID = "currentPlayerId";

    private static final String PLAYER_HAND_ID = "id";
    private static final String PLAYER_HAND_PLAYERID = "playerId";
    private static final String PLAYER_HAND_CARD = "card";

    private static final String PLAYER_REGISTER_ID = "id";
    private static final String PLAYER_REGISTER_PLAYERID = "playerId";
    private static final String PLAYER_REGISTER_CARD = "card";

    private Connector connector;

    Repository(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void createOrUpdateGameInDB(Board board, String boardMapJson) {
        if (board.getGameId() == null) {
            createGameInDB(board, boardMapJson);
        } else {
            //updateGameInDB(board);
            updateGameInDB(board);
        }
    }

    private boolean createGameInDB(Board game, String boardMapJson) {
        Connection connection = connector.getConnection();
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = getInsertGameStatementRGK();
            // TODO: the name should eventually set by the user
            //       for the game and should be then used
            //       game.getName();
            ps.setString(1, "Date: " + new Date()); // instead of name
            ps.setInt(2, game.getPhase().ordinal());
            ps.setInt(3, game.getStep());
            ps.setString(4, boardMapJson);

            // If you have a foreign key constraint for current players,
            // the check would need to be temporarily disabled, since
            // MySQL does not have a per transaction validation, but
            // validates on a per row basis.
            //Statement statement = connection.createStatement();
            //statement.execute("SET foreign_key_checks = 0");

            int affectedRows = ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (affectedRows == 1 && generatedKeys.next()) {
                game.setGameId(generatedKeys.getInt(1));
            }
            generatedKeys.close();

            // Enable foreign key constraint check again:
            // statement.execute("SET foreign_key_checks = 1");
            // statement.close();

            createPlayersInDB(game);
				/* TOODO this method needs to be implemented first
				createCardFieldsInDB(game);
				 */

            createGameState(game);
            createPlayersHandsInDB(game);
            createPlayerRegisterInDB(game);

            // since current player is a foreign key, it can oly be
            // inserted after the players are created, since MySQL does
            // not have a per transaction validation, but validates on
            // a per row basis.
            //ps = getSelectGameStatementU();
            //ps.setInt(1, game.getGameId());

//				ResultSet rs = ps.executeQuery();
//				if (rs.next()) {
//					rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
//					rs.updateRow();
//				} else {
//					// TODO error handling
//				}
//				rs.close();

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            // TODO error handling
            e.printStackTrace();
            System.err.println("Some DB error");

            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e1) {
                // TODO error handling
                e1.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public void updateGameInDB(Board game) {
        assert game.getGameId() != null;

        Connection connection = connector.getConnection();
        try {
            connection.setAutoCommit(false);

            updateGame(game);
            updatePlayers(game);
            updateGameState(game);
            updatePlayersHands(game);
            updatePlayersRegisters(game);

//            PreparedStatement ps = getSelectGameStatementU();
//            ps.setInt(1, game.getGameId());
//
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                //rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
//                rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
//                rs.updateInt(GAME_STEP, game.getStep());
//                rs.updateRow();
//            } else {
//                // TODO error handling
//            }
//            rs.close();

			/* TOODO this method needs to be implemented first
			updateCardFieldsInDB(game);
			*/

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            // TODO error handling
            e.printStackTrace();
            System.err.println("Some DB error");

            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e1) {
                // TODO error handling
                e1.printStackTrace();
            }
        }
    }

    /**
     * This method is used to load the saved game from the database.
     * @param id
     * @param roboRally
     * @return
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     * @author Salim Omar s193472@dtu.dk
     */
    //changed: implemented this load method with new roborally parameter
    @Override
    public Board loadGameFromDB(int id, RoboRally roboRally) {
        Board game;
        try {

            PreparedStatement ps = getSelectGameStatementU();
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            String boardJson = rs.getString(GAME_BOARD);
            BoardMap boardMap = roboRally.convertJsonToBoardMap(boardJson);
            game = new Board(boardMap.width, boardMap.height);
            game.setGameId(rs.getInt(GAME_GAMEID));
            game.setName(rs.getString(GAME_NAME));
            game.setPhase(Phase.valueOf(rs.getString(GAME_PHASE)));
            game.setStep(rs.getInt(GAME_STEP));
            game.setBoardMap(boardMap);

            rs.close();

            loadPlayersFromDB(game);
            loadPlayerHandCardsFromDb(game);
            loadPlayerRegisterCardsFromDb(game);

            return game;
        } catch (SQLException e) {
            // TODO error handling
            e.printStackTrace();
            System.err.println("Some DB error");
        }
        return null;
    }

    @Override
    public List<GameInDB> getGames() {
        // TODO when there many games in the DB, fetching all available games
        //      from the DB is a bit extreme; eventually there should a
        //      methods that can filter the returned games in order to
        //      reduce the number of the returned games.
        List<GameInDB> result = new ArrayList<>();
        try {
            PreparedStatement ps = getSelectGameIdsStatement();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(GAME_GAMEID);
                String name = rs.getString(GAME_NAME);
                result.add(new GameInDB(id, name));
            }
            rs.close();
        } catch (SQLException e) {
            // TODO proper error handling
            e.printStackTrace();
        }
        return result;
    }

    private void createPlayersInDB(Board game) throws SQLException {
        // TODO code should be more defensive
        PreparedStatement ps = getSelectPlayersStatementU();
        ps.setInt(1, game.getGameId());

        ResultSet rs = ps.executeQuery();
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = game.getPlayer(i);
            rs.moveToInsertRow();
            rs.updateInt(PLAYER_GAMEID, game.getGameId());
            rs.updateInt(PLAYER_ORDER, i);
            rs.updateString(PLAYER_NAME, player.getName());
            rs.updateString(PLAYER_COLOUR, player.getColor());
            rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
            rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
            rs.updateString(PLAYER_HEADING, player.getHeading().name());
            rs.insertRow();
            rs.last();
            Integer playerId = (Integer) rs.getObject(PLAYER_ID);
            player.setId(playerId);
        }

        rs.close();
    }

    private PreparedStatement select_player_hand_stmt = null;

    private PreparedStatement getSelectPlayerHandStatement() {
        if (select_player_hand_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_player_hand_stmt = connection.prepareStatement(
                        "SELECT * FROM playerHand WHERE playerId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_player_hand_stmt;
    }

    private PreparedStatement delete_player_hand_stmt = null;

    private PreparedStatement getDeletePlayerHandStatement() {
        if (delete_player_hand_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                delete_player_hand_stmt = connection.prepareStatement(
                        "DELETE FROM playerHand WHERE playerId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return delete_player_hand_stmt;
    }

    private PreparedStatement delete_player_register_stmt = null;

    private PreparedStatement getDeletePlayerRegisterStatement() {
        if (delete_player_register_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                delete_player_register_stmt = connection.prepareStatement(
                        "DELETE FROM playerRegister WHERE playerId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return delete_player_register_stmt;
    }

    private PreparedStatement select_player_register_stmt = null;

    private PreparedStatement getSelectPlayerRegisterStatement() {
        if (select_player_register_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_player_register_stmt = connection.prepareStatement(
                        "SELECT * FROM playerRegister WHERE playerId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_player_register_stmt;
    }

    private void createPlayersHandsInDB(Board board) throws SQLException {
        PreparedStatement ps = getSelectPlayerHandStatement();
        List<Player> players = board.getPlayers();
        for (int p = 0; p < players.size(); p++) {
            Player player = players.get(p);
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            CommandCardField[] cards = player.getCards();
            for (int i = 0; i < cards.length; i++) {
                CommandCardField card = cards[i];
                rs.moveToInsertRow();
                rs.updateInt(PLAYER_HAND_PLAYERID, player.getId());

                //changed: hand card can be null when its moved to program, also table playerHand.card is now nullable
                CommandCard commandCard = card.getCard();
                if (commandCard != null) {
                    rs.updateString(PLAYER_HAND_CARD, commandCard.command.name());
                } else {
                    rs.updateNull(PLAYER_HAND_CARD);
                }

                rs.insertRow();
            }
            rs.close();
        }
    }

    private void createGameState(Board board) throws SQLException {
        PreparedStatement ps = getInsertGameStateStatement();
        ps.setInt(1, board.getGameId());
        ps.setInt(2, board.getCurrentPlayer().getId());
        int affectedRows = ps.executeUpdate();
    }

    private void createPlayerRegisterInDB(Board board) throws SQLException {
        PreparedStatement ps = getSelectPlayerRegisterStatement();
        List<Player> players = board.getPlayers();
        for (int p = 0; p < players.size(); p++) {
            Player player = players.get(p);
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            CommandCardField[] programCards = player.getProgram();
            for (int i = 0; i < programCards.length; i++) {
                CommandCardField programCard = programCards[i];
                CommandCard commandCard = programCard.getCard();

                rs.moveToInsertRow();
                rs.updateInt(PLAYER_REGISTER_PLAYERID, player.getId());
                if (commandCard != null) {
                    rs.updateString(PLAYER_REGISTER_CARD, commandCard.command.name());
                } else {
                    rs.updateNull(PLAYER_REGISTER_CARD);
                }

                rs.insertRow();
            }
            rs.close();
        }
    }

    /**
     * This method uses prepared statement to execure a query and loads the players cards from the DB
     * @param game
     * @throws SQLException
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     * @author Thomas Arildtoft S193564@student.dtu.dk
     */
    private void loadPlayerHandCardsFromDb(Board game) throws SQLException {
        PreparedStatement ps = getSelectPlayerHandStatement();
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            int cardIndex = 0;
            CommandCardField[] cards = player.getCards();
            while (rs.next()) {
                CommandCardField cardField = cards[cardIndex++];
                String cardValue = rs.getString(PLAYER_HAND_CARD);
                if (cardValue != null) {
                    Command command = Command.valueOf(cardValue);
                    cardField.setCard(new CommandCard(command));
                }
            }
            rs.close();
        }
    }

    /**
     * This method uses prepared statement to load the saved cards in from the players register
     * @param game
     * @throws SQLException
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    private void loadPlayerRegisterCardsFromDb(Board game) throws SQLException {
        PreparedStatement ps = getSelectPlayerRegisterStatement();
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            int cardIndex = 0;
            CommandCardField[] cards = player.getProgram();
            while (rs.next()) {
                CommandCardField cardField = cards[cardIndex++];
                String cardValue = rs.getString(PLAYER_REGISTER_CARD);
                if (cardValue != null) {
                    Command command = Command.valueOf(cardValue);
                    cardField.setCard(new CommandCard(command));
                }
            }
            rs.close();
        }
    }

    /**
     *
     * @param game
     * @throws SQLException
     */
    private void loadPlayersFromDB(Board game) throws SQLException {
        PreparedStatement ps = getSelectPlayersASCStatement();
        ps.setInt(1, game.getGameId());
        ResultSet rs = ps.executeQuery();
        int i = 0;
        while (rs.next()) {
            int playerId = rs.getInt(PLAYER_ID);
            String name = rs.getString(PLAYER_NAME);
            String colour = rs.getString(PLAYER_COLOUR);
            Player player = new Player(game, colour, name);

            player.setId(playerId);
            int x = rs.getInt(PLAYER_POSITION_X);
            int y = rs.getInt(PLAYER_POSITION_Y);
            player.setSpace(game.getSpace(x, y));
            player.setHeading(Heading.valueOf(rs.getString((PLAYER_HEADING))));

            game.addPlayer(player);
        }
        rs.close();

        ps = getSelectGameStateStatement();
        ps.setInt(1, game.getGameId());
        rs = ps.executeQuery();
        rs.next();
        Player current = game.getPlayerById(rs.getInt(GAME_STATE_CURRENT_PLAYERID));
        game.setCurrentPlayer(current);
        rs.close();
    }

    private void updateGame(Board game) throws SQLException {
        PreparedStatement ps = getSelectGameStatementU();
        ps.setInt(1, game.getGameId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
            rs.updateInt(GAME_STEP, game.getStep());
            rs.updateRow();
        }
        rs.close();
    }

    private void updatePlayers(Board game) throws SQLException {
        PreparedStatement ps = getSelectPlayersStatementU();
        ps.setInt(1, game.getGameId());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int playerId = rs.getInt(PLAYER_ID);
            // TODO should be more defensive
            Player player = game.getPlayerById(playerId);
            rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
            rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
            rs.updateString(PLAYER_HEADING, player.getHeading().name());
            // TODO error handling
            // TODO take care of case when number of players changes, etc
            rs.updateRow();
        }
        rs.close();

        // TODO error handling/consistency check: check whether all players were updated
    }

    private void updateGameState(Board game) throws SQLException {
        PreparedStatement ps = getSelectGameStateStatement();
        ps.setInt(1, game.getGameId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            rs.updateInt(GAME_STATE_CURRENT_PLAYERID, game.getCurrentPlayer().getId());
            rs.updateRow();
        }
        rs.close();
    }

    /**
     * This is the method that update the player cards in DB
     * @param game
     * @throws SQLException
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     * @author Thomas Arildtoft S193564@student.dtu.dk
     */
    private void updatePlayersHands(Board game) throws SQLException {
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = game.getPlayer(i);
            PreparedStatement ps = getDeletePlayerHandStatement();
            ps.setInt(1, player.getId());
            ps.executeUpdate();
        }
        createPlayersHandsInDB(game);
    }

    private void updatePlayersRegisters(Board game) throws SQLException {
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = game.getPlayer(i);
            PreparedStatement ps = getDeletePlayerRegisterStatement();
            ps.setInt(1, player.getId());
            ps.executeUpdate();
        }
        createPlayerRegisterInDB(game);
    }

    private static final String SQL_INSERT_GAME = "INSERT INTO game(name, phase, step, board) VALUES (?, ?, ?, ?)";

    private PreparedStatement insert_game_stmt = null;

    private PreparedStatement getInsertGameStatementRGK() {
        if (insert_game_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                insert_game_stmt = connection.prepareStatement(
                        SQL_INSERT_GAME,
                        Statement.RETURN_GENERATED_KEYS);
            } catch (SQLException e) {
                // TODO error handling
                e.printStackTrace();
            }
        }
        return insert_game_stmt;
    }

    private PreparedStatement insert_game_state_stmt = null;

    private PreparedStatement getInsertGameStateStatement() {
        if (insert_game_state_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                insert_game_state_stmt = connection.prepareStatement(
                        "INSERT INTO gameState(gameId, currentPlayerId) VALUES (?, ?)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return insert_game_state_stmt;
    }

    private static final String SQL_SELECT_GAME =
            "SELECT * FROM game WHERE id = ?";

    private PreparedStatement select_game_stmt = null;

    private PreparedStatement getSelectGameStatementU() {
        if (select_game_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_game_stmt = connection.prepareStatement(
                        SQL_SELECT_GAME,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                // TODO error handling
                e.printStackTrace();
            }
        }
        return select_game_stmt;
    }

    private PreparedStatement select_game_state_stmt = null;

    private PreparedStatement getSelectGameStateStatement() {
        if (select_game_state_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_game_state_stmt = connection.prepareStatement(
                        "SELECT * FROM gameState WHERE gameId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_game_state_stmt;
    }

    private PreparedStatement select_players_stmt = null;

    private PreparedStatement getSelectPlayersStatementU() {
        if (select_players_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_players_stmt = connection.prepareStatement(
                        "SELECT * FROM player WHERE gameId = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_players_stmt;
    }

    private static final String SQL_SELECT_PLAYERS_ASC =
            "SELECT * FROM player WHERE gameID = ? ORDER BY `order` ASC";

    private PreparedStatement select_players_asc_stmt = null;

    private PreparedStatement getSelectPlayersASCStatement() {
        if (select_players_asc_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                // This statement does not need to be updatable
                select_players_asc_stmt = connection.prepareStatement(
                        SQL_SELECT_PLAYERS_ASC);
            } catch (SQLException e) {
                // TODO error handling
                e.printStackTrace();
            }
        }
        return select_players_asc_stmt;
    }

    private static final String SQL_SELECT_GAMES =
            "SELECT id, name FROM game";

    private PreparedStatement select_games_stmt = null;

    private PreparedStatement getSelectGameIdsStatement() {
        if (select_games_stmt == null) {
            Connection connection = connector.getConnection();
            try {
                select_games_stmt = connection.prepareStatement(
                        SQL_SELECT_GAMES);
            } catch (SQLException e) {
                // TODO error handling
                e.printStackTrace();
            }
        }
        return select_games_stmt;
    }


}
