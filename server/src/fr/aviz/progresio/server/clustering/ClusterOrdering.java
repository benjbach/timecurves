package fr.aviz.progresio.server.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import cern.colt.matrix.DoubleMatrix2D;
import fr.aviz.progresio.server.clustering.hac.HierarchicalAgglomerativeClusterer;
import fr.aviz.progresio.server.clustering.hac.agglomeration.CompleteLinkage;
import fr.aviz.progresio.server.clustering.hac.dendogram.DNode;
import fr.aviz.progresio.server.clustering.hac.dendogram.Dendrogram;
import fr.aviz.progresio.server.clustering.hac.dendogram.DendrogramBuilder;
import fr.aviz.progresio.server.clustering.hac.experiment.DissimilarityMeasure;
import fr.aviz.progresio.server.clustering.hac.experiment.Experiment;

public class ClusterOrdering extends MatrixOrdering implements Experiment, DissimilarityMeasure{

	private int vertexCount;
	private DoubleMatrix2D distanceMatrix;
	private HashMap<DNode, ArrayList<DNode>> leafMap = new HashMap<DNode, ArrayList<DNode>>();
	
	@Override
	public int[] order(DoubleMatrix2D distanceMatrix) 
	{
		this.vertexCount = distanceMatrix.rows();
		this.distanceMatrix = distanceMatrix;
		
		// Create hierarchical clustering
		DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(vertexCount);
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(this, this, new CompleteLinkage());
		clusterer.cluster(dendrogramBuilder);
		Dendrogram d = dendrogramBuilder.getDendrogram();
		
		DNode root = d.getRoot();

		long time = System.currentTimeMillis();
		MemoizedBarJoseph mbj = new MemoizedBarJoseph(distanceMatrix, root);
		int[] finalOrder = mbj.order();
		
		return finalOrder;
	}


	
	@Override
	public int getNumberOfObservations() {
		return vertexCount;
	}

	@Override
	public double computeDissimilarity(Experiment experiment, int v1, int v2) {
		return distanceMatrix.get(v1, v2);
	}

}
