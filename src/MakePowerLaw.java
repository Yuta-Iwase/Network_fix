// 2017/05/31
// ・独立したクラスではなく呼び出しに対応できるクラスに変更した。

public class MakePowerLaw {
	int[] degree;

	int N;
	double gamma;
	int minDegree,maxDegree;

	double[] c;

	public MakePowerLaw(int input_N,double input_gamma,int input_minDegree,int input_maxDegree) {
		N = input_N;
		gamma = input_gamma;
		minDegree = input_minDegree;
		maxDegree = input_maxDegree;

		generate();
	}


	public MakePowerLaw(int input_N,double input_gamma) {
		N = input_N;
		gamma = input_gamma;
		minDegree = 2;
		maxDegree = N-1;

		generate();
	}

	public void generate(){
		// 離散量の確率分布を定義
		double[] p = new double[maxDegree+1];
		double sum = 0.0;
		for(int i=minDegree;i<maxDegree;i++){
			p[i] = Math.pow(i, -gamma);
			sum += p[i];
		}
		// 規格化
		for(int i=minDegree;i<maxDegree;i++){
			p[i] /= sum;
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
		double r;
		double nextLeft,nextRight;
		for(int i=0;i<N;i++){
			r = Math.random();
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

	// 完成した次数列degree[i]を出力
	public void printList(){
		for(int i=0;i<N;i++){
			System.out.println(i + "\t" + degree[i]);
		}
	}

	public double averageDegree() {
		double average = 0.0;
		average += minDegree * (c[minDegree]-0);
		for(int k=minDegree+1 ; k<=maxDegree ; k++) {
			average += k*(c[k]-c[k-1]);
		}
		return average;
	}

}
