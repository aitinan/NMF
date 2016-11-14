package org.cis.eval;

import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;

/**
 * This class implementing the MAE metric 
 * 
 * @author Zhang Si (zhangsi.cs@gmail.com)
 *
 */
public class NmaeEvaluator{

	/**
	 * Calculate the NMAE performance of a RatingPredictor
	 */
	public static double evaluate(RatingPredictor rp, Ratings ratings) {
		int count = ratings.getCount();
		
		double nmae = 0;
		int index, user_id, item_id;
		double rating;
		double rating_hat, err;
		for( index = 0; index != count; ++index){
			user_id = ratings.getUser(index);
			item_id = ratings.getItem(index);
			rating  = ratings.getRating(index);
			
			rating_hat = rp.predict(user_id, item_id, false);
			
			err = rating_hat - rating;
			nmae += Math.abs(err);
		}
		
		nmae /= count;
		return nmae;
	}

}