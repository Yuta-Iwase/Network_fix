import java.io.File;
import java.util.Scanner;



public class CSVFileNetwork extends Network {

	public CSVFileNetwork(String inputFilePath, boolean weighted) {
		Scanner scan1 = null;
		Scanner scan2 = null; //Mを測るためのScannerクラス
		N=0;
		M=0;

		try{
			scan1 = new Scanner(new File(inputFilePath));
			scan2 = new Scanner(new File(inputFilePath));

			while(scan2.hasNext()){
				int currentNode = scan2.nextInt();
				if(N < currentNode) N = currentNode;
				currentNode = scan2.nextInt();
				if(N < currentNode) N = currentNode;
				if(weighted) scan2.nextDouble();
				M++;
			}
			scan2.close();

			edgeList = new int[M][2];
			if(weighted) weight = new double[M];
			int currentLine = 0;
			while(scan1.hasNext()){
				edgeList[currentLine][0] = scan1.nextInt();
				edgeList[currentLine][1] = scan1.nextInt();
				if(weighted) weight[currentLine] = scan2.nextDouble();
				currentLine++;
			}
			scan1.close();

		}catch (Exception e) {
			System.out.println(e);
			success = false;
		}
	}
}
