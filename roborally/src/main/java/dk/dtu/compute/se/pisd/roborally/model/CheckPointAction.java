package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

public class CheckPointAction implements FieldAction {

    public final int checkPointNumber;
    public final String imageName;


    public CheckPointAction(int checkPointNumber, String imageName) {

        this.checkPointNumber = checkPointNumber;
        this.imageName = imageName;
    }


    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player != null && player.board == space.board) {
            player.updateCheckPointNumber(checkPointNumber);
            if (player.getRecentCheckPoint() >= gameController.board.getCheckPoints().size()) {
                gameController.GameIsWon(player);
            }
        }


        return true;
    }
}
