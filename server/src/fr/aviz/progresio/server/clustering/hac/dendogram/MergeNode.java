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
 * A MergeNode represents an interior node in a Dendrogram.
 * It corresponds to a (non-singleton) cluster of observations.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class MergeNode implements DNode {
	
	private final DNode left;
	private final DNode right;
	private final double dissimilarity;
	private final int observationCount;
	
	
	public MergeNode(final DNode left, final DNode right, final double dissimilarity) {
		this.left = left;
		this.right = right;
		this.dissimilarity = dissimilarity;
		observationCount = left.getObservationCount()+right.getObservationCount();
	}
	
	public int getObservationCount() {
		return observationCount;
	}
	
	public final DNode getLeft() {
		return left;
	}
	
	public final DNode getRight() {
		return right;
	}
	
	public final double getDissimilarity() {
		return dissimilarity;
	}

}