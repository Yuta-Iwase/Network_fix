import java.io.File;
import java.util.Scanner;



public class CSVFileNetwork extends Network {

	public CSVFileNetwork(String inputFilePath, boolean weighted) {
		Scanner scan1 = null;
		Scanner scan2 = null; //Mを測るためのScannerクラス
		N=0;
		M=0;
		String delimiter = ",|\\t|\\s+|\\n\\r|\\r\\n|\\n|\\r";

		try{
			scan1 = new Scanner(new File(inputFilePath));
			scan2 = new Scanner(new File(inputFilePath));
			scan1.useDelimiter(delimiter);
			scan2.useDelimiter(delimiter);

			while(scan2.hasNext()){
				int currentNode = scan2.nextInt();
				if(N < currentNode) N = currentNode;
				currentNode = scan2.nextInt();
				if(N < currentNode) N = currentNode;
				if(weighted) scan2.nextDouble();
				M++;
			}
			N++; //頂点数Nは登場した最大の頂点番号+1となる
			scan2.close();

			edgeList = new int[M][2];
			degree = new int[N];
			if(weighted) weight = new double[M];
			int currentLine = 0;
			while(scan1.hasNext()){
				int currentNode0 = scan1.nextInt();
				int currentNode1 = scan1.nextInt();
				edgeList[currentLine][0] = currentNode0;
				edgeList[currentLine][1] = currentNode1;
				degree[currentNode0]++;
				degree[currentNode1]++;
				if(weighted) weight[currentLine] = scan1.nextDouble();
				currentLine++;
			}
			scan1.close();

		}catch (Exception e) {
			System.out.println(e);
			System.exit(1);
			success = false;
		}
	}
}
