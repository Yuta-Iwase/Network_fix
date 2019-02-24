import java.util.Random;

public class MakePowerLaw {
	/** 次数列 */
	int[] degree;

	/** 生成時に使用する変数 */
	int N;
	double gamma;
	int minDegree,maxDegree;
	long seed;
	double[] c;

	/**
	 * 頂点数N, べき指数gamma, 最小次数minDegree, 最大次数maxDegreeでのべき分布に従う次数列を生成する。<br>
	 * 次数列へのアクセスは、このオブジェクトのint[] degreeを取得すればよい。
	 * @param N 頂点数
	 * @param gamma べき指数
	 * @param minDegree 最小次数
	 * @param maxDegree 最大次数
	 */
	public MakePowerLaw(int N, double gamma, int minDegree, int maxDegree, long seed) {
		// 変数代入
		this.N = N;
		this.gamma = gamma;
		this.minDegree = minDegree;
		this.maxDegree = maxDegree;
		this.seed = seed;
		// 生成
		generate();
	}

	/**
	 * 次数列を生成するメソッド。<br>
	 * このオブジェクトはコンストラクタの時点で次数列を生成しているが、なんらかの原因で再度生成しなくてはいけないときは、これを使う。<br>
	 */
	public void generate(){
		// 離散量の確率分布を定義
		double[] p = new double[maxDegree+1];
		double sum = 0.0;
		for(int i=minDegree;i<maxDegree;i++){
			p[i] = Math.pow(i, -gamma);
			sum += p[i];
		}
		// 規格化
		double inv_sum = 1.0/sum;
		for(int i=minDegree;i<maxDegree;i++){
			p[i] *= inv_sum;
		}

		// p[i]の累積分布c[i]を定義
		c = new double[maxDegree+1];
		c[minDegree] = p[minDegree];
		for(int i=minDegree+1 ; i<maxDegree ; i++){
			c[i] = c[i-1] + p[i];
		}
		c[maxDegree] = 1.0;

		// c[i]に従い次数列degree[i]生成
		degree = new int[N];
		int currentIndex;
		Random rnd = new Random(seed);
		double nextLeft,nextRight;
		for(int i=0;i<N;i++){
			double r = rnd.nextDouble();
			currentIndex = 0;
			nextLeft = 0;
			nextRight = c[minDegree];
			while(!(nextLeft<=r && r<nextRight)){
				nextLeft = c[minDegree+currentIndex];
				nextRight = c[minDegree+currentIndex+1];
				currentIndex++;
			}
			degree[i] = minDegree + currentIndex;
		}

	}

	/**
	 * 次数列をプロット
	 */
	public void printList(){
		for(int i=0;i<N;i++){
			System.out.println(i + "\t" + degree[i]);
		}
	}

}
