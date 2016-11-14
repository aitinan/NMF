package org.cis.data;

import java.io.BufferedReader;
import java.io.FileReader;

class ReadAttack {
	static Ratings readFile(String filePath, int userIdStart) {

		Ratings ratings = new Ratings();
		String line;
		String[] uir, ir;
		int u, i;
		double r;
		int userId = userIdStart;
		try{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while((line = br.readLine()) != null){
				uir = line.split(" ");
				u = userId;
				for(int k = 1; k < uir.length; k++){
					ir = uir[k].split(":");
					i = Integer.parseInt(ir[0]);
					r = Double.parseDouble(ir[1]);
					ratings.addRating(u, i, r);
				}
				userId++;
			}
			br.close();
			line = null;
			uir = null;
			ir = null;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ratings;
	}
}
