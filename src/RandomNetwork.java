public class RandomNetwork extends Network{

	/**
	 * ランダムグラフを生成する。<br>
	 * 与えられた条件で、ネットワーク生成を成功するまで繰り返す。<br>
	 * @param N 頂点数
	 * @param p 頂点同士の接続確率
	 * @param loopLimit 生成時のループ数の許容値
	 * @param seed 乱数シード
	 */
	public RandomNetwork(int N, double p, int loopLimit, long seed) {
		do {
			double average = (N-1)*p;
			MakePoisson dist = new MakePoisson(N, average, 1, N-1, seed);
			ConfigurationNetwork.generate(this, dist.degree, loopLimit, seed);
		}while(!success);
	}

}
