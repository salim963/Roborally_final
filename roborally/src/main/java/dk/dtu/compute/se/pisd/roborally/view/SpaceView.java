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
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 75; // 60; // 75;
    final public static int SPACE_WIDTH = 75;  // 60; // 75;

    public final Space space;
    public final SpaceMap spaceMap;

    public SpaceView(@NotNull Space space, SpaceMap spaceMap) {
        this.space = space;
        this.spaceMap = spaceMap;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updatePlayer() {


        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    @Override
    public void updateView(Subject subject) {
        this.getChildren().clear();
        if (subject == this.space) {
            updateBelt();

            //Pane pane = new Pane();

            Rectangle rectangle = new Rectangle(0.0, 0.0, SPACE_WIDTH, SPACE_HEIGHT);
            rectangle.setFill(Color.TRANSPARENT);
            this.getChildren().add(rectangle);

            if (spaceMap.actions != null) {
                for (int i = 0; i < spaceMap.actions.size(); i++) {
                    FieldAction action = spaceMap.actions.get(i);
                    if (action instanceof CheckPointAction) {
                        CheckPointAction checkPointAction = (CheckPointAction) action;
                        addImage(checkPointAction.imageName,-90);
                    }
                    if (action instanceof Gear) {
                        Gear gear = (Gear) action;
                        addImage(gear.imageName,0);
                    }
                }
            }

            drawWalls();
            updatePlayer();

            //this.getChildren().add(pane);
        }
    }

    /**
     * This method is used to add Images to the board
     * @param imageName
     * @param rotation
     */
    private void addImage(String imageName, double rotation) {
        try {
            String imagePath = "images/" + imageName;
            Image img = new Image(SpaceView.class.getClassLoader().getResource(imagePath).toURI().toString());
            ImageView imgView = new ImageView(img);
            imgView.setFitHeight(SPACE_HEIGHT);
            imgView.setFitWidth(SPACE_WIDTH);
            imgView.setRotate(rotation);
            imgView.setVisible(true);
            this.getChildren().add(imgView);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    private void drawWalls() {
        SpaceWall[] walls = spaceMap.walls;
        if (walls == null) {
            return;
        }

        for (int i = 0; i < walls.length; i++) {
            SpaceWall wall = walls[i];
            Line line = getWall(wall.heading);
            this.getChildren().add(line);
        }
    }

    /**
     * This method helps user to get walls in all four directions. The if/else command referes to other methods which are used to draw the walls.
     * @param heading
     * @return
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    private Line getWall(Heading heading) {
        LinePoints points = null;
        if (heading == Heading.EAST) {
            points = getEastWallCoordinates();
        } else if (heading == Heading.NORTH) {
            points = getNorthWallCoordinates();
        } else if (heading == Heading.WEST) {
            points = getWestWallCoordinates();
        } else {
            points = getSouthWallCoordinates();
        }

        Line line = new Line(points.start.x, points.start.y, points.end.x, points.end.y);
        line.setStroke(Color.RED);
        line.setStrokeWidth(5);

        //translate wall to its correct position because they are displayed in stackPane
        //which displays them in center if we dont translate/move them to their correct positions
        double translateValue = 35;
        if (heading == Heading.EAST) {
            line.setTranslateX(translateValue);
        } else if (heading == Heading.NORTH) {
            line.setTranslateY(-translateValue);
        } else if (heading == Heading.WEST) {
            line.setTranslateX(-translateValue);
        } else {
            line.setTranslateY(translateValue);
        }

        return line;
    }

    private LinePoints getNorthWallCoordinates() {
        return new LinePoints(new Position(2, 2), new Position(SPACE_WIDTH - 2, 2));
    }

    private LinePoints getSouthWallCoordinates() {
        return new LinePoints(new Position(2, SPACE_HEIGHT - 2), new Position(SPACE_WIDTH - 2, SPACE_HEIGHT - 2));
    }

    private LinePoints getEastWallCoordinates() {
        return new LinePoints(new Position(SPACE_WIDTH - 2, 2), new Position(SPACE_WIDTH - 2, SPACE_HEIGHT - 2));
    }

    private LinePoints getWestWallCoordinates() {
        return new LinePoints(new Position(2, 2), new Position(2, SPACE_HEIGHT - 2));
    }

    private void updateBelt() {
        ConveyorBelt belt = space.getConveyorBelt();
        if (belt != null) {

            Polygon fig = new Polygon(0.0, 0.0,
                    60.0, 0.0,
                    30.0, 60.0);

            fig.setFill(Color.LIGHTGRAY);

            fig.setRotate((90 * belt.getHeading().ordinal()) % 360);
            this.getChildren().add(fig);
        }

    }
}
