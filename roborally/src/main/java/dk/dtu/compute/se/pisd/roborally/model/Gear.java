package dk.dtu.compute.se.pisd.roborally.model;


import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

public class Gear implements FieldAction {

    public Direction direction;
    public final String imageName;

    public void setDirection(Direction direction) { this.direction = direction;}

    public Gear(Direction direction, String imageName) {
        this.direction = direction;
        this.imageName = imageName;
    }

    @Override
    public boolean doAction(GameController gameController, Space space){

        Player player = space.getPlayer();

        switch (direction) {
            case LEFT:
                gameController.turnLeft(player);
                break;

            case RIGHT:
                gameController.turnRight(player);
                break;
        }

        return true;

    }
}

