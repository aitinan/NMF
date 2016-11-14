package com.chen.rcfnmf;

import org.cis.data.Ratings;
import org.cis.eval.PSEvaluator;
import org.cis.io.MovielensRatingsReader;

public class RCFNMFSimilarityTest {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
		double[] fsArr = {0.01};
		double[] asArr = {0.01,0.03};
		
		int targetItem = 256;
		String dataset = "1M";
		String attType = "average";

		Ratings testData = MovielensRatingsReader.readD("data/movielens/"+dataset+"/test.txt");
		String filePath = "data/movielens/1M/"+targetItem+"/ratingsOf"+targetItem+"_similarity_plus.txt";
		for(double fs : fsArr){
			for(double as : asArr){
				Ratings trainDataAtt = MovielensRatingsReader.readS("data/movielens/"+dataset+"/"+targetItem+"/"+attType+"Att/"+"FS"+fs+"AS"+as+".txt");
				RCFNMFSimilarity rcfnmfSimilarityAtt = new RCFNMFSimilarity(trainDataAtt, testData, 150, 0.004, 500);
				rcfnmfSimilarityAtt.trainModel();
				System.out.println("FS"+fs+"_AS"+as+"_PS = "+PSEvaluator.evaluatePre(filePath, rcfnmfSimilarityAtt, targetItem));
				trainDataAtt.clear();
				System.out.println("--------------------------------------------");
			}
		}
		testData.clear();
		System.out.println("total costs " + (System.currentTimeMillis() - start) / 1000 + " s.");
	}
}
