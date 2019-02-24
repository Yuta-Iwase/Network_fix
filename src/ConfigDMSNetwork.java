import java.util.ArrayList;
import java.util.Random;

public class ConfigDMSNetwork extends Network{
	int generateCount = 0;

	/**
	 * DMSモデルを作る。(Structure of Growing Networks: Exact Solution of the Barabasi--Albert's Model)<br>
	 * 次数分布の指数は、3+a/insertEdges となる。<br>
	 * @param N0 初期頂点数
	 * @param N 最終的な頂点数
	 * @param insertEdges 1つの頂点を追加する際に接続する辺の本数
	 * @param gamma べき指数
	 * @param loopLimit 次数分布のリトライ回数
	 */
	public ConfigDMSNetwork(int N0, int N, int insertEdges, double gamma, int loopLimit, long seed) {
		double inflateProb = (gamma-3.0)*insertEdges;
		try {
			// 引数の値が、生成不能な値になっている
			if(N0<insertEdges) {
				throw new IllegalArgumentException("引数の値が不正です(N0<insertEdges)");
			}
		}catch(IllegalArgumentException e) {
			System.out.println(e);
			System.exit(1);
		}

		Random rnd = new Random(seed);

		do {
			// 初期化
			degree = new int[N];

			// 確率の総和
			double total_probability = 0.0;
			// 追加スタブリスト
			ArrayList<Integer> additionalStub_List = new ArrayList<>();
			// 初期確率
			final double INIT_VALUE = insertEdges+inflateProb;
			// 初期確率総和
			double total_init_prob = 0.0;

			// 初期条件としてN0個の頂点を完全グラフで作成
			{
				int N0_initStub = insertEdges;
				int N0_additionalStub = (N0-1)-N0_initStub;
				for(int i=0;i<N0;i++) {
					degree[i] = N0-1;
					for(int j=0;j<N0_additionalStub;j++) {
						additionalStub_List.add(i);
					}
				}
				total_probability = N0*(N0-1)+N0*inflateProb;
				total_init_prob = N0*INIT_VALUE;
			}



			// BAモデル的な次数割り振りを行う
			for(int i=N0;i<N;i++) {
				ArrayList<Integer> currentChosedNodes = new ArrayList<>();

				// 選択
				for(int j=0;j<insertEdges;j++) {

					double r = rnd.nextDouble()*total_probability;
					int chosedNode = -1;
					if(r<=total_init_prob) {
						chosedNode = (int)(rnd.nextDouble()*i);
					}else {
						int chosedStub = (int)(rnd.nextDouble()*additionalStub_List.size());
						chosedNode = additionalStub_List.get(chosedStub);
					}

					if(!currentChosedNodes.contains(chosedNode)) {
						currentChosedNodes.add(chosedNode);
					}else {
						j--;
						continue;
					}
				}

				// 選択頂点の情報更新
				for(int j=0;j<currentChosedNodes.size();j++) {
					int currentChosedNode = currentChosedNodes.get(j);
					additionalStub_List.add(currentChosedNode);
					degree[currentChosedNode]++;
				}

				// 追加頂点の初期情報設定
				degree[i] = insertEdges;

				// total_probability, sum_degreeの更新
				total_probability += 2*insertEdges+inflateProb;
				total_init_prob += INIT_VALUE;

			}


			// Configurationの要領でネットワークを構築
			ConfigurationNetwork.generate(this, degree, loopLimit, seed);
			seed++;

			// ネットワーク生成回数カウント
			generateCount++;
		}while(!success);

	}


}
