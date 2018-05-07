package com.danjb.otherdom.client.util;

public class GameUtils {

    public static double randBetween(double min, double max){
        return min + (Math.random() * (max - min));
    }
    
}
