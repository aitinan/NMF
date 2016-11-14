package org.cis.data.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ItemPopularity {

	public static void main(String[] args)throws Exception {
		String readPath="F:\\Outlier1M\\userRatingTest2.txt";
		int userNumber=ItemPopularity.getRowNumber(readPath);
		int itemNumber=3952;
		int [][] ratingMatrix=ItemPopularity.getRatingMatrix(readPath, userNumber, itemNumber);
		
		for(int j=0;j<itemNumber;j++){
			int rating=0;
			for(int i=0;i<userNumber;i++){
				if(ratingMatrix[i][j]!=0){
					rating++;
				}
			}//for i
			System.out.println((j+1)+"\t"+rating);
		}//for j
	}//main
	
	private static int [][] getRatingMatrix(String readPath,int userNumber, int itemNumber) throws Exception{
		
		File readFile=new File(readPath);
		BufferedReader br=new BufferedReader(new FileReader(readFile));
		
		int row=0;
		int index=0;
		int value=0;
		int [][] rating= new int[userNumber][itemNumber];
		String line=br.readLine();
		while(line!=null){
			String [] str=line.split(" ");
			for(int i=1;i<str.length;i++){
				index=Integer.valueOf( str[i].substring(0,str[i].indexOf(':')));
				value=Integer.valueOf( str[i].substring(str[i].indexOf(':')+1));
				rating[row][index-1]=value;
			}
			line=br.readLine();
			row++;
		}
		br.close();
		return rating;
	}
	//Get rowNumber;
	private static int getRowNumber(String readPath) throws Exception{
		//�õ����������Լ����û���ò����Ŀ��
		File readFile=new File(readPath);
		BufferedReader br=new BufferedReader(new FileReader(readFile));
		
		int row=0;
		String line=br.readLine();
		while(line!=null){
			row++;
			line=br.readLine();
		}
		br.close();
		return row;
	}
}
