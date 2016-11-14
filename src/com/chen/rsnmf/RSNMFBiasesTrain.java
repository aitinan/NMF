package com.chen.rsnmf;

import org.cis.data.Ratings;
import org.cis.eval.RmseEvaluator;
import org.cis.io.MovielensRatingsReader;
import org.cis.io.WritePredictRatings;

public class RSNMFBiasesTrain {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        
        int targetItem = 256;
        String dataset = "1M";

        Ratings testData = MovielensRatingsReader.readD("data/movielens/"+dataset+"/test.txt");
        Ratings trainData = MovielensRatingsReader.readD("data/movielens/"+dataset+"/train.txt");
        String filePath = "data/movielens/"+dataset+"/"+ targetItem +"/rsnmf/ratingsOf" + targetItem + ".txt";
        
        RSNMFBiases rsnmfBiases = new RSNMFBiases(trainData, testData, 100, 0.06, 500);
        rsnmfBiases.trainModel();
        System.out.println("last rmse = " + RmseEvaluator.evaluate(rsnmfBiases, testData));
        WritePredictRatings.writePredictRatingsToFile(testData, rsnmfBiases, targetItem, filePath);
        
        System.out.println("total costs " + (System.currentTimeMillis() - start) / 1000 + " s.");
    }
}
