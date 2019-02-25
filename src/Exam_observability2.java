import java.io.File;
import java.io.PrintWriter;

public class Exam_observability2 {

	public static void main(String[] args) throws Exception{
		int tmax = 1; //同じ故障率で何回サンプルを取るか
		int imax = 100; //fを0から1へ動かす際の刻み数
		long seed = System.currentTimeMillis(); //シード値

		Network net;
		int N = 1000;
		int minDegree = 1;
		int maxDegree = N-1;
		double s_average = 2.0;
		double t_average = 1.0;

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
				 do {
					 MakePoisson dist_s = new MakePoisson(N, s_average, minDegree, maxDegree, seed);
					 MakePoisson dist_t = new MakePoisson(N, t_average, minDegree, maxDegree, seed);
					 net = new RandomClusteredNetwork(dist_s.degree, dist_t.degree, 100, seed);
				 }while(!net.success);
				 net.set_neightbor();

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
