package com.chen.rcfnmf;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import org.cis.data.ItemAverage;
import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;
import org.cis.data.UserAverage;
import org.cis.data.UserSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RCFNMFSimilarity implements RatingPredictor {
	
	private Ratings ratings;
	private Ratings testData;
	//user features matrix P
	private double globalBias;
	private Algebra algebra;
	private DenseDoubleMatrix2D P;
	//item features matrix Q
	private DenseDoubleMatrix2D Q;

	private DenseDoubleMatrix2D userUp, userDown;
	private DenseDoubleMatrix2D itemUp, itemDown;
	
	//global average of all the ratings
	private int userNumber;
	private int itemNumber;
	private double maxRating;
	private double minRating;
	//feature dimension
	private int feature;
	private List<Integer> rCount, cCount;
	private List<Double> bI, bU;
	private List<Double> userSimilarityList;
	//regularizing coefficient for P
	private double lamuda;
	private int loopNum;

	private List<ArrayList<Integer>> indexByUser;
	private List<ArrayList<Integer>> indexByItem;
	private static final double eps = 2.2204e-16;

	public RCFNMFSimilarity(Ratings ratings, Ratings testData, int feature, double lamuda, int loopNum) {
		this.ratings = ratings;
		this.testData = testData;
		this.feature = feature;
		this.lamuda = lamuda;
		this.loopNum = loopNum;
		this.algebra = new Algebra();
		this.maxRating = ratings.getMaxRating();
		this.minRating = ratings.getMinRating();
		this.userNumber = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.indexByUser = ratings.getIndicesByUser();
		this.indexByItem = ratings.getIndicesByItem();
		this.globalBias = ratings.averageRating();
		this.P = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.Q = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.userUp = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.userDown = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.itemUp = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.itemDown = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
	}
	
	@Override
	public void trainModel() {
		initPQ();
		initRCCount();
		//initUIOffset();
		initUserSimilarity();
		learnFeatures();
	}
	
	public double evaluate(RatingPredictor rp, Ratings ratings) {
		int count = 0;
		double totalErr = 0d;
		for(int user = 1; user <= ratings.getMaxUserId(); user++){
			//double bUValue = bU.get(user);
			double userSValue = userSimilarityList.get(user);
			for(int index : ratings.getIndicesByUser().get(user)){
				int item = ratings.getItem(index);
				//double plus = bI.get(item) + bUValue + userSValue;
				totalErr += Math.pow((rp.predict(user, item, true) + userSValue - ratings.getRating(index)), 2);
				count++;
			}
		}
		return Math.sqrt(totalErr / count);
	}
	
	private void learnFeatures(){
		double[] rmseArr = new double[loopNum + 1];
		for (int iter = 1; iter <= loopNum; iter++) {
			ratings.BuildRandomIndex();
			iterate();
			rmseArr[iter] = evaluate(this, testData);
		}
		int rIndex = 1;
		double minRmse = rmseArr[rIndex];
		for (int iter = 1; iter <= loopNum; iter++) {
			if (rmseArr[iter] < minRmse) {
				minRmse = rmseArr[iter];
				rIndex = iter;
			}
		}
		System.out.println("第" + rIndex + "次迭代, minRmse = " + minRmse);
	}
	
	private void iterate() {
		for(int u = 1; u <= userNumber; u++){
			for(int k = 1; k <= feature; k++){
				userUp.setQuick(u, k, 0d);
				userDown.setQuick(u, k, 0d);
			}
		}
		for(int i = 1; i <= itemNumber; i++){
			for(int k = 1; k <= feature; k++){
				itemUp.setQuick(k, i, 0d);
				itemDown.setQuick(k, i, 0d);
			}
		}
		
		List<Double> normIList = new ArrayList<>();
		for (int item = 0; item <= itemNumber; item++) {
			double totalSquareErr = 0d;
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				totalSquareErr += Math.pow(ratings.getRating(index) - predict(user, item, false), 2);
			}
			normIList.add(item, Math.sqrt(totalSquareErr) + eps);
		}

		for(int user = 1; user <= userNumber; user++){
			for(int index : indexByUser.get(user)){
				int item = ratings.getItem(index);
				double rating = ratings.getRating(index);
				double normI = normIList.get(item);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= feature; k++){
					double itemKI = Q.getQuick(k, item);
					userUp.setQuick(user, k, userUp.getQuick(user, k) + itemKI * rating / normI);
					userDown.setQuick(user, k, userDown.getQuick(user, k) + itemKI * estimateRui / normI);
				}
			}
		}
		for(int item = 1; item <= itemNumber; item++){
			double normI = normIList.get(item);
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				double rating = ratings.getRating(index);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= feature; k++){
					double userUK = P.getQuick(user, k);
					itemUp.setQuick(k, item, itemUp.getQuick(k, item) + userUK * rating / normI);
					itemDown.setQuick(k, item, itemDown.getQuick(k, item) + userUK * estimateRui / normI);
				}
			}
		}

		for (int u = 1; u <= userNumber; u++) {
			int rCountP = rCount.get(u);
			for (int k = 1; k <= feature; k++) {
				userDown.setQuick(u, k, userDown.getQuick(u, k) + rCountP * lamuda * P.getQuick(u, k));
				P.setQuick(u, k, P.getQuick(u, k) * userUp.getQuick(u, k) / (userDown.getQuick(u, k) + eps));
			}
		}
		for (int i = 1; i <= itemNumber; i++) {
			int cCountQ = cCount.get(i);
			for (int k = 1; k <= feature; k++) {
				itemDown.setQuick(k, i, itemDown.getQuick(k, i) + cCountQ * lamuda * Q.getQuick(k, i));
				Q.setQuick(k, i, Q.getQuick(k, i) * itemUp.getQuick(k, i) / (itemDown.getQuick(k, i) + eps));
			}
		}
	}
	
	private void initPQ() {
		RatingPredictor userAvg = new UserAverage(ratings);
		userAvg.trainModel();
		for(int u = 1; u <= userNumber; u++){
			double rowAvg = userAvg.predict(u, 1, true);
			for(int k = 1; k <= feature; k++){
				double random = 0d;
				while(random == 0){
					random = Math.random();
				}
				P.setQuick(u, k, random * rowAvg);
			}
		}
		RatingPredictor itemAvg = new ItemAverage(ratings);
		itemAvg.trainModel();
		for(int i = 1; i <= itemNumber; i++){
			double colAvg = itemAvg.predict(1, i, true);
			for(int k = 1; k <= this.feature; k++){
				double random = 0d;
				while(random == 0){
					random = Math.random();
				}
				Q.setQuick(k, i, random * colAvg);
			}
		}
	}
	
	private void initRCCount(){
		this.rCount = new ArrayList<>();
		for(int user = 0; user <= this.userNumber; user++){			
			int userCount = 0; 
			for(int index : indexByUser.get(user)){
				if(ratings.getRating(index) > 0){
					userCount += 1;
				}
			}
			rCount.add(user, userCount);
		}
		this.cCount = new ArrayList<>();
		for(int item = 0; item <= this.itemNumber; item++){	
			int itemCount = 0;
			for(int index : indexByItem.get(item)){
				if(ratings.getRating(index) > 0){
					itemCount += 1;
				}
			}
			cCount.add(item, itemCount);
		}
	}
	
	public double predict(int userId, int itemId, boolean bound) {
		double userSValue = userSimilarityList.get(userId);
		double result = algebra.mult(P.viewRow(userId), Q.viewColumn(itemId)) + userSValue;
		if(bound){
			if(result > maxRating){
				result = maxRating;
			}
			if(result < minRating){
				result = minRating;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private void initUIOffset() {
		this.bI = new ArrayList<>();
		for(int item = 0; item <= itemNumber; item++){
			double totalErr = 0d;
			List<Integer> indicesList = indexByItem.get(item);
			int size = indicesList.size();
			if(size == 0){
				bI.add(item, 0d);
			} else {
				for(int index : indicesList){
					totalErr += ratings.getRating(index) - globalBias;
				}
				bI.add(item, totalErr / indicesList.size());
			}
		}
		this.bU = new ArrayList<>();
		for(int user = 0; user <= userNumber; user++){
			double totalErr = 0d;
			List<Integer> indicesList = indexByUser.get(user);
			int size = indicesList.size();
			if(size == 0){
				bU.add(user, 0d);
			} else {				
				for(int index : indicesList){
					totalErr += ratings.getRating(index) - globalBias - bI.get(ratings.getItem(index));
				}
				bU.add(user, totalErr / indicesList.size());
			}
		}
	}
	
	//求用户相似度
	public void initUserSimilarity() {
		this.userSimilarityList = new ArrayList<>();
		UserAverage userAverage = new UserAverage(ratings);
		userAverage.trainModel();
		UserSimilarity userSimilarity = new UserSimilarity(ratings);
		for (int user = 0; user <= userNumber; user++) {
			List<Map.Entry<Integer, Double>> kuList = userSimilarity.getKuv(user);
			int n = 0;
			double totalErr = 0d;
			for (Map.Entry<Integer, Double> kuMap : kuList) {
				int userSId = kuMap.getKey();
				double userVAvg = userAverage.predict(userSId, 1, true);
				double itemErr = 0d;
				for (int index : indexByUser.get(userSId)) {
					double rating = ratings.getRating(index);
					itemErr += rating - userVAvg;
				}
				totalErr += itemErr * kuMap.getValue();
				n++;
				if (n >= 60) {
					break;
				}
			}
			userSimilarityList.add(user, totalErr / Math.sqrt(60));
		}
	}
}