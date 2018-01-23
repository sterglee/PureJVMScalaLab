
package scalaExec.scalaLab;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
 
public class fxgui extends Application {
 
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage stage) {
        stage.setTitle("Menu Sample");
        Scene scene = new Scene(new VBox(), 400, 350);
        scene.setFill(Color.OLDLACE);
 
        MenuBar mainMenuBar = new MenuBar();
 
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
 
        Menu symbolicAlgebraMenu = new Menu("Computer Algebra");
        MenuItem startsymJavaMenuItem = new MenuItem("Start the main symja GUI window");
        startsymJavaMenuItem.setOnAction(
                new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                   String []  nullArgs = new String[1];
                      nullArgs[0]= "";
                      org.matheclipse.symja.Main.main(nullArgs);
                     }
                }
        );
            
        symbolicAlgebraMenu.getItems().addAll(startsymJavaMenuItem);
        
        
        MenuItem saveEditorTextMenuItem = new MenuItem("Save Editor Text");
        saveEditorTextMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent e) {  new saveEditorTextAction(); }
        });
        
        
        MenuItem saveAsEditorTextMenuItem = new MenuItem("Save As Editor Text");
        saveAsEditorTextMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent e) {  new saveAsEditorTextAction(); }
        });
        
        
        MenuItem loadEditorTextMenuItem = new MenuItem("Load File to Editor");
        loadEditorTextMenuItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                new loadEditorTextAction();
            }
        });
        
        MenuItem displayCurrentAbbreviatonsMenuItem = new MenuItem("Display current abbreviations");
        displayCurrentAbbreviatonsMenuItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                 scalainterpreter.AbbreviationHandler.displayAbbreviations();
            }
        });
        
        
        MenuItem displayScalaSciVariables = new MenuItem("Display current ScalaSci Variables");
        displayScalaSciVariables.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                 scalaSciCommands.WatchInterpreterState.printUserNamesAndValues();
            }
        });
        
        editMenu.getItems().addAll(saveEditorTextMenuItem, saveAsEditorTextMenuItem, loadEditorTextMenuItem, 
                displayCurrentAbbreviatonsMenuItem, displayScalaSciVariables);
 
        // --- Menu View
        Menu menuView = new Menu("View");
         
        MenuBar menuBar = new MenuBar();
 
        menuBar.getMenus().addAll(fileMenu, editMenu, menuView, symbolicAlgebraMenu);
 
 
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar);
 
        stage.setScene(scene);
        stage.show();
    }
}
