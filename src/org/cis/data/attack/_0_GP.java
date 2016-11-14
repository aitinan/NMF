package org.cis.data.attack;

import java.io.*;

public class _0_GP {

	public static void main(String[] args) throws Exception{
		
		int rowNumberTrain=1000;
		
		String readPath="F:\\ziji\\userRating.txt";
		File readFile=new File(readPath);
		BufferedReader br=new BufferedReader(new FileReader(readFile));
		
		String writePath="F:\\ziji\\userRatingTrain.txt";
		File writeFile=new File(writePath);
		BufferedWriter bw=new BufferedWriter(new FileWriter(writeFile));
		
		int count=0;
		String line=br.readLine();
		while(line!=null){
			count++;
			if(count<=rowNumberTrain){
				bw.write(line);
				bw.newLine();
			}
			line=br.readLine();
		}
		
		bw.close();
		br.close();
	}
}
