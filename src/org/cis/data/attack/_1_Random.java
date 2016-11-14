package org.cis.data.attack;

import org.cis.data.util.GaussianDistribution;
import org.cis.data.Ratings;
import org.cis.io.MovielensRatingsReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class _1_Random {
    public static void main(String[] args) throws Exception {

    	double[] fsArr = {0.007};
		double[] asArr = {0.01, 0.03, 0.05, 0.07, 0.10};

		int targetItem = 7052;

		Ratings trainData = MovielensRatingsReader.readS("data/netflix/train.txt");
        for (double fSize : fsArr) {
            for (double aSize : asArr) {
                String writeFilePath = "data/netflix/"+ targetItem +"/random_1/FS" + fSize + "AS" + aSize + ".txt";
                _1_Random.getRandomAttackAP(trainData, fSize, aSize, writeFilePath, targetItem);
            }
        }
        trainData.clear();
        System.out.println("finish.");
    }

    private static void getRandomAttackAP(Ratings trainData, double fillerSize, double attackSize, String writeFilePath, int targetItem) throws Exception {

        int userNumber = trainData.getMaxUserId();
        int itemNumber = trainData.getMaxItemId();
        double targetItemRating = 5;
        double average = trainData.averageRating();
        double sigma = trainData.cigRating();
        int IDStart = 1;

        long attackUserNumber = Math.round(userNumber * attackSize);
        long fillerItemNumber = Math.round(itemNumber * fillerSize);

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writeFilePath)));

        for (int n = 0; n < attackUserNumber; n++) {
            bw.write(1 + " ");
            List<Integer> fillerItemIDList = _1_Random.getFillerItemIDList(fillerItemNumber, IDStart, itemNumber, targetItem);
            for (int i = 0; i < fillerItemIDList.size(); i++) {
                if (fillerItemIDList.get(i) == targetItem) {
                    bw.write(fillerItemIDList.get(i) + ":" + targetItemRating + " ");
                } else {
                    bw.write(fillerItemIDList.get(i) + ":" + GaussianDistribution.getGaussDisRating(trainData, average, sigma) + " ");
                }
            }
            bw.newLine();
        }
        bw.close();
    }

    private static List<Integer> getFillerItemIDList(long fillerItemNumber, int IDStart, int IDEnd, int targetItem) {
        List<Integer> listFillerItemID = new ArrayList<>();
        listFillerItemID.add(targetItem);
        Random r = new Random();
        while (listFillerItemID.size() < fillerItemNumber) {
            int itemID = r.nextInt(IDEnd - IDStart + 1);
            itemID += IDStart;
            if (!listFillerItemID.contains(itemID) && itemID != targetItem) {
                listFillerItemID.add(itemID);
            }
        }
        Collections.sort(listFillerItemID);
        return listFillerItemID;
    }
}
