package com.damiskot;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * The class Food which represents elements 'eaten' by snake. It extends
 * {@link Rectangle} for easy adding to
 * {@link javafx.scene.layout.Pane}
 */
public class Food extends Rectangle {

    private final FoodType foodType;

    /**
     * Instantiates a new Food with coordinates and it's type.
     *
     * @param x        x-coordinate
     * @param y        y-coordinate
     * @param foodType instance of
     * {@link FoodType}
     */
    public Food(int x, int y, boolean darkMode, FoodType foodType){
        super(x,y,10,10);
        super.setFill(foodType.getColour());
        super.setArcWidth(0);
        super.setArcHeight(0);
        super.setStrokeType(StrokeType.valueOf("INSIDE"));
        super.setStrokeWidth(1);
        if(darkMode){
            super.setStroke(Color.WHITE);
        }else {
            super.setStroke(Color.BLACK);
        }
        this.foodType = foodType;
    }

    /**
     * Makes string which is used to show in
     * {@link DebugController#activeFoodListView}
     *
     * @return
     * {@link String}
     */
    @Override
    public String toString(){
        return foodType.name() + ", X: " + this.getX() + ", Y: " + this.getY();
    }

    /**
     * Gets food type.
     *
     * @return
     * {@link FoodType}
     */
    public FoodType getFoodType() {
        return foodType;
    }
}
