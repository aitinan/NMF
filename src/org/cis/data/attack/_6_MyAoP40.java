package org.cis.data.attack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class _6_MyAoP40 {

	public static void main(String[] args) throws Exception {
		
		double x=0.4;////********** x% of popular;0.1 0.2 0.3 0.4 0.5 
//		String xPath=String.valueOf(  Math.round(x*100));
		int itemNumber=3952;//**********
		int maxRatingValue=5;//**********
		int minRatingValue=1;//**********
		//
		String readPathGP="F:\\ziji\\userRatingTrain.txt";//**********
		int gUserNumber=_6_MyAoP40.getRowNumber(readPathGP);
		int [][] ratingMatrix=_6_MyAoP40.getRatingMatrix(readPathGP, gUserNumber, itemNumber);
		int [] popItemOrder=_6_MyAoP40.getPopItemOrder(gUserNumber, itemNumber, ratingMatrix);
		double [] u =_6_MyAoP40.getAvgItem(gUserNumber, itemNumber, ratingMatrix);
		double [] sigma=_6_MyAoP40.getStdItem(gUserNumber, itemNumber, ratingMatrix);
		//
		int aUserNumber=60;
		int loopNumber=1;
		String [] fSize={ "0.03", "0.05", "0.10" , "0.25"};//********* "0.01", "0.03", "0.05", "0.10","0.25","0.50"  //��������ģ��
		for(int f=0;f<fSize.length;f++){
			int fItemNumber=(int)Math.round(itemNumber* Double.valueOf( fSize[f] ));
			for(int loop=1;loop<=loopNumber;loop++){
				int targetItem=_6_MyAoP40.getRandomItem(1,itemNumber);
				//
				String writePathAP= "F:\\ziji\\13Feature\\Train\\40AoP\\"+loop+" 12%"+fSize[f]+".txt";//����·��;
				File writeFileAP=new File(writePathAP);
				BufferedWriter bwAP=new BufferedWriter(new FileWriter(writeFileAP));
				for(int i=0;i<aUserNumber;i++){
					bwAP.write("1 ");
					int [] AP=_6_MyAoP40.getOneAPID(gUserNumber, itemNumber, targetItem, fItemNumber, x, popItemOrder);
					for(int j=0;j<fItemNumber;j++){
						if(AP[j]==targetItem){
							bwAP.write(AP[j]+":"+maxRatingValue+" ");
						}else{
							bwAP.write(AP[j]+":"+_6_MyAoP40.getGaussDisRating(u[AP[j]-1],sigma[AP[j]-1],minRatingValue, maxRatingValue)+" ");
						}
					}//for j
						bwAP.newLine();
//						System.out.println();
				}//for i
						bwAP.close();
			}//loop
		}//for fSize
	}//main
	
	private static int getGaussDisRating(double u, double sigma, int minRatingValue,int maxRatingValue) {
		Random randomGauss = new Random();
		double r =  randomGauss.nextGaussian();
		int Rating = (int) Math.round(r * sigma + u);
		if(Rating <minRatingValue){
			Rating=minRatingValue;
		}
		if(Rating > maxRatingValue){
			Rating=maxRatingValue;
		}
		return Rating;
	}
	//
	private static int [] getOneAPID(int gUserNumber,int itemNumber,int targetItem,int fItemNumber,double x,int []popItemOrder) throws Exception{
		//
		int n=(int)Math.round(x*itemNumber);
		List<Integer> listPopItemID = new ArrayList<Integer>();
		for(int i=0;i<n;i++){
			listPopItemID.add( popItemOrder[i]  );
		}
		//
		List<Integer> list = new ArrayList<Integer>();
		list.add(targetItem);
		while(list.size()<fItemNumber){
			int itemID=_6_MyAoP40.getRandomItem(1, itemNumber);
			if (!list.contains(itemID) && itemID != targetItem  && listPopItemID.contains(itemID)) {//item not repeat && without targetItem
				list.add(itemID);
			}
		}//while
		Collections.sort(list);//����ĿID����С-->������
		//
		int [] AP=new int [fItemNumber];
		for(int i=0;i<fItemNumber;i++){
			AP[i]=list.get(i);
		}
		return AP;
	}
	//�����г̶ȷ�����ĿID���б�
    private static int [] getPopItemOrder(int gUserNumber ,int itemNumber, int [][] ratingMatrix) throws Exception{
    	//
    	int [] count=new int [itemNumber];
    	for(int j=0;j<itemNumber;j++){
    		for(int i=0;i<gUserNumber;i++){
    			if(ratingMatrix[i][j]!=0){
    				count[j]++;
    			}
    		}
    	}
    	//
    	int [] itemOrder=new int [itemNumber];
    	for(int i=0;i<itemNumber;i++){
    		itemOrder[i]=i+1;
    	}
    	//
		for(int i=0;i<itemNumber;i++){//sort:�ɴ�С��
			for(int j=0;j<i;j++) {
				if(count[i] > count[j]) {
					int temp=count[i];
					count[i]=count[j];
					count[j]=temp;
					
					int tem=itemOrder[i];
					itemOrder[i]=itemOrder[j];
					itemOrder[j]=tem;
				}
			}
		}//for
    	return itemOrder;
    }
	//�������Ŀ����ĿID��
    private static int getRandomItem(int startID,int endID){
		Random r = new Random();
		int targetItem = r.nextInt( endID - startID + 1) + startID;
		return targetItem;
	}
    //��Ŀ�ı�׼�
    private static double [] getStdItem(int gUserNumber, int itemNumber, int [][] ratingMatrix){
    	//avg
    	double [] avgItem=new double [itemNumber];
    	for(int j=0;j<itemNumber;j++){
    		double sum=0;
    		int n=0;
    		for(int i=0;i<gUserNumber;i++){
    			if(ratingMatrix[i][j]!=0){
    				sum+=ratingMatrix[i][j];
    				n++;
    			}
    		}
    		avgItem[j]=sum/n;
    		if(n==0){
    			avgItem[j]=0;
    		}
    	}
    	//std
    	double [] stdItem=new double [itemNumber];
    	for(int j=0;j<itemNumber;j++){
    		double sum=0;
    		int n=0;
    		for(int i=0;i<gUserNumber;i++){
    			if(ratingMatrix[i][j]!=0){
    				sum+=Math.pow( (ratingMatrix[i][j]-avgItem[j]) ,2.0);
    				n++;
    			}
    		}//for i
    		stdItem[j]= Math.sqrt( sum/ n);
    		if(n==0){
    			stdItem[j]=0;
    		}
    	}//for j
    	return stdItem;
    }
    //��Ŀ��ƽ��ֵ��
    private static double [] getAvgItem(int gUserNumber, int itemNumber, int [][] ratingMatrix){
    	double [] avgItem=new double [itemNumber];
    	//
    	for(int j=0;j<itemNumber;j++){
    		double sum=0;
    		int n=0;
    		for(int i=0;i<gUserNumber;i++){
    			if(ratingMatrix[i][j]!=0){
    				sum+=ratingMatrix[i][j];
    				n++;
    			}
    		}
    		avgItem[j]=sum/n;
    		if(n==0){
    			avgItem[j]=0;
    		}
    	}
    	return avgItem;
    }
	//Get data
	private static int [][] getRatingMatrix(String readPathGP,int gUserNumber, int itemNumber) throws Exception{
		//������������������ݣ������û���Ŀ��������Ŀ��Ŀ��������־����ö�ά�����ʾ��
		File readFile=new File(readPathGP);
		BufferedReader br=new BufferedReader(new FileReader(readFile));
		
		int row=0;
		int index=0;
		int value=0;
		int [][] rating= new int[gUserNumber][itemNumber];
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
}//class
