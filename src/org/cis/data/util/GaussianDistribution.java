package org.cis.data.util;

import org.cis.data.Ratings;

import java.util.Random;

public class GaussianDistribution {

    public static double getGaussDisRating(Ratings trainData, double average, double sigma) {
    	
        double rating = Math.round(new Random().nextGaussian() * sigma + average);
        //System.out.println(rating);
        
        double minRating = trainData.getMinRating();
        double maxRating = trainData.getMaxRating();
        if (rating < minRating) {
            rating = minRating;
        }
        if (rating > maxRating) {
            rating = maxRating;
        }

        return rating;

    }

}
