package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;

import java.util.ArrayList;

public class SpaceMap {
    public Position position;
    public SpaceWall[] walls = new SpaceWall[0];
    public ArrayList<FieldAction> actions = new ArrayList<>();
}
