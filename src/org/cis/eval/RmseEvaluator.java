package org.cis.eval;

import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;

/**
 * This class implementing the RMSE metric
 *
 * @author Zhang Si (zhangsi.cs@gmail.com)
 *
 */
public class RmseEvaluator {

	/**
	 * Calculate the RMSE performance of a RatingPredictor
	 */
	public static double evaluate(RatingPredictor rp, Ratings ratings) {
		double rmse = 0;
		int count = ratings.getCount();
		for(int index = 0; index < count; index++){
			rmse += Math.pow(rp.predict(ratings.getUser(index), ratings.getItem(index), true) - ratings.getRating(index), 2);
		}
		return Math.sqrt(rmse / count);
	}
}
