package com.chen.pca;

import java.util.ArrayList;
import java.util.List;

import org.cis.data.Ratings;

class Util {
	public static double getRating(Ratings ratings, List<ArrayList<Integer>> indicesByUser, int user_id, int item_id){
		double rating = 0;
		for (int index : indicesByUser.get(user_id)) {
			if (item_id == ratings.getItem(index)) {
				rating = ratings.getRating(index);
			}
		}
		return rating;
	}
	
	/*public static void setRating(Ratings ratings, List<ArrayList<Integer>> indicesByUser, int user_id, int item_id, double newRating) {
		indicesByUser.get(user_id).stream().filter(index -> item_id == ratings.getItem(index)).forEach(index -> ratings.setRating(index, newRating));
	}*/

	static double hypot(double var0, double var2) {
		double var4;
		if(Math.abs(var0) > Math.abs(var2)) {
			var4 = var2 / var0;
			var4 = Math.abs(var0) * Math.sqrt(1.0D + var4 * var4);
		} else if(var2 != 0.0D) {
			var4 = var0 / var2;
			var4 = Math.abs(var2) * Math.sqrt(1.0D + var4 * var4);
		} else {
			var4 = 0.0D;
		}

		return var4;
	}
}
