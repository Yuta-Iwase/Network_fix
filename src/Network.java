import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

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
	ArrayList<ArrayList<Integer>> CompNDI_Member;

	/**
	 * calc_linkSalience()メソッドを実行することで数値が与えられる。<br>
	 * linkScalience[i]=(頂点iのlink salience)×N<br>
	 * が与えられている。<br>
	 * link salienceそのものではなく、N倍であることに注意すること。
	 */
	int[] linkSalience;

	/** 辺リストをコンソールへプリント */
	public void printEdgeList(){
		for(int i=0;i<edgeList.length;i++){
			System.out.println(edgeList[i][0] + "," + edgeList[i][1]);
		}
	}

	/**
	 * 辺リストをカンマ区切りのテキストとしてファイルに保存<br>
	 * @param fileName 書き込みたいファイルのパス
	 */
	public void printEdgeList(String fileName){
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
	 * edgeListを基にneightborList, addressList, neightborIndexListを定義する関数。
	 */
	public void setNeightbor(){
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

	/**
	 * 連結成分の解析を行う。<br>
	 */
	public void calc_ConnectedCompornent() {
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
	 * 連鎖故障のメソッドSitePercolation2018での連結成分を調べる。
	 */
	public void ConnectedCompornentNDI(boolean compN,boolean compD,boolean compI) {
		int code = 0;
		if(compN) code += 4;
		if(compD) code += 2;
		if(compI) code += 1;
		boolean valid = true;
		CompNDI_Member = new ArrayList<>();

		switch(code) {
		case 0:
			System.out.println("不正な引数です。");
			valid=false;
			break;
		case 6:
			System.out.println("不正な引数です。");
			valid=false;
			break;
		case 7:
			System.out.println("これでは全頂点での連結成分を調べてしまいます。");
			System.out.println("ConnectedCompornentメソッドを使ってください。");
			valid=false;
			break;
		}

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
				CompNDI_Member.add(currentMamberList);
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
					+ "このメソッドより前にsetNeightbor()を実行してください。プログラムを終了します。");
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