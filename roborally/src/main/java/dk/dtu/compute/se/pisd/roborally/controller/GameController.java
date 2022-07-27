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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class GameController {

    private BoardView boardView;
    final public Board board;
    public List<FieldAction> actions = new ArrayList<>();
    public boolean won = false;

    public GameController(@NotNull Board board) {
        this.board = board;
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    /**
     * public void moveCurrentPlayerToSpace(@NotNull Space space) {
     * // TODO Assignment V1: method should be implemented by the students:
     * //   - the current player should be moved to the given space
     * //     (if it is free()
     * //   - and the current player should be set to the player
     * //     following the current player
     * //   - the counter of moves in the game should be increased by one
     * //     if the player is moved
     * <p>
     * Player current = board.getCurrentPlayer();
     * if (space.getPlayer() == null) {
     * current.setSpace(space);
     * int number = board.getPlayerNumber(current);
     * <p>
     * if (board.getPlayersNumber() <= (number + 1)) {
     * board.setCurrentPlayer(board.getPlayer(0));
     * } else {
     * board.setCurrentPlayer(board.getPlayer(number + 1));
     * }
     * board.setCounter(board.getCounter() + 1);
     * }
     * <p>
     * }
     */


    // XXX: V2
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    // XXX: V2
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    // XXX: V2
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    // XXX: V2
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    // XXX: V2
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    // XXX: V2
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    // XXX: V2
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if (command.isInteractive()) {
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    }
                    executeCommand(currentPlayer, command);
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    /**
     * This method is in charge of executing the interactive command-cards
     * by bringing the program back to the activation phase.
     * And afterwords, the program continues once again until the interactive
     * command-cards occurs.
     *
     * @param option is the direction (left or right).
     * @author Zahed(s186517)
     * @author Thomas Arildtoft S193564@student.dtu.dk
     */
    public void executeCommandOptionContinue(@NotNull Command option) {
        Player currentPlayer = board.getCurrentPlayer();
        if (currentPlayer != null &&
                board.getPhase() == Phase.PLAYER_INTERACTION &&
                option != null) {
            board.setPhase(Phase.ACTIVATION);
            executeCommand(currentPlayer, option);
            int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
            if (nextPlayerNumber < board.getPlayersNumber()) {
                board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
            } else {
                int step = board.getStep() + 1;
                if (step < Player.NO_REGISTERS) {
                    makeProgramFieldsVisible(step);
                    board.setStep(step);
                    board.setCurrentPlayer(board.getPlayer(0));
                } else {
                    startProgrammingPhase();
                }
            }
            continuePrograms();
        }
    }

    // XXX: V2
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;

                case Move_Back:
                    this.moveBack(player);
                    break;

                case U_Turn:
                    this.uTurn(player);
                    break;

                case Move_3:
                    this.moveThree(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    /**
     * This method makes the robot move in the forward position.
     *
     * @param player
     * @author Salim Omar s193472@dtu.dk
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     *
     */
    public void moveForward(@NotNull Player player) {
        Space current = player.getSpace();
        Heading heading = player.getHeading();
        if (current != null && player.board == current.board) {
            Space target = board.getNeighbour(current, player.getHeading());
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                } catch (ImpossibleMoveException e) {// we don't do anything here  for now;// we just catch theexception so that// we do no pass it on to the caller// (which would be very bad style).}   }   }   }
                }
            }
        }
    }


    /**
     * This method pushes other players that are in front of the current player.
     * In other words, if there any barriers for a player to move forward,
     * cause of other robots' stands, then the other players will be pushed by
     * the current player. This method also checks if there is a wall in the way.
     *
     * @param player  current player
     * @param space
     * @param heading
     * @throws ImpossibleMoveException Throws exception if there any barriers
     *                                 to move other then players, for instance walls.
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     * @author Thomas Arildtoft S193564@student.dtu.dk
     */

    private void moveToSpace(@NotNull Player player,
                             @NotNull Space space, @NotNull Heading heading) throws
            ImpossibleMoveException {
        Player other = space.getPlayer();
        if (other != null) {
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                moveToSpace(other, target, heading);
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            }
        }

        Space currentSpace = player.getSpace();
        if (currentSpace.isThereWallInHeading(heading)) {
            throw new ImpossibleMoveException(player, space, heading);
        }

        Heading oppositeHeading = getOppositeHeading(heading);
        if (space.isThereWallInHeading(oppositeHeading)) {
            throw new ImpossibleMoveException(player, space, heading);
        }

        player.setSpace(space);
        space.runActions(this);
    }

    /**
     * This method finds the heading opposite to the one robot is facing.
     *
     * @param heading
     * @return
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     *
     * @author Ali Hassan Jawesh s183033@student.dtu.dk
     */
    private Heading getOppositeHeading(Heading heading) {
        if (heading == Heading.WEST) {
            return Heading.EAST;
        }
        if (heading == Heading.NORTH) {
            return Heading.SOUTH;
        }
        if (heading == Heading.EAST) {
            return Heading.WEST;
        }
        if (heading == Heading.SOUTH) {
            return Heading.NORTH;
        }
        return heading;
    }

    //player.setSpace(target);


    /**
     * Move the robot forward by two spaces
     *
     * @param player
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     * @author Salim Omar s193472@dtu.dk
     */
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);

    }

    /**
     * Turn the robot tight
     *
     * @param player
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     */
    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());

        }


    }

    /**
     * Turn the robot left
     *
     * @param player
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     */
    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());

        }

    }


    /**
     * This methods moves the player one step back without changing the direction it is facing.
     *
     * @param player
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public void moveBack(@NotNull Player player) {
        Space currentSpace = player.getSpace();
        if (player != null && player.board == board) {
            Space target = board.getPreviousNeighbour(currentSpace, player.getHeading());
            player.setSpace(target);


        }
    }

    /**
     * This method turns robot 180 degrees so it faces the opposite direction. The robot remains in the current space
     *
     * @param player
     * @author digit(Muhammad Ali)
     * @author Ali Hassan Jawesh s183033@student.dtu.dk
     */
    public void uTurn(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().opposite());

        }
    }

    /**
     * This method makes robot move three spaces ahead.
     *
     * @param player
     * @author Thomas Arildtoft S193564@student.dtu.dk
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public void moveThree(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
        moveForward(player);

    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }

    /**
     * This method initiates the win function with an alert.
     *
     * @param player
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public void GameIsWon(Player player) {
        Alert winMsg = new Alert(Alert.AlertType.INFORMATION, "Spiller \"" + player.getName() + "\" har vundet spillet.");
        this.won = true;
        this.boardView.disable();
        winMsg.showAndWait();
    }


}
