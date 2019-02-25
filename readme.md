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
