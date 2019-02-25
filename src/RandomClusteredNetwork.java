import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class RandomClusteredNetwork extends Network{

	/**
	 * 与えられた2つの次数列に従う random clustered networkを構築する。<br>
	 * 詳細は、「Random graphs with clustering」、「Observability transitions in clustered networks」を参照<br>
	 * @param s_degreeSequence この次数列でできたスタブは、通常のconfigモデル同様、もうひとつのスタブとつなぐ。
	 * @param t_degreeSequence この次数列でできたスタブは、他の2つのスタブと相互間に接続し、三角形を形成する。
	 * @param loopLimit スタブの結合に連続で失敗できる回数
	 * @param seed シード値
	 */
	public RandomClusteredNetwork(int[] s_degreeSequence, int[] t_degreeSequence, int loopLimit, long seed){
		generate(s_degreeSequence,t_degreeSequence, loopLimit, seed);
	}

	private void generate(int[] isolated_DegreeList, int[] cluster_FragmentList, int loopLimit, long seed) {
		N = isolated_DegreeList.length;
		degree = new int[N];

		ArrayList<Integer> isolatedStubArray = new ArrayList<Integer>();
		ArrayList<Integer> clusterFragmentArray = new ArrayList<Integer>();
		int[] clusteringFragmentRemainder = new int[N];
		int sum_isolatedStub = 0;
		int sum_clusteringFragment = 0;
		int sumDegree=0;
		for(int i=0;i<N;i++){
			degree[i] = 0;

			for(int j=0;j<isolated_DegreeList[i];j++){
				isolatedStubArray.add(i);
			}
			degree[i] += isolated_DegreeList[i];
			sum_isolatedStub += isolated_DegreeList[i];

			for(int j=0;j<cluster_FragmentList[i];j++){
				clusterFragmentArray.add(i);
			}
			degree[i] += 2*cluster_FragmentList[i];
			sum_clusteringFragment += cluster_FragmentList[i];

			clusteringFragmentRemainder[i] = cluster_FragmentList[i];
		}

		if(sum_isolatedStub%2 == 1){
			isolatedStubArray.add(0);
			degree[0]++;
			sum_isolatedStub++;
		}

		switch(sum_clusteringFragment%3){
		case 1:
			clusterFragmentArray.add(0);
			clusterFragmentArray.add(1);
			degree[0] += 2;
			degree[1] += 2;
			sum_clusteringFragment += 2;
			break;

		case 2:
			clusterFragmentArray.add(0);
			degree[0] += 2;
			sum_clusteringFragment += 1;
			break;
		}


		sumDegree = sum_isolatedStub + 2*sum_clusteringFragment;
		M = sumDegree/2;

		// 隣接リスト関連の変数を定義
		addressList = new int[N];
		addressList[0] = 0;
		for(int i=1;i<N;i++){
			addressList[i] = addressList[i-1] + degree[i-1];
		}
		neightborList = new int[M*2];
		neightborIndexList = new int[M*2];
		for(int i=0;i<M*2;i++) neightborIndexList[i]=-123456;

		Random rnd = new Random(seed);
		int disconnectedN=sum_isolatedStub;
		int targetEdgeA,targetEdgeB;
		int currentLine=0;
		int[] cursor = new int[N];
		for(int i=0;i<N;i++) cursor[i]=addressList[i];
		int nowLoopLimit;
		boolean selfLoop,multiple;
		success = true;
		edgeList = new int[M][2];
		if(isolatedStubArray.size()>0) {
			generateLoop: do{
				nowLoopLimit=loopLimit;
				do{
					targetEdgeA=rnd.nextInt(disconnectedN);
					targetEdgeB=rnd.nextInt(disconnectedN);

					if(nowLoopLimit<=0){
						success = false;
						break generateLoop;
					}
					nowLoopLimit--;

					int currentNodeA = isolatedStubArray.get(targetEdgeA);
					int currentNodeB = isolatedStubArray.get(targetEdgeB);
					selfLoop= currentNodeA==currentNodeB;
					// 多重辺をチェック
					multiple=false;
					if(!selfLoop) {
						cheakMultiple:for(int i=addressList[currentNodeA] ; i<cursor[currentNodeA] ; i++){
							if(neightborList[i] == currentNodeB){
								multiple=true;
								break cheakMultiple;
							}
						}
					}
				}while(selfLoop || multiple);

				if(targetEdgeA >= targetEdgeB){
					int right = isolatedStubArray.get(targetEdgeA);
					isolatedStubArray.remove(targetEdgeA);
					int left = isolatedStubArray.get(targetEdgeB);
					isolatedStubArray.remove(targetEdgeB);
					edgeList[currentLine][0] = left;
					edgeList[currentLine][1] = right;
					neightborList[cursor[left]] = right;
					neightborList[cursor[right]] = left;
					neightborIndexList[cursor[right]] = currentLine;
					neightborIndexList[cursor[left]] = currentLine;
					cursor[left]++;
					cursor[right]++;
				}else{
					int right = isolatedStubArray.get(targetEdgeB);
					isolatedStubArray.remove(targetEdgeB);
					int left = isolatedStubArray.get(targetEdgeA);
					isolatedStubArray.remove(targetEdgeA);
					edgeList[currentLine][0] = left;
					edgeList[currentLine][1] = right;
					neightborList[cursor[left]] = right;
					neightborList[cursor[right]] = left;
					neightborIndexList[cursor[right]] = currentLine;
					neightborIndexList[cursor[left]] = currentLine;
					cursor[left]++;
					cursor[right]++;
				}

				disconnectedN -= 2;
				currentLine++;
			}while(disconnectedN>0);
		}


		int fragment_base,fragment_A,fragment_B;
		int baseNode,targetNodeA,targetNodeB;
		boolean conflict;
		disconnectedN=clusterFragmentArray.size();
		success = true;
		if(clusterFragmentArray.size()>0) {
			generateLoop: do{
				nowLoopLimit=loopLimit;
				do{
					fragment_base = rnd.nextInt(clusterFragmentArray.size());
					fragment_A = rnd.nextInt(clusterFragmentArray.size());
					fragment_B = rnd.nextInt(clusterFragmentArray.size());

					baseNode = clusterFragmentArray.get(fragment_base);
					targetNodeA = clusterFragmentArray.get(fragment_A);
					targetNodeB = clusterFragmentArray.get(fragment_B);

					if(nowLoopLimit<=0){
						success = false;
						break generateLoop;
					}
					nowLoopLimit--;

					conflict = (baseNode==targetNodeA || baseNode==targetNodeB || targetNodeA==targetNodeB);
					multiple=false;
					boolean malti_Base_A,malti_Base_B,malti_A_B;
					cheakMultiple:for(int i=0;i<currentLine;i++){
						malti_Base_A = ((baseNode==edgeList[i][0]&&targetNodeA==edgeList[i][1])||
								   (baseNode==edgeList[i][1]&&targetNodeA==edgeList[i][0]));
						malti_Base_B = ((baseNode==edgeList[i][0]&&targetNodeB==edgeList[i][1])||
								   (baseNode==edgeList[i][1]&&targetNodeB==edgeList[i][0]));
						malti_A_B = ((targetNodeA==edgeList[i][0]&&targetNodeB==edgeList[i][1])||
								   (targetNodeA==edgeList[i][1]&&targetNodeB==edgeList[i][0]));

						if(malti_Base_A || malti_Base_B || malti_A_B){
							multiple=true;
							break cheakMultiple;
						}
					}

					multiple=false;
					if(!conflict) {
						cheakMultiple_bA:for(int i=addressList[baseNode] ; i<cursor[baseNode] ; i++){
							if(neightborList[i] == targetNodeA){
								multiple=true;
								break cheakMultiple_bA;
							}
						}
						if(!multiple){
							cheakMultiple_AB:for(int i=addressList[targetNodeA] ; i<cursor[targetNodeA] ; i++){
								if(neightborList[i] == targetNodeB){
									multiple=true;
									break cheakMultiple_AB;
								}
							}
						}
						if(!multiple){
							cheakMultiple_Bb:for(int i=addressList[targetNodeB] ; i<cursor[targetNodeB] ; i++){
								if(neightborList[i] == baseNode){
									multiple=true;
									break cheakMultiple_Bb;
								}
							}
						}
					}

				}while(conflict || multiple);

				int[] fragmentList = new int[3];
				fragmentList[0] = fragment_base;
				fragmentList[1] = fragment_A;
				fragmentList[2] = fragment_B;
				for(int i=0;i<3;i++) {
					int currentFragment1 = fragmentList[i%3];
					int currentFragment2 = fragmentList[(i+1)%3];
					if(currentFragment1 >= currentFragment2){
						int currentNode1 = clusterFragmentArray.get(currentFragment1);
						int currentNode2 = clusterFragmentArray.get(currentFragment2);
						edgeList[currentLine][0] = currentNode2;
						edgeList[currentLine][1] = currentNode1;
						neightborList[cursor[currentNode1]] = currentNode2;
						neightborList[cursor[currentNode2]] = currentNode1;
						neightborIndexList[cursor[currentNode1]] = currentLine;
						neightborIndexList[cursor[currentNode2]] = currentLine;
						cursor[currentNode1]++;
						cursor[currentNode2]++;
					}else{

						int currentNode1 = clusterFragmentArray.get(currentFragment1);
						int currentNode2 = clusterFragmentArray.get(currentFragment2);
						edgeList[currentLine][0] = currentNode1;
						edgeList[currentLine][1] = currentNode2;
						neightborList[cursor[currentNode1]] = currentNode2;
						neightborList[cursor[currentNode2]] = currentNode1;
						neightborIndexList[cursor[currentNode1]] = currentLine;
						neightborIndexList[cursor[currentNode2]] = currentLine;
						cursor[currentNode1]++;
						cursor[currentNode2]++;
					}
					currentLine++;
				}
				Arrays.sort(fragmentList);
				clusterFragmentArray.remove(fragmentList[2]);
				clusterFragmentArray.remove(fragmentList[1]);
				clusterFragmentArray.remove(fragmentList[0]);
			}while(!clusterFragmentArray.isEmpty());
		}

	}

}
