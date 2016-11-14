package org.cis.data;

import org.cis.io.MovielensRatingsReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Reformat {
	public static void main(String[] args) {

		double[] fsArr = { 0.001, 0.003, 0.005, 0.007 };
		//double[] asArr = {0.01, 0.03, 0.05, 0.07, 0.1};
		double[] asArr = {0.01};

		Ratings trainData = MovielensRatingsReader.readS("D:\\rcfnmf\\data/netflix/train.txt");
		int userIdStart = trainData.getMaxUserId() + 1;
		for(double fs : fsArr){
			for(double as : asArr){
				String readFilePath = "D:\\rcfnmf\\data/netflix/7052/bandwagon/FS" + fs + "AS" + as + ".txt";
				Ratings attackData = ReadAttack.readFile(readFilePath, userIdStart);
				String writeFilePath = "D:\\rcfnmf\\data/netflix/7052/bandwagonAtt_24/FS" + fs + "AS" + as + ".txt";
				Reformat.writeFile(trainData, attackData, writeFilePath);
				attackData.clear();
			}
		}
		System.out.println("finish.");
	}
	
	private static void writeFile(Ratings trainData, Ratings attackData, String filePath) {
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
			for(int i = 0; i < trainData.getCount(); i++){
				bw.write(trainData.getUser(i) + " " + trainData.getItem(i) + " " + trainData.getRating(i) + "\n");
			}
			for(int k = 0; k < attackData.getCount(); k++){
				bw.write(attackData.getUser(k) + " " + attackData.getItem(k) + " " + attackData.getRating(k) + "\n");
			}
			bw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
