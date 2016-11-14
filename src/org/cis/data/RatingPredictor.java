package org.cis.data;

import com.chen.pca.FlagBean;

/**
 * This interface defining functions of a rating predictor
 * 
 * @author Zhang Si (zhangsi.cs@gmail.com)
 *
 */
public interface RatingPredictor {
	
	/**
	 * Train the model of a rating predictor
	 */
	void trainModel();

	/**
	 * Predict the rating value of with given user_id and item id
	 * @param bound whether of bound the predicted value into [minRating, maxRating]
	 */
	double predict(int user_id, int item_id, boolean bound);

}
