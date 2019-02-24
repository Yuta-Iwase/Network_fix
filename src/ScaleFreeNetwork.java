
public class ScaleFreeNetwork extends Network{

	/**
	 * scale-free networkを生成する。<br>
	 * 与えられた条件で、ネットワーク生成を成功するまで繰り返す。<br>
	 * @param N 頂点数
	 * @param gamma べき指数
	 * @param minDegree 最小次数
	 * @param maxDegree 最大次数
	 * @param loopLimit 生成時のループ数の許容値
	 * @param seed 乱数シード
	 */
	public ScaleFreeNetwork(int N, double gamma, int minDegree, int maxDegree, int loopLimit, long seed) {
		do {
			MakePowerLaw dist = new MakePowerLaw(N, gamma, minDegree, maxDegree, seed);
			ConfigurationNetwork.generate(this, dist.degree, loopLimit, seed);
		}while(!success);
	}

}
