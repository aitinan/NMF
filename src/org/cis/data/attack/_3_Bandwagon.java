package org.cis.data.attack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.cis.data.Ratings;
import org.cis.io.MovielensRatingsReader;
import org.cis.data.util.GaussianDistribution;

public class _3_Bandwagon {

	public static void main(String[] args) throws Exception {
		
		double[] fsArr = { 0.001, 0.003, 0.005, 0.007 };
		//double[] asArr = { 0.01, 0.03, 0.05, 0.07, 0.10 };
        double[] asArr = { 0.01 };
		Ratings trainData = MovielensRatingsReader.readS("D:\\rcfnmf\\data/netflix/train.txt");
		int targetItem = 7052;
		
		for (double fs : fsArr) {
			for (double as : asArr) {
				String writeFilePath = "D:\\rcfnmf\\data/netflix/"+targetItem+"/bandwagon/FS" + fs + "AS" + as + ".txt";
				_3_Bandwagon.getBandwagonAttackAP(trainData, fs, as, writeFilePath, targetItem);
			}
		}
		System.out.println("finish.");
	}

	private static void getBandwagonAttackAP(Ratings trainData, double fillerSize, double attackSize, String writeFilePath, int targetItem) throws Exception {
		
		int userNumber = trainData.getMaxUserId();
        int itemNumber = trainData.getMaxItemId();
        // push attack 5, nuke attack 1
		int targetItemRating = 5;
		double average = trainData.averageRating();
		double sigma = trainData.cigRating();
		
		long attackUserNumber = Math.round(userNumber * attackSize);
        long fillerItemNumber = Math.round(itemNumber * fillerSize);
        
        List<Entry<Integer, Integer>> popItemOrder = sortItemCount(trainData);
        List<Integer> listPopularItem = new ArrayList<>();
        int num = 0;
        for (Entry<Integer, Integer> map : popItemOrder) {
			listPopularItem.add(map.getKey());
			num++;
			if (num >= 24) {
				break;
			}
		}
        
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writeFilePath)));
		for (int n = 0; n < attackUserNumber; n++) {
			bw.write(1 + " ");
			List<Integer> fillerItemIDList = _3_Bandwagon.getFillerItemIDList(fillerItemNumber, 1, itemNumber, targetItem, listPopularItem);
			for (Integer aFillerItemIDList : fillerItemIDList) {
				if (aFillerItemIDList == targetItem || listPopularItem.contains(aFillerItemIDList)) {
					bw.write(aFillerItemIDList + ":" + targetItemRating + " ");
				} else {
					bw.write(aFillerItemIDList + ":" + GaussianDistribution.getGaussDisRating(trainData, average, sigma) + " ");
				}
			}
			bw.newLine();
		}
		bw.close();
	}
	
	private static List<Entry<Integer, Integer>> sortItemCount(Ratings trainData) {
		int totalItem = trainData.getMaxItemId();
        List<ArrayList<Integer>> indicesByItem = trainData.getIndicesByItem();

        Map<Integer, Integer> itemMap = new HashMap<>();
        for(int item = 1; item <= totalItem; item++){
            itemMap.put(item, indicesByItem.get(item).size());
        }
        
        List<Entry<Integer, Integer>> list = new LinkedList<>(itemMap.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return list;
	}

	private static List<Integer> getFillerItemIDList(long fillerItemNumber, int IDStart, int IDEnd, int targetItem, List<Integer> listPopularItem) {
		List<Integer> listFillerItemID = new ArrayList<>();
		// add targetItem
		listFillerItemID.add(targetItem); 
		// add popularItem
		listFillerItemID.addAll(listPopularItem);
		Random r = new Random();
		while (listFillerItemID.size() < fillerItemNumber) {
			int itemID = r.nextInt(IDEnd - IDStart + 1);
			itemID += IDStart;
			if (!listFillerItemID.contains(itemID) && itemID != targetItem && !listPopularItem.contains(itemID)) {
				// item not repeat && //not targetItem && //not popularItem
				listFillerItemID.add(itemID);
			}
		}
		Collections.sort(listFillerItemID);
		return listFillerItemID;
	}

}
