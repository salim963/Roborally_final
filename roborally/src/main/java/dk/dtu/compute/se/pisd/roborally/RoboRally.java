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
package dk.dtu.compute.se.pisd.roborally;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.FieldActionAdapter;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class RoboRally extends Application {

    private static final int MIN_APP_WIDTH = 600;

    private Stage stage;
    private BorderPane boardRoot;
    // private RoboRallyMenuBar menuBar;

    // private AppController appController;
    private BoardMap boardMap;
    private Gson gson;

    public RoboRally() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FieldAction.class, new FieldActionAdapter());
        gson = gsonBuilder.create();
    }

    public String getBoardMapJson() {
        return gson.toJson(boardMap);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        AppController appController = new AppController(this);

        // create the primary scene with the a menu bar and a pane for
        // the board view (which initially is empty); it will be filled
        // when the user creates a new game or loads a game
        RoboRallyMenuBar menuBar = new RoboRallyMenuBar(appController);
        boardRoot = new BorderPane();
        VBox vbox = new VBox(menuBar, boardRoot);
        vbox.setMinWidth(MIN_APP_WIDTH);
        Scene primaryScene = new Scene(vbox);

        stage.setScene(primaryScene);
        stage.setTitle("RoboRally");
        stage.setOnCloseRequest(
                e -> {
                    e.consume();
                    appController.exit();
                });
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    public void createBoardView(GameController gameController) {
        if (gameController != null) {
            Board board = gameController.board;
            boardMap = new BoardMap();
            boardMap.width = board.width;
            boardMap.height = board.height;
            boardMap.map = new SpaceMap[board.width][board.height];
            for (int i = 0; i < board.width; i++) {
                for (int j = 0; j < board.height; j++) {
                    SpaceMap spaceMap = new SpaceMap();
                    spaceMap.walls = null;
                    spaceMap.position = new Position(i, j);

                    //_---------------test----------------
                    if (i == 0 && j == 1) {
                        spaceMap.walls = new SpaceWall[]{new SpaceWall(Heading.SOUTH)};
                        CheckPointAction checkpointAction = new CheckPointAction(2, "checkpoint2.png");
                        spaceMap.actions.add(checkpointAction);
                    }
                    //----------------end of test --------------

                    boardMap.map[i][j] = spaceMap;
                }
            }
        }
        createBoardView(gameController, boardMap);
    }

    public void createBoardView(GameController gameController, BoardMap boardMap) {
        // if present, remove old BoardView
        boardRoot.getChildren().clear();

        if (gameController != null) {
            // create and add view for new board
            BoardView boardView = new BoardView(gameController, boardMap);
            gameController.setBoardView(boardView);
            boardRoot.setCenter(boardView);
        }

        stage.sizeToScene();
    }



    /**
     * This method allows user to choose a board from the available boards in the resources folder.
     * @param gameController
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     */
    public void loadBoardMapFromResourcesFolder(GameController gameController) {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("boards");
        String path = url.getPath();
        File[] files = new File(path).listFiles();

        ArrayList<String> boardNames = new ArrayList<>();
        for(File file : files){
            boardNames.add(file.getName());
        }

        ChoiceDialog dialog = new ChoiceDialog();
        dialog.setContentText("Vælg hvilket spil, du vil indlæse");
        dialog.getItems().addAll(boardNames);
        dialog.showAndWait();

        File selectedBoardFile = null;
        String selectedBoardName = (String) dialog.getSelectedItem();
        for(File file : files){
            String fileName = file.getName();
            if(selectedBoardName.equals(fileName)){
                selectedBoardFile = file;
                break;
            }
        }
        loadBoardMapFromFile(gameController, selectedBoardFile);
    }

    /**
     * This method is used to select a file from a local machine in order to load a board.
     * @param gameController
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     */
    public void loadBoardMapFromSelectedFile(GameController gameController) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        loadBoardMapFromFile(gameController, file);
    }

    /**
     * This method loads the default board automatically after starting a new game
     * @param gameController
     * @author Muhammad Ali Khan Bangash(s092512@student.dtu.dk)
     */
    public void loadBoardMapFromDefaultFile(GameController gameController) {
        try {
            File file = new File(RoboRally.class.getClassLoader().getResource("boards/default.json").toURI());
            loadBoardMapFromFile(gameController, file);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //changed: added this method to convert json to boardmap
    public BoardMap convertJsonToBoardMap(String boardMapJson) {
        return gson.fromJson(boardMapJson, BoardMap.class);
    }

    private void loadBoardMapFromFile(GameController gameController, File file) {
        if (file == null) {
            return;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            byte[] jsonBytes = new byte[(int) file.length()];
            is.read(jsonBytes);
            is.close();
            String json = new String(jsonBytes, "UTF-8");
            boardMap = convertJsonToBoardMap(json);
            createBoardView(gameController, boardMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBoardFromResources(GameController gameController) {

    }

    /**
     * This method saves the current map to the local machine as a json file
     */
    public void saveCurrentBoardMapToFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("roborally-map.json");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }

            String json = gson.toJson(boardMap);
            PrintWriter writer = new PrintWriter(new FileOutputStream(file, false));
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // XXX just in case we need to do something here eventually;
        //     but right now the only way for the user to exit the app
        //     is delegated to the exit() method in the AppController,
        //     so that the AppController can take care of that.
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void getAvailableBoardLayouts() {
//        List boardNames = new ArrayList<String>();
//
//
//        try {
//            File folder = new File(this.getClass().getClassLoader().getResource("boards/").toURI());
//            File[] files = folder.listFiles();
//
//
//            for (File file : files) {
//                if (file.isFile()) {
//                    boardNames.add(FilenameUtils.removeExtension(file.getName()));
//                }
//            }
//
//        } catch (URISyntaxException e) {
//            // XXX Do nothing for now.
//            //     We just don't want to throw this exception any further
//            //     as we really should handle this here.
//        }
//
//        return boardNames;
//
    }

    }

