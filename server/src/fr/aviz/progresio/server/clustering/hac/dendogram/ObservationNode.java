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
 * An ObservationNode represents a leaf node in a Dendrogram.
 * It corresponds to a singleton cluster of one observation.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ObservationNode implements DNode {

	private final int observation;

	
	public ObservationNode(final int observation) {
		this.observation = observation;
	}
	
	public final DNode getLeft() {
		return null;
	}
	
	public final DNode getRight() {
		return null;
	}
	
	public int getObservationCount() {
		return 1;
	}
	
	public final int getObservation() {
		return observation;
	}

}