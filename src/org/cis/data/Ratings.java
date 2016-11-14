package org.cis.data;

import java.util.ArrayList;

public class Ratings implements DataSet {
	 
	/** the number of interaction events in the data set */
	private int count;
	
	/** the users index of ratings data */
	private ArrayList<Integer> users;
	/** the item index of ratings data */
	private ArrayList<Integer> items;
	/** the rating values index of the ratings data */
	private ArrayList<Double> values;
	
	/** max user id of the ratings data */
	private int maxUserId;
	/** max item id of the ratings data */
	private int maxItemId;
	
	/** max rating of the ratings data */
	private double maxRating;
	/** min rating of the ratings data */
	private double minRating;
	
	/** the index stored by user */
	private ArrayList<ArrayList<Integer>> indexByUser;
	/** the index stored by item */
	private ArrayList<ArrayList<Integer>> indexByItem;
	
	/** randomly generated index */
	private ArrayList<Integer> randomIndex;
	
	/**
	 * Construct an empty Ratings
	 */
	public Ratings(){
		this.count  = 0;
		this.users  = new ArrayList<>();
		this.items  = new ArrayList<>();
		this.values = new ArrayList<>();
		this.maxItemId = -1;
		this.maxUserId = -1;
		
		this.maxRating = Integer.MIN_VALUE;
		this.minRating = Integer.MAX_VALUE;
	}
	
	public void addRating(int user_id, int item_id, double rating){
		users.add(user_id);
		items.add(item_id);
		values.add(rating);
		
		count++;
		
		if(user_id > maxUserId)
			maxUserId = user_id;
		if(item_id > maxItemId)
			maxItemId = item_id;
		if(rating < minRating)
			minRating = rating;
		if(rating > maxRating)
			maxRating = rating;
		
	}
	
	/**
	 * Build the user indices
	 */
	public void BuildUserIndices() {
		indexByUser = new ArrayList<>();
		for (int u = 0; u <= maxUserId; u++)
			indexByUser.add(new ArrayList<Integer>());
		// one pass over the data
		for (int index = 0; index < count; index++)
			indexByUser.get(users.get(index)).add(index);
	}
	
	/**
	 * Build the item indices
	 */
	public void BuildItemIndices() {
		indexByItem = new ArrayList<>();
		for(int i = 0; i <= maxItemId; ++i)
			indexByItem.add(new ArrayList<Integer>());
		
		// ons pass over the data
		for (int index = 0; index < count; index++)
			indexByItem.get(items.get(index)).add(index);
	}
	
	/**
	 * Build the random index
	 */
	public void BuildRandomIndex() {
		randomIndex = new ArrayList<>(count);
		for(int index = 0; index != count; ++index){
			randomIndex.add(index);
		}
		java.util.Collections.shuffle(randomIndex);
	}



	/**
	 * Get index for a given user and item
	 * 
	 * @param user_id: the user ID
	 * @param item_id: the item ID
	 * @return the index of the first event encountered that matches the user ID and item ID
	 */
	public int GetIndex(int user_id, int item_id) {
		for(int index = 0; index != count; ++index)
			if(users.get(index) == user_id && items.get(index) == item_id)
				return index;
		return -1;
	}
	
	/**
	 * Get the index sorted by user
	 * @return the index sorted by user
	 */
	public ArrayList<ArrayList<Integer>> getIndicesByUser(){
		BuildUserIndices();
		return this.indexByUser;
	}
	
	/**
	 * Get the index sorted by item
	 * @return the index sorted by item
	 */
	public ArrayList<ArrayList<Integer>> getIndicesByItem(){
		BuildItemIndices();
		return this.indexByItem;
	}
	
	/**
	 * Get the items involved with the given user 
	 * @param user_id the given user's id 
	 * @return the items involved with the given user
	 */
	public ArrayList<Integer> getItemsByUser(int user_id){
		ArrayList<Integer> list = new ArrayList<>();
		for( int i = 0; i != count; ++i){
			if(users.get(i) == user_id){
				list.add(items.get(i));
			}
		}
		return list;
	}
	
	/**
	 * Get the users involved with the given item
	 * @param item_id the given item's id
	 * @return the users involved with the given item
	 */
	public ArrayList<Integer> getUsersByItem(int item_id){
		ArrayList<Integer> list = new ArrayList<>();
		for(int i = 0; i != count; ++i){
			if(items.get(i) == item_id){
				list.add(users.get(i));
			}
		}
		return list;
	}
	
	public ArrayList<Double> getRatingsByUser(int user_id){
		ArrayList<Double> list = new ArrayList<>();
		for(int i = 0; i != count; ++i){
			if(users.get(i) == user_id){
				list.add(values.get(i));
			}
		}
		return list;
	}
	
	public ArrayList<Double> getRatingsByItem(int item_id){
		ArrayList<Double> list = new ArrayList<>();
		for(int i = 0; i != count; ++i){
			if(items.get(i) == item_id){
				list.add(values.get(i));
			}
		}
		return list;
	}
	
	/**
	 * Get the average rating of all the rating values
	 * 
	 * @return the average rating
	 */
	public double averageRating(){
		double avg = 0;
		for( int i = 0; i != count; ++i)
			avg += values.get(i);
		return avg/count;
	}
	
	public double cigRating(){
		double sum = 0d;
		double avgRating = averageRating();
		for(int i = 0; i < count; i++){
			sum += Math.pow(values.get(i) - avgRating, 2);
		}
		return Math.sqrt(sum / count);
	}
	
	public double varianceRating() {
		double sum = 0d;
		double avgRating = averageRating();
		for(int i = 0; i < count; i++){
			sum += Math.pow(values.get(i) - avgRating, 2);
		}
		return sum / count;
	}
	/**
	 * Get the number of users
	 * 
	 * @return number of users
	 */
	public int getMaxUserId() { return maxUserId; }
	
	/**
	 * Get the number of items
	 * 
	 * @return the number of items
	 */
	public int getMaxItemId() {
		return maxItemId;
	}
	
	/**
	 * Get the max rating
	 * @return maxRating
	 */
	public double getMaxRating() { return maxRating; }
	
	/**
	 * Get the min rating
	 * @return minRating
	 */
	public double getMinRating(){
		return minRating;
	}
	
	/**
	 * Get the total number of interaction ratings
	 * 
	 * @return the number of ratings
	 */
	public int getCount(){
		return count;
	}
	
	/**
	 * Get the randomly generated index of ratings
	 * @return randomIndex
	 */
	public ArrayList<Integer> getRandomIndex(){
		BuildRandomIndex();
		return randomIndex;
	}



	/**
	 * Get the user_id given the index
	 * @return the user_id
	 */
	public int getUser(int index){
		return users.get(index);
	}
	
	/**
	 * Get the item_id given the index
	 * @return the item_id
	 */
	public int getItem(int index){
		return items.get(index);
	}
	
	/**
	 * Get the rating given the index
	 * @return the rating value
	 */
	public double getRating(int index){
		return values.get(index);
	}
	
	public void setRating(int index, double newRating) {
		values.set(index, newRating);
	}
	
	public double getRating(int user_id, int item_id){
		double rating = 0;
		for (int index : indexByUser.get(user_id)) {
			if (item_id == getItem(index)) {
				rating = getRating(index);
			}
		}
		return rating;
	}
	
	public void setRating(int item_id, int user_id, double newRating) {
		for (int index : indexByItem.get(item_id)) {
			if (user_id == getUser(index)) {
				setRating(index, newRating);
			}
		}
	}
	
	/**
	 * Clear the data set
	 */
	public void clear(){
		if(users != null){
			users.clear();
			users = null;
		}
		if(items != null){
			items.clear();
			items = null;
		}
		if(values != null){
			values.clear();
			values = null;
		}
	}
}
