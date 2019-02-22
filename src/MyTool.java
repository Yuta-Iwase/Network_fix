public class MyTool {

	final static double DOUBLE_MACHINE_EPSILON = 2.2204460492503131E-16;

	static int max(int[] array) {
		int max = array[0];
		for(int i=0;i<array.length;i++) {
			if(max<array[i]) max = array[i];
		}
		return max;
	}

	static double max(double[] array) {
		double max = array[0];
		for(int i=0;i<array.length;i++) {
			if(max<array[i]) max = array[i];
		}
		return max;
	}

	static int sum(int[] array) {
		int sum = 0;
		for(int i=0;i<array.length;i++) {
			sum += array[i];
		}
		return sum;
	}

	static double sum(double[] array) {
		double sum = 0.0;
		for(int i=0;i<array.length;i++) {
			sum += array[i];
		}
		return sum;
	}

	static double average(int[] array) {
		return sum(array)/((double)array.length);
	}

	static double average(double[] array) {
		return sum(array)/array.length;
	}

	static int square(int a) {
		return a*a;
	}

	static double square(double a) {
		return a*a;
	}

	/**
	 * マシンイプシロンを考慮して二数a,bを比較する<br>
	 * 本質的にa==bならば0を、a>bなら1、b>aなら-1を返す。<br>
	 * @param a
	 * @param b
	 * @return 0(a==b), 1(a>b), -1(b>a)
	 */
	static int compareDouble(double a, double b){
		if(Math.abs(a-b)<DOUBLE_MACHINE_EPSILON){
			return 0;
		}else{
			return a>b ? 1 : -1;
		}
	}

	static void printList(int[] list) {
		System.out.print("{");
		for(int i=0;i<list.length-1;i++) {
			System.out.print(list[i] + ", ");
		}
		System.out.println(list[list.length-1] + "}");
	}

	/**
	 *
	 * @param scatterSequence 散布列
	 * @param maxY 散布列の要素の最大値
	 * @return 度数列
	 */
	static int[] makeFrequency(int[] scatterSequence, int maxY) {
		int[] frequency = new int[maxY+1];
		for(int i=0;i<scatterSequence.length;i++) {
			frequency[scatterSequence[i]]++;
		}
		return frequency;
	}

	static int[] makeFrequency(int[] scatterSequence) {
		int maxY = Integer.MIN_VALUE;
		for(int i=0;i<scatterSequence.length;i++) {
			if(maxY<scatterSequence[i]) maxY=scatterSequence[i];
		}
		int[] frequency = new int[maxY];
		for(int i=0;i<scatterSequence.length;i++) {
			frequency[scatterSequence[i]]++;
		}
		return frequency;
	}

}
