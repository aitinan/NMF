package org.cis.eval;

import org.cis.data.RatingPredictor;
import org.cis.data.Ratings;

import java.io.BufferedReader;
import java.io.FileReader;

public class PSEvaluator {
	public static double evaluatePre(String filePath, RatingPredictor rpAtt, int targetItem) {
		int count = 0;
		double sum = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			while((line = br.readLine()) != null){
				sum += Math.abs(Double.parseDouble(line.split(" ")[2]) - rpAtt.predict(Integer.parseInt(line.split(" ")[0]), targetItem, true));
				count++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sum / count;
	}
	
	public static double evaluatePre(Ratings ratings, RatingPredictor rp, RatingPredictor rpAtt, int targetItem) {
		int userNumber = ratings.getMaxUserId();
		double sum = 0;
		for(int user = 1; user <= userNumber; user++){
			sum += Math.abs(rp.predict(user, targetItem, true) - rpAtt.predict(user, targetItem, true));
		}
		return sum / userNumber;
	}
}
