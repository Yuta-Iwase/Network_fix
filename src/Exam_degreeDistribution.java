
public class Exam_degreeDistribution {

	public static void main(String[] args) {
		//頂点数
		int N = 10000;
		// べき指数
		double gamma = 2.7;
		// 最小次数
		int minDegree = 2;
		// 最大次数
		int maxDegree = N/10;
		// 乱数シード
		long seed = 123456;
		// 上記のパラメータをもつべき分布に従う次数列を生成
		MakePowerLaw dist = new MakePowerLaw(N, gamma, minDegree, maxDegree, seed);

		// configurationモデルにおいて連続で失敗できる回数
		int loopLimit = 100;
		// configurationモデルを用いて、先程作った次数列に従うネットワークを生成
		ConfigurationNetwork net = new ConfigurationNetwork(dist.degree, loopLimit, seed);

		// 次数の頻度をカウント
		int[] degreeFreq = new int[N+1];
		for(int i=0;i<N;i++){
			degreeFreq[net.degree[i]]++;
		}

		// 次数カウントを次数分布へと規格化
		double inv_N = 1.0/N;
		for(int i=0;i<N+1;i++){
			if(degreeFreq[i] > 0){
				System.out.println(i + "\t" + degreeFreq[i]*inv_N);
			}
		}

		// 今回生成手段したネットワークをデスクトップにcsv形式で保存
		// パスは自分の環境に合わせて書くこと(以下のままでは、大抵の環境ではエラーを吐く)
		net.exec_printEdgeList("c:\\desktop\\test.csv");

	}

}
