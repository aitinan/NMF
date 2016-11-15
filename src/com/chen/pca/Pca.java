package com.chen.pca;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import org.cis.data.Ratings;
import org.cis.data.UserAverage;

import cern.colt.matrix.DoubleMatrix2D;

import java.util.*;

public class Pca {

	private Ratings ratings;
	private int userNumber;
	private int itemNumber;
	private DoubleMatrix2D Zui;
	private List<ArrayList<Integer>> indicesByUser;

	public Pca(Ratings ratings) {
		this.ratings = ratings;
		this.userNumber = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.Zui = new DenseDoubleMatrix2D(userNumber, itemNumber);
		this.indicesByUser = ratings.getIndicesByUser();
	}
	
	public FlagBean pca(int trainUserNum, double fs, double as) {
		z_scores();
		Svd zsvd = new Svd(Zui, userNumber, itemNumber);
		zsvd.svd();

		DoubleMatrix2D U = zsvd.getU();
		DoubleMatrix2D pca1 = zsvd.getPca(U, 0, userNumber - 1, 0, 0);
		DoubleMatrix2D pca2 = zsvd.getPca(U, 0, userNumber - 1, 1, 1);
		DoubleMatrix2D pca3 = zsvd.getPca(U, 0, userNumber - 1, 2, 2);
		
		double[] ratingArr = new double[userNumber];
		double totalRating = 0d;
		for (int u = 0; u < userNumber; u++) {
			ratingArr[u] = (Math.abs(pca1.get(u, 0)) + Math.abs(pca2.get(u, 0)) + Math.abs(pca3.get(u, 0))) / 3;
			totalRating += ratingArr[u];
		}

		double[] usersScore = new double[userNumber];
		for (int u = 0; u < userNumber; u++) {
			usersScore[u] = ratingArr[u] / totalRating;
		}

		int r1 = 0;
		double d = 1.0 / userNumber;
		for (double rating : usersScore) {
			if (rating < d) {
				r1++;
			}
		}

		int r2 = (int) (trainUserNum * as);
		int flag = r1 < r2 ? r1 : r2;

		double[] usersScoreS = new double[userNumber];
		System.arraycopy(usersScore, 0, usersScoreS, 0, userNumber);
		Arrays.sort(usersScoreS);

		List<Integer> idList = new ArrayList<>();
		for (int i = 0; i < usersScore.length; i++) {
			for (int j = 0; j < usersScoreS.length; j++) {
				if (usersScoreS[i] == usersScore[j]) {
					idList.add(i, j + 1);
				}
			}
		}

		// 求用户评分背离度和用户与其他用户相适应度
		double[] count = new double[flag];
		double[] sum = new double[flag];
		double[] mean = new double[flag];

		for (int i = 0; i < ratings.getCount(); i++)  {
			for (int j = 0; j < flag; j++) {
				if (ratings.getUser(i) == idList.get(j)) {
					count[j]++;
					sum[j] = ratings.getRating(i);
				}
			}
		}

		for (int i = 0; i < flag; i++) {
			mean[i] = sum[i] / count[i];
		}

		double[] SUDR = new double[flag];
		for (int i = 0; i < ratings.getCount(); i++) {
			for (int j = 0; j < flag; j++) {
				if (ratings.getUser(i) == idList.get(j)) {
					SUDR[j] = Math.abs(ratings.getRating(i) - mean[j]);
				}
			}
		}

		double[] SUDRAsc  = new double[flag];
		System.arraycopy(SUDR, 0, SUDRAsc, 0, flag);
		Arrays.sort(SUDRAsc);
		int[] userSDUR = new int[flag];
		for (int i = 0; i < flag; i++) {
			for (int j = 0; j < flag; j ++) {
				if (SUDRAsc[i] == SUDR[j]) {
					userSDUR[i] = idList.get(j);
				}
			}
		}
		return new FlagBean((int) (trainUserNum * fs * as), flag, userSDUR);
	}
	
	// 生成规范化的评分矩阵
	private void z_scores() {
		UserAverage userAverage = new UserAverage(ratings);
		userAverage.trainModel();
		for (int user = 1; user <= userNumber; user++) {
			double userAvg = userAverage.predict(user);
			List<Integer> indicesList = indicesByUser.get(user);
			double totalErr = 0d;
			for (int index : indicesList) {
				totalErr += Math.pow(ratings.getRating(index) - userAvg, 2);
			}
			double userStdDev = Math.sqrt(totalErr / (itemNumber - 1));
			for (int item = 1; item <= itemNumber; item++) {
				Zui.setQuick(user - 1, item - 1, (Util.getRating(ratings, indicesByUser, user, item) - userAvg) / userStdDev);
			}
		}
	}
}
