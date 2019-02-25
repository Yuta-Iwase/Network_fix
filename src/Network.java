import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Network {

	/** 頂点数 */
	int N;

	/** 辺数 */
	int M;

	/**
	 * 辺リスト<br>
	 * edgeList[i][0],edgeList[i][1]はi番目の辺の頂点番号を表す。
	 */
	int[][] edgeList;

	//// 新設
	int[] neightborList = null;
	int[] addressList = null;
	int[] neightborIndexList = null;

	/**
	 * 次数列<br>
	 * degree[i]は頂点iの次数を表す。
	 */
	int[] degree;

	/**
	 * 重みリスト<br>
	 * weight[i]は辺iの重みを表す。
	 */
	double[] weight;

	// ConnectedCompornentにより関連する変数
	/** 連結成分の個数 */
	int ccCount;
	/** ccSizeList[i]はi番目の連結成分のサイズを表す。 */
	int[] ccSizeList;
	/** ccIDList[i]は頂点iが所属する連結成分のindexを表す。 */
	int[] ccIDList;


	// 各頂点のDirect,In-direct破壊されているかの情報
	boolean[] directDeleted;
	boolean[] indirectDeleted;

	// Direct,In-direct破壊でのN,D,I,DI,NIの各連結成分データ
	// TODO ここ書き直し
	ArrayList<ArrayList<Integer>> ccMember_NDI;
	int max_ccSize_NDI;

	/**
	 * calc_betweenness()メソッドを実行することで数値が与えられる。<br>
	 * nodeBetweenness[i]=(頂点iの媒介中心性)×(N*(N-1)/2)<br>
	 * が与えられている。<br>
	 * node betweennessそのものではなく、(N*(N-1)/2)倍であることに注意すること。<br>
	 */
	double[] nodeBetweenness;

	/**
	 * calc_betweenness()メソッドを実行することで数値が与えられる。<br>
	 * edgeBetweenness[i]=(辺iの媒介中心性)×(N*(N-1)/2)<br>
	 * が与えられている。<br>
	 * edge betweennessそのものではなく、(N*(N-1)/2)倍であることに注意すること。<br>
	 */
	double[] edgeBetweenness;

	/**
	 * calc_linkSalience()メソッドを実行することで数値が与えられる。<br>
	 * linkScalience[i]=(頂点iのlink salience)×N<br>
	 * が与えられている。<br>
	 * link salienceそのものではなく、N倍であることに注意すること。<br>
	 */
	int[] linkSalience;


	/**
	 * このネットワークが正しく生成されているのか、判定する真偽値<br>
	 * 主にconfigurationで用いる。<br>
	 */
	boolean success;



	/** 辺リストをコンソールへプリント */
	public void exec_printEdgeList(){
		for(int i=0;i<edgeList.length;i++){
			System.out.println(edgeList[i][0] + "," + edgeList[i][1]);
		}
	}

	/**
	 * 辺リストをカンマ区切りのテキストとしてファイルに保存<br>
	 * このメソッドで保存したcsvファイルはgephiで開くことができる。<br>
	 * @param fileName 書き込みたいファイルのパス
	 */
	public void exec_printEdgeList(String fileName){
		PrintWriter pw;
		try{
			pw = new PrintWriter(new File(fileName));
			for(int i=0;i<edgeList.length;i++){
				pw.println(edgeList[i][0] + "," + edgeList[i][1]);
			}
			pw.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	/**
	 * edgeListを基にneightborList, addressList, neightborIndexListを定義する関数。<br>
	 * @param overwrite 真の場合、すでにneightborListが定義済み、それを上書きして再定義する。
	 */
	public void set_neightbor(boolean overwrite){
		if(neightborList == null || overwrite){
			// addressList初期化
			addressList = new int[N];
			addressList[0] = 0;
			for(int i=1;i<N;i++){
				addressList[i] = addressList[i-1] + degree[i-1];
			}

			// neightborListをどこまで埋めたか管理する変数
			int[] cursor = new int[N];
			for(int i=0;i<N;i++) cursor[i]=addressList[i];

			// edgeListを参照してneightborListを生成
			neightborList = new int[M*2];
			neightborIndexList = new int[M*2];
			for(int i=0;i<M;i++) neightborIndexList[i]=-123456;
			for(int i=0;i<M;i++){
				int left = edgeList[i][0];
				int right = edgeList[i][1];
				neightborList[cursor[left]] = right;
				neightborList[cursor[right]] = left;
				neightborIndexList[cursor[left]] = i;
				neightborIndexList[cursor[right]] = i;
				cursor[left]++;
				cursor[right]++;
			}
		}

	}

	/**
	 * edgeListを基にneightborList, addressList, neightborIndexListを定義する関数。<br>
	 */
	public void set_neightbor(){
		set_neightbor(false);
	}

	/**
	 * 連結成分の解析を行う。<br>
	 */
	public void calc_connectedCompornent() {
		ccCount = 0;
		ccIDList = new int[N];

		boolean[] visit= new boolean[N];
		for(int i=0;i<N;i++) visit[i]=false;
		ArrayList<Integer> queue = new ArrayList<>();

		while(true) {
			// 探索の出発となる頂点を探す。
			for(int i=0;i<N;i++) {
				if(!visit[i]) {
					queue.add(i);
					visit[i] = true;
					break;
				}
			}

			// 「queueが空⇔未探索頂点が存在しない」なので無限ループを抜け出す。
			if(queue.isEmpty()) break;

			// 一般的な幅優先探索
			while(!queue.isEmpty()) {
				// queueの先頭の頂点を抽出。currentNodeとする。
				int currentNode = queue.get(0);
				queue.remove(0);
				ccIDList[currentNode] = ccCount;
				int currentDegree = degree[currentNode];

				// currentNodeの未探索隣接頂点をqueueに格納
				for(int i=0;i<currentDegree;i++) {
					int neighborNode = neightborList[addressList[currentNode]+i];
					if(!visit[neighborNode]) {
						visit[neighborNode] = true;
						queue.add(neighborNode);
					}
				}
			}
			ccCount++;
		}
	}





	/**
	 * 「Observability transitions in clustered networks」用にサイトパーコレーションを拡張したメソッド<br>
	 * 通常のパーコレーションの処理に加え、連鎖故障の処理を加えている。<br>
	 * 連鎖故障をしたければ、引数名chainをtrueにすれば良い。<br>
	 * 逆にchainをfalseにすれば、通常のサイトパーコレーションになる。<br>
	 * <b>(注1)</b>neightborListを定義していないと、実行することはできません。<br>
	 * <b>(注2)</b>このメソッドでは、故障情報directDeleted, indirectDeletedを計算しているだけで、
	 * 		実際にデータから頂点データが失われるわけではない。<br>
	 * @param f 故障確率
	 * @param chain 連鎖故障させるか?
	 */
	public void exec_sitePercolationNDI(double f, boolean chain, long seed) {
		Random rnd = new Random(seed);

		// D,I情報初期化
		directDeleted = new boolean[N];
		indirectDeleted = new boolean[N];
		for(int i=0;i<N;i++) {
			directDeleted[i] = false;
			indirectDeleted[i] = false;
		}
		// D情報を計算。連鎖故障フラグ(boolean chain)がtrueなら、I情報も計算。
		for(int i=0;i<N;i++) {
			int currentNode = i;
			if(rnd.nextDouble() < f) {
				directDeleted[currentNode] = true;
				if(chain) {
					for(int j=0;j<degree[currentNode];j++) {
						int neighborNode = neightborList[addressList[currentNode]+j];
						indirectDeleted[neighborNode] = true;
					}
				}
			}
		}
		// 処理の関係上、D=trueかつI=trueであることがある。
		// その場合、D=true,I=falseとする。
		for(int i=0;i<N;i++) {
			int currentNode = i;
			if(directDeleted[currentNode] && indirectDeleted[currentNode]) {
				indirectDeleted[currentNode] = false;
			}
		}
	}



	/**
	 * メソッドexec_sitePercolationNDIで計算したN,D,I情報を基に、それらでできる連結成分を解析する。<br>
	 * 3つのフラグcompN, compD, compIにより探索する連結成分を決める。<br>
	 * 例えば、compN=true, compD=false, compI=falseならNコンポーネントを計算する。<br>
	 * compN=false, compD=true, compI=trueならDIコンポーネントを計算する。<br>
	 * @param compN
	 * @param compD
	 * @param compI
	 */
	public void calc_connectedCompornentNDI(boolean compN, boolean compD, boolean compI) {
		// 変数codeで引数の情報を整理する。
		int code = 0;
		if(compN) code += 4;
		if(compD) code += 2;
		if(compI) code += 1;
		boolean valid = true;
		ccMember_NDI = new ArrayList<>();

		switch(code) {
		case 0:
			System.out.println("不正な引数です。すべてがfalseになっています。");
			valid=false;
			break;
		case 6:
			System.out.println("不正な引数です。NとDが隣接することはないです。");
			valid=false;
			break;
		case 7:
			System.out.println("これでは全頂点での連結成分を調べてしまいます。");
			System.out.println("ConnectedCompornentメソッドを使ってください。");
			valid=false;
			break;
		}

		max_ccSize_NDI = 0;
		if(valid) {
			boolean[] visitList = new boolean[N];
			for(int i=0;i<N;i++) visitList[i]=true;

			if(compN) {
				for(int i=0;i<N;i++) {
					if(!directDeleted[i] && !indirectDeleted[i]) {
						visitList[i] = false;
					}
				}
			}
			if(compD) {
				for(int i=0;i<N;i++) {
					if(directDeleted[i]) {
						visitList[i] = false;
					}
				}
			}
			if(compI) {
				for(int i=0;i<N;i++) {
					if(indirectDeleted[i]) {
						visitList[i] = false;
					}
				}
			}

			ArrayList<Integer> queue = new ArrayList<>();
			ArrayList<Integer> currentMamberList;
			while(true) {
				currentMamberList = new ArrayList<>();
				for(int i=0;i<N;i++) {
					if(!visitList[i]) {
						queue.add(i);
						visitList[i] = true;
						break;
					}
				}

				if(queue.isEmpty()) break;

				while(!queue.isEmpty()) {
					int currentNode = queue.get(0);
					int currentDegree = degree[currentNode];
					queue.remove(0);
					currentMamberList.add(currentNode);
					for(int i=0;i<currentDegree;i++) {
						int neighborNode = neightborList[addressList[currentNode]+i];
						if(!visitList[neighborNode]) {
							visitList[neighborNode] = true;
							queue.add(neighborNode);
						}
					}
				}
				ccMember_NDI.add(currentMamberList);
				if(max_ccSize_NDI < currentMamberList.size()) max_ccSize_NDI = currentMamberList.size();
			}
		}

	}

	/**
	 * 重みw_{ij}を、w_{ij} \propto {k_i k_j}^alphaになるように割り振る。
	 * @param alpha 相関の強度
	 */
	public void set_weightToAlpha(double alpha){
		weight = new double[M];
		for(int i=0;i<M;i++) {
			int[] currentNode = new int[2];
			currentNode[0] = edgeList[i][0];
			currentNode[1] = edgeList[i][1];
			int degreeProduct = degree[currentNode[0]]*degree[currentNode[1]];
			double powered_degreeProduct = Math.pow(degreeProduct, alpha);
			weight[i] = powered_degreeProduct;
		}
	}


	// TODO ここにjavadoc
	public void set_weight_by_BiasedRW(int step, double alpha, double teleportP, long seed) {
		Random rnd = new Random(seed);

		weight = new double[M];
		for(int i=0;i<M;i++) weight[i]=1.0;

		int currentNode = rnd.nextInt(N);
		for(int t=0;t<step;t++){
			if(degree[currentNode]>=1) {
				// ここが各ランダムウォークで変化する内容(辺の選択方法)
				double sumPoweredDegree = 0.0;
				for(int i=0;i<degree[currentNode];i++){
					int currentNeightbor = neightborList[addressList[currentNode]+i];
					sumPoweredDegree += Math.pow(degree[currentNeightbor], alpha);
				}
				double r = sumPoweredDegree*rnd.nextDouble();
				int selectedEdge = 0;
				double threshold = Math.pow(degree[neightborList[addressList[currentNode]]], alpha);
				while(r > threshold){
					selectedEdge++;
					threshold += Math.pow(degree[neightborList[addressList[currentNode]+selectedEdge]], alpha);
				}

				// 加重
				int throughEdgeIndex = neightborIndexList[addressList[currentNode]+selectedEdge];
				weight[throughEdgeIndex] += 1.0;

				// 隣接点へ遷移
				currentNode = neightborList[addressList[currentNode]+selectedEdge];
			}else {
				// 次数0なら確定ワープ
				t--;
				currentNode = rnd.nextInt(N);
				continue;
			}

			// テレポート判定
			if(rnd.nextDouble() < teleportP){
				currentNode = rnd.nextInt(N);
			}

		}
	}

	// TODO ここにjavadoc
	public void set_weight_by_reinforcedRW(int step, double deltaW, double teleportP, long seed) {
		Random rnd = new Random(seed);

		weight = new double[M];
		for(int i=0;i<M;i++) weight[i]=1.0;

		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}

		int currentNode = rnd.nextInt(N);
		for(int t=0;t<step;t++){
			if(degree[currentNode]>=1) {
				// ここが各ランダムウォークで変化する内容(辺の選択方法)
				double r = (sumW[currentNode]*rnd.nextDouble());
				int selectedEdgeOrder = 0;
				int selectedEdge = neightborIndexList[addressList[currentNode]];
				double threshold = weight[selectedEdge];
				while(r > threshold){
					selectedEdgeOrder++;
					selectedEdge = neightborIndexList[addressList[currentNode]+selectedEdgeOrder];
					threshold += weight[selectedEdge];
				}

				// 加重
				int throughEdgeIndex = neightborIndexList[addressList[currentNode]+selectedEdgeOrder];
				weight[throughEdgeIndex] += deltaW;
				sumW[edgeList[selectedEdge][0]] += deltaW;
				sumW[edgeList[selectedEdge][1]] += deltaW;

				// 隣接点へ遷移
				currentNode = neightborList[addressList[currentNode]+selectedEdgeOrder];
			}else {
				// 次数0なら確定ワープ
				t--;
				currentNode = rnd.nextInt(N);
				continue;
			}

			// テレポート判定
			if(rnd.nextDouble() < teleportP){
				currentNode = rnd.nextInt(N);
			}

		}
	}


	/**
	 * 重みを僅かにブレさせる
	 * @param seed
	 */
	public void exec_weightDisturb(long seed) {
		double smallNumber = 1E-6;
		Random rnd = new Random(seed);
		for(int i=0 ; i<weight.length ; i++) {
			weight[i] = weight[i] * (1 + rnd.nextDouble()*smallNumber);
		}
	}


	/**
	 * 重みをシャッフルする。<br>
	 * TODO フィッシャーイェーツのくじ箱構造を利用することで、効率化できる可能性がある。<br>
	 * @param seed
	 */
	public void weightShuffle(int seed) {
		if(weight.length>0) {
			ArrayList<Double>  weightList = new ArrayList<Double>();
			for(int i=0;i<weight.length;i++) {
				weightList.add(weight[i]);
			}

			Random rnd = new Random(seed);
			int r;
			for(int i=0;i<weight.length;i++) {
				r = rnd.nextInt(weightList.size());
				weight[i] = weightList.get(r);
				weightList.remove(r);
			}
		}
	}




	/**
	 * 頂点と辺の媒介中心性を測定する。
	 */
	public void calc_betweenness(){
		if(weight==null || weight.length<=0) {
			weight = new double[M];
			for(int i=0;i<M;i++) weight[i]=1.0;
		}
		if(neightborList==null || neightborList.length<=0) {
			System.out.println("neighborListが正しく定義されていません。プログラムを終了します。");
			System.exit(1);
		}

		ArrayList<Integer> queue = new ArrayList<Integer>();
		ArrayList<Integer> stack = new ArrayList<Integer>();

		double[] sigma = new double[N];
		double[] delta = new double[N];
		for(int i=0;i<N;i++)sigma[i]=0;

		//distance from source
		double[] dist = new double[N];

		//list of predecessors on shortest paths from source
		// Pred:sから各頂点への最短路において一つ前の頂点番号を格納するリスト
		// 最短路が複数ある場合に対応するためにPredは少々複雑な構造をしている。
		// PredとPredCursorを組み合わせて使う。
		int[] Pred = new int[2*M];
		int[] PredCursor = new int[N];
		// PredIndex:一つ前の頂点へと結ぶ辺番号を返す。
		int[] PredIndex = new int[2*M];

		// contentQueue[i]=trueならば、頂点iはqueueに入っていることを示す。
		// これを用いて『insert/update w』を実行する。
		// TODO ここ、もっとよくかけるんじゃないか?
		boolean[] contentQueue = new boolean[N];

		nodeBetweenness = new double[N];
		edgeBetweenness = new double[M];

		// 論文のラムダに相当する配列
		double[] inv_weight = new double[M];
		for(int i=0;i<M;i++) inv_weight[i] = 1.0/weight[i];


		for(int s=0 ; s<N ; s++){
			//// single-source shortest-paths problem
			// initialization
			for(int i=0;i<N;i++) PredCursor[i]=addressList[i]; //PredCursor初期化(事実上のPred初期化)
			for(int i=0;i<N;i++) dist[i]=Double.MAX_VALUE;
			for(int i=0;i<N;i++) contentQueue[i]=false;
			for(int i=0;i<N;i++) sigma[i]=0;

			dist[s] = 0;
			sigma[s] = 1;
			queue.add(s);
			contentQueue[s] = true;

			while(!queue.isEmpty()){
				// queueからdist[v]が最小となるものを取り出す
				double minDis = Double.MAX_VALUE;
				int v = -1;
				int minIndex = -1;
				for(int i=0;i<queue.size();i++){
					if(minDis > dist[queue.get(i)]){
						minDis = dist[queue.get(i)];
						v = queue.get(i);
						minIndex = i;
					}
				}
				queue.remove(minIndex);
				contentQueue[v] = false;
				stack.add(v);

				final int vAddress = addressList[v];
				for(int neighbor=0 ; neighbor<degree[v] ; neighbor++){
					final int currentCursor = vAddress + neighbor;
					int w = neightborList[currentCursor];
					// path discovery
					int vwEdge = neightborIndexList[currentCursor];
					if(dist[w] > dist[v] + inv_weight[vwEdge]){
						dist[w] = dist[v] + inv_weight[vwEdge];

						// insert/update w
						if(contentQueue[w]) {
							for(int i=0;i<queue.size();i++){
								if(queue.get(i) == w){
									queue.remove(i);
									break;
								}
							}
						}
						queue.add(w);
						contentQueue[w] = true;

						sigma[w] = 0;

						PredCursor[w] = addressList[w];
					}
					//path counting
					if(dist[w] == dist[v]+inv_weight[vwEdge]){
						sigma[w] = sigma[w] + sigma[v];
						Pred[PredCursor[w]] = v;
						PredIndex[PredCursor[w]] = vwEdge;
						PredCursor[w]++;
					}
				}
			}

			for(int i=0;i<delta.length;i++)delta[i]=0.0;

			//// accumulation
			while(!stack.isEmpty()){
				int w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				final int PredSize = PredCursor[w]-addressList[w];
				for(int i=0 ; i<PredSize ; i++){
					int v = Pred[addressList[w]+i];

					int vwEdge = PredIndex[addressList[w]+i];
					double c = (sigma[v]/sigma[w]) * (1.0+delta[w]);
					edgeBetweenness[vwEdge] += c;
					delta[v] = delta[v] + c;
				}

				if(w!=s){
					nodeBetweenness[w] = nodeBetweenness[w] + delta[w];
				}
			}
		}


	}


	/**
	 * link salienceを計算する。<br>
	 * このアルゴリズムは『Robust classification of salient links in complex networks』のサプリメントに載っているものを参考にしている。<br>
	 * URL:
	 * https://media.nature.com/original/nature-assets/ncomms/journal/v3/n5/extref/ncomms1847-s1.pdf
	 */
	public void calc_linkSalience(){
		// 正しく定義されていない場合、強制終了させる。
		if(weight==null || weight.length<=0) {
			System.out.println("weightが正しく定義されていません。プログラムを終了します。");
			System.exit(1);
		}
		if(neightborList==null || neightborList.length<=0) {
			System.out.println("neightborListが正しく定義されていません。"
					+ "このメソッドより前にset_neightbor()を実行してください。プログラムを終了します。");
			System.exit(1);
		}

		// salience初期化
		linkSalience = new int[M];

		ArrayList<Integer> queue = new ArrayList<Integer>();
		ArrayList<Integer> stack = new ArrayList<Integer>();

		//distance from source
		double[] dist = new double[N];

		//list of predecessors on shortest paths from source
		// Pred:sから各頂点への最短路において一つ前の頂点番号を格納するリスト
		// 最短路が複数ある場合に対応するためにPredは少々複雑な構造をしている。
		// PredとPredCursorを組み合わせて使う。
		int[] Pred = new int[2*M];
		int[] PredCursor = new int[N];
		// PredIndex:一つ前の頂点へと結ぶ辺番号を返す。
		int[] PredIndex = new int[2*M];

		// contentQueue[i]=trueならば、頂点iはqueueに入っていることを示す。
		// これを用いて『insert/update w』を実行する。
		// TODO ここ、もっとよくかけるんじゃないか?
		boolean[] contentQueue = new boolean[N];

		// 論文のラムダに相当する配列
		double[] inv_weight = new double[M];
		for(int i=0;i<M;i++) inv_weight[i] = 1.0/weight[i];

		// ここから本処理
		for(int s=0 ; s<N ; s++){
			//// single-source shortest-paths problem
			// initialization
			for(int i=0;i<N;i++) PredCursor[i]=addressList[i]; //PredCursor初期化(事実上のPred初期化)
			for(int i=0;i<N;i++) dist[i]=Double.MAX_VALUE;
			for(int i=0;i<N;i++) contentQueue[i]=false;

			dist[s] = 0;
			queue.add(s);
			contentQueue[s] = true;

			while(!queue.isEmpty()){
				// queueからdist[v]が最小となるものを取り出す
				double minDis = Double.MAX_VALUE;
				int v = -1;
				int minIndex = -1;
				for(int i=0;i<queue.size();i++){
					if(minDis > dist[queue.get(i)]){
						minDis = dist[queue.get(i)];
						v = queue.get(i);
						minIndex = i;
					}
				}
				queue.remove(minIndex);
				contentQueue[v] = false;
				stack.add(v);

				final int vAddress = addressList[v];
				for(int neighbor=0 ; neighbor<degree[v] ; neighbor++){
					final int currentCursor = vAddress + neighbor;
					int w = neightborList[currentCursor];
					// path discovery
					int vwEdge = neightborIndexList[currentCursor];
					if(dist[w] > dist[v] + inv_weight[vwEdge]){
						dist[w] = dist[v] + inv_weight[vwEdge];

						// insert/update w
						if(contentQueue[w]) {
							for(int i=0;i<queue.size();i++){
								int currentNode = queue.get(i);
								if(currentNode == w){
									queue.remove(i);
									break;
								}
							}
						}
						queue.add(w);
						contentQueue[w] = true;

						PredCursor[w] = addressList[w];
					}
					//path counting
					if(Double.compare(dist[w], dist[v]+inv_weight[vwEdge]) == 0){
						Pred[PredCursor[w]] = v;
						PredIndex[PredCursor[w]] = vwEdge;
						PredCursor[w]++;
					}
				}
			}

			//// accumulation
			while(!stack.isEmpty()){
				int w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				// 各辺のlink salienceを与える。
				// ここをみると複数最短路があっても、1を折半せずに、両方の辺に1ずつ与えているのが確認できる。
				// 元論文の時点でこうなっているが、修正するべきなのかもしれない。
				// このアルゴリズムの大本となっている『A faster algorithm for betweenness centrality』には1を折半する方法が書かれている。
				final int PredSize = PredCursor[w]-addressList[w];
				for(int i=0 ; i<PredSize ; i++){
					int vwEdge = PredIndex[addressList[w]+i];
					linkSalience[vwEdge]++;
				}
			}
		}

	}
















}
