import java.util.ArrayList;
import java.util.Random;



public class ConfigurationNetwork extends Network{

	/**
	 * 与えられた次数列に従うネットワークを構築する。
	 * @param degreeSequence 次数列
	 * @param loopLimit スタブの結合に連続で失敗できる回数
	 * @param seed 乱数シード
	 */
	public ConfigurationNetwork(int[] degreeSequence, int loopLimit, long seed){
		generate(this, degreeSequence, loopLimit, seed);
	}


	/**
	 * Configurationモデルの生成機構を他のクラスでも利用できるメソッド。<br>
	 * @param net Configurationモデルの生成機構を適用するネットワーク。
	 * @param degreeSequence 次数列
	 * @param loopLimit スタブの結合に連続で失敗できる回数
	 * @param seed 乱数シード
	 */
	public static void generate(Network net, int[] degreeSequence, int loopLimit, long seed) {
		Random rnd = new Random(seed);

		// 基礎的な変数を定義
		net.N = degreeSequence.length;
		net.degree = new int[net.N];

		// TODO ArrayListを使わない書き方ができるはず。
		// 次数列読み込み
		ArrayList<Integer> stubList = new ArrayList<Integer>();
		int sumDegree=0;
		for(int i=0;i<degreeSequence.length;i++){
			int currentDegree = degreeSequence[i];
			for(int j=0;j<currentDegree;j++){
				stubList.add(i);
			}
			net.degree[i] = currentDegree;
			sumDegree += currentDegree;
		}
		if(sumDegree%2==1){
			stubList.add(0);
			net.degree[0]++;
			sumDegree++;
		}
		net.M = sumDegree/2;

		// 隣接リスト関連の変数を定義
		net.addressList = new int[net.N];
		net.addressList[0] = 0;
		for(int i=1;i<net.N;i++){
			net.addressList[i] = net.addressList[i-1] + net.degree[i-1];
		}
		net.neightborList = new int[net.M*2];
		net.neightborIndexList = new int[net.M*2];
		for(int i=0;i<net.M;i++) net.neightborIndexList[i]=-123456;


		int disconnectedStubs = sumDegree;
		net.edgeList = new int[net.M][2];
		net.success = true;
		int currentLine = 0;
		int[] cursor = new int[net.N];
		for(int i=0;i<net.N;i++) cursor[i]=net.addressList[i];

		generateLoop: do{
			int targetStubIndexA, targetStubIndexB;
			boolean selfLoop, multiple;
			int currentLoopLimit = loopLimit; //ループ回数回復
			do{
				// リストからスタブを2つ選択
				targetStubIndexA=rnd.nextInt(disconnectedStubs);
				targetStubIndexB=rnd.nextInt(disconnectedStubs);

				// ループ回数が制限を超えた場合、失敗として処理する
				if(currentLoopLimit<=0){
					net.success = false;
					break generateLoop;
				}
				currentLoopLimit--;

				// 自己ループをチェック
				int stubANode = stubList.get(targetStubIndexA);
				int stubBNode = stubList.get(targetStubIndexB);
				selfLoop= (stubANode==stubBNode);

				// 多重辺をチェック
				multiple=false;
				if(!selfLoop) {
					cheakMultiple:for(int i=net.addressList[stubANode] ; i<cursor[stubANode] ; i++){
						if(net.neightborList[i] == stubBNode){
							multiple=true;
							break cheakMultiple;
						}
					}
				}
			}while(selfLoop || multiple);

			// stubListの後ろから取り出す。(前からだと順序が変わってしまうため)
			int smallerStub = Math.min(targetStubIndexA, targetStubIndexB);
			int largerStub = Math.max(targetStubIndexA, targetStubIndexB);
			int[] nodeIndexs = new int[2];
			nodeIndexs[0] = stubList.get(largerStub);
			stubList.remove(largerStub);
			nodeIndexs[1] = stubList.get(smallerStub);
			stubList.remove(smallerStub);

			// 登録作業
			int minNode = Math.min(nodeIndexs[0], nodeIndexs[1]);
			int maxNode = Math.max(nodeIndexs[0], nodeIndexs[1]);
			net.edgeList[currentLine][0] = minNode;
			net.edgeList[currentLine][1] = maxNode;
			net.neightborList[cursor[minNode]] = maxNode;
			net.neightborList[cursor[maxNode]] = minNode;
			net.neightborIndexList[cursor[minNode]] = currentLine;
			net.neightborIndexList[cursor[maxNode]] = currentLine;
			cursor[minNode]++;
			cursor[maxNode]++;

			disconnectedStubs -= 2;
			currentLine++;
		}while(disconnectedStubs>0);

	}

}
