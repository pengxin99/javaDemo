
import java.util.ArrayList;
import java.util.List;

// 簇类
class CT {
    // 类成员变量，member用来存放簇中的成员；distance用来存放该簇和其他簇的距离
    public static final int INF = 9;
    List<String> member = new ArrayList<>();
    private int[] distance = {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF};
    private boolean isMerged = false;
    private  boolean isLock = false;


    CT(String ID, int[] distance) {
        this.member.add(ID);
        this.distance = distance;
    }

    public int[] getDistance(){
        return this.distance;
    }

    /**
     * 当进行簇合并时，增加簇成员
     * @param otherCT 需要被合并的簇
     * @return
     */
    public boolean memAdd(CT otherCT, int model){
        int size = otherCT.member.size();
        for (int i = 0; i < size; i++) {
            this.member.add(otherCT.member.get(i)) ;

        }
        // 标记被合并
        otherCT.isMerged = true;
        int otherCTId = Integer.parseInt(otherCT.member.get(0));
        int[] tempDis = otherCT.getDistance();
        switch (model) {
            // 单链接
            case 1:
                System.out.println("SINGLE model!");
                for (int i = 0; i < this.distance.length; i++) {
                    this.distance[i] = this.distance[i] < tempDis[i] ? this.distance[i] : tempDis[i];
                }
                break;
            // 全链接
            case 2:
                System.out.println("ALL model!");
                for (int i = 0; i < this.distance.length; i++) {
                    if (this.distance[i] != INF && tempDis[i] != INF){
                        this.distance[i] = this.distance[i] > tempDis[i] ? this.distance[i] : tempDis[i];
                    }
                    else this.distance[i] = this.distance[i] < tempDis[i] ? this.distance[i] : tempDis[i];
                }
                break;
             // 均链接
            case 3:
                System.out.println("AVERGE model!");
                for (int i = 0; i < this.distance.length; i++) {
                    if (this.distance[i] != INF && tempDis[i] != INF){
                        this.distance[i] = (this.distance[i] + tempDis[i]) / 2;
                    }
                    else this.distance[i] = this.distance[i] < tempDis[i] ? this.distance[i] : tempDis[i];
                }
                break;

                default:
                    System.out.println("Model is ERROR!");
        }
        this.distance[otherCTId - 1] = 1000 ;
        return true;
    }

    // 判断簇是否并合并
    public boolean isMerged() {
        if (this.member.size() >= 4) {
//            this.isMerged = true;
            this.isLock = true;
        }
        return isMerged;
    }
    public boolean isLock(){
        return this.isLock;
    }
    // 判断是否含有特定ID的元素
    public boolean hasMemberById(int id){
        return this.member.contains(String.valueOf(id));
    }
    public void setDistance(int otherId){
        this.distance[otherId] = INF;
    }

    public int getSize(){
        return this.member.size();
    }
    public void ShowMem(){
        for (String ID: this.member
             ) {
            System.out.print(ID+" ");
        }
    }
}
// 标记类
class Model {
    int x = 0;
    int y = 0;
    int value = 0;
}

public class Cluster {
    public static final int c = 3; // 最终分为三类
    public static int d = 0;       // 初始距离0
    public static int nummber = 10; // 成员数量
    public static final int INF = 9;
    // 定义连接方式
    public static final int SINGLECONNECT = 1;
    public static final int ALLCONNECT = 2;
    public static final int AVERGECONNECT = 3;
    private static int DistanceNow = 0 ;
    // 初始距离矩阵
    /*
    public static final int[][] DISTANCE = {
            {INF, 1, INF, INF, INF, INF, 4, INF, 2, INF},
            {INF, INF, 4, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, 2, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, 5, 6, INF},
            {INF, INF, INF, INF, INF, INF, INF, 3, INF, INF},
            {INF, INF, INF, 2, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, 1, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, 7, INF, INF, INF, INF, INF},
            {INF, INF, 1, INF, 8, INF, INF, INF, INF, INF}};
*/
    public static final int [][] DISTANCE = {
            {INF, 8, 9, 9, 9, 9, 5, 9, 7, 9},
            {INF, INF, 6, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, 7, INF, 8},
            {INF, INF, INF, INF, INF, 7, INF, 4, 3, INF},
            {INF, INF, INF, INF, INF, INF, INF, 6, 2, 1},
            {INF, INF, INF, INF, INF, INF, 8, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF}};

    public static void main(String[] args) {

        CT one = new CT("1", DISTANCE[0]);
        CT two = new CT("2", DISTANCE[1]);
        CT three = new CT("3", DISTANCE[2]);
        CT four = new CT("4", DISTANCE[3]);
        CT five = new CT("5", DISTANCE[4]);
        CT six = new CT("6", DISTANCE[5]);
        CT seven = new CT("7", DISTANCE[6]);
        CT eight = new CT("8", DISTANCE[7]);
        CT nine = new CT("9", DISTANCE[8]);
        CT ten = new CT("10", DISTANCE[9]);
        CT[] Total = {one, two, three, four, five, six, seven, eight, nine, ten};

        int connectModel = SINGLECONNECT;           // 选择单链接
        while(nummber > 3){
            Model temp = findMinValueOfMatrix(Total) ;
            DistanceNow = temp.value;
            int x = temp.x;
            int y = temp.y;
            merge(Total, x, y, connectModel);
            nummber --;
        }
        // 打印结果+
        for (CT ct:Total) {
            if ( !ct.isMerged() ){
                ct.ShowMem();
                System.out.println();
            }
        }
    }

    /**
     * 找出簇集合中没有被合并的簇中最小距离的值，及对于的簇
     * @param matrix 簇集合
     * @return modle类对象，包含最短距离及距离对应的两个簇
     */
    private static Model findMinValueOfMatrix(CT[] matrix) {    // 找出距离最近的两个簇
        Model model = new Model();
        int min = 0xffff;
        for (int i = 0; i < matrix.length; ++i) {
            // 如果该簇没有被合并，则进行检测
            if (!matrix[i].isMerged() && !matrix[i].isLock()){
                int [] tempDis = matrix[i].getDistance();
                for (int j = 0; j < tempDis.length; ++j) {
                    // 找到当前距离最小的两个簇，检测是否符合要求
                    CT X = findCTbyID(matrix,i+1);
                    CT Y = findCTbyID(matrix,j+1);
                    // 空簇、其中一个的数量超过4或者 两个簇合并后数量超过4，都认为不合格，则继续寻找
                    if ( Y == null || Y.isLock() || (X.getSize()+Y.getSize()) > 4)
                        continue;
                    // 如果两个簇合格，则记录下来，作为目前最小距离
                    if (min > tempDis[j] && tempDis[j] != 0) {
                        min = tempDis[j];
                        model.x = i + 1;
                        model.y = j + 1;
                        model.value = min;
                    }
                }
            }
        }
        return model;
    }

    /**
     * 找到包含特定成员的簇员
     * @param total 簇集合
     * @param id 特定成员ID，具体为id
     * @return 包含 id 成员的簇
     */
    public static CT findCTbyID(CT[] total, int id){
        int i = 0;
        for (; i < total.length; i++) {
            // 现在没有被合并的簇中找到包含 id 的，有且仅有一个
            if ( !total[i].isMerged() && total[i].hasMemberById(id)){
                break;
            }
        }
        if (i == 10) return null;
        else    return total[i];
    }

    /**
     * 合并两个簇
     * @param total 簇集合
     * @param x 要合并的第一个簇ID
     * @param y 要合并的第二个簇ID
     * @param conModel
     */
    public static void merge(CT[] total, int x, int y, int conModel){
        // 先找到要合并的簇，然后进行合并，合并无顺序
        CT X = findCTbyID(total,x);
        CT Y = findCTbyID(total,y);
        X.memAdd(Y, conModel) ;
    }

}
