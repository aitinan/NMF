package com.chen.rcfnmf;

import org.cis.data.Ratings;
import org.cis.io.MovielensRatingsReader;
import org.cis.io.WritePredictRatings;

import java.util.Date;

public class RCFNMFSimilarityTrain {
    public static void main(String[] args) {
        Date start = new Date();

        int targetItem = 256;
        double lamuda = 0.004;
        
        Ratings testData = MovielensRatingsReader.readD("data/movielens/1M/test.txt");
        Ratings trainData = MovielensRatingsReader.readD("data/movielens/1M/train.txt");
             	
        String filePath = "data/movielens/1M/"+targetItem+"/ratingsOf"+targetItem+"_similarity_plus.txt";
        RCFNMFSimilarity rcfnmfSimilarity = new RCFNMFSimilarity(trainData, testData, 150, lamuda, 500);
        rcfnmfSimilarity.trainModel();
        System.out.println("last rmse = " + rcfnmfSimilarity.evaluate(rcfnmfSimilarity, testData));
        WritePredictRatings.writePredictRatingsToFile(testData, rcfnmfSimilarity, targetItem, filePath);
       
        System.out.println("total cost " + (new Date().getTime() - start.getTime()) / 1000 + " s.");
    }
}
