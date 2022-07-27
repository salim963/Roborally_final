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
package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

import java.util.ArrayList;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class Space extends Subject {

    public final Board board;

    public final int x;
    public final int y;

    private Player player;
    private ArrayList<Heading> walls = new ArrayList<>();
    private ArrayList<FieldAction> actions;

    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
    }

    public ArrayList<Heading> getWalls() {
        return walls;
    }

    public ArrayList<FieldAction> getActions() {
        return actions;
    }

    public void setActions(ArrayList<FieldAction> actions) {
        this.actions = actions;
    }

    /**
     * This method run the field action of the conveyorbelt.
     * @return
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public ConveyorBelt getConveyorBelt() {

        ConveyorBelt belt = null;

        for (FieldAction action : this.actions) {
            if (action instanceof ConveyorBelt && belt == null) {
                belt = (ConveyorBelt) action;
            }
        }

        return belt;

    }


    public Player getPlayer() {
        return player;
    }

    /**
     * This method run all the field actions
     *
     * @param gameController
     * @author Muhammad Ali Khan Bangash
     * @author Thomas Arildtoft S193564@student.dtu.dk
     */
    public void runActions(GameController gameController) {
        if (actions == null) {
            return;
        }
        for (int i = 0; i < actions.size(); i++) {
            FieldAction action = actions.get(i);
            action.doAction(gameController, this);
        }
    }

    /**
     * This method is used to check if there is a wall in the same direction as robot
     *
     * @param heading
     * @return
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public boolean isThereWallInHeading(Heading heading) {
        for (int i = 0; i < walls.size(); i++) {
            Heading wallHeading = walls.get(i);
            if (wallHeading == heading) {
                return true;
            }
        }
        return false;
    }

    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                // this should actually not happen
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }

    void playerChanged() {
        // This is a minor hack; since some views that are registered with the space
        // also need to update when some player attributes change, the player can
        // notify the space of these changes by calling this method.
        notifyChange();
    }

    public Space getNeighbourSpace(Heading heading) {
        int newX, newY;
        switch (heading) {

            case NORTH:
                newX = x;
                newY = (y - 1) % board.height;

                if (newY == -1)
                    newY = 7;

                break;
            case SOUTH:
                newX = x;
                newY = (y + 1) % board.height;
                break;
            case WEST:
                newX = (x - 1) % board.width;

                if (newX == -1)
                    newX = 7;

                newY = y;
                break;
            case EAST:
                newX = (x + 1) % board.width;
                newY = y;
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + heading);
        }

        return this.board.getSpace(newX, newY);

    }

}
