package BuddingTreeMultiClass;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static BuddingTreeMultiClass.SetReader.getGithubDatasetNoTag;
import static BuddingTreeMultiClass.SetReader.getGithubDataset;
import static BuddingTreeMultiClass.SetReader.getGithubDatasetNoTag_v2;

public class Runner {

    public static int similar_count = 5;
    static boolean g_newversion = false;
    public static String toFile = "btm_tag_v2__0.txt";
    public static int[] class_counts = new int[38];
    public static double down_learning_rate = 1;//if 1, works normal
    public static double punish_gama = 0;//if 0, works normal
    public static double w_start_point = 0.00001;

    public static void main(String[] args) throws IOException {
//        ArrayList<Instance>[] sets = getGithubDataset();
        ArrayList<Instance>[] sets = getGithubDatasetNoTag();
//        ArrayList<Instance>[] sets = getGithubDatasetNoTag_v2();

        BTM btm = new BTM(sets[0], sets[1], 1, 100, 0.0001);

        System.out.println(SetReader.tag_size + " " + sets[0].get(0).x.length + " " + sets[0].size() + " " + sets[1].size() + " " + btm.LEARNING_RATE + " " + btm.LAMBDA + " " + g_newversion + " " + down_learning_rate);
        btm.learnTree();
        btm.printToFile(toFile);

//        BTM btm2 = new BTM(sets[0], sets[1], "btm_tag.txt", 0);
//        BTM btm2 = new BTM(sets[0], sets[1], "btm_notag.txt", 0);
//        btm2.treeNodeRoot = new TreeNode();
////        btm2.find_ymeans(sets[1]);
////        System.out.println("Size: " + btm2.size() + " " + btm2.eff_size() + "\n" + btm2.getErrors() + "\n-----------------------\n");
////        btm2.write_ymeans();
////        System.out.println(BTM.ROOT.toStringWeights() + "\n" + BTM.ROOT.leftNode.toStringWeights() + "\n" + BTM.ROOT.rightNode.toStringWeights());
////        System.out.println("\n" + sets[0].get(0).toStringX() + "\n" + sets[0].get(1).toStringX() + "\n" + sets[0].get(2).toStringX());
//
//        class_counts = SetReader.class_counts(sets[0]);
//        for(int i = 0; i< 38; i++)
//            System.out.println(class_counts[i]);
//        btm2.findAllMinDifferences(sets[0]);
//        btm2.findScaledRhos();
//        btm2.findCumulativeG(sets[0]);
//
////        btm2.treeNodeRoot.printToFile("tree_tag.png");
//        btm2.treeNodeRoot.printToFile("tree_notag.png");
//        System.out.println(BTM.ROOT.toStringIndexesAndRhos(0, sets[0]));

//        System.out.println(BTM.ROOT.minDifferences(sets[0]));
//        btm2.followInstance(sets[1].get(0));
//
//        btm2.followInstance(sets[1].get(1));
//
//        btm2.followInstance(sets[1].get(2));
//
//        btm2.followInstance(sets[1].get(3));

//        btm2.learnTree();
    }


}
