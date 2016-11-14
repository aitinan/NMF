package com.chen.rcfnmf;

import com.chen.pca.FlagBean;
import com.chen.pca.Pca;
import org.cis.data.Ratings;
import org.cis.eval.PSEvaluator;
import org.cis.io.MovielensRatingsReader;

public class RCFNMFTest {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
		double[] fsArr = {0.01};
		double[] asArr = {0.01};
		
		int targetItem = 256;
		String attType = "random";
		
		Ratings testData = MovielensRatingsReader.readD("D:/ideaProjects/data/1M/test.txt");
		String filePath = "D:/ideaProjects/data/1M/ratingsOf"+targetItem+".txt";
		for(double fs : fsArr){
			for(double as : asArr){
				String attFilePath = "D:/ideaProjects/data/1M/"+targetItem+"/"+attType+"Att/FS"+fs+"AS"+as+".txt";
				Ratings trainDataAtt = MovielensRatingsReader.readS(attFilePath);

				RCFNMF rcfnmfAtt = new RCFNMF(trainDataAtt, testData, 100, 0.004, 400);
				rcfnmfAtt.trainModel();
				System.out.println("FS" + fs + "_AS" + as + "_PS = " + PSEvaluator.evaluatePre(filePath, rcfnmfAtt, targetItem));

				long noPcaTime = System.currentTimeMillis();
				System.out.println("不加pca执行完成，用时 " + (noPcaTime - start) / 1000 + " s.");
				System.out.println("--------------------------------------------");

				Pca pca = new Pca(trainDataAtt);
				FlagBean flagBean = pca.pca(testData.getMaxUserId(), fs, as);
				RCFNMF rcfnmfAttFlag = new RCFNMF(trainDataAtt, testData, flagBean, 100, 0.004, 400);
				rcfnmfAttFlag.trainModelFlag();
				System.out.println("FS" + fs + "_AS" + as + "_PS = " + PSEvaluator.evaluatePre(filePath, rcfnmfAttFlag, targetItem));

				System.out.println("加pca执行完成，用时 " + (System.currentTimeMillis() - noPcaTime) / 1000 + " s.");
				System.out.println("--------------------------------------------");
				System.out.println("--------------------------------------------");
			}
		}
		System.out.println("total costs " + (System.currentTimeMillis() - start) / 1000 + " s.");
	}
}
