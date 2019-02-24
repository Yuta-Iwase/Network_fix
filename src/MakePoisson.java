import java.util.Random;

public class MakePoisson {
	/** 次数列 */
	int[] degree;

	/** 生成時に使用する変数 */
	int N;
	double average;
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
	public MakePoisson(int N, double average, int minDegree, int maxDegree, long seed) {
		// 変数代入
		this.N = N;
		this.average = average;
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
		if(average>0) {
			// 離散量の確率分布を定義
			double[] p = new double[maxDegree+1];
			p[minDegree] = (Math.pow(average, minDegree)*Math.exp(-average))/factorial(minDegree);
			double sum = p[minDegree];
			for(int i=minDegree+1;i<=maxDegree;i++){
				p[i] = p[i-1]*(average/i);
				sum += p[i];
			}
			// 規格化
			for(int i=minDegree;i<=maxDegree;i++){
				p[i] /= sum;
			}

			// p[i]の累積分布c[i]を定義
			c = new double[maxDegree+1];
			c[minDegree] = p[minDegree];
			for(int i=minDegree+1 ; i<=maxDegree ; i++){
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
		}else {
			degree = new int[N];
			for(int i=0;i<N;i++){
				degree[i] = 0;
			}
		}


	}

	// 階乗
	private int factorial(int n) {
		int result = 1;
		for(int i=1;i<=n;i++) {
			result *= i;
		}
		return result;
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
