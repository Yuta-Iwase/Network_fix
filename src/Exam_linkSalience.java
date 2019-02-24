
public class Exam_linkSalience {

	public static void main(String[] args) {
		int N = 1000;
		double gamma = 2.7;
		double alpha = 1.5;

		long seed = System.currentTimeMillis();
		MakePowerLaw dist = new MakePowerLaw(N, gamma, 2, N/10);
		ConfigurationNetwork net = new ConfigurationNetwork(dist.degree, 100, seed);

		net.set_neightbor();
		net.set_weightToAlpha(alpha);
		net.exec_weightDisturb(seed);
		net.calc_linkSalience();

		int hs_freq = 0;
		double hs_threshold = 0.9*net.N;
		double inv_N = 1.0/net.N;
		for(int i=0;i<net.M;i++){
			if(net.linkSalience[i] > hs_threshold){
				hs_freq++;
			}
		}
		double f_hs = hs_freq*inv_N;

		System.out.println(f_hs);

	}

}
