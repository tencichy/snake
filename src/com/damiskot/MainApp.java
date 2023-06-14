package com.damiskot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The class MainApp extending
 * {@link Application}, which is used to run application.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootPane;

    @Override
    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Snake");
        this.primaryStage.setResizable(false);
        this.primaryStage.setOnCloseRequest(event -> System.exit(0));

        initRootLayout();

        initMainLayout();

    }

    /**
     * Initializing root layout which contains only empty
     * {@link BorderPane}. Prepared for adding menu bar.
     */
    private void initRootLayout(){
        try {
            FXMLLoader loaderRoot = new FXMLLoader();
            loaderRoot.setLocation(MainApp.class.getResource("views/rootLayout.fxml"));
            rootPane = loaderRoot.load();

            Scene scene = new Scene(rootPane);

            GameController.gameScene = scene;
            GameController.mainStage = primaryStage;

            primaryStage.setScene(scene);

            primaryStage.show();
        }catch (IOException e){
            new TextAlertGenerator(e, Alert.AlertType.ERROR);
        }
    }

    /**
     * Initializing main game layout which contains few javafx controls
     */
    private void initMainLayout(){
        try {
            FXMLLoader loaderMain = new FXMLLoader();
            loaderMain.setLocation(MainApp.class.getResource("views/gameLayout.fxml"));
            AnchorPane anchorPane = loaderMain.load();
            anchorPane.setStyle("-fx-background-color: white");

            GameController.gamePane = anchorPane;

            rootPane.setCenter(anchorPane);
        }catch (IOException e){
            new TextAlertGenerator(e, Alert.AlertType.ERROR);
        }
    }

    /**
     * The entry point of application. Launching application.
     *
     * @param args the input arguments - not used
     */
    public static void main(String[] args) {
        launch(args);
    }

}