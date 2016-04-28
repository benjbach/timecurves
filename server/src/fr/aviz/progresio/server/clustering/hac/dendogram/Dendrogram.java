/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package fr.aviz.progresio.server.clustering.hac.dendogram;


/**
 * A Dendrogram represents the results of hierachical agglomerative clustering.
 * The root represents a single cluster containing all observations.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class Dendrogram {

    private final DNode root;


    public Dendrogram(final DNode root) {
        this.root = root;
    }

    public DNode getRoot() {
        return root;
    }

    public void dump() {
        dumpNode("  ", root);
    }

    private void dumpNode(final String indent, final DNode node) {
        if (node==null) {
            System.out.println(indent+"<null>");
        } else if (node instanceof ObservationNode) {
            System.out.println(indent+"Observation: "+ ((ObservationNode)node).getObservation());
        } else if (node instanceof MergeNode) {
            System.out.println(indent+"Merge: " + node);
            dumpNode(indent+"  ", ((MergeNode)node).getLeft());
            dumpNode(indent+"  ", ((MergeNode)node).getRight());
        }
    }
}
