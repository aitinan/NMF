package org.cis.data;

import java.util.ArrayList;

public class UserAverage implements RatingPredictor {

	/** training data set of ratings */
	private Ratings ratings;
	/** number of users */
	private int userNumber;
	private int itemNumber;
	/** number of training ratings */
	private int trainNumber;
	
	/** the sum of ratings for each user */
	private ArrayList<Double> userRatingSum;
	/** the count of ratings for each user */
	private ArrayList<Integer> userRatingCount;
	
	/**
	 * Construct UserAverage algorithm
	 * 
	 * @param ratings training ratings
	 */
	public UserAverage(Ratings ratings) {
		this.ratings     = ratings;
		this.userNumber  = ratings.getMaxUserId();
		this.itemNumber = ratings.getMaxItemId();
		this.trainNumber = ratings.getCount();
		
		userRatingSum   = new ArrayList<>();
		userRatingCount = new ArrayList<>();
		for(int u = 0; u <= userNumber; ++u){
			userRatingSum.add(0d);
			userRatingCount.add(0);
		}	
	}
	
	/**
	 * Train the model of Item Average
	 */
	public void trainModel() {
		int index;
		int user_id;
		double rating;
		for( index = 0; index != trainNumber; ++index){
			user_id = ratings.getUser(index);
			rating  = ratings.getRating(index);
			
			userRatingSum.set(user_id, userRatingSum.get(user_id) + rating);
			userRatingCount.set(user_id, userRatingCount.get(user_id) + 1);
		}
	}

	/**
	 * Predict the rating value with given user and item
	 */
	public double predict(int user_id, int item_id, boolean bound) {
		if(userRatingCount.get(user_id) == 0){
			return 0d;
		}
		return (double) (userRatingSum.get(user_id) / userRatingCount.get(user_id));
	}
	
	public double predict(int user_id) {
		if(userRatingCount.get(user_id) == 0){
			return 0d;
		}
		return (double) (userRatingSum.get(user_id) / itemNumber);
	}

}
