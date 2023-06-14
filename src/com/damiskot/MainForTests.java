package com.damiskot;

import java.util.ArrayList;
import java.util.SplittableRandom;

/**
 * The type Main for tests.
 */
public class MainForTests {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ArrayList<FoodType> foodTypes = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            SplittableRandom splRnd = new SplittableRandom(System.nanoTime());
            if(splRnd.nextInt(1000) <= 100){ // 10% Chance
                foodTypes.add(FoodType.values()[splRnd.nextInt(4-2)+2]); // 50% * 10% = 5%
            }else if(splRnd.nextInt(1000) <= 200 ){ // 20% Chance
                foodTypes.add(FoodType.values()[splRnd.nextInt(6-4)+4]); // 50% * 20% = 10%
            }else if(splRnd.nextInt(1000) <= 1000){ // 100% Chance
                foodTypes.add(FoodType.values()[splRnd.nextInt(2)]); // 50% * 100% = 50%
            }
        }
        for (int i = 0; i < FoodType.values().length; i++) {
            int finalI = i;
            System.out.println(FoodType.values()[i].name() + " - " + ((foodTypes.stream().filter(x -> x == FoodType.values()[finalI]).count()/(double)foodTypes.size())*100.0) + "%");
        }
    }

}
