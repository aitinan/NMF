package com.chen.rsnmf;

import org.cis.data.Ratings;
import org.cis.eval.PSEvaluator;
import org.cis.io.MovielensRatingsReader;

import com.chen.rsnmf.RSNMF;

public class RSNMFTest {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
		double[] fsArr = {0.01};
		double[] asArr = {0.01,0.03};
		
		int targetItem = 256;
		String dataset = "1M";
		String attType = "average";
		
		Ratings testData = MovielensRatingsReader.readD("data/movielens/"+dataset+"/test.txt");
		String ratingsOfTargetItem = "data/movielens/"+dataset+"/"+targetItem+"/rsnmf/ratingsOf"+targetItem+".txt";
		for(double fs : fsArr){
			for(double as : asArr){
				Ratings trainDataAtt = MovielensRatingsReader.readS("data/movielens/"+dataset+"/"+targetItem+"/"+attType+"Att/"+"FS"+fs+"AS"+as+".txt");	
				RSNMF rsnmfAtt = new RSNMF(trainDataAtt, testData, 100, 0.06, 500);
				rsnmfAtt.trainModel();
				System.out.println("FS"+fs+"_AS"+as+"_PS = "+PSEvaluator.evaluatePre(ratingsOfTargetItem, rsnmfAtt, targetItem));
				trainDataAtt.clear();
				System.out.println("--------------------------------------------");
			}
		}
		testData.clear();
		System.out.println("total costs " + (System.currentTimeMillis() - start) / 1000 + " s.");
	}
}
