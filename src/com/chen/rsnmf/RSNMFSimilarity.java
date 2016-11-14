package com.chen.rsnmf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cis.data.ItemAverage;
import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;
import org.cis.data.UserAverage;
import org.cis.data.UserSimilarity;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class RSNMFSimilarity implements RatingPredictor {
	
	private Ratings ratings;
	private Ratings testData;
	private double globalBias;
	private Algebra algebra;
	//user features matrix P
	private DenseDoubleMatrix2D P;
	//item features matrix Q
	private DenseDoubleMatrix2D Q;
	
	private DenseDoubleMatrix2D userUp, userDown;
	private DenseDoubleMatrix2D itemUp, itemDown;
	
	private int userNumber;
	private int itemNumber;
	
	//feature dimension
	private int feature;
	List<Integer> rCount, cCount;
	private List<Double> bI, bU;
	private List<Double> userSimilarityList;
	//regularizing coefficient for P&Q
	private double lamuda;
	private int loopNum;
	
	private List<ArrayList<Integer>> indexByUser;
	private List<ArrayList<Integer>> indexByItem;
	
	public RSNMFSimilarity(Ratings ratings, Ratings testData, int feature, double lamuda, int loopNum) {
		this.ratings = ratings;
		this.testData = testData;
		this.feature = feature;
		this.algebra = new Algebra();
		this.globalBias = this.ratings.averageRating();
		this.userNumber = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.indexByUser = ratings.getIndicesByUser();
		this.indexByItem = ratings.getIndicesByItem();
		this.P = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.Q = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.userUp = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.userDown = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.itemUp = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.itemDown = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.lamuda = lamuda;
		this.loopNum = loopNum;
		this.rCount = new ArrayList<>();
		this.cCount = new ArrayList<>();
	}
	
	//初始化PQ
	private void initPQ(){
		RatingPredictor userAvg = new UserAverage(ratings);
		userAvg.trainModel();
		for(int u = 1; u <= userNumber; u++){
			double rowAvg = userAvg.predict(u, 1, true);
			for(int k = 1; k <= this.feature; k++){
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
	
	//初始化RCCount
	private void initRCCount(){
		for(int user = 0; user <= this.userNumber; user++){			
			int userCount = 0; 
			for(int index : indexByUser.get(user)){
				if(ratings.getRating(index) > 0){
					userCount += 1;
				}
			}
			rCount.add(user, userCount);
		}
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
	
	private void iterate() {
		for(int u = 1; u <= userNumber; u++){
			for(int k = 1; k <= this.feature; k++){
				userUp.setQuick(u, k, 0);
				userDown.setQuick(u, k, 0);
			}
		}
		for(int i = 1; i <= itemNumber; i++){
			for(int k = 1; k <= this.feature; k++){
				itemUp.setQuick(k, i, 0);
				itemDown.setQuick(k, i, 0);
			}
		}
		
		//更新辅助矩阵userUp、userDown
		for(int user = 1; user <= userNumber; user++){
			for(int index : indexByUser.get(user)){
				int item = ratings.getItem(index);
				double rating = ratings.getRating(index);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= this.feature; k++){
					double itemKI = Q.getQuick(k, item);
					userUp.setQuick(user, k, userUp.getQuick(user, k) 
							+ itemKI * rating);
					userDown.setQuick(user, k, userDown.getQuick(user, k) 
							+ itemKI * estimateRui);
				}
			}
		}
		for(int item = 1; item <= itemNumber; item++){
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				double rating = ratings.getRating(index);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= this.feature; k++){
					double userUK = P.getQuick(user, k);
					itemUp.setQuick(k, item, itemUp.getQuick(k, item) 
							+ userUK * rating);
					itemDown.setQuick(k, item, itemDown.getQuick(k, item) 
							+ userUK * estimateRui);
				}
			}
		}
		
		for (int u = 1; u <= userNumber; u++) {
			int rCountP = rCount.get(u);
			if(rCountP > 0){	
				for (int k = 1; k <= this.feature; k++) {
					userDown.setQuick(u, k, userDown.getQuick(u, k) 
							+ rCountP * lamuda * P.getQuick(u, k));
					P.setQuick(u, k, P.getQuick(u, k) * userUp.getQuick(u, k) 
							/ userDown.getQuick(u, k));
				}
			}
		}
		for (int i = 1; i <= itemNumber; i++) {
			int cCountQ = cCount.get(i);
			if(cCountQ > 0){
				for (int k = 1; k <= this.feature; k++) {
					itemDown.setQuick(k, i, itemDown.getQuick(k, i) 
							+ cCountQ * lamuda * Q.getQuick(k, i));
					Q.setQuick(k, i, Q.getQuick(k, i) * itemUp.getQuick(k, i) 
							/ itemDown.getQuick(k, i));
				}
			}	
		}
	}
	
	public double predict(int userId, int itemId, boolean bound) {
		double plus = userSimilarityList.get(userId);
		double result = algebra.mult(P.viewRow(userId), Q.viewColumn(itemId)) - plus;
		if(bound){
			double maxRating = ratings.getMaxRating();
			double minRating = ratings.getMinRating();
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
	
	//@SuppressWarnings("unused")
	private void initUserSimilarity() {
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
	
	@Override
	public void trainModel() {
		initPQ();
		initRCCount();
		//initUIOffset();
		initUserSimilarity();
		learnFeatures();
	}
	
	private void learnFeatures(){
		double[] rmseArr = new double[loopNum+1];
		for(int iter = 1; iter <= loopNum; iter++){
			//long start = System.currentTimeMillis();
			this.ratings.getRandomIndex();
			iterate();
			//System.out.println("第" + iter + "次迭代，costs " + (System.currentTimeMillis() - start));
			rmseArr[iter] = this.evaluate(this, testData);
			//System.out.println("第" + iter + "次迭代, Rmse = " + rmseArr[iter]);
		}

		int rIndex = 1;
		double minRmse = rmseArr[rIndex];
		for(int iter = 1; iter <= loopNum; iter++){
			if(rmseArr[iter] < minRmse){
				minRmse = rmseArr[iter];
				rIndex = iter;
			}
		}
		System.out.println("第" + rIndex + "次迭代, minRmse = " + minRmse);
	}
	
	public double evaluate(RatingPredictor rp, Ratings ratings) {
		int count = 0;
		double totalErr = 0d;
		for(int user = 1; user <= ratings.getMaxUserId(); user++){
			//double bUValue = bU.get(user);
			double userSValue = userSimilarityList.get(user);
			for(int index : ratings.getIndicesByUser().get(user)){
				int item = ratings.getItem(index);
				//double plus = userSValue;
				totalErr += Math.pow((rp.predict(user, item, true) - userSValue - ratings.getRating(index)), 2);
				count++;
			}
		}
		return Math.sqrt(totalErr / count);
	}
}