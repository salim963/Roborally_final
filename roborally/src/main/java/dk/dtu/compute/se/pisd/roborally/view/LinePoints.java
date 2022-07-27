package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.roborally.model.Position;

/**
 * This class is used to help the user in the GUI by giving a line a start and end point. This is later used in spaceview
 * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
 */
public class LinePoints {

    public final Position start;
    public final Position end;

    /**
     * This method is used to give the start and end position of the lines drawn to create a wall.
     * @param start
     * @param end
     * @author Muhammad Ali Khan Bangash s092512@student.dtu.dk
     */
    public LinePoints(Position start, Position end){
        this.start = start;
        this.end = end;

    }

}
