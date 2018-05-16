import java.util.ArrayList;

class VertexGroup {
    private ArrayList<Integer> Member = new ArrayList<Integer>();
    private double[][] edgeCost;
    static double Gmin = -Double.MAX_VALUE;

    VertexGroup(double[][] edgeCost, int[] member) {
        this.edgeCost = edgeCost;
        for (int temp : member) {
            this.addMember(temp);
        }
    }

    public ArrayList<Integer> getMember() {
        return this.Member;
    }

    public void addMember(int a) {
        this.Member.add(a);
    }

    public void removeMember(int a) {
        this.Member.remove(this.Member.indexOf(a));
    }

    public double getICost(int a) {
        double Icost = 0.0;
        for (int vertex : Member) {
            if (vertex != a) {
                Icost += this.edgeCost[vertex][a];
            }
        }
        return Icost;
    }

    public double getECost(int a, VertexGroup bGroup) {
        double Ecost = 0.0;
        for (int i = 0; i < this.edgeCost.length; i++) {
            if (this.Member.indexOf(i) == -1 && bGroup.Member.indexOf(i) != -1) {
                // 该元素不在此点集中，但是在另一组点集中
                Ecost += edgeCost[a][i];
            }
        }
        return Ecost;
    }

    public double getDCost(int a, VertexGroup bGroup) {
        return this.getECost(a, bGroup) - this.getICost(a);
    }

    public double getCostOf(int a, int b) {
        return edgeCost[a][b];
    }
}

class Label {
    int a;
    int b;
    double dis;
}

public class KL_algorithm {
    static double[][] edgeCost = {
            {0, 0, 0.5, 0, 0.5, 0, 0, 0},
            {0, 0, 0.5, 0.5, 0, 0, 0, 0},
            {0.5, 0.5, 0, 0.5, 1, 0.5, 0, 0},
            {0, 0.5, 0.5, 0, 0, 1, 0, 0},
            {0.5, 0, 1, 0, 0, 0.5, 1, 0},
            {0, 0, 0.5, 1, 0.5, 0, 0.5, 0.5},
            {0, 0, 0, 0, 1, 0.5, 0, 0.5},
            {0, 0, 0, 0, 0, 0.5, 0.5, 0}
    };
    //    static int[] a = {1, 3, 4, 6};
    //    static int[] b = {2, 5, 7, 8};
//    static int[] a = {0, 2, 3, 5};
//    static int[] b = {1, 4, 6, 7};
    static int[] a = {0, 1, 2, 3};
static int[] b = {5, 4, 6, 7};
    static double Gmin = -Double.MAX_VALUE;

    public static void main(String[] args) {

        // 存放此次交换最大的gain，以及要换的两个节点
        ArrayList<Integer> a_swap = new ArrayList<>();
        ArrayList<Integer> b_swap = new ArrayList<>();
        ArrayList<Double> gain = new ArrayList<Double>();

        do {
            // 每次生成算法需要的两个新group
            VertexGroup aGroup = new VertexGroup(edgeCost, a);
            VertexGroup bGroup = new VertexGroup(edgeCost, b);
            // 清空保存中间数据的ArrayList
            CleanArrayList(a_swap);
            CleanArrayList(b_swap);
            CleanArrayList(gain);

            Label label = new Label();
            // 当两个数组都不为空的时候，进行元素检测，每次选出增益最大的两个元素，进行记录（存入a_swap, b_swap, gain）
            while (aGroup.getMember().size() != 0) {
                label.dis = Gmin;
                /** 打印本次所有的Dcost **/
                System.out.println("\t ------ a group ---------");
                for (int member:aGroup.getMember()) {
                    double Dcost = aGroup.getDCost(member,bGroup);
                    System.out.println("\t\tD" + (member+1) + " = " + Dcost);
                }
                System.out.println("\t ------------------------");
                System.out.println("\t ------ b group ---------");
                for (int member:bGroup.getMember()) {
                    double Dcost = bGroup.getDCost(member,aGroup);
                    System.out.println("\t\tD" + (member+1) + " = " + Dcost);
                }
                System.out.println("\t ------------------------");

                // 选择a∈Ap和b∈Bp使gp=Da+Db-2Cab最大值，将结果存在label里面
                MaxGain(aGroup, bGroup, label);
                // 记录本次的数据
                gain.add(label.dis);
                a_swap.add(label.a);
                b_swap.add(label.b);

                 /*** **/
                // Ap+1<--Ap-{a}     Bp+1<--Bp-{b}
                aGroup.removeMember(label.a);
                bGroup.removeMember(label.b);
                // 输出本次的结果
                System.out.println(label.dis);
                System.out.print((label.a + 1) + "\t");
                System.out.println(label.b + 1);

            }
            System.out.println("*************************");
        } while (Swap(a, b, a_swap, b_swap, gain));             // 如果有gian>0的元素对，则进行交换，进行下一轮循环

        // 输出最后的结果，即交换结束后的两个数组
        System.out.println("a 数组：");
        PrintArrsy(a);
        System.out.println("b 数组：");
        PrintArrsy(b);
        double cost = CostBetween(a, b, edgeCost) ;
        System.out.println("the cost after KL is: " + cost);

    }

    /**
     * 选择当前两个点集中，可获得交换增益最大的两个元素
     *
     * @param aGroup 第一个点集合
     * @param bGroup 第二个点集合
     * @param label  保存当前最大交换增益，以及两个元素
     * @return 存有结果的label
     */
    public static Label MaxGain(VertexGroup aGroup, VertexGroup bGroup, Label label) {
        // 选择a∈Ap和b∈Bp使gp=Da+Db-2Cab最大
        for (int i = 0; i < aGroup.getMember().size(); i++) {
            for (int j = 0; j < bGroup.getMember().size(); j++) {
                int aMember = aGroup.getMember().get(i);
                int bMember = bGroup.getMember().get(j);
                double temp = aGroup.getDCost(aMember, bGroup) + bGroup.getDCost(bMember, aGroup) - 2 * aGroup.getCostOf(aMember, bMember);
                System.out.println("g" + (aMember+1)+(bMember+1) + " = "+ temp);

                if (label.dis < temp) {
                    label.a = aMember;
                    label.b = bMember;
                    label.dis = temp;

                }
            }
        }
        return label;
    }

    /**
     * 在记录中，如果存在交换增益大于0的元素对，则交换这对元素对，返回true；否则，返回false
     *
     * @param a
     * @param b
     * @param a_swap 要交换的a中元素集合
     * @param b_swap 对应的要交换的b中元素集合
     * @param gain   对应的交换增益
     * @return 本次是否有元素交换，即是否有增益大于0的元素
     */
    public static boolean Swap(int[] a, int[] b, ArrayList<Integer> a_swap, ArrayList<Integer> b_swap, ArrayList<Double> gain) {
        boolean gain_test = false;
        double[] gain_sum = new double[ gain.size()];       // 计算不同k值对应的Gain
        int k = 0;                                          // k值，表示前k个元素进行交换
        double Gain = -Double.MAX_VALUE;
        // 计算 前k个 gain 之和
        for (int i = 0; i < gain_sum.length; i++) {
            if (i == 0) {
                gain_sum[i] = gain.get(i);
            } else {
                gain_sum[i] = gain_sum[i - 1] + gain.get(i);
            }
        }

        // 找到最大的 G 和 对应的  k，即对前k个元素进行交换
        for (int i = 0; i < gain_sum.length; i++) {
            if (Gain < gain_sum[i]) {
                Gain = gain_sum[i];
                k = i;
            }
            if (Gain > 0) {
                gain_test = true;
            }
        }

        // 如果前 Gmax > 0， 对应的k个元素进行交换
        if (gain_test) {
            for (int i = 0; i <= k; i++) {
                a[i] = b_swap.get(i);
                b[i] = a_swap.get(i);
                System.out.print("swap " + (a_swap.get(i) + 1) + " and " + (b_swap.get(i) + 1) + ",\t");
                System.out.println("the gain in this swap is: " + gain.get(i));
                System.out.println("***************************");

            }
        }
        return gain_test;
    }

    /**
     * 清空 ArrayList
     *
     * @param arrayList
     */
    //  ArrayList 泛型用？
    public static void CleanArrayList(ArrayList<?> arrayList) {
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            arrayList.remove(i);
        }
    }

    public static void PrintArrsy(int[] a) {
        for (int temp : a
                ) {
            System.out.print((temp + 1) + "\t");
        }
        System.out.println();
    }

    public static double CostBetween(int[]a ,int[] b, double[][] edgeCost){
        double cost = 0.0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                cost += edgeCost[a[i]][b[j]];
            }
        }
        return cost;
    }
}

