package com.damiskot;

import javafx.scene.shape.Rectangle;

/**
 * The class Bounds represents rectangle which have collision with other object.
 * @see FoodType
 * @see Food
 */
public class Bounds {

    private final Rectangle shape;
    private final boolean collision;
    private FoodType foodType;

    /**
     * Instantiates a new Bounds for object:
     * {@link Food}
     *
     * @param shape     rectangle defining object in game
     * @param collision true - if there is collision
     * @param foodType  if shape is instance of:
     * {@link Food} then it's used to determine it parameters when returned
     */
    Bounds(Rectangle shape, boolean collision, FoodType foodType) {
        this.shape = shape;
        this.collision = collision;
        this.foodType = foodType;
    }

    /**
     * Instantiates a new Bounds for object:
     * {@link Rectangle} which is used as body of snake
     *
     * @param shape     rectangle defining object in game
     * @param collision true - if there is collision
     */
    public Bounds(Rectangle shape, boolean collision) {
        this.shape = shape;
        this.collision = collision;
    }

    /**
     * Gets shape.
     *
     * @return instance of:
     * {@link Rectangle} but it could be also:
     * {@link Food} as it extends:
     * {@link Rectangle}
     */
    Rectangle getShape() {
        return shape;
    }

    /**
     * Is collision boolean.
     *
     * @return true - if there is collision
     */
    boolean isCollision() {
        return collision;
    }

    /**
     * Get food type. Used when:
     * {@link Bounds#shape} is instance of:
     * {@link Food}
     *
     * @return
     * {@link FoodType}
     */
    FoodType getFoodType(){
        return this.foodType;
    }

}
