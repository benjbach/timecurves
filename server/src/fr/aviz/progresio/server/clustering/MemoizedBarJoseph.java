package fr.aviz.progresio.server.clustering;

import java.util.HashMap;

import cern.colt.matrix.DoubleMatrix2D;
import fr.aviz.progresio.server.clustering.hac.dendogram.DNode;

/**
 * Class MemoizedBarJoseph
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class MemoizedBarJoseph extends BarJoseph {
    /**
     * @param distanceMatrix
     * @param root
     */
    public MemoizedBarJoseph(DoubleMatrix2D distanceMatrix, DNode root) {
        super(distanceMatrix, root);
    }


    HashMap<DNode, NodeList> leavesMap  = new HashMap<DNode, BarJoseph.NodeList>();
    int leavesMissCount = 0;
    int leavesCallCount = 0;
    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeList leaves(DNode n) {
        leavesCallCount++;
        NodeList ret = leavesMap.get(n);
        if (ret == null) {
            leavesMissCount++;
            ret = super.leaves(n);
            leavesMap.put(n, ret);
        }
        return ret;
    }
    
    static class OrderParams {
        DNode v;
        int i, j;
        
        public OrderParams(DNode v, int i, int j) {
            this.v = v; this.i = i; this.j = j;
        }
        
        public OrderParams(OrderParams other) {
            this.v = other.v; this.i = other.i; this.j = other.j;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OrderParams) {
                OrderParams op = (OrderParams) obj;
                return op.v == v && op.i == i && op.j == j;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return v.hashCode() + i * 1000 + j;
        }
    };
    private OrderParams TMPP = new OrderParams(null, 0, 0);
    
    HashMap<OrderParams, SimOrder> orderMap = new HashMap<MemoizedBarJoseph.OrderParams, BarJoseph.SimOrder>();
    int orderCallCount = 0;
    int orderMissCount = 0;
    /**
     * {@inheritDoc}
     */
    @Override
    protected SimOrder order(DNode v, int i, int j) {
        orderCallCount++;
        TMPP.v = v;
        TMPP.i = i;
        TMPP.j = j;
        SimOrder so = orderMap.get(TMPP);
        if (so == null) {
            OrderParams op = new OrderParams(TMPP);
            orderMissCount++;
            so = super.order(v, i, j);
            orderMap.put(op, so);
        }
        return so;
    }
}
