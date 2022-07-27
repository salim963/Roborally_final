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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class BoardView extends VBox implements ViewObserver {

    private Board board;

    private GridPane mainBoardPane;
    private SpaceView[][] spaces;

    private PlayersView playersView;

    private Label statusLabel;

    private SpaceEventHandler spaceEventHandler;
    private Random random = new Random();
    private BoardMap boardMap;

    public BoardView(@NotNull GameController gameController, BoardMap boardMap) {
        this.boardMap = boardMap;
        board = gameController.board;

        mainBoardPane = new GridPane();
        playersView = new PlayersView(gameController);
        statusLabel = new Label("<no status>");

        this.getChildren().add(mainBoardPane);
        this.getChildren().add(playersView);
        this.getChildren().add(statusLabel);

        spaces = generateMap(boardMap);
        spaceEventHandler = new SpaceEventHandler(gameController);
        board.attach(this);
        update(board);
    }

    public void disable(){
        setDisable(true);
    }

    private SpaceView[][] generateMap(BoardMap boardMap){
        SpaceView[][] spaces = new SpaceView[board.width][board.height];
        for (int x = 0; x < boardMap.width; x++) {
            for (int y = 0; y < boardMap.height; y++) {
                SpaceMap spaceMap = boardMap.map[x][y];

                ArrayList<FieldAction> actions = spaceMap.actions;
                for (int k = 0; k < actions.size(); k++) {
                    FieldAction action = actions.get(k);
                    if (action instanceof CheckPointAction) {
                        board.addCheckpoint((CheckPointAction) action);

                    }
                }

                ArrayList<Heading> walls = new ArrayList<>();
                for (int k = 0; k < spaceMap.walls.length; k++) {
                    walls.add(spaceMap.walls[k].heading);
                }

                Space space = board.getSpace(x, y);
                space.setActions(spaceMap.actions);
                space.getWalls().addAll(walls);
                SpaceView spaceView = new SpaceView(space, spaceMap);
                spaces[x][y] = spaceView;
                mainBoardPane.add(spaceView, x, y);
                spaceView.setOnMouseClicked(spaceEventHandler);
            }
        }
        return spaces;
    }

    public BoardMap getBoardMap() {
        return boardMap;
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == board) {
            Phase phase = board.getPhase();
            statusLabel.setText(board.getStatusMessage());
        }
    }

    // XXX this handler and its uses should eventually be deleted! This is just to help test the
    //     behaviour of the game by being able to explicitly move the players on the board!
    private class SpaceEventHandler implements EventHandler<MouseEvent> {

        final public GameController gameController;

        public SpaceEventHandler(@NotNull GameController gameController) {
            this.gameController = gameController;
        }

        @Override
        public void handle(MouseEvent event) {
            Object source = event.getSource();
            if (source instanceof SpaceView) {
                SpaceView spaceView = (SpaceView) source;
                Space space = spaceView.space;
                Board board = space.board;

                if (board == gameController.board) {
                  //  gameController.moveCurrentPlayerToSpace(space);
                    event.consume();
                }
            }
        }

    }

}
