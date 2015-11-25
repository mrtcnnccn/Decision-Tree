package SDT;


import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static SDT.Util.dotProduct;
import static SDT.Util.rand;
import static SDT.Util.sigmoid;

class Node {
    int ATTRIBUTE_COUNT;
    Node parent = null;
    Node leftNode = null;
    Node rightNode = null;
    boolean isLeaf = true;
    boolean isLeft;
    double w0;
    double[] w;
    static boolean hardInit = false;

    double y;
    double g;

    Node(int attribute_count) {
        ATTRIBUTE_COUNT = attribute_count;
    }


    public double F(Instance instance) {
        if (isLeaf)
            y = w0;
        else {
            g = sigmoid(dotProduct(w, instance.attributes) + w0);
            y = g * (leftNode.F(instance)) + (1 - g) * (rightNode.F(instance));
        }
        return y;

    }

    int size() {
        if (isLeaf)
            return 1;
        else
            return 1 + leftNode.size() + rightNode.size();
    }

    void learnParameters(ArrayList<Instance> X, ArrayList<Instance> V, double alpha, SDT tree, int MAX_EPOCH) {
        double u = 0.1;

        double[] dw = new double[ATTRIBUTE_COUNT];
        double[] dwp = new double[ATTRIBUTE_COUNT];
        Arrays.fill(dw, 0);
        Arrays.fill(dwp, 0);
        double dw10p = 0, dw20p = 0, dw0p = 0;
        double dw10, dw20, dw0;


        for (int e = 0; e < MAX_EPOCH; e++) {
            ArrayList<Integer> indices = new ArrayList<>();
            for (int i = 0; i < X.size(); i++) indices.add(i);
            Collections.shuffle(indices);
            for (int i = 0; i < X.size(); i++) {
                int j = indices.get(i);
                double[] x = X.get(j).attributes;
                double r = X.get(j).classValue;
                double y = tree.eval(X.get(j));
                double d = y - r;

                double t = alpha * d;
                Node m = this;
                Node p;

                while (m.parent != null) {
                    p = m.parent;
                    if (m.isLeft)
                        t *= p.g;
                    else
                        t *= (1 - p.g);
                    m = m.parent;
                }

                for (int count = 0; count < ATTRIBUTE_COUNT; count++)
                    dw[count] = (-t * (leftNode.y - rightNode.y) * g * (1 - g)) * x[count];

                dw0 = (-t * (leftNode.y - rightNode.y) * g * (1 - g));
                dw10 = -t * (g);
                dw20 = -t * (1 - g);


                for (int count = 0; count < ATTRIBUTE_COUNT; count++)
                    w[count] += dw[count] + u * dwp[count];

                w0 += dw0 + u * dw0p;
                leftNode.w0 += dw10 + u * dw10p;
                rightNode.w0 += dw20 + u * dw20p;

                dwp = dw;
                dw0p = dw0;
                dw10p = dw10;
                dw20p = dw20;

                alpha *= 0.9999;


            }
        }
    }

    void splitNode(ArrayList<Instance> X, ArrayList<Instance> V, SDT tree) {
        double err = tree.ErrorOfTree(V);

        double oldw0 = w0;

        isLeaf = false;
        w = new double[ATTRIBUTE_COUNT];

        leftNode = new Node(ATTRIBUTE_COUNT);
        leftNode.isLeft = true;
        leftNode.parent = this;


        rightNode = new Node(ATTRIBUTE_COUNT);
        rightNode.isLeft = false;
        rightNode.parent = this;

        double[] bestw = new double[ATTRIBUTE_COUNT];
        double bestw0 = 0, bestw0l = 0, bestw0r = 0;
        double bestErr = 1e10;
        double newErr;


        double alpha;
        for (int t = 0; t < tree.MAX_STEP; t++) {
            if(hardInit)
                hardinit(X, V);
            else {
                for (int i = 0; i < ATTRIBUTE_COUNT; i++)
                    w[i] = rand(-0.005, 0.005);
                w0 = rand(-0.005, 0.005);
                leftNode.w0 = rand(-0.005, 0.005);
                rightNode.w0 = rand(-0.005, 0.005);
            }

            alpha = (tree.LEARNING_RATE + 0.0) / Math.pow(2, t + 1);
            learnParameters(X, V, alpha, tree, tree.EPOCH);

            newErr = tree.ErrorOfTree(V);

            if (newErr < bestErr) {

                bestw = Arrays.copyOf(w, w.length);
                bestw0 = w0;
                bestw0l = leftNode.w0;
                bestw0r = rightNode.w0;
                bestErr = newErr;
            }
        }


        w = bestw;
        w0 = bestw0;
        leftNode.w0 = bestw0l;
        rightNode.w0 = bestw0r;

        if (bestErr + 1e-3 < err) {
            SDT.split_q.add(leftNode);
            SDT.split_q.add(rightNode);
//            leftNode.splitNode(X, V, tree);
//            rightNode.splitNode(X, V, tree);
        } else {
            isLeaf = true;
            leftNode = null;
            rightNode = null;
            w0 = oldw0;
            y = w0;
        }
    }

    public void hardinit(ArrayList<Instance> X, ArrayList<Instance> V){
        ArrayList<Double> sv = new ArrayList<>();
        double total=0;


        // (1) compute soft memberships
        for (int j = 0; j < X.size(); j++) {
            double t = 1;
            Node m = this;
            Node p;

            while(m.parent != null) {
                p = m.parent;
                if (m.isLeft)
                    t *= sigmoid(dotProduct(p.w, X.get(j).attributes) + p.w0);
                else
                    t *= (1-sigmoid(dotProduct(p.w, X.get(j).attributes) + p.w0));
                m = m.parent;
            }
            sv.add(t);
            total += t;
        }

        if (total <= 1) { // not enough data, init randomly
            w = new double[X.get(0).attributes.length];
            for (int i=0; i < w.length; i++)
                w[i] = rand(-0.005, 0.005);
            w0 = rand(-0.005, 0.005);
            leftNode.w0 = rand(-0.005, 0.005);
            rightNode.w0 = rand(-0.005, 0.005);
            return;
        }

        int dim, bestDim=-1;
        double errBest = -1;
        double bestSplit = 0;
        double bestw10 = 0, bestw20 = 0;
        ArrayList<Double> bestw1 = new ArrayList<>();
        ArrayList<Double> bestw2 = new ArrayList<>();

        // (2) look for the best hard split
        for (dim=0; dim < X.get(0).attributes.length; dim++)
        {
            ArrayList<Pair<Double,Integer>> f = new ArrayList<>();

            double[] atts = new double[X.size()];
            for (int i=0; i < X.size(); i++)
                atts[i] = X.get(i).attributes[dim];
            Util.ArrayIndexComparator comparator = new Util.ArrayIndexComparator(atts);
            Integer[] indexes = comparator.createIndexArray();

            Arrays.sort(indexes, comparator);

            for (int i=0; i < X.size(); i++)
                f.add(new Pair(X.get(indexes[i]).attributes[dim],indexes[i]));



            double sp;
            for (int i=0; i < f.size()-1; i++) {

                if (f.get(i).getKey() == f.get(i + 1).getKey()) continue;
                sp = 0.5 * (f.get(i).getKey() + f.get(i).getKey());

                double w10,w20,left,right,lsum,rsum;

                w10 = w20 = lsum = rsum = 0;
                for (int j = 0; j <= i; j++) {
                    w10 += X.get(f.get(j).getValue()).classValue * sv.get(f.get(j).getValue());
                    lsum += sv.get(f.get(j).getValue());
                }
                w10 /= lsum;

                for (int j=i+1; j < f.size(); j++) {
                    w20 += X.get(f.get(j).getValue()).classValue * sv.get(f.get(j).getValue());
                    rsum += sv.get(f.get(j).getValue());
                }
                w20 /= rsum;

                // weighted MSE for regression and
                // weighted Gini Impurity for classification
                double errl = 0, errr = 0;
                for (int j=0; j <= i; j++)
                    errl += (w10 - X.get(f.get(j).getValue()).classValue)*(w10 - X.get(f.get(j).getValue()).classValue)*sv.get(f.get(j).getValue());
                errl /= lsum;
                for (int j=i+1; j < f.size(); j++)
                    errr += (w20 - X.get(f.get(j).getValue()).classValue)*(w20 - X.get(f.get(j).getValue()).classValue)*sv.get(f.get(j).getValue());
                errr /= rsum;

                double a = lsum/(lsum+rsum+0.0);
                double b = rsum/(lsum+rsum+0.0);

                if (a*errl + b*errr < errBest || errBest == -1) {
                    bestSplit = sp;
                    bestDim = dim;
                    errBest = a*errl + b*errr;
                    bestw10 = w10;
                    bestw20 = w20;
                    //cout << errbest << endl;
                }
            }
        }

        // (3) init params according to best hard split

        w = new double[X.get(0).attributes.length];
        for (int i = 0; i < w.length; i++)
            w[i] = rand(-0.005, 0.005);
        w[bestDim] = -0.5;
        w0 = bestSplit*0.5;
        leftNode.w0 = bestw10;
        rightNode.w0 = bestw20;
    }

    public String toString(int tab) {
        String s = "";
        for (int i = 0; i < tab; i++) {
            s += "\t";
        }
        if (isLeaf)
            s += "LEAF";
        else {
            s += "NODE" + "\n";
            s += this.leftNode.toString(tab + 1) + "\n";
            s += this.rightNode.toString(tab + 1);
        }
        return s;
    }
}