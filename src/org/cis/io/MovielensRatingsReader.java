package org.cis.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.cis.data.Ratings;

/**
 * This class reads the movielens ratings data
 * 
 * @author Zhang Si (zhangsi.cs@gmail.com)
 *
 */
public class MovielensRatingsReader {

	/**
	 * Read movielens ratings data from text file
	 */
	public static Ratings readD(String filePath) {

		Ratings ratings = new Ratings();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] words;
			int count = 0;
			while( (line = br.readLine()) != null){
				words = line.split(",");
				ratings.addRating(Integer.parseInt(words[0]), Integer.parseInt(words[1]), Double.parseDouble(words[2]));
				count++;
			}
			System.out.println("read file: " + filePath + " end. The total line number is: " + count);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ratings;
	}

	public static Ratings readS(String filePath) {
		Ratings ratings = new Ratings();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] words;
			int u,i;
			double r;
			int count = 0;
			while( (line = br.readLine()) != null){
				words = line.split(" ");
				u = Integer.parseInt(words[0]);
				i = Integer.parseInt(words[1]);
				r = Double.parseDouble(words[2]);
				ratings.addRating(u, i, r);
				count++;
			}
			System.out.println("read file: " + filePath + " end. The total line number is: " + count + ".");
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ratings;
	}

	public static Ratings readM(String filePath) {
		Ratings ratings = new Ratings();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] words;
			int u,i;
			double r;
			//int count = 0;
			while( (line = br.readLine()) != null){
				words = line.split("::");
				u = Integer.parseInt(words[0]);
				i = Integer.parseInt(words[1]);
				r = Double.parseDouble(words[2]);
				ratings.addRating(u, i, r);
				//count++;
			}
			//System.out.println("read file: " + filePath + " end. The total line number is: " + count);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ratings;
	}
	
	public static Ratings readN(String filePath) {
		Ratings ratings = new Ratings();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] words;
			int u,i;
			double r;
			int count = 0;
			while( (line = br.readLine()) != null){
				words = line.split("\t");
				u = Integer.parseInt(words[0]);
				i = Integer.parseInt(words[1]);
				r = Double.parseDouble(words[2]);
				ratings.addRating(u, i, r);
				count++;
			}
			System.out.println("read file: " + filePath + " end. The total line number is: " + count);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ratings;
	}
}
