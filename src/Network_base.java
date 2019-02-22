import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

// 課題:
//// ・重み付きネットワークに対応していない(7/28にて改善)
//// ・頂点のラベルに未対応
//// ・フィールド変数はprivateとしてゲッターを用いて呼び出すべきか?
//// ・フィールド変数weight[] と Edgeクラスのweight が役割として重複している。
//// ・(16/12/12-14)にかけてのエラーの大本はココ、このプロジェクトの火薬庫
//// ・[17/03/01]linkSalience()の吟味
// 更新
// 16/7/28
// ・Edgeクラス追加
// ・フィールド変数weight[],weighted追加
// ・searchEdgeメソッドを追加
// 16/8/8
// ・csvを読み込み対応した頂点に対応したラベルを頂点に与える
//   メソッド setLabel(String inputFilePath)の追加
// 16/12/14
// ・ダブルカウント(例:{0,1},{1,0}と書く)への対策を行う
// 　反動で正しく動作しないメソッド、クラスが現れるかも？
// 17/02/04
// ・setEdgeメソッドを定義
//  (注)list[][]が定義されてから使ってください
//  内容:ネットワークのEdgeのリストを作る
//       頂点について、所有する辺のリストeListを使うことができる
// 17/03/12
// ・「●doubleCount=true」の部分を
// 　「●doubleCount=false」に変更(間違えている可能性あり)
// 17/05/02
// ・reinforcedRWのメソッド化
// 17/06/18
// ・reinforcedRWメソッドにオーバーロード
// 　テレポート確率を追加

public class Network_base implements Cloneable{
	// フィールド変数 N,M,list,success の4つを持つ
	int N,M;
	int[][] list;
	int[] degree;
	double[] weight;
	boolean directed;
	boolean doubleCount;
	boolean weighted;
	boolean success = true; //基本true サブクラス次第でfalseにもなる

	double degreeCorrelationCoefficient;
	double DCC_divider; //次数相関係数の分母

	//// レガシー変数
	// setNode()メソッドを実行することでノードリストを使うことができる
	ArrayList<Node> nodeList = new ArrayList<Node>();
	// setNode()->setEdge()で使用可能
	ArrayList<Edge> edgeList = new ArrayList<Edge>();

	//// 新設
	int[] neightborList = null;
	int[] addressList = null;
	int[] neightborIndexList = null;
	int[] linkSalience = null;

	 // BiasedRandomWalk_continueWeightで用いる訪問済みリスト
	boolean[] visitedNodes_onRW = null;
	boolean[] visitedEdge_onRW = null;


	// setLabel(String inputFilePath)メソッドを実行することでラベル設定を読み込むことができる
	String[] nodeLabel;
	// 直接この変数にアクセスして、手動で定義して利用する。
	String[] edgeLabel;

	// ConnectedCompornentにより生成される変数
	int count_cc;
	ArrayList<ArrayList<Node>> ccMember;
	int[] ccID_List;
	int maxCC;

	// Direct,In-direct破壊でのN,D,I,DI,NIの各連結成分データ
	ArrayList<ArrayList<Node>> CompNDI_Member;
	int maxCC_NID;

	// パーコレーション等により頂点数が変化するとき用の頂点リスト
	private ArrayList<Integer> existNodeList = new ArrayList<Integer>();

	// MinimumSpanningTreeメソッドで作成された最小生成木の頂点Indexリスト
	ArrayList<Integer> MST_Nodes = new ArrayList<Integer>();
	// MinimumSpanningTreeメソッドで作成された最小生成木の辺Indexリスト
	ArrayList<Integer> MST_Edges = new ArrayList<Integer>();
	// MinimumSpanningTreeメソッドで作成された最小生成木の隣接リスト
	int[][] MST_list;

	// MinimumSpanningTree(s)メソッドで作成された最小生成木の辺Indexリスト
	ArrayList<Integer> SPT_Edges = new ArrayList<Integer>();
	// MinimumSpanningTreeメソッドで作成された最小生成木の隣接リスト
	int[][] SPT_list;


	/** 隣接リストをコンソールへプリント */
	public void printList(){
		if(success)
			for(int i=0;i<list.length;i++){
				System.out.println(list[i][0] + "," + list[i][1]);
			}
		else
			System.out.println("生成に失敗しているため表示できません。");
	}

	/** 隣接リストをcsv形式で保存 */
	public void printList(String fileName){
		PrintWriter pw;
		if(success){
			try{
				pw = new PrintWriter(new File(fileName));
				for(int i=0;i<list.length;i++){
					pw.println(list[i][0] + "," + list[i][1]);
				}
				pw.close();
			}catch(Exception e){
				System.out.println(e);
			}
		}else{
			System.out.println("生成に失敗しているため表示できません。");
		}
	}

	/**
	 * 平均次数を返す
	 */
	public double averageDegree() {
		double sumDegree = 0.0;
		for(int i=0;i<degree.length;i++) {
			sumDegree += degree[i];
		}
		return (sumDegree/(double)degree.length);
	}

	/** 隣接リストをcsv形式で保存
	次数0などの特別な頂点に対応(nodeListを定義しないと使えない) */
	public void printListExtention(String fileName){
		PrintWriter pw;
		if(success) {
			if(existNodeList.size()>0) {
				try{
					pw = new PrintWriter(new File(fileName));
					for(int i=0 ; i<existNodeList.size() ; i++){
						pw.println(existNodeList.get(i));
					}
					for(int i=0;i<list.length;i++){
						pw.println(list[i][0] + "," + list[i][1]);
					}
					pw.close();
				}catch(Exception e){
					System.out.println(e);
				}
			}else {
				try{
					pw = new PrintWriter(new File(fileName));
					for(int i=0 ; i<N ; i++){
						pw.println(i);
					}
					for(int i=0;i<list.length;i++){
						pw.println(list[i][0] + "," + list[i][1]);
					}
					pw.close();
				}catch(Exception e){
					System.out.println(e);
				}
			}
		}else{
			System.out.println("条件を満たさないため表示できません。");
		}

	}

	// sort()メソッドのためのメソッド
	private int[][] quickSort(int[][] list,int low,int high, int level){
		int space1,space2;
		if(low<high){
			int mid = (low + high)/2;
			int x = list[mid][level];
			int i=low;
			int j=high;
			while(i<=j){
				while(list[i][level] < x) i++;
				while(list[j][level] > x) j--;
				if(i<=j){
					space1=list[i][0]; space2=list[i][1];
					list[i][0]=list[j][0]; list[i][1]=list[j][1];
					list[j][0]=space1; list[j][1]=space2;
					i++; j--;
				}
			}
			quickSort(list,low,j,level);
			quickSort(list,i,high,level);
		}
		return list;
	}

	/** 隣接リストを辞書式順序(昇順)へ整列 */
	public void sort(){
		if(success){
			if(doubleCount) quickSort(list,0,M*2-1,0);
			else quickSort(list,0,M-1,0);
			int low=0;
			int high=0;
			sortLevel2 : for(int n=0 ; n<N ; n++){
				while(list[high][0] == n){
					high++;
					if(high == M)break sortLevel2;
				}
				if( (high-low) > 1){
					quickSort(list,low,high-1,1);
				}
				low = high;
			}
		}else{
			System.out.println("生成に失敗しているためソートできません。");
		}
	}

	/** このメソッドを実行することで<br>
	 * 頂点のリストnodeListを使うことができる。<br>
	 * (注):doubleCountが偽のときはsetNode(boolean input_doubleCount)を使うこと<br>*/
	public void setNode(){
		nodeList.clear();
		// 隣接リストを辞書式順序(昇順)へ整列
		sort();

		// Nodeを初期化しnodeListへ追加する
		for(int n=0;n<N;n++){
			nodeList.add( new Node(n) );
		}

		//各Nodeの隣接リストを定義
		int currentEdge = 0;
		for(int n=0;n<N;n++){
			for(int i=0;i<degree[n];i++){
				nodeList.get(n).list.add(nodeList.get(list[currentEdge+i][1]));
			}
			currentEdge += degree[n];
		}
	}
	/** setNode()ではdoubleCountが偽のときの
	 * 動作がおかしかった。それを修正するためのオーバーロード*/
	public void setNode(boolean input_doubleCount){
		nodeList.clear();
		// 隣接リストを辞書式順序(昇順)へ整列
		sort();

		// Nodeを初期化しnodeListへ追加する
		for(int n=0;n<N;n++){
			nodeList.add( new Node(n) );
		}

		//各Nodeの隣接リストを定義
		if(input_doubleCount){
			int currentEdge = 0;
			for(int n=0;n<N;n++){
				for(int i=0;i<degree[n];i++){
					nodeList.get(n).list.add(nodeList.get(list[currentEdge+i][1]));
				}
				currentEdge += degree[n];
			}
		}else{
			for(int m=0;m<M;m++){
				nodeList.get(list[m][0]).list.add(nodeList.get(list[m][1]));
				nodeList.get(list[m][1]).list.add(nodeList.get(list[m][0]));
			}
		}
	}

	/** このメソッドを実行することで<br>
	 * 辺のリストedgeListを使うことができる。<br>
	 * 頂点について、所有する辺のリストeListを使うことができる。<br>
	 * (注)<br>
	 * list[][]が定義されている場合のみ使用可能<br>
	 * さらにsetNode()またはsetNode(false)適用後でないと使えない<br>
	 * */
	public void setEdge(){
		edgeList.clear();
		Edge currentEdge = null;
		for(int i=0;i<M;i++){
			// 現在のループで扱う辺
			currentEdge = new Edge(list[i][0],list[i][1],i);
			// edgeListへ登録
			edgeList.add(currentEdge);
			// eListへ登録
			nodeList.get(list[i][0]).eList.add(currentEdge);
			nodeList.get(list[i][1]).eList.add(currentEdge);
		}
	}
	/**
	 *
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
			int left = list[i][0];
			int right = list[i][1];
			neightborList[cursor[left]] = right;
			neightborList[cursor[right]] = left;
			neightborIndexList[cursor[left]] = i;
			neightborIndexList[cursor[right]] = i;
			cursor[left]++;
			cursor[right]++;
		}
	}

	/** csvファイルを読み込みラベルを割り当てるメソッド
	 * 書式は「(頂点番号),(ラベル名)」を1行ずつ羅列させていく
	 * ※ 頂点間の区切りは原則カンマとするが、スペースやタブでも対応できるようにしておく。
	 * ！ 読み込みファイルは書き方を誤ると想定しないエラーが起こりやすいので注意 ！
	 */
	public void setLabel(String inputFilePath){
		Scanner scan = null;
		nodeLabel = new String[N];
		String punctuation = "";
		String currentLine;
		int pancPos;
		try{
			scan = new Scanner(new File(inputFilePath));

			// 区切り文字の識別
			currentLine = scan.nextLine(); //1行目のみwhileループ外で行う
			if(currentLine.indexOf(",") > -1){
				punctuation = ",";
			}else if(currentLine.indexOf(" ") > -1){
				punctuation = " ";
			}else if(currentLine.indexOf("\t") > -1){
				punctuation = "\t";
			}else{
				scan.close();
				throw new Exception();
			}

			// 読み込みファイル1行目のみループ外で処理する
			pancPos = currentLine.indexOf(punctuation);
			nodeLabel[Integer.parseInt(currentLine.substring(0, pancPos))] = currentLine.substring(pancPos+1);

			// ループ開始
			while(scan.hasNextLine()){
				currentLine = scan.nextLine();
				pancPos = currentLine.indexOf(punctuation);
				nodeLabel[Integer.parseInt(currentLine.substring(0, pancPos))] = currentLine.substring(pancPos+1);
			}

			scan.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	/**
	 * 頂点vの隣接頂点の配列を返す
	 */
	public int[] neightbor(int v) {
		int[] v_neightbor = new int[degree[v]];
		for(int i=0;i<degree[v];i++) {
			v_neightbor[i] = neightborList[addressList[v]+i];
		}
		return v_neightbor;
	}

	/** 引数n,mをもつ辺のindexを返す*/
	public int searchEdge(int n,int m){
		int index;
		for(index=0 ; index<M ; index++){
			if( (list[index][0]==n&&list[index][1]==m) || (list[index][0]==m&&list[index][1]==n) ){
				break;
			}
		}
		return index;
	}

	/**
	 * 連結成分の解析を行う。
	 * setNode,setEdgeを実行させておく必要がある。
	 */
	public void ConnectedCompornent() {
		count_cc = 0;
		ccMember = new ArrayList<>();
		ccID_List = new int[N];

		boolean[] visit= new boolean[N];
		for(int i=0;i<N;i++) visit[i]=false;
		ArrayList<Node> queue = new ArrayList<>();
		ArrayList<Node> currentMamberList;

		while(true) {
			currentMamberList = new ArrayList<>();
			for(int i=0;i<N;i++) {
				if(!visit[i]) {
					queue.add(nodeList.get(i));
					visit[i] = true;
					break;
				}
			}

			if(queue.isEmpty()) break;

			while(!queue.isEmpty()) {
				Node currentNode = queue.get(0);
				queue.remove(0);
				ccID_List[currentNode.index] = count_cc;
				currentMamberList.add(currentNode);
				for(int i=0;i<currentNode.list.size();i++) {
					Node neighborNode = currentNode.list.get(i);
					if(!visit[neighborNode.index]) {
						visit[neighborNode.index] = true;
						queue.add(neighborNode);
					}
				}
			}
			count_cc++;
			ccMember.add(currentMamberList);
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

		maxCC_NID = 0;
		if(valid) {
			boolean[] visitList = new boolean[N];
			for(int i=0;i<N;i++) visitList[i]=true;

			if(compN) {
				for(int i=0;i<N;i++) {
					if(!nodeList.get(i).directDeleted && !nodeList.get(i).indirectDeleted) {
						visitList[i] = false;
					}
				}
			}
			if(compD) {
				for(int i=0;i<N;i++) {
					if(nodeList.get(i).directDeleted) {
						visitList[i] = false;
					}
				}
			}
			if(compI) {
				for(int i=0;i<N;i++) {
					if(nodeList.get(i).indirectDeleted) {
						visitList[i] = false;
					}
				}
			}

			ArrayList<Node> queue = new ArrayList<>();
			ArrayList<Node> currentMamberList;
			while(true) {
				currentMamberList = new ArrayList<>();
				for(int i=0;i<N;i++) {
					if(!visitList[i]) {
						queue.add(nodeList.get(i));
						visitList[i] = true;
						break;
					}
				}

				if(queue.isEmpty()) break;

				while(!queue.isEmpty()) {
					Node currentNode = queue.get(0);
					queue.remove(0);
					currentMamberList.add(currentNode);
					for(int i=0;i<currentNode.list.size();i++) {
						Node neighborNode = currentNode.list.get(i);
						if(!visitList[neighborNode.index]) {
							visitList[neighborNode.index] = true;
							queue.add(neighborNode);
						}
					}
				}
				CompNDI_Member.add(currentMamberList);
				if(maxCC_NID < currentMamberList.size()) maxCC_NID = currentMamberList.size();
			}
		}

	}

	/**
	 * BondPercolation2018メソッド実行後の連結成分を調べるときに使うメソッド<br>
	 */
	public void ConnectedCompornent_BP2018() {
		ccMember = new ArrayList<>();
		ccID_List = new int[N];
		maxCC = 0;

		boolean[] visit = new boolean[N];
		for(int i=0;i<N;i++) visit[i]=false;

		ArrayList<Node> queue = new ArrayList<>();
		ArrayList<Node> currentMamberList;
		int current_CCIndex = -1;
		while(true) {
			currentMamberList = new ArrayList<>();
			current_CCIndex++;
			for(int i=0;i<N;i++) {
				if(!visit[i]) {
					queue.add(nodeList.get(i));
					visit[i] = true;
					break;
				}
			}
			System.out.println("debag: newCC is maked now");

			if(queue.isEmpty()) break;

			while(!queue.isEmpty()) {
				Node currentNode = queue.get(0);
				queue.remove(0);
				currentMamberList.add(currentNode);
				ccID_List[currentNode.index] = current_CCIndex;
				for(int i=0;i<currentNode.list.size();i++) {
					Node neighborNode = currentNode.list.get(i);
					Edge neighborEdge = currentNode.eList.get(i);
					if(!neighborEdge.deleted) {
						if(!visit[neighborNode.index]) {
							queue.add(neighborNode);
							visit[neighborNode.index] = true;
						}
					}
				}
			}
			ccMember.add(currentMamberList);
			if(maxCC < currentMamberList.size()) maxCC=currentMamberList.size();
		}

	}

	/** サイト・パーコレーションを実行 */
	public void SitePercolation(double f){
		if(success){
			double x;
			int newM=0;
			ArrayList<Integer> vacantNodeList = new ArrayList<Integer>();
			int[] vacantNodeBinary = new int[N];
			for(int i=0;i<N;i++) vacantNodeBinary[i]=0;
			int currentLink=0;
			int[][] newList = new int[list.length][2];
			boolean occupied = true;
			for(int n=0 ; n<N ; n++){
				x = Math.random();
				// 非占有状態( !(x<f) のとき)なら次を実行
				if( !(x<f) ){
					vacantNodeList.add(n);
					vacantNodeBinary[n]=1;
				}else{
					existNodeList.add(n);
				}
			}
			for(int n=0;n<N;n++){
				if(vacantNodeBinary[n]==1){
					// ノードが空ならば無視し
					// ポインタを次の頂点へ移す
					currentLink += degree[n];
				}else{
					// 現在の辺の存在を判定
					// 以下、頂点nの次数の分、辺をループ
					for(int m=currentLink;m<(currentLink+degree[n]);m++){
						// 以下、空のノードリストと比較
						for(int i=0;i<vacantNodeList.size();i++){
							occupied = true;
							if(list[m][1]==vacantNodeList.get(i)){
								occupied = false;
								break;
							}
							if(list[m][1]<vacantNodeList.get(i)){
								break;
							}
						}
						if(occupied){
							newList[newM][0] = list[m][0];
							newList[newM][1] = list[m][1];
							newM++;
						}
					}
					currentLink += degree[n];
				}
			}
			// newListをlistへコピー(配列の長さはnewMへ制限)
			list = new int[newM][2];
			for(int m=0 ; m<newM ; m++){
				list[m][0] = newList[m][0];
				list[m][1] = newList[m][1];
			}

			// 変数更新
			if(directed) M=newM;
			else M=(newM/2);


		}else{
			System.out.println("生成に失敗しているためパーコレーションできません。");
		}
	}

	/** ボンド・パーコレーションを実行<br>
	 * 確率fで残り、1-fで故障<br>
	 * 無向,ダブルカウントのとき不具合があるかも？(16/12/14)<br>
	 * 次数を更新させる処理を追加(17/07/05)<br>
	 * */
	public void BondPercolation(double f){
		if(success){
			double x;
			int newM=0;
			int[] newDegree = new int[N];
			int[][] newList = new int[list.length][2];
			for(int m=0 ; m<list.length ; m++){
				x = Math.random();
				// 占有状態( x<f のとき)なら次を実行
				if(x<f){
					newList[newM][0] = list[m][0];
					newList[newM][1] = list[m][1];
					newDegree[ newList[newM][0] ]++;
					newDegree[ newList[newM][1] ]++;
					System.out.println(newDegree[ newList[newM][0] ]);
					newM++;
				}
			}
			// newListをlistへコピー(配列の長さはnewMへ制限)
			list = new int[newM][2];
			for(int m=0 ; m<newM ; m++){
				list[m][0] = newList[m][0];
				list[m][1] = newList[m][1];
			}
			// degree更新
			for(int i=0 ; i<N ; i++){
				degree[i] = newDegree[i];
			}

			// 変数更新
			if(directed) M=newM;
			else M=(newM/2);
		}else{
			System.out.println("生成に失敗しているためパーコレーションできません。");
		}
	}

	/** 探索アルゴリズムを実行し結果をプリント */
	public int SearchAlgorithm(boolean print){
		if(success){
			// nodeListが未定義のときここで定義される
			// (nodeListはサイト・パーコレーション メソッドにて定義されている変数)
			if(existNodeList.isEmpty()){
				for(int i=0;i<N;i++) existNodeList.add(i);
			}

			// 探索用変数
			int[] vis = new int[N];
			int currentVis = 0;
			int currentNode;
			for(int i=0;i<N;i++) vis[i]=0;
			ArrayList<Integer> queue = new ArrayList<Integer>();

			// プロット用変数
			int compN = 0;
			int nodes;
			int maxNodes=0;

			// 探索部分
			for(int i=0;i<existNodeList.size();i++){
				if(vis[existNodeList.get(i)]==0){
					nodes=0;
					compN++;
					queue.add(existNodeList.get(i));
					vis[existNodeList.get(i)] = (++currentVis);
					nodes++;
					while(!queue.isEmpty()){
						currentNode = queue.get(0);
						queue.remove(0);

						for(int k=0;k<list.length;k++){
							if(list[k][0]==currentNode && vis[list[k][1]]==0){
								queue.add(list[k][1]);
								vis[list[k][1]] = (++currentVis);
								nodes++;
							}
						}
					}
					maxNodes = Math.max(maxNodes, nodes);
				}
			}
			if(print){
				System.out.println("連結成分数=" + compN);
				System.out.println("最大連結成分の頂点数=" + maxNodes);
			}
			return maxNodes;
		}else{
			System.out.println("生成に失敗しているため探索できません。");
			return 0;
		}
	}

	/**
	 * 2017-8版 サイトパーコレーション<br>
	 * 頂点は破壊されてもデータ上は存在していて、Nodeクラスのフラグがtrueとなる。<br>
	 * このvisitを使うことで、各種の故障に対しての連結成分を調べることができる。<br>
	 * (注)<br>
	 * setNode()またはsetNode(false)を使う必要がある<br>
	 * @param f 故障確率
	 * @param chain 連鎖故障させるか?
	 */
	public void SitePercolation2018(double f,boolean chain) {
		for(int i=0;i<N;i++) {
			nodeList.get(i).directDeleted = false;
			nodeList.get(i).indirectDeleted = false;
		}
		for(int i=0;i<N;i++) {
			Node currentNode =nodeList.get(i);
			if(Math.random() < f) {
				currentNode.directDeleted = true;
				if(chain) {
					for(int j=0;j<currentNode.list.size();j++) {
						Node neighborNode = currentNode.list.get(j);
						neighborNode.indirectDeleted = true;
					}
				}
			}
		}
		for(int i=0;i<N;i++) {
			Node currentNode =nodeList.get(i);
			if(currentNode.directDeleted && currentNode.indirectDeleted) {
				currentNode.indirectDeleted = false;
			}
		}
	}

	/**
	 * 2018版 ボンドパーコレーション<br>
	 * 辺は破壊されてもデータ上は存在していて、Nodeクラスのフラグがtrueとなる。<br>
	 * このvisitを使うことで、各種の故障に対しての連結成分を調べることができる。<br>
	 * (注)<br>
	 * 事前にsetNode()またはsetNode(false)を実行し<br>
	 * その後、setEdge()を実行させる必要がある。
	 * @param f 故障確率
	 */
	public void BondPercolation2018(double f) {
		for(int i=0;i<M;i++) {
			if(Math.random() < f) {
				edgeList.get(i).deleted = true;
			}else {
				edgeList.get(i).deleted = false;
			}
		}
	}

	/**
	 * 次数相関係数を計算する。<br>
	 * edge swapを用いて係数を調整する場合はdegreeCorrelationCoefficient_forSwapping()を使う。<br>
	 * 参考:Newman, Mark EJ. "Mixing patterns in networks." Physical Review E 67.2 (2003): 026126.<br>
	 * 参考:矢久保考介『複雑ネットワークとその構造』共立出版<br>
	 */
	public void degreeCorrelationCoefficient() {
		double productMean = 0.0;
		double sumMean = 0.0;
		double squareSumMean = 0.0;
		for(int i=0;i<M;i++) {
			int node0 = list[i][0];	int node1 = list[i][1];
			int degree0 = degree[node0];	int degree1 = degree[node1];

			productMean += degree0*degree1;
			sumMean += (degree0 + degree1);
			squareSumMean += degree0*degree0 + degree1*degree1;
		}
		productMean /= M;
		sumMean /= M;
		squareSumMean /= M;

		DCC_divider = (2*squareSumMean-sumMean*sumMean);
		degreeCorrelationCoefficient = (4*productMean-sumMean*sumMean) / DCC_divider;
	}

	public double degreeCorrelationCoefficient_forSwapping(boolean positiveCorrelation, int loopLimit) {
		boolean swapValidity = false;

		int edgeA=-1, edgeB=-1;
		int nodeA0=-1, nodeA1=-1, nodeB0=-1, nodeB1=-1;
		int currentFailCount = -1;
		while(!swapValidity) {
			currentFailCount++;
			if(currentFailCount > loopLimit){
				return -123457;
			}
			swapValidity = true;
			edgeA = (int)(Math.random()*list.length);
			edgeB = (int)(Math.random()*list.length);

			if(edgeA==edgeB) swapValidity=false; // 同一辺の交換を禁じる


			nodeA0 = list[edgeA][0];	nodeA1 = list[edgeA][1];
			nodeB0 = list[edgeB][0];	nodeB1 = list[edgeB][1];

			if(nodeA0 == nodeB1 || nodeA1==nodeB0) swapValidity=false; //自己ループを禁じる

			// A0-B1のマルチパスを禁じる
			if(swapValidity) {
				int[] nodeA0_neightborList = neightbor(nodeA0);
				for(int i=0;i<nodeA0_neightborList.length;i++) {
					if(nodeB1 == nodeA0_neightborList[i]) {
						swapValidity = false;
						break;
					}
				}
			}

			// B0-A1のマルチパスを禁じる
			if(swapValidity) {
				int[] nodeB0_neightborList = neightbor(nodeB0);
				for(int i=0;i<nodeB0_neightborList.length;i++) {
					if(nodeA1 == nodeB0_neightborList[i]) {
						swapValidity = false;
						break;
					}
				}
			}

			// 相関係数を目的通りの方向にかえられているか
			if(swapValidity) {
				double DCC_difference = (
						- (degree[nodeA0]*degree[nodeA1]+degree[nodeB0]*degree[nodeB1])
						+ (degree[nodeA0]*degree[nodeB1]+degree[nodeB0]*degree[nodeA1])
						) * (4.0/M) / DCC_divider;
				// 正しい設定時の処理
				if(positiveCorrelation ? DCC_difference > 0 : DCC_difference < 0) {
					degreeCorrelationCoefficient += DCC_difference;
					list[edgeA][1] = nodeB1;
					list[edgeB][1] = nodeA1;

					//TODO アルゴリズム最適化
					setNeightbor();

				}else {
					swapValidity = false;
				}
			}
		}
		return degreeCorrelationCoefficient;
	}


	/** 頂点の媒介中心性を計算しプロットする(Brandesらの方法)<br>
	* (注)<br>
	* 無向グラフのみ実行可能 <br>
	* さらにsetNode()またはsetNode(false)を使う必要がある<br>
	*
	* */
	public void betweenCentrality(){
		// 変数定義
		Node currentNode;
		ArrayList<Node> stack = new ArrayList<Node>();
		ArrayList<Node> queue = new ArrayList<Node>();
		int[] distance = new int[N];
		double[] sigma = new double[N];
		double[] delta = new double[N];
		int v,w,x,y;
		 // P:pの集合(lenght=N)
		 //└p[i]:頂点iのリスト(要素:Node)
		 // └Node
		 //
		 //↑この構造を作る
		 ArrayList<ArrayList<Node>> P = new ArrayList<ArrayList<Node>>();
		 for(int n=0;n<N;n++){
			 P.add(new ArrayList<Node>());
		 }

		for(int s=0;s<N;s++){
			// 初期化
			stack.clear(); queue.clear();
			for(int i=0;i<N;i++){
				distance[i] = -1;
				sigma[i] = 0;
				P.get(i).clear();
			}

			// 頂点sに対する処理
			distance[s] = 0;
			sigma[s] = 1;
			queue.add(nodeList.get(s));

			// 主となる処理
			while(!queue.isEmpty()){
				currentNode = queue.get(0);
				v = currentNode.index;
				stack.add( currentNode );
				queue.remove(0);
				// 現頂点の隣接頂点についてループ
				for(int neightbor=0 ; neightbor<currentNode.list.size() ; neightbor++){
					// 現ループの隣接頂点のindexをwとおく
					w = currentNode.list.get(neightbor).index;
					// wが未訪問のとき
					if(distance[w]<0){
						queue.add( nodeList.get(w) );
						distance[w] = distance[v]+1;
					}
					// sからwへの最短経路にvが含まれるとき
					// (⇔distance[w] = distance[v]+1のとき)
					if(distance[w] == distance[v]+1){
						sigma[w] += sigma[v];
						P.get(w).add(currentNode);
					}
				}
			}
			// 初期化
			for(int n=0;n<N;n++) delta[n]=0;
			// stackは頂点sからの距離が遠い順で返す
			while(!stack.isEmpty()){
				x = stack.get(stack.size()-1).index;
				stack.remove(stack.size()-1);
				for(int i=0 ; i<P.get(x).size() ; i++){
					y = P.get(x).get(i).index;
					delta[y] += (sigma[y]/sigma[x])*(1+delta[x]);
				}
				if(x!=s){
					nodeList.get(x).betweenCentrality += delta[x];
				}
			}
		}
	}

	/**
	 * 重み付きネットワークにおいて頂点媒介中心性を計算する(Brandesらの方法)<br>
	 * 無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●weight[]定義済み<br>
	 * ●doubleCount=false<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void nodeBetweenness_for_WeightedNet(){
		Node currentNode;
		ArrayList<Node> stack = new ArrayList<Node>();
		ArrayList<Node> queue = new ArrayList<Node>();
		double[] distance = new double[N];
		double[] sigma = new double[N];
		double[] delta = new double[N];
		int v, w,vwEdge, x, y;
		// P:pの集合(lenght=N)
		// └p[i]:頂点iのリスト(要素:Node)
		// └Node
		//
		// ↑この構造を作る
		ArrayList<ArrayList<Node>> P = new ArrayList<ArrayList<Node>>();
		for (int n = 0; n < N; n++) {
			P.add(new ArrayList<Node>());
		}

		for (int s = 0; s < N; s++) {
			// 初期化
			stack.clear();
			queue.clear();
			for (int i = 0; i < N; i++) {
				distance[i] = -1;
				sigma[i] = 0;
				P.get(i).clear();
			}

			// 頂点sに対する処理
			distance[s] = 0;
			sigma[s] = 1;
			queue.add(nodeList.get(s));

			// 主となる処理
			while (!queue.isEmpty()) {
				currentNode = queue.get(0);
				v = currentNode.index;
				stack.add(currentNode);
				queue.remove(0);
				// 現頂点の隣接頂点についてループ
				for (int neightbor = 0; neightbor < currentNode.list.size(); neightbor++) {
					// 現ループの隣接頂点のindexをwとおく
					w = currentNode.list.get(neightbor).index;
					// 【追加部分】リンク(v,w)を検索
					vwEdge = searchEdge(v,w);
					// wが未訪問のとき
					if (distance[w] < 0) {
						queue.add(nodeList.get(w));
						distance[w] = distance[v] + 1.0/weight[vwEdge];
					}
					// sからwへの最短経路にvが含まれるとき
					if (distance[w] == distance[v] + 1.0/weight[vwEdge]) {
						sigma[w] += sigma[v];
						P.get(w).add(currentNode);
					}
				}
			}
			// 初期化
			for (int n = 0; n < N; n++)
				delta[n] = 0;
			// stackは頂点sからの距離が遠い順で返す
			while (!stack.isEmpty()) {
				x = stack.get(stack.size() - 1).index;
				stack.remove(stack.size() - 1);
				for (int i = 0; i < P.get(x).size(); i++) {
					y = P.get(x).get(i).index;
					delta[y] += (sigma[y] / sigma[x]) * (1 + delta[x]);
				}
				if (x != s) {
					nodeList.get(x).betweenCentrality += delta[x];
				}
			}
		}
	}

	/**
	 * 辺の媒介中心性を計算しプロットする(Brandesらの方法)<br>
	 * 無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●weight[]定義済み<br>
	 * ●doubleCount=false<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void EdgeBetweenness(){
		if(weight==null || weight.length<=0) {
			System.out.println("weightが正しく定義されていません。プログラムを終了します。");
			System.exit(1);
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
		int[] Pred = new int[2*M];
		int[] PredIndex = new int[2*M];
		int[] PredCursor = new int[N];

		boolean[] contentQueue = new boolean[N];

		int v,w,minIndex;
		double c;
		double[] node_bc = new double[N];
		for(int i=0;i<N;i++) node_bc[i]=0;
		Double minDis;


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
				// 【修正箇所】
				// queueからdist[v]が最小となるものを取り出す
				minDis = Double.MAX_VALUE;
				v = -1;
				minIndex = -1;
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
				for(int neighbor=0 ; neighbor<nodeList.get(v).list.size() ; neighbor++){
					final int currentCursor = vAddress + neighbor;
					w = neightborList[currentCursor];
					// path discovery
					int vwEdge = neightborIndexList[currentCursor];
					if(dist[w] > dist[v] + 1.0/weight[vwEdge]){
						dist[w] = dist[v] + 1.0/weight[vwEdge];

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
					if(dist[w] == dist[v]+1.0/weight[vwEdge]){
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
				w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				final int PredSize = PredCursor[w]-addressList[w];
				for(int i=0 ; i<PredSize ; i++){
					v = Pred[addressList[w]+i];

					int vwEdge = PredIndex[addressList[w]+i];
					c = (sigma[v]/sigma[w]) * (1.0+delta[w]);
					edgeList.get(vwEdge).betweenCentrality = edgeList.get(vwEdge).betweenCentrality + c;
					delta[v] = delta[v] + c;
				}

				if(w!=s){
					node_bc[w] = node_bc[w] + delta[w];
				}
			}
		}


	}

	/**
	 * 辺のsalienceを計算しEdgeクラスのlinkSalienceへ受け渡す(Brandesを書き換えGradyが作った方法)<br>
	 * 無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●weight[]定義済み<br>
	 * ●neighborList[]定義済み<br>
	 */
	public void LinkSalience(){
		if(weight==null || weight.length<=0) {
			System.out.println("weightが正しく定義されていません。プログラムを終了します。");
			System.exit(1);
		}
		if(neightborList==null || neightborList.length<=0) {
			System.out.println("neighborListが正しく定義されていません。プログラムを終了します。");
			System.exit(1);
		}

		// salience初期化
		linkSalience = new int[M];

		ArrayList<Integer> queue = new ArrayList<Integer>();
		ArrayList<Integer> stack = new ArrayList<Integer>();

		//distance from source
		double[] dist = new double[N];

		//list of predecessors on shortest paths from source
		int[] Pred = new int[2*M];
		int[] PredIndex = new int[2*M];
		int[] PredCursor = new int[N];

		boolean[] contentQueue = new boolean[N];

		int v,w,minIndex;
		Double minDis;

		double[] inv_weight = new double[M];
		for(int i=0;i<M;i++) inv_weight[i] = 1.0/weight[i];
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
//				System.out.println(queue.size());
				// queueからdist[v]が最小となるものを取り出す
				minDis = Double.MAX_VALUE;
				v = -1;
				minIndex = -1;
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
					w = neightborList[currentCursor];
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
					if(MyTool.compareDouble(dist[w], dist[v]+1.0/weight[vwEdge]) == 0){
						Pred[PredCursor[w]] = v;
						PredIndex[PredCursor[w]] = vwEdge;
						PredCursor[w]++;
					}
				}
			}

			//// accumulation
			while(!stack.isEmpty()){
				w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				final int PredSize = PredCursor[w]-addressList[w];
				for(int i=0 ; i<PredSize ; i++){
					int vwEdge = PredIndex[addressList[w]+i];
					linkSalience[vwEdge]++;
				}
			}
		}

		// 旧プログラム用の配慮
		if(edgeList!=null){
			if(edgeList.size()>0){
				for(int i=0;i<M;i++){
					edgeList.get(i).linkSalience = linkSalience[i];
				}
			}
		}

	}

	public void LinkSalience_legacy(){
		ArrayList<Integer> queue = new ArrayList<Integer>();
		ArrayList<Integer> stack = new ArrayList<Integer>();

		//distance from source
		double[] dist = new double[N];

		//list of predecessors on shortest paths from source
		ArrayList<ArrayList<Integer>> Pred = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<N;i++) Pred.add(new ArrayList<Integer>());

		ArrayList<Edge> edge = new ArrayList<Edge>();
		for(int i=0;i<M;i++)edge.add(new Edge());

		int v,w,m,minIndex,vwEdge;
		Double minDis;


		for(int s=0 ; s<N ; s++){
			//// single-source shortest-paths problem
			// initialization
			for(int i=0 ; i<N ; i++){
				Pred.get(i).clear();
				dist[i] = Double.MAX_VALUE;
			}
			dist[s] = 0;
			queue.add(s);

			while(!queue.isEmpty()){
				// queueからdist[v]が最小となるものを取り出す
				minDis = Double.MAX_VALUE - 1.0;
				v = -1;
				minIndex = -1;
				for(int i=0;i<queue.size();i++){
					if(minDis > dist[queue.get(i)]){
						minDis = dist[queue.get(i)];
						v = queue.get(i);
						minIndex = i;
					}
				}
				queue.remove(minIndex);
				stack.add(v);

				for(int neighbor=0 ; neighbor<nodeList.get(v).list.size() ; neighbor++){
					w = nodeList.get(v).list.get(neighbor).index;
					// path discovery
					vwEdge = searchEdge(v,w);
//					vwEdge = nodeList.get(v).eList.get(neighbor).index;
					if(dist[w] > dist[v] + 1.0/weight[vwEdge]){
						dist[w] = dist[v] + 1.0/weight[vwEdge];

						// insert/update w
						queue.add(w);
						for(int i=0;i<queue.size()-1;i++){
							if(queue.get(i) == w){
								queue.remove(i);
								break;
							}
						}

						Pred.get(w).clear();
					}
					//path counting
					if(dist[w] == dist[v]+1.0/weight[vwEdge]){
//					if(MyTool.compareDouble(dist[w], dist[v]+1.0/weight[vwEdge]) == 0){
						Pred.get(w).add(v);
					}
				}
			}

			int[] node = new int[2];
			int[] listNode = new int[2];
			//// accumulation
			while(!stack.isEmpty()){
				w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				for(int i=0 ; i<Pred.get(w).size() ; i++){
					v = Pred.get(w).get(i);
					node[0] = Math.min(v,w);
					node[1] = Math.max(v,w);
					for(m=0;m<M;m++){
						listNode[0] = Math.min(list[m][0],list[m][1]);
						listNode[1] = Math.max(list[m][0],list[m][1]);
						if(listNode[0]==node[0]&&listNode[1]==node[1])break;
					}
					edge.get(m).setNode(node[0], node[1]);
//					edge.get(m).linkSalience = edge.get(m).linkSalience+1;
					edgeList.get(m).linkSalience += 1;
				}
			}
		}
	}

	/**
	 * 辺のsalienceを計算しEdgeクラスのlinkSalienceへ受け渡す(Brandesを書き換えGradyが作った方法)<br>
	 * 無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●weight[]定義済み<br>
	 * ●doubleCount=false<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void LinkSalience_legacy2(){
		ArrayList<Integer> queue = new ArrayList<Integer>();
		ArrayList<Integer> stack = new ArrayList<Integer>();

		//distance from source
		double[] dist = new double[N];

		//list of predecessors on shortest paths from source
		ArrayList<ArrayList<Integer>> Pred = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<N;i++) Pred.add(new ArrayList<Integer>());

//		ArrayList<Edge> edge = new ArrayList<Edge>();
//		for(int i=0;i<M;i++)edge.add(new Edge());

		int v,w,m,minIndex,vwEdge;
		Double minDis;


		for(int s=0 ; s<N ; s++){
			//// single-source shortest-paths problem
			// initialization
			for(int i=0 ; i<N ; i++){
				Pred.get(i).clear();
				dist[i] = Double.MAX_VALUE;
			}
			dist[s] = 0;
			queue.add(s);

			while(!queue.isEmpty()){
				// queueからdist[v]が最小となるものを取り出す
				minDis = Double.MAX_VALUE - 1.0;
				v = -1;
				minIndex = -1;
				for(int i=0;i<queue.size();i++){
					if(minDis > dist[queue.get(i)]){
						minDis = dist[queue.get(i)];
						v = queue.get(i);
						minIndex = i;
					}
				}
				queue.remove(minIndex);
				stack.add(v);

				for(int neighbor=0 ; neighbor<nodeList.get(v).list.size() ; neighbor++){
					w = nodeList.get(v).list.get(neighbor).index;
					// path discovery
//					vwEdge = searchEdge(v,w);
					vwEdge = nodeList.get(v).eList.get(neighbor).index;
					if(dist[w] > dist[v] + 1.0/weight[vwEdge]){
						dist[w] = dist[v] + 1.0/weight[vwEdge];

						// insert/update w
						queue.add(w);
						for(int i=0;i<queue.size()-1;i++){
							if(queue.get(i) == w){
								queue.remove(i);
								break;
							}
						}

						Pred.get(w).clear();
					}
					//path counting
//					if(dist[w] == dist[v]+1.0/weight[vwEdge]){
					if(MyTool.compareDouble(dist[w], dist[v]+1.0/weight[vwEdge]) == 0){
						Pred.get(w).add(v);
					}
				}
			}

			int[] node = new int[2];
			int[] listNode = new int[2];
			//// accumulation
			while(!stack.isEmpty()){
				w = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);

				for(int i=0 ; i<Pred.get(w).size() ; i++){
					v = Pred.get(w).get(i);
					node[0] = Math.min(v,w);
					node[1] = Math.max(v,w);
					for(m=0;m<M;m++){
						listNode[0] = Math.min(list[m][0],list[m][1]);
						listNode[1] = Math.max(list[m][0],list[m][1]);
						if(listNode[0]==node[0]&&listNode[1]==node[1])break;
					}
//					edge.get(m).setNode(node[0], node[1]);
//					edge.get(m).linkSalience = edge.get(m).linkSalience+1;
					edgeList.get(m).linkSalience += 1;
				}
			}
		}
	}

	/**
	 * SimpleRandomWalkを実行する。<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 *
	 */
	public int SimpleRandomWalk(int step, double deltaW, int seed, double teleportP, boolean disturb) {
		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex = seed%N;

		int selectedEdge,nextNodeIndex;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) newWeight[i]=1.0;
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			if(currentNode.eList.size()>=1) {
				// ここが各ランダムウォークで変化する内容(辺の選択方法)
				selectedEdge = (int)(currentNode.eList.size()*Math.random());

				// 加重
				newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;

				// nextNodeIndexの決定
				if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
				}else{
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
				}
				currentNodeIndex = nextNodeIndex;
			}else {
				// 次数0なら確定ワープ
				t--;
				currentNodeIndex = (int)(N*Math.random());
				continue;
			}



			// テレポート判定
			if(Math.random() < teleportP){
				currentNodeIndex = (int)(N*Math.random());
			}

		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

		if(disturb) disturb();

		return currentNodeIndex;
	}

	public int SimpleRandomWalk(int step, double deltaW, double teleportP, boolean disturb) {
		int currentNodeIndex = (int)(N * Math.random());
		return SimpleRandomWalk(step, deltaW, currentNodeIndex, teleportP, disturb);
	}

	/**
	 * BiasedRandomWalkの大本となるメソッド
	 */
	private Object[] BiasedRandomWalk_Core(int step, double deltaW, double alpha, int startNode, double teleportP, boolean disturb, boolean checkVisitedNodes, boolean continueWeight) {
		// 戻り値用リスト
		// 0:終了時のwalkerの居る頂点のindex
		// 1:各ステップごとの訪問済み頂点数の数
		Object[] returnList = new Object[3];

		// 作業変数定義
		int currentNodeIndex = startNode%N;
		boolean[] temp_visited = null;
		int[] visitedNodes = null;
		int currentVisitedNodes = 0;
		if(checkVisitedNodes){
			temp_visited = new boolean[N];
			visitedNodes = new int[step];
			for(int i=0;i<N;i++) temp_visited[i]=false;
		}

		if(!continueWeight) weight = new double[M];
		if(visitedNodes_onRW==null) {
			visitedNodes_onRW = new boolean[N];
			for(int i=0;i<N;i++)visitedNodes_onRW[i]=false;
		}
		if(visitedEdge_onRW==null) {
			visitedEdge_onRW = new boolean[M];
			for(int i=0;i<N;i++)visitedEdge_onRW[i]=false;
		}
		visitedNodes_onRW[currentNodeIndex] = true;

		int selectedEdge,nextNodeIndex;
		double sumDegree;
		double r,threshold;
		double[] newWeight = new double[M];
		if(continueWeight) {
			for(int i=0;i<M;i++) newWeight[i]=weight[i];
		}else {
			for(int i=0;i<M;i++) newWeight[i]=1.0;
		}
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			if(checkVisitedNodes) {
				// 訪問済み頂点数をチェック
				if(!temp_visited[currentNodeIndex]){
					temp_visited[currentNodeIndex] = true;
					currentVisitedNodes++;
				}
				visitedNodes[t] = currentVisitedNodes;
			}

			if(currentNode.eList.size()>=1) {
				// ここが各ランダムウォークで変化する内容(辺の選択方法)
				sumDegree = 0.0;
				for(int i=0;i<currentNode.list.size();i++) sumDegree+=Math.pow(degree[currentNode.list.get(i).index], alpha);
				r = sumDegree*Math.random();
				selectedEdge = 0;
				threshold = Math.pow(degree[currentNode.list.get(0).index], alpha);
				while(r > threshold){
					selectedEdge++;
					threshold += Math.pow(degree[currentNode.list.get(selectedEdge).index], alpha);
				}

				// 加重
				int throughEdgeIndex = currentNode.eList.get(selectedEdge).index;
				newWeight[throughEdgeIndex] += deltaW;

				// nextNodeIndexの決定
				if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
				}else{
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
				}
				currentNodeIndex = nextNodeIndex;

				// 訪問済み更新(continue用)
				visitedNodes_onRW[nextNodeIndex] = true;
				visitedEdge_onRW[throughEdgeIndex] = true;
			}else {
				// 次数0なら確定ワープ
				t--;
				currentNodeIndex = (int)(N*Math.random());
				continue;
			}

			// テレポート判定
			if(Math.random() < teleportP){
				currentNodeIndex = (int)(N*Math.random());
			}

		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

		if(disturb) disturb();

		// returnList[]へ格納
		returnList[0] = currentNodeIndex;
		returnList[1] = visitedNodes;

		return returnList;
	}

	/**
	 * BiasedRandomWalkを実行する。<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 *
	 */
	public int BiasedRandomWalk(int step, double deltaW, double alpha, int startNode, double teleportP, boolean disturb) {
		return (int)(BiasedRandomWalk_Core(step, deltaW, alpha, startNode, teleportP, disturb, false, false)[0]);
	}

	public int BiasedRandomWalk(int step, double deltaW, double alpha, double teleportP, boolean disturb) {
		int currentNodeIndex = (int)(N * Math.random());
		return BiasedRandomWalk(step, deltaW, alpha, currentNodeIndex, teleportP, disturb);
	}

	public int BiasedRandomWalk(int step, double deltaW, double teleportP, boolean disturb) {
		int currentNodeIndex = (int)(N * Math.random());
		return BiasedRandomWalk(step, deltaW, 1, currentNodeIndex, teleportP, disturb);
	}

	/**
	 * BiasedRandomWalkを実行する。<br>
	 * 加えて、各ステップごとの訪問済み頂点数を返す。
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 *
	 */
	public int[] BiasedRandomWalk_checkVisitedNodes(int step, double deltaW, double alpha, int seed, double teleportP, boolean disturb) {
		return (int[])(BiasedRandomWalk_Core(step, deltaW, alpha, seed, teleportP, disturb, true, false)[1]);
	}

	/**
	 * BiasedRandomWalkを実行する。<br>
	 * 加えて、各ステップごとの訪問済み頂点数を返す。<br>
	 * <b>開始時の重みは、開始以前の重みを利用し、加算していく。</b><br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 *
	 */
	public int BiasedRandomWalk_continueWeight(int step, double deltaW, double alpha, int seed, double teleportP, boolean disturb) {
		return (int)(BiasedRandomWalk_Core(step, deltaW, alpha, seed, teleportP, disturb, true, true)[0]);
	}

	/**
	 * 重みwをw∝(kk)^alphaになるように割り振る。
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void SetWeight_to_Alpha(double alpha){
		weight = new double[M];
		for(int i=0;i<M;i++) {
			int[] nodeIndex = new int[2];
//			nodeIndex[0]=edgeList.get(i).node[0];
//			nodeIndex[1]=edgeList.get(i).node[1];
			nodeIndex[0]=list[i][0];
			nodeIndex[1]=list[i][1];
			int degreeProduct = degree[nodeIndex[0]]*degree[nodeIndex[1]];
			double powered_degreeProduct = Math.pow(degreeProduct, alpha);
//			weight[edgeList.get(i).index] = powered_degreeProduct;
			weight[i] = powered_degreeProduct;
		}
	}

	/**
	 * biasedRW(alpha)で振られる重みの理論値に振り分ける。
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void SetWeight_to_Alpha(double alpha, int steps) {
		SetWeight_to_Alpha_Faithfully(alpha, steps);
	}

	/**
	 * biasedRW(alpha)で振られる重みの理論値に振り分ける。
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 */
	public void SetWeight_to_Alpha_Faithfully(double alpha, int steps){
		double constFactor;
		double sum_kk = 0.0;
		weight = new double[edgeList.size()];
		for(int i=0;i<edgeList.size();i++) {
			int[] nodeIndex = new int[2];
			nodeIndex[0]=edgeList.get(i).node[0];
			nodeIndex[1]=edgeList.get(i).node[1];
			int degreeProduct = degree[nodeIndex[0]]*degree[nodeIndex[1]];
			double powered_degreeProduct = Math.pow(degreeProduct, alpha);
			weight[edgeList.get(i).index] = powered_degreeProduct;
			sum_kk += powered_degreeProduct;
		}
		constFactor = steps/sum_kk;
		for(int i=0;i<edgeList.size();i++) {
			weight[edgeList.get(i).index] *= constFactor;
			weight[edgeList.get(i).index] += 1;
		}
	}

	/**
	 *  ReinforcedRandomWalkを実行する。<br>
	 *  seed値を変えることで開始地点を変えられる。<br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, int seed){
		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex;
		currentNodeIndex=seed;
		while(degree[currentNodeIndex]==0){
			currentNodeIndex = (currentNodeIndex+1)%N ;
		}
		int selectedEdge,nextNodeIndex;
		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}
		double r,threshold;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) newWeight[i]=1.0;
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedEdge = 0;
			threshold = newWeight[currentNode.eList.get(0).index];
			while(r > threshold){
				selectedEdge++;
				threshold += newWeight[currentNode.eList.get(selectedEdge).index];
			}

			//degag
//			System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

			// 加重
			newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;
			sumW[currentNode.eList.get(selectedEdge).node[0]] += deltaW;
			sumW[currentNode.eList.get(selectedEdge).node[1]] += deltaW;
			// nextNodeIndexの決定
			if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
				nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
			}else{
				nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
			}
			currentNodeIndex = nextNodeIndex;
		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

//		System.out.println();
		return currentNodeIndex;
	}

	/**
	 *  ReinforcedRandomWalkを実行する。<br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 */
	public int ReinforcedRandomWalk(int step, double deltaW){
			// 作業変数定義
			int currentNodeIndex;
			do{
				currentNodeIndex = (int)(N * Math.random());
			}while(degree[currentNodeIndex]==0);
			return ReinforcedRandomWalk(step, deltaW, currentNodeIndex);
	}

	/**
	 *  ReinforcedRandomWalkを<b>テレポートありで</b>実行する。<br>
	 *  <b>テレポート確率はteleportP</b><br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 * @param teleportP
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, int seed, double teleportP){
		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex;
		currentNodeIndex=seed;
		while(degree[currentNodeIndex]==0){
			currentNodeIndex = (currentNodeIndex+1)%N ;
		}
		int selectedEdge,nextNodeIndex;
		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}
		double r,threshold;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) newWeight[i]=1.0;
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedEdge = 0;
			threshold = newWeight[currentNode.eList.get(0).index];
			while(r > threshold){
				selectedEdge++;
				threshold += newWeight[currentNode.eList.get(selectedEdge).index];
			}

			//degag
//			System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

			// 加重
			newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;
			sumW[currentNode.eList.get(selectedEdge).node[0]] += deltaW;
			sumW[currentNode.eList.get(selectedEdge).node[1]] += deltaW;
			// nextNodeIndexの決定
			if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
				nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
			}else{
				nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
			}
			currentNodeIndex = nextNodeIndex;

			// テレポート判定
			if(Math.random() < teleportP){
				currentNodeIndex = (int)(N*Math.random());
			}

		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

//		System.out.println();
		return currentNodeIndex;
	}

	/**
	 *  ReinforcedRandomWalkを<b>テレポートありで</b>実行する。<br>
	 *  <b>テレポート確率はteleportP</b><br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param teleportP
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, double teleportP){
		// 作業変数定義
		int currentNodeIndex;
		do{
			currentNodeIndex = (int)(N * Math.random());
		}while(degree[currentNodeIndex]==0);
		return ReinforcedRandomWalk(step, deltaW, currentNodeIndex, teleportP);
}

	/**
	 *  ReinforcedRandomWalkを<b>テレポートありで</b>実行する。<br>
	 *  <b>テレポート確率はteleportP</b><br>
	 *  disturbをtrueにすることで重みにブレを生じさせることができる。<br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 * @param teleportP
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, int seed, double teleportP, boolean disturb){
		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex;
		currentNodeIndex=seed;
		while(degree[currentNodeIndex]==0){
			currentNodeIndex = (currentNodeIndex+1)%N ;
		}
		int selectedEdge,nextNodeIndex;
		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}
		double r,threshold;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) newWeight[i]=1.0;
//		for(int i=0;i<M;i++) newWeight[i]=Math.pow(10, -6);
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedEdge = 0;
			if(currentNode.eList.size()>=1) {
				threshold = newWeight[currentNode.eList.get(0).index];
				while(r > threshold){
					selectedEdge++;
					threshold += newWeight[currentNode.eList.get(selectedEdge).index];
				}

				//degag
//				System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

				// 加重
				newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;
				sumW[currentNode.eList.get(selectedEdge).node[0]] += deltaW;
				sumW[currentNode.eList.get(selectedEdge).node[1]] += deltaW;
				// nextNodeIndexの決定
				if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
				}else{
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
				}
				currentNodeIndex = nextNodeIndex;
			}else {
				// 次数0なら確定ワープ
				t--;
				currentNodeIndex = (int)(N*Math.random());
				continue;
			}


			// テレポート判定
			if(Math.random() < teleportP){
				currentNodeIndex = (int)(N*Math.random());
			}

		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

		if(disturb)disturb();

//		System.out.println();
		return currentNodeIndex;
	}

	/**
	 *  ReinforcedRandomWalkを<b>テレポートありで</b>実行する。<br>
	 *  <b>テレポート確率はteleportP</b><br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param teleportP
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, double teleportP, boolean disturb){
		// 作業変数定義
		int currentNodeIndex;
		do{
			currentNodeIndex = (int)(N * Math.random());
		}while(degree[currentNodeIndex]==0);
		return ReinforcedRandomWalk(step, deltaW, currentNodeIndex, teleportP, disturb);
	}

	/**
	 *  ReinforcedRandomWalkを<b>テレポートありで</b>実行する。<br>
	 *  <b>テレポート確率はteleportP</b><br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param teleportP
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, int seed, boolean disturb){
		double teleportP = 0.0;
		return ReinforcedRandomWalk(step, deltaW, seed, teleportP, disturb);
	}

	/**
	 *  ReinforcedRandomWalkを実行する。<br>
	 *  戻り値には、RWで最後にいた頂点番号が返される。<br>
	 *  無向グラフのみ実行可能<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 */
	public int ReinforcedRandomWalk(int step, double deltaW, boolean disturb){
			// 作業変数定義
			int currentNodeIndex;
			do{
				currentNodeIndex = (int)(N * Math.random());
			}while(degree[currentNodeIndex]==0);
			return ReinforcedRandomWalk(step, deltaW, currentNodeIndex, disturb);
	}



	/**
	 *  頂点についてReinforcedRandomWalkを実行する。<br>
	 *  seed値を変えることで開始地点を変えられる。<br>
	 *  無向グラフのみ実行可能<br>
	 *  このメソッドはvWeightを入力し、返す<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 */
	public double[] VertexReinforcedRandomWalk(int step, double deltaW, int seed){
		double[] vWeight = new double[N];

		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex;
		currentNodeIndex=seed;
		while(degree[currentNodeIndex]==0){
			currentNodeIndex = (currentNodeIndex+1)%N ;
		}
		int selectedNode,nextNodeIndex;
		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}
		double r,threshold;
		for(int i=0;i<N;i++) vWeight[i]=1.0;
		double[] newEdgeWeight = new double[M];
		for(int i=0;i<M;i++) newEdgeWeight[i]=1.0;
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedNode = 0;
			threshold = vWeight[currentNode.list.get(0).index];
			while(r > threshold){
				selectedNode++;
				threshold += vWeight[currentNode.list.get(selectedNode).index];
			}

			//degag
//			System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

			// 加重
			vWeight[currentNode.list.get(selectedNode).index] += deltaW;
			for(int i=0;i<currentNode.list.get(selectedNode).list.size();i++){
				sumW[currentNode.list.get(selectedNode).list.get(i).index] += deltaW;
			}
			newEdgeWeight[currentNode.eList.get(selectedNode).index] += deltaW;
			// nextNodeIndexの決定
			if(currentNode.eList.get(selectedNode).node[0]!=currentNodeIndex){
				nextNodeIndex = currentNode.eList.get(selectedNode).node[0];
			}else{
				nextNodeIndex = currentNode.eList.get(selectedNode).node[1];
			}
			currentNodeIndex = nextNodeIndex;
		}

		for(int i=0;i<M;i++){
			weight[i] = newEdgeWeight[i];
		}

//		System.out.println();
		return vWeight;
	}

	/**
	 *  頂点についてReinforcedRandomWalkを実行する。<br>
	 *  無向グラフのみ実行可能<br>
	 *  このメソッドはvWeightを入力し、返す<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 */
	public double[] VertexReinforcedRandomWalk(int step, double deltaW){
			// 作業変数定義
			int currentNodeIndex;
			do{
				currentNodeIndex = (int)(N * Math.random());
			}while(degree[currentNodeIndex]==0);
			return VertexReinforcedRandomWalk(step, deltaW, currentNodeIndex);
	}

	/**
	 *  頂点についてReinforcedRandomWalkを実行する。<br>
	 *  teleportPにテレポート確率を入力することでテレポートを導入できる。<br>
	 *  seed値を変えることで開始地点を変えられる。<br>
	 *  無向グラフのみ実行可能<br>
	 *  このメソッドはvWeightを入力し、返す<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 * @param teleportP
	 */
	public double[] VertexReinforcedRandomWalk(int step, double deltaW, int seed, double teleportP){
		double[] vWeight = new double[N];

		weight = new double[M];

		// 作業変数定義
		int currentNodeIndex;
		currentNodeIndex=seed;
		while(degree[currentNodeIndex]==0){
			currentNodeIndex = (currentNodeIndex+1)%N ;
		}
		int selectedNode,nextNodeIndex;
		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i]= (double)degree[i];
		}
		double r,threshold;
		for(int i=0;i<N;i++) vWeight[i]=1.0;
		double[] newEdgeWeight = new double[M];
		for(int i=0;i<M;i++) newEdgeWeight[i]=1.0;
		for(int t=0;t<step;t++){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedNode = 0;
			threshold = vWeight[currentNode.list.get(0).index];
			while(r > threshold){
				selectedNode++;
				threshold += vWeight[currentNode.list.get(selectedNode).index];
			}

			//degag
//			System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

			// 加重
			vWeight[currentNode.list.get(selectedNode).index] += deltaW;
			for(int i=0;i<currentNode.list.get(selectedNode).list.size();i++){
				sumW[currentNode.list.get(selectedNode).list.get(i).index] += deltaW;
			}
			newEdgeWeight[currentNode.eList.get(selectedNode).index] += deltaW;
			// nextNodeIndexの決定
			if(currentNode.eList.get(selectedNode).node[0]!=currentNodeIndex){
				nextNodeIndex = currentNode.eList.get(selectedNode).node[0];
			}else{
				nextNodeIndex = currentNode.eList.get(selectedNode).node[1];
			}
			currentNodeIndex = nextNodeIndex;

			// テレポート判定
			if(Math.random() < teleportP){
				currentNodeIndex = (int)(N*Math.random());
			}

		}

		for(int i=0;i<M;i++){
			weight[i] = newEdgeWeight[i];
		}

//		System.out.println();
		return vWeight;
	}

	/**
	 * 最小生成木を計算する。<br>
	 * isMSTが真なら、距離=重み<br>
	 * 偽なら、距離=(重みの逆数)と定義する。<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param isMST
	 */
	public ArrayList<Integer> MinimumSpanningTree(boolean isMST) {
		// 初期頂点は0とする
		int firstNode = 0;

		// 距離の定義
		double[] d = new double[weight.length];
		for(int i=0;i<d.length;i++) {
			if(isMST) {
				d[i] = weight[i];
			}else {
				d[i] = 1.0/weight[i];
			}
		}

		// まだペアが決まってない辺のリスト
		ArrayList<Integer> stubList = new ArrayList<Integer>();
		// stubListの各辺がどの頂点の所有物かのリスト
		ArrayList<Integer> sourceNodeList = new ArrayList<Integer>();


		// 初期頂点をリストへ追加
		MST_Nodes.add(firstNode);
		int currentMST_N = 1;

		// 初期頂点の所有する辺をリストへ追加(ついでに最小の重みの辺情報を取得)
		int nextTargetEdge=-1;
		int nextTargetNode=-1;
		double shortestPathLength = Double.MAX_VALUE;
		for(int i=0;i<degree[firstNode];i++) {
			int currentEdgeIndex = nodeList.get(firstNode).list.get(i).index;
			// 辺をリストに追加
			stubList.add(nodeList.get(firstNode).eList.get(i).index);
			sourceNodeList.add(firstNode);
			// 最小辺情報を取得
			if(shortestPathLength > d[currentEdgeIndex]) {
				shortestPathLength = d[currentEdgeIndex];
				nextTargetEdge = currentEdgeIndex;
			}
		}
		if(edgeList.get(nextTargetEdge).node[0]==firstNode) {
			nextTargetNode = edgeList.get(nextTargetEdge).node[1];
		}else {
			nextTargetNode = edgeList.get(nextTargetEdge).node[0];
		}


		// プリム法を実行
		while(currentMST_N < N) {
			//// ターゲットの頂点と辺をリストに追加
			MST_Nodes.add(nextTargetNode);
			MST_Edges.add(nextTargetEdge);
			currentMST_N++;

			//// 辺情報更新
			// 辺削除
			ArrayList<Integer> removeEdgeIndexList = new ArrayList<Integer>();
			for(int m=0;m<stubList.size();m++) {
				Edge currentEdge = edgeList.get(stubList.get(m));
				if(currentEdge.node[0]==nextTargetNode || currentEdge.node[1]==nextTargetNode) {
					removeEdgeIndexList.add(m);
				}
			}
			for(int i=removeEdgeIndexList.size()-1;i>=0;i--) {
				stubList.remove			( (int)removeEdgeIndexList.get(i) );
				sourceNodeList.remove	( (int)removeEdgeIndexList.get(i) );
			}
			// 辺追加
			boolean add;
			for(int m=0;m<nodeList.get(nextTargetNode).eList.size();m++) {
				Edge currentEdge = nodeList.get(nextTargetNode).eList.get(m);
				// MST_Nodesへと続く辺は追加しない
				add = true;
				for(int n=0;n<MST_Nodes.size()-1;n++) {
					if(currentEdge.node[0]==MST_Nodes.get(n) || currentEdge.node[1]==MST_Nodes.get(n)) {
						add = false;
						break;
					}
				}
				if(add) {
					stubList.add(currentEdge.index);
					sourceNodeList.add(nextTargetNode);
				}
			}

			//// 次のターゲットを決める
			shortestPathLength = Double.MAX_VALUE;
			int sourceNode = -1;
			for(int m=0;m<stubList.size();m++) {
				int currentEdgeIndex = edgeList.get(stubList.get(m)).index;
				// 最小辺情報を取得
				if(shortestPathLength > d[currentEdgeIndex]) {
					shortestPathLength = d[currentEdgeIndex];
					nextTargetEdge = currentEdgeIndex;
					sourceNode = sourceNodeList.get(m);
				}
			}
			if(edgeList.get(nextTargetEdge).node[0]==sourceNode) {
				nextTargetNode = edgeList.get(nextTargetEdge).node[1];
			}else {
				nextTargetNode = edgeList.get(nextTargetEdge).node[0];
			}
		}

		// 隣接リスト作成
		MST_list = new int[MST_Edges.size()][2];
		for(int i=0;i<MST_Edges.size();i++) {
			MST_list[i][0] = edgeList.get(MST_Edges.get(i)).node[0];
			MST_list[i][1] = edgeList.get(MST_Edges.get(i)).node[1];
		}

		return MST_Edges;
	}

	/**
	 *
	 *
	 */
	public ArrayList<Integer> ShortestPathTree(int rootNodeIndex) {
		// 出力用データ
		SPT_Edges = new ArrayList<Integer>();
		SPT_Edges.clear();
		SPT_list = new int[N-1][2];

		// 作業用変数
		ArrayList<Integer> queue = new ArrayList<>();
		boolean[] isVisited = new boolean[N];
		double[] dist = new double[N];
		int[] prevNode = new int[N];
		int[] prevEdge = new int[N]; //要素数Nは間違いではない
		for(int i=0;i<isVisited.length;i++) {
			isVisited[i]=false;
			dist[i] = Double.MAX_VALUE;
		}

		// rootNodeの処理
		queue.add(rootNodeIndex);
		dist[rootNodeIndex] = 0.0;
		isVisited[rootNodeIndex] = true;
		prevNode[rootNodeIndex] = -1;
		prevEdge[rootNodeIndex] = -1;

		while(!queue.isEmpty()) {
			// queue内でdist[i]が最小の頂点をpop
			int popIndex = -1;
			double currentShortestDist = Double.MAX_VALUE;
			for(int i=0;i<queue.size();i++) {
				if(dist[queue.get(i)] < currentShortestDist) {
					currentShortestDist = dist[queue.get(i)];
					popIndex = i;
				}
			}
			Node currentNode = nodeList.get(queue.get(popIndex));
			queue.remove(popIndex);

			// 距離の更新
			for(int i=0;i<currentNode.eList.size();i++) {
				Edge currentEdge = currentNode.eList.get(i);
				int targetNodeIndex = -123;
				if(currentEdge.node[0]==currentNode.index) {
					targetNodeIndex = currentEdge.node[1];
				}else if(currentEdge.node[1]==currentNode.index){
					targetNodeIndex = currentEdge.node[0];
				}

				double currentDist = dist[currentNode.index] + 1.0/weight[currentEdge.index];
				if(currentDist < dist[targetNodeIndex]) {
					dist[targetNodeIndex] = currentDist;
					prevNode[targetNodeIndex] = currentNode.index;
					prevEdge[targetNodeIndex] = currentEdge.index;
				}

				if(!isVisited[targetNodeIndex]) {
					isVisited[targetNodeIndex] = true;
					queue.add(targetNodeIndex);
				}
			}
		}

		// 計算したSPTのデータを集計
		int currentLine = 0;
		for(int i=0;i<N;i++) {
			if(prevEdge[i] >= 0) {
				SPT_Edges.add(prevEdge[i]);
				SPT_list[currentLine][0] = list[prevEdge[i]][0];
				SPT_list[currentLine][1] = list[prevEdge[i]][1];
				currentLine++;
			}
		}
		return SPT_Edges;
	}

	/**
	 * 辺の重みをシャッフルする
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
	 * 辺の重みをシャッフルする
	 */
	public void weightShuffle() {
		weightShuffle((int)(Math.random()*10000));
	}

	/**
	 * 重みに僅かなブレwidth分ブレさせる
	 */
	public void disturb(long seed) {
		double width = 2*Math.pow(10, -8);
		Random rnd = new Random(seed);
		for(int i=0 ; i<weight.length ; i++) {
			weight[i] = weight[i] * (1 + ((rnd.nextDouble()-0.5)*width));
		}
	}

	/**
	 * 重みに僅かなブレwidth分ブレさせる
	 */
	public void disturb() {
		disturb(System.currentTimeMillis());
	}



	/**
	 * 重みを1へ戻す
	 */
	public void turnUniform(){
		weight = new double[M];
		for(int i=0;i<M;i++) weight[i]=1.0;
	}

	/**
	 *  頂点についてReinforcedRandomWalkを実行する。<br>
	 *  teleportPにテレポート確率を入力することでテレポートを導入できる。<br>
	 *  seed値を変えることで開始地点を変えられる。<br>
	 *  無向グラフのみ実行可能<br>
	 *  このメソッドはvWeightを入力し、返す<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param teleportP
	 */
	public double[] VertexReinforcedRandomWalk(int step, double deltaW,double teleportP){
		// 作業変数定義
		int currentNodeIndex;
		do{
			currentNodeIndex = (int)(N * Math.random());
		}while(degree[currentNodeIndex]==0);
		return VertexReinforcedRandomWalk(step, deltaW, currentNodeIndex, teleportP);
	}

	/**
	 *  全頂点を巡回するRWを数回実行する。<br>
	 *  一度、巡回が終了したら、重みを加算する。<br>
	 *  RWは前回までの重みを参照して歩行する。<br>
	 *  startNodeを負の数にした場合は、ランダムにstartを決定する<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 * @param teleportP
	 */
	public int CircuitReinforcedRandomWalk(int tryN, double deltaW, int input_startNodeIndex, boolean multiCount, boolean disturb){
		weight = new double[M];
		for(int i=0;i<weight.length;i++){
			weight[i] = 1.0;
		}

		int totalStep = 0;

		int[] resultValueList;
		int subSpendingStep;

		int startNodeIndex;
		if(input_startNodeIndex<0){
			startNodeIndex = (int)(N*Math.random());
		}else{
			startNodeIndex = input_startNodeIndex%N;
		}
		for(int i=0;i<tryN;i++){
			resultValueList = SubCircuitReinforcedRandomWalk(startNodeIndex, deltaW, multiCount);
			System.out.println("cRW" + i + ":");
			subSpendingStep = resultValueList[0];

			totalStep += subSpendingStep;

			if(startNodeIndex<0) startNodeIndex = (int)(N*Math.random());
		}

		if(disturb) disturb();

		return totalStep;
	}

	private int[] SubCircuitReinforcedRandomWalk(int startNode, double deltaW,boolean multiCount){
		ArrayList<Integer> visitedNodeIndexList = new ArrayList<Integer>();
		visitedNodeIndexList.add(startNode);
		int visitedNodeN = 1;

		int currentNodeIndex = startNode;

		int selectedEdge,nextNodeIndex;

		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i] = 0.0;
			for(int j=0;j<nodeList.get(i).eList.size();j++){
				sumW[i] += weight[nodeList.get(i).eList.get(j).index];
			}
		}
		double r,threshold;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) newWeight[i]=weight[i];
		int spendingSteps = 0;
		while(visitedNodeN < N){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedEdge = 0;
			if(currentNode.eList.size()>=1) {
				threshold = weight[currentNode.eList.get(0).index];

				while(r > threshold){
					selectedEdge++;
					threshold += weight[currentNode.eList.get(selectedEdge).index];
				}

				//degag
//				System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

				// nextNodeIndexの決定
				if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
				}else{
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
				}

				// multiCountがONになっているなら多重回加算
				if(multiCount) newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;

				// 未訪問だったときの処理
				if(!visitedNodeIndexList.contains(nextNodeIndex)){
					visitedNodeIndexList.add(nextNodeIndex);
					visitedNodeN++;
					if(!multiCount) newWeight[currentNode.eList.get(selectedEdge).index] += deltaW;
				}

				spendingSteps++;

				currentNodeIndex = nextNodeIndex;
			}else{
				System.out.println("次数0の頂点があります。");
			}
		}

		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

		int[] resultValueList = {spendingSteps, currentNodeIndex};
		return resultValueList;
	}

	/**
	 *  全頂点を巡回するRWを数回実行する。<br>
	 *  一度、巡回が終了したら、重みを減算する。<br>
	 *  RWは前回までの重みを参照して歩行する。<br>
	 *  startNodeを負の数にした場合は、ランダムにstartを決定する<br>
	 * (注)<br>
	 * 以下の状況が必要<br>
	 * ●setNode()またはsetNode(false)適用済み<br>
	 * ●setEdge()適用済み<br>
	 * @param step
	 * @param deltaW
	 * @param seed
	 * @param teleportP
	 */
	public int CircuitReinforcedRandomWalk2(int tryN, double divider, int input_startNodeIndex, boolean disturb, boolean consolePlot){
		if(weight == null) {
			weight = new double[M];
			for(int i=0;i<weight.length;i++){
				weight[i] = 1.0;
			}
		}

		int totalStep = 0;

		int[] resultValueList;
		int subSpendingStep;

		int startNodeIndex;
		if(input_startNodeIndex<0){
			startNodeIndex = (int)(N*Math.random());
		}else{
			startNodeIndex = input_startNodeIndex%N;
		}
		long startTime = System.currentTimeMillis();

		int debag_a=10;

		for(int i=0;i<tryN;i++){
			resultValueList = SubCircuitReinforcedRandomWalk2(startNodeIndex, divider);
			if(consolePlot) {
				if((i+1)==debag_a) {
					System.out.println("i=" + i);
					System.out.println("rap:"+(System.currentTimeMillis()-startTime)*0.001+"[s]");
					System.out.println("remEdge =" + debag_rem());
					System.out.println();
					debag_a *=10;
					startTime = System.currentTimeMillis();
				}
			}

			subSpendingStep = resultValueList[0];

			totalStep += subSpendingStep;

			if(input_startNodeIndex<0) startNodeIndex = (int)(N*Math.random());
		}

		if(disturb) disturb();
		for(int i=0;i<M;i++) {
			if(weight[i] <=0) weight[i]=Math.pow(10, -6);
		}

		return totalStep;
	}

	private int debag_rem() {
		int remEdge = 0;
		for(int i=0;i<M;i++) {
			if(weight[i] <= 1.0E-6) {
				remEdge++;
			}
		}
		return (M-remEdge);
	}

	private int[] SubCircuitReinforcedRandomWalk2(int startNode, double divider){
		ArrayList<Integer> visitedNodeIndexList = new ArrayList<Integer>();
		visitedNodeIndexList.add(startNode);
//		ArrayList<Integer> visitedEdgeIndexList  = new ArrayList<Integer>();
		short[] visitedEdgeList = new short[M];
		for(int i=0;i<M;i++) visitedEdgeList[i] = 0;
		int visitedNodeN = 1;

		int currentNodeIndex = startNode;

		int selectedEdge,nextNodeIndex;

		double[] sumW = new double[N];
		for(int i=0;i<N;i++){
			sumW[i] = 0.0;
			for(int j=0;j<nodeList.get(i).eList.size();j++){
				sumW[i] += weight[nodeList.get(i).eList.get(j).index];
			}
		}
		double r,threshold;
		double[] newWeight = new double[M];
		for(int i=0;i<M;i++) {
			newWeight[i]=weight[i];
		}
		int spendingSteps = 0;
		while(visitedNodeN < N){
			Network_base.Node currentNode = nodeList.get(currentNodeIndex);

			// ここが各ランダムウォークで変化する内容(辺の選択方法)
			r = (sumW[currentNodeIndex]*Math.random());
			selectedEdge = 0;
			if(currentNode.eList.size()>=1) {
				threshold = weight[currentNode.eList.get(0).index];

				while(r > threshold){
					selectedEdge++;
					threshold += weight[currentNode.eList.get(selectedEdge).index];
				}

				//degag
//				System.out.print(currentNodeIndex + ":" + degree[currentNodeIndex] + ",");

				// 辺が未訪問だった場合、訪問済み辺リストに追加
				if(visitedEdgeList[currentNode.eList.get(selectedEdge).index]==0) {
					visitedEdgeList[currentNode.eList.get(selectedEdge).index] = 1;
				}


				// nextNodeIndexの決定
				if(currentNode.eList.get(selectedEdge).node[0]!=currentNodeIndex){
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[0];
				}else{
					nextNodeIndex = currentNode.eList.get(selectedEdge).node[1];
				}

				// 頂点が未訪問だったときの処理
				if(!visitedNodeIndexList.contains(nextNodeIndex)){
					visitedNodeIndexList.add(nextNodeIndex);
					visitedNodeN++;
				}

				spendingSteps++;

				currentNodeIndex = nextNodeIndex;
			}else{
				System.out.println("次数0の頂点があります。");
			}
		}

		double inv_divider = 1.0/divider;

		// 未訪問の辺をdividerで割る
		for(int i=0;i<M;i++) {
			if(visitedEdgeList[i]==0) {
				newWeight[i] *= inv_divider;
			}
			if(newWeight[i] <= Math.pow(10, -6)) newWeight[i]=0.0;
		}

		// wieght更新
		for(int i=0;i<M;i++){
			weight[i] = newWeight[i];
		}

		int[] resultValueList = {spendingSteps, currentNodeIndex};
		return resultValueList;
	}

	public int CircuitReinforcedRandomWalk2(int tryN, double divider, int input_startNodeIndex, boolean disturb) {
		return CircuitReinforcedRandomWalk2(tryN, divider, input_startNodeIndex, disturb, false);
	}

	/**
	 * 無作為に辺を2つ選び、頂点の組み合わせを交換する。<br>
	 * 引数で交換回数を指定する。<br>
	 * 未指定の場合、10*M*log(M)回交換する。<br>
	 * ・setNode及びsetEdgeを実行済みであることが前提。
	 */
	public void EdgeRewiring(int swap_times) {
		for(int i=0;i<swap_times;i++) {
			// 交換される辺を選択
			int edge1 = (int)(M*Math.random());
			int edge2 = (int)((M-1)*Math.random());
			if(edge1==edge2) edge2=M-1;
			// どちらの頂点を切断するか選択
			int picked_edge1_index = (int)(2*Math.random());
			int picked_edge2_index = (int)(2*Math.random());

			// 自己ループの判定
			boolean selfLoop =
					(list[edge1][(picked_edge1_index+1)%2]==list[edge2][picked_edge2_index]) ||
					(list[edge2][(picked_edge2_index+1)%2]==list[edge1][picked_edge1_index]);
			// 多重辺の判定
			boolean multi = false;
			if(!selfLoop) {
				for(int j=0;j<nodeList.get(list[edge1][(picked_edge1_index+1)%2]).list.size();j++) {
					int current_node_index = nodeList.get(list[edge1][(picked_edge1_index+1)%2]).list.get(j).index;
					if(current_node_index==list[edge2][picked_edge2_index]) {
						multi = true;
						break;
					}
				}
			}
			if(!multi) {
				for(int j=0;j<nodeList.get(list[edge2][(picked_edge2_index+1)%2]).list.size();j++) {
					int current_node_index = nodeList.get(list[edge2][(picked_edge2_index+1)%2]).list.get(j).index;
					if(current_node_index==list[edge1][picked_edge1_index]) {
						multi = true;
						break;
					}
				}
			}

			// 問題ないようならスワッピング、だめならやり直し
			if(!selfLoop && !multi) {
				// 隣接頂点情報を消去
				int current_node_index;
				ArrayList<Node> current_list;
				current_node_index = list[edge1][(picked_edge1_index+1)%2];
				current_list = nodeList.get(current_node_index).list;
				for(int j=0;j<current_list.size();j++) {
					if(list[edge1][picked_edge1_index] == current_list.get(j).index) {
						current_list.remove(j);
						break;
					}
				}
				current_node_index = list[edge1][picked_edge1_index];
				current_list = nodeList.get(current_node_index).list;
				for(int j=0;j<current_list.size();j++) {
					if(list[edge1][(picked_edge1_index+1)%2] == current_list.get(j).index) {
						current_list.remove(j);
						break;
					}
				}
				current_node_index = list[edge2][(picked_edge2_index+1)%2];
				current_list = nodeList.get(current_node_index).list;
				for(int j=0;j<current_list.size();j++) {
					if(list[edge2][picked_edge2_index] == current_list.get(j).index) {
						current_list.remove(j);
						break;
					}
				}
				current_node_index = list[edge2][picked_edge2_index];
				current_list = nodeList.get(current_node_index).list;
				for(int j=0;j<current_list.size();j++) {
					if(list[edge2][(picked_edge2_index+1)%2] == current_list.get(j).index) {
						current_list.remove(j);
						break;
					}
				}

				// 頂点を交換
				int temp_index = list[edge2][picked_edge2_index];
				list[edge2][picked_edge2_index] = list[edge1][picked_edge1_index];
				list[edge1][picked_edge1_index] = temp_index;

				// 隣接頂点情報を更新
				current_node_index = list[edge1][0];
				nodeList.get(current_node_index).list.add(nodeList.get(list[edge1][1]));
				current_node_index = list[edge1][1];
				nodeList.get(current_node_index).list.add(nodeList.get(list[edge1][0]));
				current_node_index = list[edge2][0];
				nodeList.get(current_node_index).list.add(nodeList.get(list[edge2][1]));
				current_node_index = list[edge2][1];
				nodeList.get(current_node_index).list.add(nodeList.get(list[edge2][0]));

			}else {
				i--;
				continue;
			}

		}

		// Node, Edgeデータを再構成
		nodeList.clear();
		edgeList.clear();
		if(M==list.length) setNode(false);
		else setNode();
		setEdge();
	}

	public void EdgeRewiring() {
		int t = (int)(10*M*Math.log(M));
		EdgeRewiring(t);
	}

	public Network_base FilteringBySalience(double min_ExcludeSection, double max_ExcludeSection) {
		Network_base net = new Network_base();
		net.N = N;
		int newM = M;

		boolean[] exists = new boolean[edgeList.size()];
		for(int i=0;i<edgeList.size();i++) {
			if(min_ExcludeSection<=edgeList.get(i).linkSalience && edgeList.get(i).linkSalience<=max_ExcludeSection) {
				exists[i] = false;
				newM--;
			}else {
				exists[i] = true;
			}
		}

		net.degree = new int[net.N];
		for(int i=0;i<degree.length;i++) net.degree[i]=degree[i];
		net.M = newM;
		net.list = new int[newM][2];
		net.weight = new double[newM];
		net.edgeLabel = new String[newM]; //フィルタリング前のindexを記憶
		boolean this_is_original = (edgeLabel==null);
		int currentLine = 0;
		for(int i=0;i<M;i++) {
			if(exists[i]) {
				net.list[currentLine][0] = list[i][0];
				net.list[currentLine][1] = list[i][1];
				net.weight[currentLine] = 1.0;
				if(this_is_original) net.edgeLabel[currentLine] = Integer.toString(i);
				else net.edgeLabel[currentLine] = edgeLabel[i];
				currentLine++;
			}else {
				net.degree[list[i][0]]--;
				net.degree[list[i][1]]--;
			}
		}

		return net;
	}


	// Networkｵﾌﾞｼﾞｪｸﾄを複製できるようにメソッド追加
	public Network_base clone(){
		Network_base net = new Network_base();
		net.N = N;
		net.M = M;
		net.degree = new int[N];
		for(int i=0;i<N;i++) net.degree[i]=degree[i];
		net.list = new int[M][2];
		for(int i=0;i<M;i++) {
			net.list[i][0] = list[i][0];
			net.list[i][1] = list[i][1];

		}

		if(weight != null) {
			net.weight = new double[M];
			for(int i=0;i<M;i++) net.weight[i]=weight[i];
		}
		return net;
	}

	protected class Node{
		int index;
		double betweenCentrality;

		int visits;
		boolean directDeleted;
		boolean indirectDeleted;

		ArrayList<Node> list = new ArrayList<Node>();
		ArrayList<Edge> eList = new ArrayList<Edge>();

		Node(int inputIndex){
			index = inputIndex;
			betweenCentrality = 0;
			visits = 0;
		}

	}

	protected static class Edge{
		int index;
		int[] node = new int[2];
		double betweenCentrality;
		int linkSalience;
		int visits;
		boolean deleted;

		Edge() {
			init();
			node[0] = -1;
			node[1] = -1;
		}

		Edge(int i,int j) {
			init();
			node[0]=i;
			node[1]=j;
		}

		Edge(int i,int j,int inputIndex) {
			init();
			node[0]=i;
			node[1]=j;
			index = inputIndex;
		}

		void setNode(int n1,int n2){
			node[0] = Math.min(n1, n2);
			node[1] = Math.max(n1, n2);
		}

		private void init(){
			linkSalience=0;
			betweenCentrality=0;
			visits=0;
		}

	}

}
