package com.chen.rsnmf;

import java.util.ArrayList;
import java.util.List;

import org.cis.data.ItemAverage;
import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;
import org.cis.data.UserAverage;
import org.cis.eval.RmseEvaluator;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class RSNMFBiases implements RatingPredictor {
	
	Ratings ratings;
	Ratings testData;
	Algebra algebra;
	//user features matrix P
	DenseDoubleMatrix2D P;
	//item features matrix Q
	DenseDoubleMatrix2D Q;
	
	DenseDoubleMatrix2D userUp, userDown;
	DenseDoubleMatrix2D itemUp, itemDown;
	
	int userNumber;
	int itemNumber;
	
	//feature dimension
	int f;
	List<Integer> rCount, cCount;
	//regularizing coefficient for P&Q
	double lamuda;
	int loopNum;
	
	List<ArrayList<Integer>> indexByUser;
	List<ArrayList<Integer>> indexByItem;
	
	public RSNMFBiases(Ratings ratings, Ratings testData, int feature, double lamuda, int loopNum) {
		this.ratings = ratings;
		this.testData = testData;
		this.algebra = new Algebra();
		this.f = feature;
		this.userNumber = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.indexByUser = ratings.getIndicesByUser();
		this.indexByItem = ratings.getIndicesByItem();
		this.P = new DenseDoubleMatrix2D(userNumber+1, f+1);
		this.Q = new DenseDoubleMatrix2D(f+1, itemNumber+1);
		this.userUp = new DenseDoubleMatrix2D(userNumber+1, f+1);
		this.userDown = new DenseDoubleMatrix2D(userNumber+1, f+1);
		this.itemUp = new DenseDoubleMatrix2D(f+1, itemNumber+1);
		this.itemDown = new DenseDoubleMatrix2D(f+1, itemNumber+1);
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
			for(int k = 1; k <= this.f; k++){
				double random = 0d;
				while(random == 0){
					random = Math.random();
				}
				if(u == 1){
					P.setQuick(u, k, 1);
				}				
				P.setQuick(u, k, random * rowAvg);
			}
		}
		RatingPredictor itemAvg = new ItemAverage(ratings);
		itemAvg.trainModel();
		for(int i = 1; i <= itemNumber; i++){
			double colAvg = itemAvg.predict(1, i, true);		
			for(int k = 1; k <= this.f; k++){
				double random = 0d;
				while(random == 0){
					random = Math.random();
				}
				if(i == 2){
					Q.setQuick(k, i, 1);					
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
			for(int k = 1; k <= f; k++){
				userUp.setQuick(u, k, 0);
				userDown.setQuick(u, k, 0);
			}
		}
		for(int i = 1; i <= itemNumber; i++){
			for(int k = 1; k <= f; k++){
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
				for(int k = 1; k <= f; k++){
					double itemKI = Q.getQuick(k, item);
					userUp.setQuick(user, k, userUp.getQuick(user, k) + itemKI * rating);
					userDown.setQuick(user, k, userDown.getQuick(user, k) + itemKI * estimateRui);
				}
			}
		}
		for(int item = 1; item <= itemNumber; item++){
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				double rating = ratings.getRating(index);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= f; k++){
					double userUK = P.getQuick(user, k);
					itemUp.setQuick(k, item, itemUp.getQuick(k, item) + userUK * rating);
					itemDown.setQuick(k, item, itemDown.getQuick(k, item) + userUK * estimateRui);
				}
			}
		}
		
		for (int u = 1; u <= userNumber; u++) {
			int rCountP = rCount.get(u);
			if(rCountP > 0){	
				for (int k = 1; k <= this.f; k++) {
					userDown.setQuick(u, k, userDown.getQuick(u, k) + rCountP * lamuda * P.getQuick(u, k));
					if(u == 1){						
						P.setQuick(u, k, 1);
					}
					P.setQuick(u, k, P.getQuick(u, k) * userUp.getQuick(u, k) / userDown.getQuick(u, k));
				}
			}
		}
		for (int i = 1; i <= itemNumber; i++) {
			int cCountQ = cCount.get(i);
			if(cCountQ > 0){
				for (int k = 1; k <= this.f; k++) {
					itemDown.setQuick(k, i, itemDown.getQuick(k, i) + cCountQ * lamuda * Q.getQuick(k, i));
					if( i == 2){
						Q.setQuick(k, i, 1);
					}
					Q.setQuick(k, i, Q.getQuick(k, i) * itemUp.getQuick(k, i) / itemDown.getQuick(k, i));
				}
			}	
		}
	}
	
	public double predict(int userId, int itemId, boolean bound) {
		double result = algebra.mult(P.viewRow(userId), Q.viewColumn(itemId));
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
	
	private void learnFeatures(){

		double[] rmseArr = new double[loopNum+1];
		for(int iter = 1; iter <= loopNum; iter++){
			ratings.getRandomIndex();
			iterate();
			rmseArr[iter] = RmseEvaluator.evaluate(this, testData);
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

	@Override
	public void trainModel() {
		initPQ();
		initRCCount();
		learnFeatures();
	}
}