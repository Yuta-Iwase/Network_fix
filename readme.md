<!-- 数式生成には https://www.codecogs.com/latex/eqneditor.php が便利 -->

# 使い方
## 概要
このプロジェクトのプログラムを使うことで、ネットワークの生成と解析を行うことができる。
生成できる主なネットワークと解析は以下の通りである。

### 生成
- **ランダムグラフ(Erdős–Rényi model)**<br>
次数分布がポアソン分布![](https://latex.codecogs.com/gif.latex?p%28k%29%20%3D%20%5Cfrac%20%7B%20%7B%5Clangle%20k%20%5Crangle%20%7D%20%5E%20%7B%20k%20%7D%20e%20%5E%20%7B%20-%20%5Clangle%20k%20%5Crangle%20%7D%20%7D%20%7B%20k%20%21%20%7D)
 であるネットワークを生成する。
 - **スケールフリーネットワーク**<br>
 次数分布がべき分布![](https://latex.codecogs.com/gif.latex?p%28k%29%20%5Cpropto%20k%5E%7B-%20%5Cgamma%7D)
 であるネットワークを生成する。
 - **DMSモデル**[[1]](https://pdfs.semanticscholar.org/1735/6d327c5040417ce9ac8e993ca026961c17f2.pdf)<br>
 優先的選択を導入し、頂点が追加されてゆくネットワークを生成する。
 - **辺リストを読み込み生成されるネットワーク**<br>
 辺リストを記したcsvファイルを指定することで、その辺リストを持つネットワークを生成する(重み付きネットワークにも対応)。
 
 ### 解析
 - 連結成分解析
 - obserbability解析[[2]](https://arxiv.org/abs/1808.02255)
 - link salience解析[[3]](https://www.nature.com/articles/ncomms1847.pdf)
 - 重み![](https://latex.codecogs.com/gif.latex?w_%7Bij%7D)を![](https://latex.codecogs.com/gif.latex?w_%7Bij%7D%20%5Cpropto%20%7B%28k_i%20k_j%29%7D%5E%5Calpha)とする重み付け
 - biased random walkを用いた重み付け
 - reinforced random walkを用いた重み付け

## 使用例
いきなりライブラリ集を見るよりも実例を見るほうがわかりやすいので、先に実例を出す。
ライブラリと同一パッケージ内に以下のプログラムを組むことで、「スケールフリーネットワークの生成」と「次数分布のプロット」ができる。

```java
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
```
この例のように、ネットワークの生成自体は1行で行うことができる。
このように新たなクラスを作成し、ライブラリを用いて解析を行う。
以下は主要なプログラム例を記載する。

### obserbability解析
csvファイルに記憶されている辺リストを持つネットワークを生成、observabilityを計算する。

```java
import java.io.File;
import java.io.PrintWriter;

public class Exam_observability {

	public static void main(String[] args) throws Exception{
		int tmax = 1; //同じ故障率で何回サンプルを取るか
		int imax = 100; //fを0から1へ動かす際の刻み数
		long seed = System.currentTimeMillis(); //シード値

		// 実ネットワークを読み込み
		// 今回はhttp://konect.uni-koblenz.de/networks/as-caida20071105から取得したデータを読み込めるように加工し使用した
		CSVFileNetwork net = new CSVFileNetwork("c:\\desktop\\caida.csv", false);
		net.set_neightbor();

		int N = net.N;

		// 各最大連結成分のサイズを記憶する変数
		double sum_N = 0.0;
		double sum_D = 0.0;
		double sum_I = 0.0;
		double sum_NI = 0.0;
		double sum_DI = 0.0;

		// データの保存先の設定
		String folderName = "c:\\desktop\\observability";
		new File(folderName).mkdirs();
		folderName = folderName + "\\";
		PrintWriter pw1 = new PrintWriter(new File(folderName + "N-comp.csv"));
		PrintWriter pw2 = new PrintWriter(new File(folderName + "D-comp.csv"));
		PrintWriter pw3 = new PrintWriter(new File(folderName + "I-comp.csv"));
		PrintWriter pw4 = new PrintWriter(new File(folderName + "NI-comp.csv"));
		PrintWriter pw5 = new PrintWriter(new File(folderName + "DI-comp.csv"));

		// 解析
		for(int i=0;i<imax;i++) {
			double f = i / (double)imax;
			sum_N = 0.0;
			sum_D = 0.0;
			sum_I = 0.0;
			sum_NI = 0.0;
			sum_DI = 0.0;
			for(int t=0;t<tmax;t++) {
				net.exec_sitePercolationNDI(f, true, seed);

				net.calc_connectedCompornentNDI(true, false, false);
				sum_N += net.max_ccSize_NDI;

				net.calc_connectedCompornentNDI(false, true, false);
				sum_D += net.max_ccSize_NDI;

				net.calc_connectedCompornentNDI(false, false, true);
				sum_I += net.max_ccSize_NDI;

				net.calc_connectedCompornentNDI(true, false, true);
				sum_NI += net.max_ccSize_NDI;

				net.calc_connectedCompornentNDI(false, true, true);
				sum_DI += net.max_ccSize_NDI;
			}
			sum_N /= (tmax*N);
			sum_D /= (tmax*N);
			sum_I /= (tmax*N);
			sum_NI /= (tmax*N);
			sum_DI /= (tmax*N);

			pw1.println(f + "," + sum_N);
			pw2.println(f + "," + sum_D);
			pw3.println(f + "," + sum_I);
			pw4.println(f + "," + sum_NI);
			pw5.println(f + "," + sum_DI);
		}

		pw1.close();
		pw2.close();
		pw3.close();
		pw4.close();
		pw5.close();
	}

}
```

### link salience解析
スケールフリーネットワークを生成し、biased random walkを用いて重み付けしたネットワークのhigh salience fractionを計算している。
```java
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
```
