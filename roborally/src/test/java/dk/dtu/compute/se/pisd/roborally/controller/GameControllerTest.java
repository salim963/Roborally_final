package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null, "Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(1, i));
            player.setHeading(Heading.values()[(i + 1) % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
//    void tearDown() {
//        gameController = null;
//    }
//
//
//     @Test
//     void someTest() {
//         Board board = gameController.board;
//
//         Player player = board.getCurrentPlayer();
//         gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));
//
//         assertEquals(player, board.getSpace(0, 4).getPlayer(), "Player " + player.getName() + " should beSpace (0,4)!");
//     }
    /*
        @Test
        void moveCurrentPlayerToSpace() {
            Board board = gameController.board;
            Player player1 = board.getPlayer(0);
            Player player2 = board.getPlayer(1);
            gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

            assertEquals(player1, board.getSpace(0, 4).getPlayer(), "Player 1" + player1.getName() + "should be space (0,4)!");
            Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "PLace (0,0) should be empty now");
            Assertions.assertEquals(player2, board.getCurrentPlayer(), "Player 2" + player2.getName() + " should be the current player");
        }
     */
    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(gameController.board.getSpace(0, 0));
        current.setHeading(Heading.EAST);
        gameController.moveForward(current);
        Space playersNewSpace = current.getSpace();
        Assertions.assertEquals(1, playersNewSpace.x);
        Assertions.assertEquals(0, playersNewSpace.y);


    }

    @Test
    void pushingPlayers() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);

        player1.setSpace(gameController.board.getSpace(1, 2));
        player1.setHeading(Heading.SOUTH);
        player2.setSpace(gameController.board.getSpace(1, 3));
        gameController.moveForward(player1);
        Assertions.assertEquals(1, player1.getSpace().x);
        Assertions.assertEquals(3, player1.getSpace().y);


        Assertions.assertEquals(1, player2.getSpace().x);
        Assertions.assertEquals(4, player2.getSpace().y);

    }

    @Test
    void fastForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(0, 0));
        current.setHeading(Heading.SOUTH);
        gameController.fastForward(current);
        Space playersNewSpace = current.getSpace();
        Assertions.assertEquals(0, playersNewSpace.x);
        Assertions.assertEquals(2, playersNewSpace.y);

    }

    @Test
    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.NORTH);
        gameController.turnRight(current);
        Assertions.assertEquals(current.getHeading(), Heading.EAST);

    }

    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.NORTH);
        gameController.turnLeft(current);
        Assertions.assertEquals(current.getHeading(), Heading.WEST);

    }

    @Test
    void moveBack() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(0, 1));
        current.setHeading(Heading.SOUTH);
        gameController.moveBack(current);
        Space newPosition = current.getSpace();
        Assertions.assertEquals(0, newPosition.x);
        Assertions.assertEquals(0, newPosition.y);

    }

    @Test
    void uTurn() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.NORTH);
        gameController.uTurn(current);
        Assertions.assertEquals(current.getHeading(), Heading.SOUTH);

    }

    @Test
    void moveThree() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(gameController.board.getSpace(0, 0));
        current.setHeading(Heading.EAST);
        gameController.moveThree(current);
        Space playersNewSpace = current.getSpace();
        Assertions.assertEquals(3, playersNewSpace.x);
        Assertions.assertEquals(0, playersNewSpace.y);
    }
}