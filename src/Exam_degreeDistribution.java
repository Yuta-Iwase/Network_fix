
public class Exam_degreeDistribution {

	public static void main(String[] args) {
		int N = 10000;
		MakePowerLaw dist = new MakePowerLaw(N, 2.7);
		ConfigurationNetwork net = new ConfigurationNetwork(dist.degree, 100, System.currentTimeMillis());

		int[] degreeFreq = new int[N+1];
		for(int i=0;i<N;i++){
			degreeFreq[net.degree[i]]++;
		}

		double inv_N = 1.0/N;
		for(int i=0;i<N+1;i++){
			if(degreeFreq[i] > 0){
				System.out.println(i + "\t" + degreeFreq[i]*inv_N);
			}
		}

		net.exec_printEdgeList("c:\\desktop\\test.csv");

	}

}
