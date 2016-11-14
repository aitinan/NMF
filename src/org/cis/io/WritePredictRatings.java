package org.cis.io;

import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class WritePredictRatings {
	
    public static void writePredictRatingsToFile(Ratings ratings, RatingPredictor rp, int targetItem, String filePath) {
        int totalUser = ratings.getMaxUserId();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
            for (int user = 1; user <= totalUser; user++) {
                bw.write(user + " " + targetItem + " " + rp.predict(user, targetItem, true) + "\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
