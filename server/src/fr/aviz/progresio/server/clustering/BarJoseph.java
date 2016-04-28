package fr.aviz.progresio.server.clustering;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import fr.aviz.progresio.server.clustering.hac.dendogram.DNode;
import fr.aviz.progresio.server.clustering.hac.dendogram.ObservationNode;

/**
 * optimal dendrogram ordering
 * 
 * implementation of binary tree ordering described in [Bar-Joseph et al., 2003]
 * by Renaud Blanch.

 * [Bar-Joseph et al., 2003]
 * K-ary Clustering with Optimal Leaf Ordering for Gene Expression Data.
 * Ziv Bar-Joseph, Erik D. Demaine, David K. Gifford, Ang��le M. Hamel,
 * Tommy S. Jaakkola and Nathan Srebro
 * Bioinformatics, 19(9), pp 1070-8, 2003
 * http://www.cs.cmu.edu/~zivbj/compBio/k-aryBio.pdf
 */


public class BarJoseph {
    protected DoubleMatrix2D distanceMatrix;
    protected DNode root;
    
    /**
     * Creates a BarJoseph resolver with a specified distance matrix and a root node
     * @param distanceMatrix the distance matrix
     * @param root the root node
     */
    public BarJoseph(DoubleMatrix2D distanceMatrix, DNode root) {
        assert(distanceMatrix.columns() == root.getObservationCount());
        this.distanceMatrix = distanceMatrix;
        this.root = root;
    }
    
    static class NodeList extends IntArrayList {
        public NodeList(int s) { 
            super(s);
        }
        public NodeList(DNode d) {
            super(1);
            add(getObservation(d));
        }
        public NodeList(NodeList l, NodeList r) {
            super(l.size() + r.size());
            addAllOf(l);
            addAllOf(r);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int[] elements() {
            assert(size == elements.length);
            return super.elements();
        }
        
    };
    private static final NodeList EMPTY = new NodeList(0);
    
    static class SimOrder {
        double sim;
        NodeList order;
        
        SimOrder(double s, NodeList o) {
            this.sim = s;
            this.order = o;
        }
    };
    
    private static boolean isLeaf(DNode n) {
        return n instanceof ObservationNode;
//        return n.getLeft() == null && n.getRight() == null;
    }
    
    private static int getObservation(DNode n) {
        if (n != null && n instanceof ObservationNode) {
            ObservationNode on = (ObservationNode) n;
            return on.getObservation();
        }
        throw new RuntimeException("Cannot get observation from a non-observation node");
    }
    

    /**
     * Collect the leaves
     * @param n root node
     * @return the list of leaves
     *
     */
    protected NodeList leaves(DNode n) {
        if (n == null)
            return EMPTY;
        if (isLeaf(n)) {
            return new NodeList(n);
        }
        NodeList l = new NodeList(n.getObservationCount());
        l.addAllOf(leaves(n.getLeft()));
        l.addAllOf(leaves(n.getRight()));
        return l;
    }

    /**
     * Compute the maximal order of v with leftmost leaf i and rightmost leaf j
     * @param v root node
     * @param i leftmost child
     * @param j rightmost child
     * @return MaxList
     */
    protected SimOrder order(DNode v, int i, int j) {
        DNode w, x;
        if (isLeaf(v))
            return new SimOrder(0, new NodeList(v));
        else {
            DNode l = v.getLeft();
            DNode r = v.getRight();
            NodeList L = leaves(v.getLeft());
            NodeList R = leaves(v.getRight());
            
            if (L.contains(i) && R.contains(j)) {
                w = l;
                x = r;
            }
            else if (R.contains(i) && L.contains(j)) {
                w = r;
                x = l;
            }
            else {
                throw new RuntimeException("Node is not common ancestor or "+i+", "+j);
            }
        }
        NodeList Wl = leaves(w.getLeft());
        NodeList Wr = leaves(w.getRight());
        NodeList Ks = Wr.contains(i) ? Wl : Wr;
        if (Ks == EMPTY) {
            Ks = new NodeList(1);
            Ks.add(i);;
        }
        
        NodeList Xl = leaves(x.getLeft());
        NodeList Xr = leaves(x.getRight());
        NodeList Ls = Xr.contains(j) ? Xl : Xr;
        if (Ls == EMPTY) {
            Ls = new NodeList(1);
            Ls.add(j);;
        }
        
        double maximum = Double.POSITIVE_INFINITY;
        NodeList order = EMPTY;
        
        for (int k : Ks.elements()) {
            SimOrder w_maximum = order(w, i, k);
            for (int l : Ls.elements()) {
                SimOrder x_maximum = order(x, l, j);
                double similarity = w_maximum.sim + distanceMatrix.get(k, l) + x_maximum.sim;
                if (similarity < maximum) {
                    maximum = similarity;
                    order = new NodeList(w_maximum.order, x_maximum.order); 
                }
            }
        }
        return new SimOrder(maximum, order);
    }
    
    protected NodeList order(DNode v) {
        double maximum = Double.POSITIVE_INFINITY;
        NodeList optimal_order = EMPTY;
        NodeList left = leaves(v.getLeft());
        NodeList right = leaves(v.getRight());
        
        for (int i : left.elements()) {
            for (int j : right.elements()) {
                SimOrder so = order(v, i, j);
                if (so.sim < maximum) {
                    maximum = so.sim;
                    optimal_order = so.order;
                }
            }
        }
        return optimal_order;
    }
    
    /**
     * @return the optimal leaf order
     */
    public int[] order() {
        NodeList nl = order(root);
        return nl.elements();
    }
    
}
