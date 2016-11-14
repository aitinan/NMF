package org.cis.data.attack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cis.data.ItemAverage;
import org.cis.data.Ratings;
import org.cis.data.util.GaussianDistribution;
import org.cis.data.util.Random_;
import org.cis.io.MovielensRatingsReader;


public class _5_MyAoP30 {

	public static void main(String[] args) throws Exception {
		
		double x = 0.3;
		double[] fsArr = { 0.001, 0.003, 0.005, 0.007 };
		double[] asArr = { 0.01, 0.03, 0.05, 0.07, 0.10 };
		Ratings trainData = MovielensRatingsReader.readS("data/netflix/train.txt");
		int targetItem = 7052;
		
		for(double fs : fsArr){
			for(double as : asArr){
				String writeFilePath = "data/netflix/aop30/FS" + fs + "AS" + as + ".txt";
				_5_MyAoP30.getAopAttackData(trainData, x, fs, as, writeFilePath, targetItem);
			}
		}
		trainData.clear();
		System.out.println("finish.");
	}
	
	public static void getAopAttackData(Ratings trainData, double x, double fillerSize, double attackSize, String writeFilePath, int targetItem) throws Exception {
		
		int itemNumber = trainData.getMaxItemId();
		int userNumber = trainData.getMaxUserId();
		double maxRatingValue = trainData.getMaxRating();
		
		List<ArrayList<Integer>> indicesByItem = trainData.getIndicesByItem();
		ItemAverage itemAverage = new ItemAverage(trainData);
		itemAverage.trainModel();
		
		List<Double> colAvgList = new ArrayList<>();
		List<Double> colCigList = new ArrayList<>();
		
		for(int item = 0; item <= itemNumber; item++){
			double itemAvgI = itemAverage.predict(0, item, false);
			colAvgList.add(item, itemAvgI);
			double sum = 0;
			List<Integer> items = indicesByItem.get(item);
			if(items.size() == 0){
				colCigList.add(item, 0d);
			} else {
				for(int i : items) {
					sum += Math.pow(trainData.getRating(i) - itemAvgI, 2);
				}
				colCigList.add(item, Math.sqrt(sum / items.size()));
			}
		}
		
		long attackUserNumber = Math.round(userNumber * attackSize);
		long fillerItemNumber = Math.round(itemNumber * fillerSize);
		List<Map.Entry<Integer, Integer>> popItemOrder = sortItemCount(trainData);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writeFilePath)));
		for(int u = 0; u < attackUserNumber; u++){
			bw.write(1 + " ");
			List<Integer> fillerItemIDList = _5_MyAoP30.getFillerItemIDList(userNumber, itemNumber, targetItem, fillerItemNumber, x, popItemOrder);
			for(int i = 0; i < fillerItemNumber; i++){
				if (fillerItemIDList.get(i) == targetItem) {
					bw.write(fillerItemIDList.get(i) + ":" + maxRatingValue + " ");
				} else {
					bw.write(fillerItemIDList.get(i) + ":" + GaussianDistribution.getGaussDisRating(trainData, colAvgList.get(fillerItemIDList.get(i)), colCigList.get(fillerItemIDList.get(i))) + " ");
				}
			}
			bw.newLine();
		}
		bw.close();
		
		
	}
	
	private static List<Map.Entry<Integer, Integer>> sortItemCount(Ratings trainData) {
		int totalItem = trainData.getMaxItemId();
        List<ArrayList<Integer>> indicesByItem = trainData.getIndicesByItem();

        Map<Integer, Integer> itemMap = new HashMap<>();
        for(int item = 1; item <= totalItem; item++){
            itemMap.put(item, indicesByItem.get(item).size());
        }
        
        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(itemMap.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				if(Integer.parseInt(o1.getValue().toString()) > Integer.parseInt(o2.getValue().toString())){
	                return -1;
	            }
	            if(Integer.parseInt(o1.getValue().toString()) == Integer.parseInt(o2.getValue().toString())){
	                return 0;
	            } else {
	                return 1;
	            }
			}
		});
        return list;
	}
	
	private static List<Integer> getFillerItemIDList(int userNumber, int itemNumber, int targetItem, long fillerItemNumber, double x, List<Map.Entry<Integer, Integer>> popItemOrder) throws Exception{
		int fillerItemNum = (int) Math.round(x * itemNumber);
		List<Integer> listPopItemID = new ArrayList<Integer>();
		int n = 0; 
		for (Map.Entry<Integer, Integer> map : popItemOrder) {
			listPopItemID.add(map.getKey());
			n++;
			if (n >= fillerItemNum) {
				break;
			}
		}
		
		List<Integer> fillerItemIDList = new ArrayList<Integer>();
		fillerItemIDList.add(targetItem);
		while(fillerItemIDList.size() < fillerItemNumber){
			int itemID = Random_.getRandom(1, itemNumber);
			if (!fillerItemIDList.contains(itemID) && itemID != targetItem  && listPopItemID.contains(itemID)) {
				fillerItemIDList.add(itemID);
			}
		}
		Collections.sort(fillerItemIDList);
		return fillerItemIDList;
	}
}
