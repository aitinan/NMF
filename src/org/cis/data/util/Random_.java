package org.cis.data.util;

import java.util.Random;

public class Random_ {
    public static int getRandom(int startID, int endID) {
        return new Random().nextInt(endID - startID + 1) + startID;
    }
}
