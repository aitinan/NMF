package org.cis.data.attack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.cis.data.util.GaussianDistribution;
import org.cis.data.Ratings;
import org.cis.data.ItemAverage;
import org.cis.io.MovielensRatingsReader;

public class _2_Average {
	public static void main(String[] args) throws Exception {

		double[] fsArr = {0.001, 0.003, 0.005, 0.007};
		double[] asArr = {0.01, 0.03, 0.05, 0.07, 0.1};

		int targetItem = 7052;

		Ratings trainData = MovielensRatingsReader.readS("data/netflix/train.txt");
		for(double fSize : fsArr){
			for(double aSize : asArr){
				String writeFilePath = "data/netflix/"+ targetItem +"/average_1/FS" + fSize + "AS" + aSize + ".txt";
				_2_Average.getAverageAttackAP(trainData, fSize, aSize, writeFilePath, targetItem);
			}
		}
		trainData.clear();
		System.out.println("finish.");
	}
	
    private static void getAverageAttackAP(Ratings trainData, double fillerSize, double attackSize, String writeFilePath, int targetItem) throws Exception{

		int IDStart=1;
		int userNumber = trainData.getMaxUserId();
		int itemNumber = trainData.getMaxItemId();
		long attackUserNumber = Math.round(userNumber * attackSize);
		long fillerItemNumber = Math.round(itemNumber * fillerSize);
		double targetItemRating = trainData.getMaxRating();

		List<ArrayList<Integer>> indicesByItem = trainData.getIndicesByItem();
		ItemAverage itemAvg = new ItemAverage(trainData);
		itemAvg.trainModel();
		List<Double> colAvgList = new ArrayList<>();
		List<Double> colCigList = new ArrayList<>();

		for(int item = 0; item <= itemNumber; item++){
			double itemAvgI = itemAvg.predict(0, item, false);
			colAvgList.add(item, itemAvgI);
			double sum = 0;
			List<Integer> items = indicesByItem.get(item);
			if(items.size() == 0){
				colCigList.add(item, 0d);
			} else {
				for (int i : items) {
					sum += Math.pow(trainData.getRating(i) - itemAvgI, 2);
				}
				colCigList.add(item, Math.sqrt(sum / items.size()));
			}
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writeFilePath)));

		for (int n = 0; n < attackUserNumber; n++) {
			bw.write(1 + " ");
			List<Integer> fillerItemIDList = _2_Average.getFillerItemIDList(fillerItemNumber, IDStart, itemNumber, targetItem);
			for (int i = 0; i < fillerItemIDList.size(); i++) {
				if (fillerItemIDList.get(i) == targetItem) {
					bw.write(fillerItemIDList.get(i) + ":" + targetItemRating + " ");
				} else {
					double avgItem = colAvgList.get(fillerItemIDList.get(i));
					double sigmaItem = colCigList.get(fillerItemIDList.get(i));
					if (avgItem == 0) {
						bw.write(fillerItemIDList.get(i) + ":" + "1 ");
					} else {
						bw.write(fillerItemIDList.get(i) + ":" + GaussianDistribution.getGaussDisRating(trainData, avgItem, sigmaItem) + " ");
					}
				}
			}
			bw.newLine();
		}
		bw.close();
	}
	
    private static List<Integer> getFillerItemIDList(long fillerItemNumber,int IDStart, int IDEnd, int targetItem) {
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
