package org.cis.data;

import java.util.*;
import java.util.Map.Entry;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class UserSimilarity {
	
	private Ratings ratings;
	private double globalBias;
	private DenseDoubleMatrix2D Zui, Kuv;
	private int userNumber, itemNumber;
	private List<ArrayList<Integer>> indicesByUser;
	
	public UserSimilarity(Ratings ratings) {
		this.ratings = ratings;
		this.userNumber = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.globalBias = ratings.averageRating();
		this.indicesByUser = ratings.getIndicesByUser();
		this.Kuv = new DenseDoubleMatrix2D(userNumber+1, userNumber+1);
		this.Zui = new DenseDoubleMatrix2D(userNumber+1, itemNumber+1);
	}
	
	private void z_scores() {
		UserAverage userAverage = new UserAverage(ratings);
		userAverage.trainModel();
		for(int user = 1; user <= userNumber; user++){
			double userAvg = userAverage.predict(user, 1, true);
			List<Integer> indicesList = indicesByUser.get(user);
			double totalErr = 0d; 
			for(int index : indicesList){
				totalErr += Math.pow(ratings.getRating(index) - userAvg, 2);
			}
			double userStdDev = Math.sqrt(totalErr / (indicesList.size() - 1));//?到底是除以indicesList.size()(user实际评分的项目数)，还是除以itemNumber(总项目数)。。。
			for(int index : indicesList){
				Zui.setQuick(user, ratings.getItem(index), (ratings.getRating(index) - userAvg) / userStdDev);
			}
		}
	}
	
	public void initKuv() {
		this.z_scores();
		for(int user_1 = 1; user_1 < userNumber; user_1++){
			for(int user_2 = user_1 + 1 ; user_2 <= userNumber; user_2++){
				double totalErr = 0d;
				for(int item = 1; item <= itemNumber; item++){
					totalErr += Math.pow(Zui.getQuick(user_1, item) - Zui.getQuick(user_2, item), 2);
				}
				double exponential = - totalErr / (2 * ratings.varianceRating());
				Kuv.setQuick(user_1, user_2, Math.sqrt(2 - 2 * Math.exp(exponential)) / globalBias);
			}
		}
	}
	
	public List<Map.Entry<Integer, Double>> getKuv(int user_1) {
		Map<Integer, Double> kuMap = new HashMap<>();
		for(int user_2 = 0; user_2 <= userNumber; user_2++){
			if(user_2 < user_1){
				kuMap.put(user_2, Kuv.getQuick(user_2, user_1));
			}
			kuMap.put(user_2, Kuv.getQuick(user_1, user_2));
		}
		List<Map.Entry<Integer, Double>> kuList = new ArrayList<Map.Entry<Integer,Double>>(kuMap.entrySet());
		Collections.sort(kuList, new Comparator<Map.Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> arg0, Entry<Integer, Double> arg1) {
				if(Double.parseDouble(arg0.getValue().toString()) < Double.parseDouble(arg1.getValue().toString())){
					return 1;
				} else if(Double.parseDouble(arg0.getValue().toString()) == Double.parseDouble(arg1.getValue().toString())){
					return 0;
				} else {					
					return -1;
				}
			}
		});
		return kuList;
	}
}
