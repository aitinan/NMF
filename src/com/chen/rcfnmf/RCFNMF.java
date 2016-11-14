package com.chen.rcfnmf;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.chen.pca.FlagBean;
import org.cis.data.ItemAverage;
import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;
import org.cis.data.UserAverage;
import org.cis.eval.RmseEvaluator;

import java.util.ArrayList;
import java.util.List;

class RCFNMF implements RatingPredictor {
	
	private Ratings ratings;
	private Ratings testData;
	private Algebra algebra;
	private DenseDoubleMatrix2D W;
	//item features matrix Q
	private DenseDoubleMatrix2D H;

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
	
	//regularizing coefficient for P
	private double lamuda;
	private int loopNum;

	private List<ArrayList<Integer>> indexByUser;
	private List<ArrayList<Integer>> indexByItem;
	private static final double eps = 2.2204e-16;

	private FlagBean flagBean;

	RCFNMF(Ratings ratings, Ratings testData, int feature, double lamuda, int loopNum) {
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
		this.W = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.H = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.userUp = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.userDown = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.itemUp = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.itemDown = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
	}

	RCFNMF(Ratings ratings, Ratings testData, FlagBean flagBean, int feature, double lamuda, int loopNum) {
		this.ratings = ratings;
		this.testData = testData;
		this.flagBean = flagBean;
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
		this.W = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.H = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.userUp = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.userDown = new DenseDoubleMatrix2D(userNumber+1, this.feature+1);
		this.itemUp = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
		this.itemDown = new DenseDoubleMatrix2D(this.feature+1, itemNumber+1);
	}

	@Override
	public void trainModel() {
		initWH();
		initRCCount();
		learnFeatures();
	}

	void trainModelFlag() {
		initWH();
		initRCCount();
		learnFeaturesFlag();
	}

	private void learnFeatures(){
		double[] rmseArr = new double[loopNum+1];
		for(int iter = 1; iter <= loopNum; iter++){
			iterate();
			rmseArr[iter] = RmseEvaluator.evaluate(this, testData);
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
	
	private void learnFeaturesFlag(){
		double[] rmseArr = new double[loopNum+1];
		for(int iter = 1; iter <= loopNum; iter++){
			iterateFlag();
			rmseArr[iter] = RmseEvaluator.evaluate(this, testData);
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
		
		//②计算每个项目的范数
		List<Double> normIList = new ArrayList<>();
		for (int item = 0; item <= itemNumber; item++) {
			double totalSquareErr = 0d;
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				totalSquareErr += Math.pow(ratings.getRating(index) - predict(user, item, false), 2);
			}
			normIList.add(item, Math.sqrt(totalSquareErr) + eps);
		}
		
		//③计算userUp、userDown、itemUp、itemDown的值
		for(int user = 1; user <= userNumber; user++){
			for(int index : indexByUser.get(user)){
				int item = ratings.getItem(index);
				double rating = ratings.getRating(index);
				double normI = normIList.get(item);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= feature; k++){
					double itemKI = H.getQuick(k, item);
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
					double userUK = W.getQuick(user, k);
					itemUp.setQuick(k, item, itemUp.getQuick(k, item) + userUK * rating / normI);
					itemDown.setQuick(k, item, itemDown.getQuick(k, item) + userUK * estimateRui / normI);
				}
			}
		}
		
		//④更新用户特征矩阵W和项目特征矩阵H
		for (int u = 1; u <= userNumber; u++) {
			int rCountP = rCount.get(u);
			for (int k = 1; k <= feature; k++) {
				userDown.setQuick(u, k, userDown.getQuick(u, k) + rCountP * lamuda * W.getQuick(u, k));
				W.setQuick(u, k, W.getQuick(u, k) * userUp.getQuick(u, k) / (userDown.getQuick(u, k) + eps));
			}
		}
		for (int i = 1; i <= itemNumber; i++) {
			int cCountQ = cCount.get(i);
			for (int k = 1; k <= feature; k++) {
				itemDown.setQuick(k, i, itemDown.getQuick(k, i) + cCountQ * lamuda * H.getQuick(k, i));
				H.setQuick(k, i, H.getQuick(k, i) * itemUp.getQuick(k, i) / (itemDown.getQuick(k, i) + eps));
			}
		}
	}

	private void iterateFlag() {
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

		//②计算每个项目的范数
		List<Double> normIList = new ArrayList<>();
		for (int item = 0; item <= itemNumber; item++) {
			double totalSquareErr = 0d;
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				totalSquareErr += Math.pow(ratings.getRating(index) - predict(user, item, false), 2);
			}
			normIList.add(item, Math.sqrt(totalSquareErr) + eps);
		}

		//③计算userUp、userDown、itemUp、itemDown的值
		for(int user = 1; user <= userNumber; user++){
			for(int index : indexByUser.get(user)){
				int item = ratings.getItem(index);
				double rating = ratings.getRating(index);
				double normI = normIList.get(item);
				double estimateRui = predict(user, item, false);
				for(int k = 1; k <= feature; k++){
					double itemKI = H.getQuick(k, item);
					userUp.setQuick(user, k, userUp.getQuick(user, k) + itemKI * rating / normI);
					userDown.setQuick(user, k, userDown.getQuick(user, k) + itemKI * estimateRui / normI);
				}
			}
		}
		for(int item = 1; item <= itemNumber; item++){
			double normI = normIList.get(item);
			for(int index : indexByItem.get(item)){
				int user = ratings.getUser(index);
				boolean flag = true;
				for (int i = flagBean.getStartFlag(); i < flagBean.getEndFlag(); i++) {
					if (user == flagBean.getFlagUser()[i]) {
						flag = false;
					}
				}
				if (flag) {
					double rating = ratings.getRating(index);
					double estimateRui = predict(user, item, false);
					for(int k = 1; k <= feature; k++){
						double userUK = W.getQuick(user, k);
						itemUp.setQuick(k, item, itemUp.getQuick(k, item) + userUK * rating / normI);
						itemDown.setQuick(k, item, itemDown.getQuick(k, item) + userUK * estimateRui / normI);
					}
				}
			}
		}

		//④更新用户特征矩阵W和项目特征矩阵H
		for (int u = 1; u <= userNumber; u++) {
			int rCountP = rCount.get(u);
			for (int k = 1; k <= feature; k++) {
				userDown.setQuick(u, k, userDown.getQuick(u, k) + rCountP * lamuda * W.getQuick(u, k));
				W.setQuick(u, k, W.getQuick(u, k) * userUp.getQuick(u, k) / (userDown.getQuick(u, k) + eps));
			}
		}
		for (int i = 1; i <= itemNumber; i++) {
			int cCountQ = cCount.get(i);
			for (int k = 1; k <= feature; k++) {
				itemDown.setQuick(k, i, itemDown.getQuick(k, i) + cCountQ * lamuda * H.getQuick(k, i));
				H.setQuick(k, i, H.getQuick(k, i) * itemUp.getQuick(k, i) / (itemDown.getQuick(k, i) + eps));
			}
		}
	}
	
	//①初始化用户特征矩阵W和项目特征矩阵H
	private void initWH() {
		RatingPredictor userAvg = new UserAverage(ratings);
		userAvg.trainModel();
		for(int u = 1; u <= userNumber; u++){
			double rowAvg = userAvg.predict(u, 1, true);
			for(int k = 1; k <= feature; k++){
				double random = 0d;
				while(random == 0){
					random = Math.random();
				}
				W.setQuick(u, k, random * rowAvg);
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
				H.setQuick(k, i, random * colAvg);
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
		double result = algebra.mult(W.viewRow(userId), H.viewColumn(itemId));
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
	
	public void predict(int userId, int itemId) {
		algebra.mult(W.viewRow(userId), H.viewColumn(itemId));
	}
}