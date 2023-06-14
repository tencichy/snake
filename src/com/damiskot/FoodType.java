package com.damiskot;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * The enum Food type. It's holds various values used for calculating 'food' behaviour
 */
public enum FoodType {

    NORMAL(false, +1,+1,+0.05, Color.BROWN),
    FASTER(false,+10,+5,+0.1, Color.YELLOW),
    INSANE(false, +50,+10,+0.5, Color.HOTPINK),
    RESET_SPEED(true, 150,0,1, Color.WHITE),
    SLOW(false, -1,+1,-0.1, Color.LIGHTSKYBLUE),
    SLOWER(false, -10,+1,-0.2, Color.BLUE);

    private final boolean wholeChange;
    private final int speed;
    private final int score;
    private final double multiplier;
    private final Paint colour;

    FoodType(boolean wholeChange, int speed, int score, double multiplier, Paint colour){
        this.wholeChange = wholeChange;
        this.speed = speed;
        this.score = score;
        this.multiplier = multiplier;
        this.colour = colour;
    }

    /**
     * Makes string which is used to show in
     * {@link DebugController#foodListView}
     *
     * @return
     * {@link String}
     */
    @Override
    public String toString(){
        return this.name() + ", speed: " + this.speed + ", score: " + this.score + ", multiplier: " + this.multiplier + ", colour: " + this.colour.toString();
    }

    /**
     * It's returns true if the values of
     * {@link FoodType#multiplier} and
     * {@link FoodType#speed} are overwritten
     *
     * @return the boolean
     */
    public boolean isWholeChange() {
        return wholeChange;
    }

    /**
     * Gets speed.
     *
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Get colour paint.
     *
     * @return
     * {@link Paint}
     */
    public Paint getColour(){
        return this.colour;
    }

    /**
     * Gets score.
     *
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets multiplier.
     *
     * @return the multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }
}
