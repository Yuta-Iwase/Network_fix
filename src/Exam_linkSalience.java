
public class Exam_linkSalience {

	public static void main(String[] args) {
		int N = 1000;
		double gamma = 2.7;
		int minDegree = 2;
		int maxDegree = N/10;
		int loopLimit = 100;
		// System.currentTimeMillis()とすることで毎回異なるシードを指定できる。シード指定が面倒な人向け
		long seed = System.currentTimeMillis();

		// random walkのステップ数
		int step = N*100;
		// biased RWにおいて次数との相関の強度
		double alpha = 1.5;
		// 各ステップ毎にネットワーク上の任意の頂点にテレポートする確率
		double teleportP = 0.0;

		// スケールフリーネットワーク生成
		ScaleFreeNetwork net = new ScaleFreeNetwork(N, gamma, minDegree, maxDegree, loopLimit, seed);

		// 隣接頂点の情報を記憶するneightborListを定義
		net.set_neightbor();
		// biased random walkを用いて重み付けを行う
		net.set_weight_by_BiasedRW(step, alpha, teleportP, seed);
		// 各重みに、ほぼ1に等しい係数をかける
		net.exec_weightDisturb(seed);
		// 現時点でのnetのlink salienceを計測する
		net.calc_linkSalience();

		// HS linkの本数
		int hs_freq = 0;
		// link salienceが0.9以上の辺をHS linkとする
		double hs_threshold = 0.9*net.N;
		double inv_N = 1.0/net.N;
		for(int i=0;i<net.M;i++){
			if(net.linkSalience[i] > hs_threshold){
				hs_freq++;
			}
		}
		// high salience fraction f_HSは (HS linkの本数)/N として与えられる
		double f_hs = hs_freq*inv_N;

		System.out.println(f_hs);

	}

}
