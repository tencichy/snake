package com.damiskot;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The class GameController, used to connect .fxml view with the code
 */
public class GameController {

    private javax.swing.Timer gameTimer = null;

    private final ArrayList<Node> snakeBody = new ArrayList<>();
    private final ArrayList<Node> snakeFood = new ArrayList<>();


    private final int NO_MOVE = 0;
    private final int LEFT = 1;
    private final int UP = 2;
    private final int RIGHT = 3;
    private final int DOWN = 4;

    private double SCORE = 0;
    private int SPEED = 150;
    private double MULTIPLIER = 1;

    private int currentDirection = NO_MOVE;

    private boolean debug = false;
    private boolean darkMode = false;
    private boolean showGrid = false;
    private boolean pause = false;
    private boolean gameOver = false;

    static Scene gameScene;
    static Stage mainStage;
    static AnchorPane gamePane;
    private final Stage debugStage = new Stage();
    private DebugController debugController;

    @FXML
    private Rectangle head;

    @FXML
    private Label gamePausedLabel;

    @FXML
    private Label gameOverLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label sumLabel;

    @FXML
    private Label multiplierLabel;

    @FXML
    private Label snakeLengthLabel;

    /**
     * Initialize. Method runs by default after loading fxml file.
     */
    @FXML
    public void initialize(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/debugLayout.fxml"));
            AnchorPane pane = loader.load();
            debugController = loader.getController();
            AtomicReference<Double> x = new AtomicReference<>((double) 0);
            AtomicReference<Double> y = new AtomicReference<>((double) 0);
            pane.setOnMousePressed(mouseEvent -> {
                x.set(debugStage.getX() - mouseEvent.getScreenX());
                y.set(debugStage.getY() - mouseEvent.getScreenY());
            });
            pane.setOnMouseDragged(mouseEvent -> {
                debugStage.setX(mouseEvent.getScreenX() + x.get());
                debugStage.setY(mouseEvent.getScreenY() + y.get());
            });
            Scene scene = new Scene(pane);
            KeyCombination kcInner = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
            Runnable rnInner = ()->{
                debug = !debug;
                mainStage.requestFocus();
                debugStage.close();
            };
            scene.getAccelerators().put(kcInner, rnInner);
            debugStage.setX(0);
            debugStage.setY(0);
            debugStage.initStyle(StageStyle.TRANSPARENT);
            debugStage.setResizable(false);
            debugStage.setTitle("Snake - debug");
            debugStage.setScene(scene);
            debugStage.setOnCloseRequest(windowEvent -> debug = !debug);
        } catch (IOException e) {
            e.printStackTrace();
            new TextAlertGenerator(e, Alert.AlertType.ERROR);
        }

        HashMap<String,Integer> pos = randomPosition(10,580,10,380);
        head.setX(pos.get("X"));
        head.setY(pos.get("Y"));

        scoreLabel.setVisible(false);
        gamePausedLabel.setVisible(false);
        gameOverLabel.setVisible(false);
        sumLabel.setVisible(false);
        multiplierLabel.setVisible(false);
        snakeLengthLabel.setVisible(false);
        snakeBody.add(head);

        Platform.runLater(this::generateGrid);

        Platform.runLater(()->gameOverLabel.toFront());
        Platform.runLater(()->scoreLabel.toFront());
        Platform.runLater(()->gamePausedLabel.toFront());
        Platform.runLater(()->sumLabel.toFront());
        Platform.runLater(()->multiplierLabel.toFront());
        Platform.runLater(()->snakeLengthLabel.toFront());

        Platform.runLater(()->addFood(FoodType.NORMAL));

        KeyCombination kc = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
        Runnable rn = ()-> {
            debug = !debug;
            if(debug) {
                debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                debugController.updateSnakeList(snakeBody);
                debugController.updateFoodList(snakeFood);
                debugStage.show();
                mainStage.requestFocus();
            }else{
                debugStage.close();
            }
        };
        gameScene.getAccelerators().put(kc, rn);

        gameTimer = new javax.swing.Timer(SPEED, e -> {
            if(!gameOver) {
                if (!pause) {
                    if (currentDirection != NO_MOVE) {
                        move();
                        eat();
                        if(debug){
                            Platform.runLater(()->debugController.updateSnakeList(snakeBody));
                        }
                        gameOver = checkSnake() || isCollidingWithBorder();
                    }
                }
            }else {
                Platform.runLater(()->scoreLabel.setText("score " + new DecimalFormat("##.##").format(SCORE)));
                Platform.runLater(()->multiplierLabel.setText("multiplier " + new DecimalFormat("##.##").format(MULTIPLIER)));
                Platform.runLater(()->snakeLengthLabel.setText("snake length " + snakeBody.size()));
                Platform.runLater(()->sumLabel.setText("sum " + new DecimalFormat("##.##").format((SCORE*MULTIPLIER)+(snakeBody.size()*MULTIPLIER))));
                if(!darkMode){
                    scoreLabel.setTextFill(Color.FORESTGREEN);
                    gameOverLabel.setTextFill(Color.FORESTGREEN);
                    multiplierLabel.setTextFill(Color.FORESTGREEN);
                    snakeLengthLabel.setTextFill(Color.FORESTGREEN);
                    sumLabel.setTextFill(Color.FORESTGREEN);
                }else{
                    scoreLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    gameOverLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    multiplierLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    snakeLengthLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    sumLabel.setTextFill(Color.valueOf("#b6ffbb"));
                }
                gameOverLabel.setVisible(true);
                scoreLabel.setVisible(true);
                multiplierLabel.setVisible(true);
                snakeLengthLabel.setVisible(true);
                sumLabel.setVisible(true);
                Platform.runLater(()->gameOverLabel.toFront());
                Platform.runLater(()->scoreLabel.toFront());
                Platform.runLater(()->sumLabel.toFront());
                Platform.runLater(()->multiplierLabel.toFront());
                Platform.runLater(()->snakeLengthLabel.toFront());
            }
        });

        gameTimer.start();

        Timer pauseTimer = new Timer();
        TimerTask pauseTask = new TimerTask() {
            @Override
            public void run() {
                if(pause){
                    gamePausedLabel.setVisible(!gamePausedLabel.isVisible());
                }
            }
        };

        pauseTimer.schedule(pauseTask,0,600);

        gameScene.setOnKeyPressed(event -> {
            if (!gameOver){
                if (!pause) {
                    switch (event.getCode()){
                        case UP -> currentDirection = UP;
                        case DOWN -> currentDirection = DOWN;
                        case LEFT -> currentDirection = LEFT;
                        case RIGHT -> currentDirection = RIGHT;
                        case P -> pause = true;
                        case B -> switchTheme(darkMode = !darkMode);
                        case G -> switchGrid(showGrid = !showGrid);
                    }
                    if(debug){
                        switch (event.getCode()){
                            case DIGIT1 -> addRectangle();
                            case DIGIT2 -> addFood(FoodType.NORMAL);
                            case DIGIT3 -> addFood(FoodType.FASTER);
                            case DIGIT4 -> addFood(FoodType.INSANE);
                            case DIGIT5 -> addFood(FoodType.RESET_SPEED);
                            case DIGIT6 -> addFood(FoodType.SLOW);
                            case DIGIT7 -> addFood(FoodType.SLOWER);
                            case PAGE_UP -> {
                                if(SPEED - 10 > 20) {
                                    SPEED -= 10;
                                    gameTimer.setDelay(SPEED);
                                    debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                                }
                            }
                            case PAGE_DOWN -> {
                                if(SPEED + 10 < 150){
                                    SPEED += 10;
                                    gameTimer.setDelay(SPEED);
                                    debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                                }
                            }
                            case C -> {
                                Node[] nodesToRemove = new Node[snakeFood.size()];
                                int nodesToRemoveCounter = 0;
                                snakeFood.clear();
                                for (Node n: gamePane.getChildren()){
                                    if(n.getClass() == Food.class){
                                        nodesToRemove[nodesToRemoveCounter] = n;
                                        nodesToRemoveCounter++;
                                    }
                                }
                                gamePane.getChildren().removeAll(nodesToRemove);
                                addFood(FoodType.NORMAL);
                            }
                        }
                    }
                } else {
                    if (event.getCode() == KeyCode.P) {
                        pause = false;
                        gamePausedLabel.setVisible(false);
                    }
                }
        }else{
                if(event.getCode() == KeyCode.R){
                    currentDirection = NO_MOVE;
                    for (Node shape: snakeBody) {
                        if(shape != head) {
                            Platform.runLater(() -> gamePane.getChildren().remove(shape));
                        }
                    }
                    for (Node shape: snakeFood) {
                        Platform.runLater(()->gamePane.getChildren().remove(shape));
                    }
                    snakeBody.clear();
                    snakeFood.clear();
                    SCORE = 0;
                    MULTIPLIER = 1;
                    SPEED = 150;
                    gameTimer.setDelay(SPEED);
                    gameOver = false;
                    scoreLabel.setVisible(false);
                    multiplierLabel.setVisible(false);
                    snakeLengthLabel.setVisible(false);
                    sumLabel.setVisible(false);
                    gamePausedLabel.setVisible(false);
                    gameOverLabel.setVisible(false);
                    HashMap<String,Integer> pos1 = randomPosition(10,580,10,380);
                    head.setX(pos1.get("X"));
                    head.setY(pos1.get("Y"));
                    Platform.runLater(()->addFood(FoodType.NORMAL));
                    snakeBody.add(head);
                    debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                    debugController.updateSnakeList(snakeBody);
                }
            }
        });
    }

    /**
     *
     * @return true - if there is collision if borders of pane
     */
    private boolean isCollidingWithBorder(){
        if(head.getY() < 0){
            return true;
        }else if(head.getX() < 0){
            return true;
        }else if(head.getX() > 590){
            return true;
        }else if(head.getY() > 390){
            return true;
        }
        return false;
    }

    /**
     * Method used to generate random positions for elements
     *
     * @param xMin minimal value of generated x-coordinate
     * @param xMax maximal value of generated x-coordinate
     * @param yMin minimal value of generated y-coordinate
     * @param yMax maximal value of generated y-coordinate
     * @return
     * {@link HashMap} with values of x-coordinate and y-coordinate
     */
    private HashMap<String,Integer> randomPosition(int xMin,int xMax,int yMin,int yMax){
        HashMap<String,Integer> toReturn = new HashMap<>();
        Random random = new Random(System.nanoTime());
        int randomX = random.nextInt((xMax - xMin) + 1) + xMin;
        int randomY = random.nextInt((yMax - yMin) + 1) + yMin;
        char[] charsX = String.valueOf(randomX).toCharArray();
        char[] charsY = String.valueOf(randomY).toCharArray();
        randomX = randomX - Character.getNumericValue(charsX[charsX.length - 1]);
        randomY = randomY - Character.getNumericValue(charsY[charsY.length - 1]);
        toReturn.put("X",randomX);
        toReturn.put("Y",randomY);
        return toReturn;
    }

    /**
     * Method which is used to move whole body of snake
     */
    private void move(){
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        for (Node aSnakeBody : snakeBody) {
            x.add(((Rectangle)aSnakeBody).getX());
            y.add(((Rectangle)aSnakeBody).getY());
        }
        moveHead();
        for (int i = 1; i < snakeBody.size(); i++) {
            ((Rectangle)snakeBody.get(i)).setX(x.get(i-1));
            ((Rectangle)snakeBody.get(i)).setY(y.get(i-1));
        }
    }

    /**
     * Method which is used to move only head of snake
     */
    private void moveHead(){
        if(currentDirection == UP){
            head.setY(head.getY() - 10);
        }else if(currentDirection == DOWN){
            head.setY(head.getY() + 10);
        }else if(currentDirection == LEFT){
            head.setX(head.getX() - 10);
        }else if(currentDirection == RIGHT){
            head.setX(head.getX() + 10);
        }
    }

    /**
     * Method used to adding snake 'food' in random places
     * @see GameController#randomPosition(int, int, int, int)
     * @see FoodType
     * @see Food
     *
     * @param foodType instance of
     * {@link FoodType} for determining what kind of 'food' will be placed
     */
    private void addFood(FoodType foodType){
        HashMap<String, Integer> pos = randomPosition(0, 590, 0, 390);
        System.out.println(darkMode);
        Food foodToAdd = new Food(pos.get("X"), pos.get("Y"), darkMode, foodType);
        gamePane.getChildren().add(foodToAdd);
        snakeFood.add(foodToAdd);
        Platform.runLater(()->debugController.updateFoodList(snakeFood));
    }

    /**
     * Method used to adding body of the snake
     * @see Rectangle
     */
    private void addRectangle(){
        Rectangle rectangleToAdd = new Rectangle(((Rectangle)snakeBody.get(snakeBody.size()-1)).getX(), ((Rectangle)snakeBody.get(snakeBody.size()-1)).getY()+0.001,10,10);
        snakeBody.add(rectangleToAdd);
        rectangleToAdd.setArcWidth(0);
        rectangleToAdd.setArcHeight(0);
        rectangleToAdd.setFill(Paint.valueOf("#00a806"));
        rectangleToAdd.setStrokeType(StrokeType.valueOf("INSIDE"));
        rectangleToAdd.setStrokeWidth(1);
        if(darkMode) {
            rectangleToAdd.setStroke(Color.WHITE);
        }else{
            rectangleToAdd.setStroke(Color.BLACK);
        }
        gamePane.getChildren().add(rectangleToAdd);
    }

    /**
     * Method used to check if there is collision with food. After removing colliding food,
     * the new is added. All types have fixed probabilities of occurrence. Also
     * {@link GameController#SCORE} and
     * {@link GameController#MULTIPLIER} is updated
     */
    private void eat(){
        Bounds bound = checkBounds(head, snakeFood);
        if(bound.isCollision()){
            snakeFood.remove(bound.getShape());
            Platform.runLater(()->gamePane.getChildren().remove(bound.getShape()));
            Platform.runLater(this::addRectangle);
            Platform.runLater(()-> {
                SplittableRandom splRnd = new SplittableRandom(System.nanoTime());
                if(splRnd.nextInt(1000) <= 100){ // 10% chance
                    addFood(FoodType.values()[splRnd.nextInt(4-2)+2]); // 50% chance * 10% chance = 5 % chance
                }else if(splRnd.nextInt(1000) <= 200 ){ //20% chance
                    addFood(FoodType.values()[splRnd.nextInt(6-4)+4]); // 50% chance * 20% chance = 10% chance
                }else if(splRnd.nextInt(1000) <= 1000){ //100% chance
                    addFood(FoodType.values()[splRnd.nextInt(2)]); //50% chance * 100% chance = 50% chance
                }
            });
            if(bound.getFoodType().isWholeChange()){
                MULTIPLIER = bound.getFoodType().getMultiplier();
            }else{
                MULTIPLIER += bound.getFoodType().getMultiplier();
            }
            SCORE += bound.getFoodType().getScore()*MULTIPLIER;
            setSpeed(bound.getFoodType());
            if(debug){
                Platform.runLater(()->debugController.updateLabels(SCORE, SPEED, MULTIPLIER));
            }
        }
    }

    /**
     * Used to check if snake hits itself
     *
     * @return true - if snake's head is colliding with body
     */
    private boolean checkSnake(){
        return checkBounds(head, snakeBody).isCollision();
    }

    /**
     * Method sets and updated speed of snake
     *
     * @param foodType
     * {@link FoodType} used to determine speed
     */
    private void setSpeed(FoodType foodType){
        if(foodType.isWholeChange()){
            SPEED = foodType.getSpeed();
        }else {
            if (!(SPEED+foodType.getSpeed()*(-1) < 20) && !(SPEED+foodType.getSpeed()*(-1) > 150)) {
                SPEED += foodType.getSpeed()*(-1);
            }
        }
        gameTimer.setDelay(SPEED);
    }

    /**
     * Method used for checking if there is collision
     *
     * @param block
     *{@link Rectangle} for which it is checked if the collision occurred
     * @param shapes
     * {@link ArrayList} of
     * {@link javafx.scene.shape.Shape}s or classes extending it within it's expected to collision occur
     * @return
     * {@link Bounds} which contains
     * {@link javafx.scene.shape.Shape} or classes extending it with which the collision occurred
     */
    private Bounds checkBounds(Rectangle block, ArrayList<Node> shapes) {
        boolean collisionDetected = false;
        if(shapes.get(0).getClass() == Food.class) {
            Food collisionShape = null;
            FoodType foodType = null;
            for (Node static_bloc : shapes) {
                if (static_bloc != block) {
                    if (((Food)static_bloc).getY() == block.getY() && ((Food)static_bloc).getX() == block.getX()) {
                        collisionShape = (Food) static_bloc;
                        collisionDetected = true;
                        foodType = ((Food)static_bloc).getFoodType();
                    }
                }
            }
            return new Bounds(collisionShape, collisionDetected, foodType);
        }else{
            Rectangle collisionShape = null;
            for (Node static_bloc : shapes) {
                if (static_bloc != block) {
                    if (((Rectangle)static_bloc).getY() == block.getY() && ((Rectangle)static_bloc).getX() == block.getX()) {
                        collisionShape = (Rectangle) static_bloc;
                        collisionDetected = true;
                    }
                }
            }
            return new Bounds(collisionShape, collisionDetected);
        }
    }

    /**
     * This method is used to switch between theme bright and dark
     *
     * @param dark true - if theme is switching to dark
     */
    private void switchTheme(boolean dark){
        if(dark){
            gamePane.setStyle("-fx-background-color: black");
            ArrayList<Node> nodes = new ArrayList<>(gamePane.getChildren());
            for (Node n: nodes) {
                if(n.getClass() == Rectangle.class || n.getClass() == Food.class) {
                    ((Rectangle) n).setStroke(Color.WHITE);
                }else if(n.getClass() == Line.class){
                    ((Line)n).setStroke(Color.WHITE);
                }
            }
            gamePane.getChildren().clear();
            gamePane.getChildren().addAll(nodes);
        }else{
            gamePane.setStyle("-fx-background-color: white");
            ArrayList<Node> nodes = new ArrayList<>(gamePane.getChildren());
            for (Node n: nodes) {
                if(n.getClass() == Rectangle.class || n.getClass() == Food.class) {
                    ((Rectangle) n).setStroke(Color.BLACK);
                }else if(n.getClass() == Line.class){
                    ((Line)n).setStroke(Color.BLACK);
                }
            }
            gamePane.getChildren().clear();
            gamePane.getChildren().addAll(nodes);
        }
    }

    /**
     * Method used to turn on and off grid
     *
     * @param show true - if grid is turning on
     */
    private void switchGrid(boolean show){
            for (int i = 0; i < gamePane.getChildren().size(); i++) {
                if(gamePane.getChildren().get(i).getClass() == Line.class){
                    Node node = gamePane.getChildren().get(i);
                    node.setVisible(show);
                    gamePane.getChildren().set(i, node);
                }
            }
    }

    /**
     * Generating hidden grid
     */
    private void generateGrid(){
        for (int x = 0; x < 600; x+=10) {
            Line line = new Line(x,0,x,400);
            line.setStroke(Color.BLACK);
            line.setOpacity(0.25);
            line.setVisible(false);
            gamePane.getChildren().add(line);
        }
        for (int y = 0; y < 400; y+=10) {
            Line line = new Line(0,y,600,y);
            line.setStroke(Color.BLACK);
            line.setOpacity(0.25);
            line.setVisible(false);
            gamePane.getChildren().add(line);
        }
    }

}
