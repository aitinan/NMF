package com.chen.rcfnmf;

import org.cis.data.Ratings;
import org.cis.io.MovielensRatingsReader;
import org.cis.io.WritePredictRatings;

public class RCFNMFTrain {
    public static void main(String[] args) {
    	long start = System.currentTimeMillis();
    	
        int targetItem = 256;

        Ratings trainData = MovielensRatingsReader.readD("D:\\ideaProjects\\data\\1M\\train.txt");
        Ratings testData = MovielensRatingsReader.readD("D:\\ideaProjects\\data\\1M\\test.txt");
        
        String filePath = "D:\\ideaProjects\\data\\1M\\ratingsOf"+targetItem+".txt";
        RCFNMF rcfnmf = new RCFNMF(trainData, testData, 100, 0.004, 400);
        rcfnmf.trainModel();
        WritePredictRatings.writePredictRatingsToFile(testData, rcfnmf, targetItem, filePath);
        
        System.out.println("total cost " + (System.currentTimeMillis() - start) / 1000 + " s.");
    }
}
