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
 * The class GameController, used to connect .fxml view with the code.
 * Main heart of game, collisions stuff, moving stuff and other...
 */
public class GameController {

    /**
     * Main game timer. With it, it's easy to add delay to movement
     */
    private javax.swing.Timer gameTimer = null;

    /**
     * {@link GameController#snakeBody} list with all snake nodes
     * {@link GameController#snakeFood} list with all food nodes currently on the board
     * These lists are used to calculate collisions and to move snake
     */
    private final ArrayList<Node> snakeBody = new ArrayList<>();
    private final ArrayList<Node> snakeFood = new ArrayList<>();

    /**
     * Predefined movements directions and it's numbers
     * @see GameController#moveHead()
     */
    private final int NO_MOVE = 0;
    private final int LEFT = 1;
    private final int UP = 2;
    private final int RIGHT = 3;
    private final int DOWN = 4;

    /**
     * Variables storing current score, speed, and score multiplier
     * @see GameController#eat()
     * @see GameController#setSpeed(FoodType)
     */
    private double SCORE = 0;
    private int SPEED = 150;
    private double MULTIPLIER = 1;

    /**
     * Initializing and setting direction variable, which will be used
     * to connect KeyboardListener with
     * {@link GameController#moveHead()} function
     */
    private int currentDirection = NO_MOVE;

    /**
     * Some other variables to define game state and look
     * @see GameController#switchTheme(boolean)
     * @see GameController#switchGrid(boolean)
     */
    private boolean debug = false;
    private boolean darkMode = false;
    private boolean showGrid = false;
    private boolean pause = false;
    private boolean gameOver = false;

    /**
     * Some static variables holding things passed directly from
     * {@link MainApp}
     */
    static Scene gameScene;
    static Stage mainStage;
    static AnchorPane gamePane;

    /**
     * Debug window stage and controller. Controller will be used later
     * for updating labels from this Controller
     * @see DebugController
     */
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
     * Initialize. Method runs by default after loading fxml file. Importing debug window layout, setting
     * stage, pane and it's options.
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

        //Setting snake head in new random position on the board
        HashMap<String,Integer> pos = randomPosition(10,580,10,380);
        head.setX(pos.get("X"));
        head.setY(pos.get("Y"));

        if(darkMode){
            gamePausedLabel.setTextFill(Color.valueOf("b6ffbb"));
        }else{
            gamePausedLabel.setTextFill(Color.FORESTGREEN);
        }

        //Hiding end game labels, with score, and etc.
        scoreLabel.setVisible(false);
        gamePausedLabel.setVisible(false);
        gameOverLabel.setVisible(false);
        sumLabel.setVisible(false);
        multiplierLabel.setVisible(false);
        snakeLengthLabel.setVisible(false);
        snakeBody.add(head);

        //Generating grid in hidden mode
        Platform.runLater(this::generateGrid);

        //Bringing labels to front in case of ending game
        Platform.runLater(()->gameOverLabel.toFront());
        Platform.runLater(()->scoreLabel.toFront());
        Platform.runLater(()->gamePausedLabel.toFront());
        Platform.runLater(()->sumLabel.toFront());
        Platform.runLater(()->multiplierLabel.toFront());
        Platform.runLater(()->snakeLengthLabel.toFront());

        //Adding one food to begin game with
        Platform.runLater(()->addFood(FoodType.NORMAL));

        //Registering key combination for turning on debug mode, and showing debug window
        KeyCombination kc = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
        //Creating new runnable for key combination
        Runnable rn = ()-> {
            //Changing state of debug variable
            debug = !debug;
            if(debug) {
                //Updating labels and tabels
                debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                debugController.updateSnakeList(snakeBody);
                debugController.updateFoodList(snakeFood);
                //Showing debug window
                debugStage.show();
                //But don't want to lose focus from game, so requesting focus on it
                mainStage.requestFocus();
            }else{
                //Closing debug window
                debugStage.close();
            }
        };
        //Add key combination to scene
        gameScene.getAccelerators().put(kc, rn);

        //Setting main game timer with minimal speed, and with lambda function to actually run game
        gameTimer = new javax.swing.Timer(SPEED, e -> {
            if(!gameOver) {
                if (!pause) {
                    if (currentDirection != NO_MOVE) {
                        move();
                        eat();
                        if(debug){
                            //updating list view in debug window
                            Platform.runLater(()->debugController.updateSnakeList(snakeBody));
                        }
                        gameOver = checkSnake() || isCollidingWithBorder();
                    }
                }
            }else {
                //If it's game over, showing label
                Platform.runLater(()->scoreLabel.setText("score " + new DecimalFormat("##.##").format(SCORE)));
                Platform.runLater(()->multiplierLabel.setText("multiplier " + new DecimalFormat("##.##").format(MULTIPLIER)));
                Platform.runLater(()->snakeLengthLabel.setText("snake length " + snakeBody.size()));
                Platform.runLater(()->sumLabel.setText("sum " + new DecimalFormat("##.##").format((SCORE*MULTIPLIER)+(snakeBody.size()*MULTIPLIER))));
                //Need to change a little colour because of white background
                if(!darkMode){
                    scoreLabel.setTextFill(Color.FORESTGREEN);
                    gameOverLabel.setTextFill(Color.FORESTGREEN);
                    multiplierLabel.setTextFill(Color.FORESTGREEN);
                    snakeLengthLabel.setTextFill(Color.FORESTGREEN);
                    sumLabel.setTextFill(Color.FORESTGREEN);
                    gamePausedLabel.setTextFill(Color.FORESTGREEN);
                }else{
                    //Or if background its dark leave it as it was
                    scoreLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    gameOverLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    multiplierLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    snakeLengthLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    sumLabel.setTextFill(Color.valueOf("#b6ffbb"));
                    gamePausedLabel.setTextFill(Color.valueOf("#b6ffbb"));
                }
                //Showing labels with counted score
                gameOverLabel.setVisible(true);
                scoreLabel.setVisible(true);
                multiplierLabel.setVisible(true);
                snakeLengthLabel.setVisible(true);
                sumLabel.setVisible(true);
                //Again bringing labels to front, just for sure
                Platform.runLater(()->gameOverLabel.toFront());
                Platform.runLater(()->scoreLabel.toFront());
                Platform.runLater(()->sumLabel.toFront());
                Platform.runLater(()->multiplierLabel.toFront());
                Platform.runLater(()->snakeLengthLabel.toFront());
            }
        });

        //Starting main game timer
        gameTimer.start();

        //Setting up new timer for flashing pause text,
        //So it's just for nice look
        Timer pauseTimer = new Timer();
        TimerTask pauseTask = new TimerTask() {
            @Override
            public void run() {
                if(pause){
                    gamePausedLabel.setVisible(!gamePausedLabel.isVisible());
                }
            }
        };

        //Scheduling pause label task with fixed period
        pauseTimer.schedule(pauseTask,0,600);

        //Setting up keyboard listener
        gameScene.setOnKeyPressed(event -> {
            if (!gameOver){
                if (!pause) {
                    switch (event.getCode()){
                        case UP -> currentDirection = UP;
                        case DOWN -> currentDirection = DOWN;
                        case LEFT -> currentDirection = LEFT;
                        case RIGHT -> currentDirection = RIGHT;
                        case P -> pause = true; //Pausing game
                        case B -> switchTheme(darkMode = !darkMode);
                        case G -> switchGrid(showGrid = !showGrid);
                    }
                    //Additional keys for fun stuff in debug mode
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
                            //Clearing all 'food' nodes on board, and generating one normal
                            //Just like on the start of the game
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
                    //Unpausing the game
                    if (event.getCode() == KeyCode.P) {
                        pause = false;
                        gamePausedLabel.setVisible(false);
                    }
                }
        }else{
                //Reset game. Set values to init state. Only works after game over
                if(event.getCode() == KeyCode.R){
                    currentDirection = NO_MOVE;
                    //Removing snake body from the pane
                    for (Node shape: snakeBody) {
                        if(shape != head) {
                            Platform.runLater(() -> gamePane.getChildren().remove(shape));
                        }
                    }
                    //Removing food from the pane
                    for (Node shape: snakeFood) {
                        Platform.runLater(()->gamePane.getChildren().remove(shape));
                    }
                    //Clearing lists with snake body and food
                    snakeBody.clear();
                    snakeFood.clear();
                    //Setting score, multiplier and speed to default values
                    SCORE = 0;
                    MULTIPLIER = 1;
                    SPEED = 150;
                    //Setting timer to default speed
                    gameTimer.setDelay(SPEED);
                    //Changing game over to not game over ;)
                    gameOver = false;
                    //Hiding end score labels
                    scoreLabel.setVisible(false);
                    multiplierLabel.setVisible(false);
                    snakeLengthLabel.setVisible(false);
                    sumLabel.setVisible(false);
                    //Hiding pause and game over label
                    gamePausedLabel.setVisible(false);
                    gameOverLabel.setVisible(false);
                    //Setting new random position for sneak head
                    HashMap<String,Integer> pos1 = randomPosition(10,580,10,380);
                    head.setX(pos1.get("X"));
                    head.setY(pos1.get("Y"));
                    //Adding food
                    Platform.runLater(()->addFood(FoodType.NORMAL));
                    //Adding head to body list
                    snakeBody.add(head);
                    //And lastly updating debug labels, and list
                    debugController.updateLabels(SCORE, SPEED, MULTIPLIER);
                    debugController.updateSnakeList(snakeBody);
                }
            }
        });
    }

    /**
     * Simple code to check if head is colliding with border
     * it could be simplified, although it looks to me kinda nicer
     * and easier to understand what's going on
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
    // TODO: 14.06.2023 It's surely possible to change HashMap to something more basic
    private HashMap<String,Integer> randomPosition(int xMin,int xMax,int yMin,int yMax){
        //Making new hash map to easily store coordinates
        HashMap<String,Integer> toReturn = new HashMap<>();
        //Setting up random and generating random x and y values in boundaries
        Random random = new Random(System.nanoTime());
        int randomX = random.nextInt((xMax - xMin) + 1) + xMin;
        int randomY = random.nextInt((yMax - yMin) + 1) + yMin;
        //Fixing up position
        char[] charsX = String.valueOf(randomX).toCharArray();
        char[] charsY = String.valueOf(randomY).toCharArray();
        randomX = randomX - Character.getNumericValue(charsX[charsX.length - 1]);
        randomY = randomY - Character.getNumericValue(charsY[charsY.length - 1]);
        //Returning values in hash map
        toReturn.put("X",randomX);
        toReturn.put("Y",randomY);
        return toReturn;
    }

    /**
     * Method which is used to move whole body of snake
     */
    private void move(){
        //Copying each coordinate of each node
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        for (Node aSnakeBody : snakeBody) {
            x.add(((Rectangle)aSnakeBody).getX());
            y.add(((Rectangle)aSnakeBody).getY());
        }
        //Moving only head
        moveHead();
        //Moving rest part of the body using previous saved values
        for (int i = 1; i < snakeBody.size(); i++) {
            ((Rectangle)snakeBody.get(i)).setX(x.get(i-1));
            ((Rectangle)snakeBody.get(i)).setY(y.get(i-1));
        }
    }

    /**
     * Method which is used to move only head of snake
     * It uses
     * {@link GameController#currentDirection} to determine in which direction should head move
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
        //Generating random x and y coordinates
        HashMap<String, Integer> pos = randomPosition(0, 590, 0, 390);
        //Generating new instance of food, and also passing boolean with dark mode setting
        //In order to change stroke color
        Food foodToAdd = new Food(pos.get("X"), pos.get("Y"), darkMode, foodType);
        //Adding food to pane
        gamePane.getChildren().add(foodToAdd);
        //Adding food to list
        snakeFood.add(foodToAdd);
        //Updating food list in debug window
        Platform.runLater(()->debugController.updateFoodList(snakeFood));
    }

    /**
     * Method used to adding body of the snake
     * @see Rectangle
     */
    private void addRectangle(){
        //Making new rectangle for snake body
        Rectangle rectangleToAdd = new Rectangle(((Rectangle)snakeBody.get(snakeBody.size()-1)).getX(), ((Rectangle)snakeBody.get(snakeBody.size()-1)).getY()+0.001,10,10);
        //Adding to list
        snakeBody.add(rectangleToAdd);
        //Changing some visuals
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
        //Adding to pane
        gamePane.getChildren().add(rectangleToAdd);
    }

    /**
     * Method used to check if there is collision with food. After removing colliding food,
     * the new is added. All types have fixed probabilities of occurrence. Also
     * {@link GameController#SCORE} and
     * {@link GameController#MULTIPLIER} is updated
     */
    private void eat(){
        //Check bounds for collision
        Bounds bound = checkBounds(head, snakeFood);
        //If there is collision, remove food, add effects and score, and generate new one
        if(bound.isCollision()){
            // TODO: 14.06.2023 Add more then one food at the time, and then wait till no left and then add again randomly few
            //Remove food from list
            snakeFood.remove(bound.getShape());
            //Remove from pane
            Platform.runLater(()->gamePane.getChildren().remove(bound.getShape()));
            //Add new 'segment' of snake
            Platform.runLater(this::addRectangle);
            //Randomly with some kind of probability generate new food
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
            //With this parameter it's possible to determine if values
            //from FoodType are being added to variable or overwrites variable
            if(bound.getFoodType().isWholeChange()){
                MULTIPLIER = bound.getFoodType().getMultiplier();
            }else{
                MULTIPLIER += bound.getFoodType().getMultiplier();
            }
            //Counting score: SCORE = SCORE + MULTIPLIER * FoodType.get
            SCORE += bound.getFoodType().getScore()*MULTIPLIER;
            //Set speed
            setSpeed(bound.getFoodType());
            if(debug){
                //Update labels in debug controller
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
        //Set speed or add to speed
        if(foodType.isWholeChange()){
            SPEED = foodType.getSpeed();
        }else {
            if (!(SPEED+foodType.getSpeed()*(-1) < 20) && !(SPEED+foodType.getSpeed()*(-1) > 150)) {
                SPEED += foodType.getSpeed()*(-1);
            }
        }
        //Update speed
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
        //Need to stop the timer because the elements of the pane are updated
        gameTimer.stop();
        //Changing theme to dark
        if(dark){
            gamePane.setStyle("-fx-background-color: black");
            gamePausedLabel.setTextFill(Color.valueOf("b6ffbb"));
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
        //Changing theme to bright
        }else{
            gamePane.setStyle("-fx-background-color: white");
            gamePausedLabel.setTextFill(Color.FORESTGREEN);
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
        gameTimer.start();
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
