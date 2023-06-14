package com.damiskot;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * The class DebugController, used to connect .fxml view with the code
 */
public class DebugController {

    @FXML
    private Label scoreLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label multiplierLabel;

    @FXML
    private Label snakeLengthLabel;

    @FXML
    private ListView<String> foodListView;

    @FXML
    private ListView<String> snakeListView;

    @FXML
    private ListView<String> activeFoodListView;

    /**
     * Initialize. Method runs by default after loading fxml file.
     */
    @FXML
    public void initialize(){
        for (FoodType ft: FoodType.values()) {
            foodListView.getItems().add(ft.toString());
        }
    }

    /**
     * Update labels with new data.
     *
     * @param score      the score
     * @param speed      the speed
     * @param multiplier the multiplier
     */
    public void updateLabels(double score, int speed, double multiplier){
        scoreLabel.setText("Score: " + new DecimalFormat("##.##").format(score));
        speedLabel.setText("Speed: " + (150-speed));
        multiplierLabel.setText("Multiplier: " + new DecimalFormat("##.##").format(multiplier));
    }

    /**
     * Update list with:
     * {@link Rectangle}s which builds the snake
     *
     * @param snakeBody
     * {@link ArrayList} of
     * {@link Node}s that builds snake body
     */
    public void updateSnakeList(ArrayList<Node> snakeBody){
        snakeListView.getItems().clear();
        for (Node n: snakeBody){
            snakeListView.getItems().add(n.getId() + ", X: " + ((Rectangle)n).getX() + ", Y: " + ((Rectangle)n).getY());
        }
        snakeLengthLabel.setText("Length: " + snakeListView.getItems().size());
    }

    /**
     * Update list with:
     * {@link FoodType}s that are currently on the board
     *
     * @param foodList
     * {@link ArrayList} of
     * {@link Node}s that contains snake food
     */
    public void updateFoodList(ArrayList<Node> foodList){
        activeFoodListView.getItems().clear();
        for(Node n: foodList){
            activeFoodListView.getItems().add(n.toString());
        }
    }

}
