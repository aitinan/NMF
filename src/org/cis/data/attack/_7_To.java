package org.cis.data.attack;

import java.io.*;

public class _7_To {
/*
 * ��ͬ����ģ�µĹ������ݷŵ�һ��
 */
	public static void main(String[] args)  throws Exception{

		int loopNumber=50;
		//-------------------------------------------------	
		for(int loop=1;loop<=loopNumber;loop++){
			String [] at={"Random","Average","Bandwagon"};//{"Random","Average","Bandwagon"}
			for(int j=0;j<at.length;j++){
				String writePath="F:\\ziji\\13Feature\\Test\\"+at[j]+"\\"+loop+" 3%.txt";
				File writeFile=new File(writePath);
				System.out.println("writePath="+writePath);
				BufferedWriter bw=new BufferedWriter(new FileWriter(writeFile));
				//
				String [] fSize={ "0.03", "0.05", "0.10", "0.25" }; //********* "0.01", "0.03", "0.05", "0.10", "0.25" ,"0.50"
				for(int f=0;f<fSize.length;f++){
					String readPath="F:\\ziji\\13Feature\\Test\\"+at[j]+"\\"+loop+" 3% "+fSize[f]+".txt";
					System.out.println("readPath="+readPath);
					File readFile=new File(readPath);
					BufferedReader br=new BufferedReader(new FileReader(readFile));
					String line=br.readLine();
					while(line!=null){
						bw.write(line);
						bw.newLine();
						line=br.readLine();
					}
					br.close();
				}//f
				bw.close();
			}//for j	
		}//loop
		//-------------------------------------------------	
		for(int loop=1;loop<=loopNumber;loop++){
			String [] at={"40AoP"};//{"20AoP","30AoP","40AoP","Mixture"}
			for(int j=0;j<at.length;j++){
				String writePath="F:\\ziji\\13Feature\\Test\\"+at[j]+"\\"+loop+" 3%.txt";
				System.out.println("writePath="+writePath);
				File writeFile=new File(writePath);
				BufferedWriter bw=new BufferedWriter(new FileWriter(writeFile));
				//	
				String [] fSize={ "0.03", "0.05", "0.10","0.25"}; //********* 
				for(int f=0;f<fSize.length;f++){
					String readPath="F:\\ziji\\13Feature\\Test\\"+at[j]+"\\"+loop+" 3% "+fSize[f]+".txt";
					System.out.println("readPath="+readPath);
					File readFile=new File(readPath);
					BufferedReader br=new BufferedReader(new FileReader(readFile));
					String line=br.readLine();
					while(line!=null){
						bw.write(line);
						bw.newLine();
						line=br.readLine();
					}
					br.close();
				}//f
				bw.close();
			}	
		}//loop
	}//main
}//class
