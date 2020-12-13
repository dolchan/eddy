package eddy;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamTokenizer;

import bayesiannetwork.Edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class StructureProbabilityDistributionEvaluator {
	public boolean isConsideringMarkovBlanket;
	public boolean isConsideringAllStructure;
	public double independencyPValueThreshold;
	public Data data;
	public VariableSet allVariable;
	public VariableSet targetGeneSetVariable;
	public SampleClass sampleClass;
	public Data[] classData;
	public int numberOfProbableStructure;
	public double equivalentSampleSize;
	public double probabilityPowerFactor = 1;
	public int maxParentAmount = -1;	// -1 represents no limitation.
    public boolean edgeDirFixed = false;
    public boolean includeNeighbor = false;
    public boolean priorDirectionality = true;
	public DependencyStructureSet dependencyStructureSet = null;
	public DependencyStructureMap priorMap;
	public double[][] distribution;
	public double[][][] connectionProbability;
	double connProbThreshold = 0.8;    // TODO: need getter/setter.
	double priorWeight = 1.0;
	double resamplingRate = 0.8;
	int	nAdditionalStructures = 0;     // TODO: does not seem to work... no use for now.
	long starttime, endtime, starttime_internal, endtime_internal;
	long tally_probs, tally_nets;
	
	public StructureProbabilityDistributionEvaluator(
			boolean isConsideringMarkovBlanket, 
			boolean isConsideringAllStructure, 
			double independencyPValueThreshold,
			Data data, 
			VariableSet allVariable, 
			VariableSet targetGeneSetVariable, 
			SampleClass sampleClass, 
			int numberOfProbableStructure, 
			double equivalentSampleSize, 
			double priorWeight,
			boolean edgeDirFixed,
			boolean includeNeighbor,
			boolean priorDirectionality,
			double resamplingRate) {
		// TODO Auto-generated constructor stub
		this.isConsideringMarkovBlanket = isConsideringMarkovBlanket;
		this.isConsideringAllStructure = isConsideringAllStructure;
		this.independencyPValueThreshold = independencyPValueThreshold;
		this.data = data;
		this.allVariable = allVariable;
		this.targetGeneSetVariable = targetGeneSetVariable;
		this.sampleClass = sampleClass;
		this.numberOfProbableStructure = numberOfProbableStructure;
		this.equivalentSampleSize = equivalentSampleSize;
		this.priorWeight = priorWeight;
		this.edgeDirFixed = edgeDirFixed;
		this.includeNeighbor = includeNeighbor;
		this.priorDirectionality = priorDirectionality;
		this.resamplingRate = resamplingRate;
		priorMap = new DependencyStructureMap(targetGeneSetVariable.priorSize);
	}
	
	public StructureProbabilityDistributionEvaluator(
			boolean isConsideringMarkovBlanket,
			boolean isConsideringAllStructure, 
			double independencyPValueThreshold, 
			Data data, 
			VariableSet allVariable, 
			VariableSet targetGeneSetVariable, 
			SampleClass sampleClass, 
			int numberOfProbableStructure, 
			double equivalentSampleSize, 
			double probabilityPowerFactor, 
			int maxParentAmount,
			double priorWeight,
			boolean edgeDirFixed,
			boolean includeNeighbor,
			boolean priorDirectionality,
			double resamplingRate) {
		// TODO Auto-generated constructor stub
		this.isConsideringMarkovBlanket = isConsideringMarkovBlanket;
		this.isConsideringAllStructure = isConsideringAllStructure;
		this.independencyPValueThreshold = independencyPValueThreshold;
		this.data = data;
		this.allVariable = allVariable;
		this.targetGeneSetVariable = targetGeneSetVariable;
		this.sampleClass = sampleClass;
		this.numberOfProbableStructure = numberOfProbableStructure;
		this.equivalentSampleSize = equivalentSampleSize;
		this.probabilityPowerFactor = probabilityPowerFactor;
		this.priorWeight = priorWeight;
		this.maxParentAmount = maxParentAmount;		
		this.edgeDirFixed = edgeDirFixed;
		this.includeNeighbor = includeNeighbor;
		this.priorDirectionality = priorDirectionality;
		this.resamplingRate = resamplingRate;
		priorMap = new DependencyStructureMap(targetGeneSetVariable.priorSize);
	}
	
	public boolean writeNetworkToFile(String geneSetName) {
		if (dependencyStructureSet == null)
			return false;
		
		String summaryFileName = geneSetName + "_summary.txt";
		DependencyStructure original = dependencyStructureSet.get(0);
		int priorIndex1, priorIndex2;
		
		// Writing a summary file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFileName));
			bw.write("Network structure");
			
			for (int i = 0; i < sampleClass.numberOfClass; i++)
				bw.write("\tProbability in " + sampleClass.uniqueClassLabel.get(i));
			
			bw.write("\n");
			
			for (int i = 0; i < dependencyStructureSet.size(); i++) {
				bw.write("Network " + (i + 1));
				
				for (int j = 0; j < sampleClass.numberOfClass; j++)
					bw.write("\t" + distribution[j][i]);
				
				bw.write("\n");
			}
			
			bw.close();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		// Writing network files
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(geneSetName + "_Network" + ".txt"));
			System.out.println("Number of unique networks: " + dependencyStructureSet.size());

			for (int i = 0; i < dependencyStructureSet.size(); i++) {
	
				bw.write("Network" + (i+1) + "\n");
				bw.write("===============\n");
				DependencyStructure s = dependencyStructureSet.get(i);
				Vector<Edge> edge = s.edge;
				
				for (int j = 0; j < edge.size(); j++) {
					Edge e = edge.get(j);
					bw.write(e.parent.name + "\t" + e.child.name + "\n");
				}
				
				bw.write("\n\n");
			}
			
			bw.close();
			
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		// Writing connection probability matrix file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(geneSetName + "_adjacencyMatrix.txt"));
			
			for (int classIndex = 0; classIndex < sampleClass.numberOfClass; classIndex++) {
				if (classIndex != 0)
					bw.write("\n");
				
				bw.write(sampleClass.getClassName(classIndex) + "\n");
				bw.write("Gene");

				for (int i = 0; i < targetGeneSetVariable.size(); i++)
					bw.write("\t" + targetGeneSetVariable.get(i).name);

				bw.write("\n");

				for (int i = 0; i < targetGeneSetVariable.size(); i++) {
					bw.write(targetGeneSetVariable.get(i).name);
					
					for (int j = 0; j < targetGeneSetVariable.size(); j++)
						bw.write("\t" + connectionProbability[classIndex][i][j]);
					
					bw.write("\n");
				}
			}
			
			// Writing a summary matrix for the case of two classes
			if (sampleClass.numberOfClass == 2) {
				bw.write("\n");
				bw.write("Summary\n");
				bw.write("Gene");

				for (int i = 0; i < targetGeneSetVariable.size(); i++)
					bw.write("\t" + targetGeneSetVariable.get(i).name);

				bw.write("\n");

				for (int i = 0; i < targetGeneSetVariable.size(); i++) {
					bw.write(targetGeneSetVariable.get(i).name);

					for (int j = 0; j < targetGeneSetVariable.size(); j++) {
						priorIndex1 = targetGeneSetVariable.priorIndexOf(targetGeneSetVariable.get(i).name);
						priorIndex2 = targetGeneSetVariable.priorIndexOf(targetGeneSetVariable.get(j).name);
						if (connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]) && connectionProbability[1][i][j] < connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]))
							bw.write("\t" + sampleClass.getClassName(0));
						else if (connectionProbability[0][i][j] < connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]) && connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]))
							bw.write("\t" + sampleClass.getClassName(1));
						else if (connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]) && connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight*priorMap.map[priorIndex1][priorIndex2]))
							bw.write("\tBoth");
						else
							bw.write("\tNone");
					}
					
					bw.write("\n");
				}
			}
			
			bw.close();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		// Write the list of edges, only for the case of two classes
		if (sampleClass.numberOfClass == 2) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(geneSetName + "_EdgeList.txt"));

				for (int i = 0; i < targetGeneSetVariable.size() - 1; i++) {
					for (int j = i + 1; j < targetGeneSetVariable.size(); j++) {
						priorIndex1 = targetGeneSetVariable.priorIndexOf(targetGeneSetVariable.get(i).name);
						priorIndex2 = targetGeneSetVariable.priorIndexOf(targetGeneSetVariable.get(j).name);
						int priorval = priorMap.map[priorIndex1][priorIndex2];
						int priorval2 = priorMap.map[priorIndex2][priorIndex1];
						int net0val = original.structureMap.map[i][j];
						if ((priorval==0)&&(priorval2==0)) {
						if ((connectionProbability[0][i][j] >= connProbThreshold) && (connectionProbability[1][i][j] < connProbThreshold)) {
							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(0));
							bw.write("\tNONE\n");
						}
						else if ((connectionProbability[0][i][j] < connProbThreshold) && (connectionProbability[1][i][j] >= connProbThreshold)) {
							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(1));
							bw.write("\tNONE\n");
						}
						else if ((connectionProbability[0][i][j] >= connProbThreshold) && (connectionProbability[1][i][j] >= connProbThreshold)) {
							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\tBoth");
							bw.write("\tNONE\n");
						}
						}
						else {
							if (priorval==1) {
							if ((connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] < connProbThreshold*(1-priorWeight))) {
								bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(0));
								if (priorval==1) 
									bw.write("\tPRIOR");
								else bw.write("\tNONE");
								bw.write("\n");
							}
							else if ((connectionProbability[0][i][j] < connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight))) {
								bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(1));
								if (priorval==1) 
									bw.write("\tPRIOR");
								else bw.write("\tNONE");
								bw.write("\n");
							}
							else if ((connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight))) {
								bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\tBoth");
								if (priorval==1) 
									bw.write("\tPRIOR");
								else bw.write("\tNONE");
								bw.write("\n");
							}
							else if (priorval==1) {
								bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name);
								bw.write("\tNeither\tPRIOR\n");
							}
							}
							if (priorDirectionality && (priorval2==1)) {
								if ((connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] < connProbThreshold*(1-priorWeight))) {
									bw.write(targetGeneSetVariable.get(j).name + "\t" + targetGeneSetVariable.get(i).name + "\t" + sampleClass.getClassName(0));
									if (priorval2==1) 
										bw.write("\tPRIOR");
									else bw.write("\tNONE");
									bw.write("\n");
								}
								else if ((connectionProbability[0][i][j] < connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight))) {
									bw.write(targetGeneSetVariable.get(j).name + "\t" + targetGeneSetVariable.get(i).name + "\t" + sampleClass.getClassName(1));
									if (priorval2==1) 
										bw.write("\tPRIOR");
									else bw.write("\tNONE");
									bw.write("\n");
								}
								else if ((connectionProbability[0][i][j] >= connProbThreshold*(1-priorWeight)) && (connectionProbability[1][i][j] >= connProbThreshold*(1-priorWeight))) {
									bw.write(targetGeneSetVariable.get(j).name + "\t" + targetGeneSetVariable.get(i).name + "\tBoth");
									if (priorval2==1) 
										bw.write("\tPRIOR");
									else bw.write("\tNONE");
									bw.write("\n");
								}
								else if (priorval2==1) {
									bw.write(targetGeneSetVariable.get(j).name + "\t" + targetGeneSetVariable.get(i).name);
									bw.write("\tNeither\tPRIOR\n");
								}
							}
						}
					}
				}
								
				
//				for (int i = 0; i < targetGeneSetVariable.size() - 1; i++) {
//					for (int j = i + 1; j < targetGeneSetVariable.size(); j++) {
//						if (connectionProbability[0][i][j] >= connProbThreshold && connectionProbability[1][i][j] < connProbThreshold)
//							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(0) + "\n");
//						else if (connectionProbability[0][i][j] < connProbThreshold && connectionProbability[1][i][j] >= connProbThreshold)
//							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\t" + sampleClass.getClassName(1) + "\n");
//						else if (connectionProbability[0][i][j] >= connProbThreshold && connectionProbability[1][i][j] >= connProbThreshold)
//							bw.write(targetGeneSetVariable.get(i).name + "\t" + targetGeneSetVariable.get(j).name + "\tBoth\n");
//						else
//							;
//					}
//				}
				

				bw.close();
			} catch (Exception e) {
				System.out.println(e);
				System.exit(-1);
			}
		}
		
		return true;
	}
	
	public double[][] getProbabilityDistribution() {
		// Split "data" to each target class data.
		classData = new Data[sampleClass.numberOfClass];
		initializePriorMap();

		for (int i = 0; i < sampleClass.numberOfClass; i++) {
			String c = sampleClass.uniqueClassLabel.get(i);
			classData[i] = sampleClass.getClassDataFrom(data, c);
		}
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			for (int i = 0; i < sampleClass.numberOfClass; i++)
				System.out.println("Class " + (i + 1) + " samples: " + classData[i].numberOfSample);
		}
		
		// Find MBs for each class
		VariableSet[] classMarkovBlanketUnion = new VariableSet[sampleClass.numberOfClass];
		
		if (isConsideringMarkovBlanket) {
			for (int i = 0; i < classData.length; i++) {	// Find Markov blankets for each class.
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Finding Markov Blanket variables for class " + (i + 1));
				
				// This is for ith class.
				MarkovBlanket[] MB = new MarkovBlanket[targetGeneSetVariable.size()];
				
				// Reload data to variables for this class.
				allVariable.reloadData(classData[i]);
				
				// Find a Markov blanket for each target variable.
				for (int j = 0; j < targetGeneSetVariable.size(); j++) {
					Variable T = targetGeneSetVariable.get(j);
					
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.print("Searching Markov Blanket for " + T.name + "...");
					
					MarkovBlanketFinder MBFinder_T = new MarkovBlanketFinder(T, allVariable, independencyPValueThreshold);
					MB[j] = MBFinder_T.findMB();
					
					if (RuntimeConfiguration.IS_DEBUG_MODE) {
						System.out.print(" (" + MB[j].size() + "):");
						
						for (int k = 0; k < MB[j].size(); k++)
							System.out.print(" " + MB[j].get(k).name);
						
						System.out.println("");
					}
				}
				
				// Make union of all MBs
				classMarkovBlanketUnion[i] = new VariableSet();
				
				for (int j = 0; j < MB.length; j++)
					classMarkovBlanketUnion[i].beUnionWith(MB[j]);
				
				// Get rid of target variables from MB
				for (int j = classMarkovBlanketUnion[i].size() - 1; j >= 0; j--) {
					Variable v = classMarkovBlanketUnion[i].get(j);
					
					if (targetGeneSetVariable.contains(v)) {
						classMarkovBlanketUnion[i].remove(j);
						
						if (RuntimeConfiguration.IS_DEBUG_MODE)
							System.out.println("Removing target variable " + v.name + " from MB.");
					}
				}
				
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Number of all Markov Blankets consolidated: " + classMarkovBlanketUnion[i].size());
			}
		}
		else {	// Without considering Markov blankets,
			for (int i = 0; i < classData.length; i++)
				classMarkovBlanketUnion[i] = new VariableSet();	// They are set empty.
		}
		
		// Collect structures.
		dependencyStructureSet = new DependencyStructureSet();
		connectionProbability = new double[sampleClass.numberOfClass][][];
		
		if (isConsideringAllStructure) {	// Collect all possible structures for "targetGeneSetVariable".
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print("Collecting all possible dependency structures");
			
			DependencyStructure currentStructure;
			
			if (maxParentAmount != -1)
				currentStructure = new DependencyStructure(targetGeneSetVariable, maxParentAmount);	// Begin with a structure having no connection.
			else
				currentStructure = new DependencyStructure(targetGeneSetVariable);
			
			while (currentStructure != null) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.print(".");
				
				dependencyStructureSet.add(currentStructure);
				currentStructure = currentStructure.nextIterationStructure();
			}
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println(dependencyStructureSet.size() + " structures collected.");
		}
		// TODO: network proposed using LOO strategy
		// 		AUTHOR: Seungchan Kim
		else if (numberOfProbableStructure == -1) { // leave one out strategy

			if (RuntimeConfiguration.IS_DEBUG_MODE) 
				System.out.println("Collecting " + (sampleClass.classLabel.length + 2) + " structures using LOO.");
			
			for (int i = 0; i < sampleClass.numberOfClass; i++) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Collecting for class " + (i + 1) + "...");
				
//				System.out.println("Class " + i + "\n"); 
				tally_probs=0;
				tally_nets=0;
				starttime = System.currentTimeMillis();
				ProbableStructureProposer proposer = new ProbableStructureProposer(targetGeneSetVariable, classData[i], probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
				connectionProbability[i] = proposer.connectionProposalProbability;
				endtime = System.currentTimeMillis() - starttime;
				tally_probs+=endtime;
//				System.out.println("Total time edge prob: " + endtime + "ms \n"); 				

				// Collect "the most probable" structure from the full data set. 
				starttime = System.currentTimeMillis();
//				System.out.println("Network #0\n");
				dependencyStructureSet.add(proposer.proposeBest(connProbThreshold,priorWeight,priorMap)); 	
				endtime = System.currentTimeMillis() - starttime;
				tally_nets+=endtime;
//				System.out.println("Total time constructing network: " + endtime + "ms \n");					
				
				int nSamples = classData[i].numberOfSample;
				if (nSamples < 6) {
					System.out.println("Exiting: There must be at least 6 samples in each of the classes.");
					System.exit(-1);
				}
				// initializing subsetSamples to be included, leaving out the first sample;
				int subsetSamples[] = new int[nSamples - 1]; 
				int leftOut = 0;
				for (int j = 0; j < nSamples-1; j++) {
					subsetSamples[j] = j + 1;
				}
				starttime = System.currentTimeMillis();		
				starttime_internal = System.currentTimeMillis();	
				ProbableStructureProposer proposerLOO_1 = new ProbableStructureProposer(targetGeneSetVariable, classData[i].getSubsetSamples(subsetSamples), probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
				endtime_internal = System.currentTimeMillis() - starttime_internal;
				tally_probs+=endtime_internal;
//				System.out.println("Total time edge prob L00_1: " + endtime_internal + "ms \n");
				starttime_internal = System.currentTimeMillis();
//				System.out.println("Network #1\n");
//				dependencyStructureSet.add(proposerLOO_1.proposeBest(connProbThreshold,priorWeight,priorMap));
				DependencyStructure s2 = proposerLOO_1.proposeBest(connProbThreshold,priorWeight,priorMap);
				if (!dependencyStructureSet.contains(s2)) {
					dependencyStructureSet.add(s2);			
//					System.out.println("Added L00 \n");
				}
				endtime_internal = System.currentTimeMillis() - starttime_internal;
				tally_nets+=endtime_internal;
//				System.out.println("Total time construct network L00_1: " + endtime_internal + "ms \n");
				
				// SK: adding some additional structures to account for directional changes
				for (int j = 0; j < nAdditionalStructures; j++ ) {
					DependencyStructure s = proposerLOO_1.proposeBest(connProbThreshold,priorWeight,priorMap);
					
					if (!dependencyStructureSet.contains(s))
						dependencyStructureSet.add(s);
				}
				
				for (int j = 0; j < nSamples-1; j++) {
					int tmpSwap = leftOut;       // swapping out a sample with leftOut
					leftOut = subsetSamples[j];
					subsetSamples[j] = tmpSwap;
//					System.out.println("Network #" + (j+2) + "\n");
					
					// TODO: this has to be more efficient -- copying all variables is not
					//       should be able to copy only target variables.
					starttime_internal = System.currentTimeMillis();
					ProbableStructureProposer proposerLOO = new ProbableStructureProposer(targetGeneSetVariable, classData[i].getSubsetSamples(subsetSamples), probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
					endtime_internal = System.currentTimeMillis() - starttime_internal;
					tally_probs+=endtime_internal;
//					System.out.println("Total time edge prob L00." + j + ": " + endtime_internal + "ms \n");
					starttime_internal = System.currentTimeMillis();
					DependencyStructure s = proposerLOO.proposeBest(connProbThreshold,priorWeight,priorMap);
					endtime_internal = System.currentTimeMillis() - starttime_internal;
					tally_nets+=endtime_internal;
//					System.out.println("Total time constructing network L00." + j + ": " + endtime_internal + "ms \n");
					
					if (!dependencyStructureSet.contains(s)) {
						dependencyStructureSet.add(s);
//						System.out.println("Added L0" + j + "\n");
					}
					// SK: adding some additional structures to account for directional changes
					for (int k = 0; k < nAdditionalStructures; k++ ) {
						s = proposerLOO_1.proposeBest(connProbThreshold,priorWeight,priorMap);
						
						if (!dependencyStructureSet.contains(s))
							dependencyStructureSet.add(s);
					}
				}
				endtime = System.currentTimeMillis() - starttime;
//				System.out.println("Final collected structures: " + dependencyStructureSet.size());
//				System.out.println("Total time all networks:" + endtime + "ms.  Edge probs " + tally_probs + "ms.  Nets " + tally_nets + "ms\n");
			}

			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("Final collected structures: " + dependencyStructureSet.size());
		}
		else {	// Only "numberOfProbableStructure" will be collected. For each class, "numberOfProbableStructure"/number_of_class probable structures are collected.
/*			int amountOfStructurePerClass = numberOfProbableStructure/sampleClass.numberOfClass;
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("Collecting " + amountOfStructurePerClass + "*" + sampleClass.numberOfClass + "...");
			
			// Collect "amountOfStructurePerClass" structures for each class.
			for (int i = 0; i < sampleClass.numberOfClass; i++) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Collecting for class " + (i + 1) + "...");
				
				ProbableStructureProposer proposer = new ProbableStructureProposer(targetGeneSetVariable, classData[i], probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
				connectionProbability[i] = proposer.connectionProposalProbability;
				
				// Collect "the most probable" structure.
				//dependencyStructureSet.add(proposer.proposeBest(0.5));
				
				// Collect "amountOfStructurePerClass" structures.
				for (int j = 0; j < amountOfStructurePerClass; j++) {
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.println("Proposing structure " + (j + 1) + "...");
					
					DependencyStructure s = proposer.propose();
					
					if (!dependencyStructureSet.contains(s))
						dependencyStructureSet.add(s);
				}
			}
		
//			if (RuntimeConfiguration.IS_DEBUG_MODE)*/
			// if rate was entered as a percentage
			if (resamplingRate>1) resamplingRate=resamplingRate/100.0;
			
			int totsamps = classData[0].numberOfSample + classData[1].numberOfSample;
			int numberOfNetworks[] = new int[2];
				numberOfNetworks[0] = (int)((double)numberOfProbableStructure*(double) classData[0].numberOfSample/ (double)totsamps);
				numberOfNetworks[1] = (int)((double)numberOfProbableStructure*(double) classData[1].numberOfSample/ (double)totsamps);

//				System.out.println("Number of networks: " + numberOfNetworks[0] + " " + numberOfNetworks[1]);
			for (int i = 0; i < sampleClass.numberOfClass; i++) {
				int nSamples = classData[i].numberOfSample;

				
				Integer[] line = new Integer[nSamples];
				for(int k=0; k < nSamples; k++){
					line[k] = Integer.valueOf(k);
				}
				List<Integer> newlist = new ArrayList<Integer>();
				newlist = Arrays.asList(line);
				//shuffle the newlist
				
				Integer[] finallist;

				int newvalue = nSamples;
				newvalue = (int) (newvalue*resamplingRate);

				int[] finalarray = new int[newvalue];		
			

				ProbableStructureProposer proposer = new ProbableStructureProposer(targetGeneSetVariable, classData[i], probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
				connectionProbability[i] = proposer.connectionProposalProbability;
				dependencyStructureSet.add(proposer.proposeBest(connProbThreshold,priorWeight,priorMap)); 
				
				for (int j = 0; j < numberOfNetworks[i]; j++) {
		
					Collections.shuffle(newlist);
					finallist= newlist.toArray(new Integer[newlist.size()]);

					for(int m=0; m<newvalue; m++){
						finalarray[m] = finallist[m].intValue();
					}
					
					ProbableStructureProposer proposer80 = new ProbableStructureProposer(targetGeneSetVariable, classData[i].getSubsetSamples(finalarray), probabilityPowerFactor, maxParentAmount, edgeDirFixed, priorDirectionality);
					//connectionProbability[i] = proposer80.connectionProposalProbability;
					DependencyStructure s = proposer80.proposeBest(connProbThreshold,priorWeight,priorMap);
				
					if (!dependencyStructureSet.contains(s))
						dependencyStructureSet.add(s);
				}
	//			System.out.println("Final collected structures: " + dependencyStructureSet.size());
			}
		}
		
		double[][] loggedStructureProbabilityTable = new double[dependencyStructureSet.size()][sampleClass.numberOfClass];
		double[][] structureProbabilityTable = new double[dependencyStructureSet.size()][sampleClass.numberOfClass];
		
		// For each class, evaluate the probability of each structure;
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Evaluating probabilities of dependency structures...");
		
		// TODO: temporary variables to test out -- should remove after testing.
		double minScores[] = new double[sampleClass.numberOfClass];
		double maxScores[] = new double[sampleClass.numberOfClass];
		
		double sumOfLoggedProbability[] = new double[sampleClass.numberOfClass];

		System.out.println("# Networks: " + dependencyStructureSet.size() + "\n");
		
		starttime = System.currentTimeMillis();	
		
		for (int i = 0; i < sampleClass.numberOfClass; i++) {
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("Computing for class " + (i + 1) + "...");
			
			if (classMarkovBlanketUnion[i].size() == 0) {	// If there is no MB variable, simply computing the probability for each structure will suffice.
				
				sumOfLoggedProbability[i] = 0;
				for (int j = 0; j < dependencyStructureSet.size(); j++) {
					DependencyStructure s = dependencyStructureSet.get(j);
					//structureProbabilityTable[j][i] = s.evaluateBDeuScore(classData[i], equivalentSampleSize);
					loggedStructureProbabilityTable[j][i] = s.evaluateLnBDeuScore(classData[i], equivalentSampleSize);
					sumOfLoggedProbability[i] += loggedStructureProbabilityTable[j][i];
//					System.out.println("Logged structure probability" + (j + 1) + ": " + loggedStructureProbabilityTable[j][i]);
					// TODO: temp
					if (j == 0) {
						maxScores[i] = (minScores[i] = loggedStructureProbabilityTable[j][i]);
					} else {
						if (minScores[i] > loggedStructureProbabilityTable[j][i])
							minScores[i] = loggedStructureProbabilityTable[j][i];
						if (maxScores[i] < loggedStructureProbabilityTable[j][i])
							maxScores[i] = loggedStructureProbabilityTable[j][i];
					}
					
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.println("Logged structure probability" + (j + 1) + ": " + loggedStructureProbabilityTable[j][i]);
				}
				// Adjust the logged probabilities. ln(P) + ln(alpha) = ln(P*alpha)
				// Take (-1 * mean of ln(P)) as ln(alpha).
				double lnAlpha = -1*(sumOfLoggedProbability[i]/dependencyStructureSet.size());
				double probScale = 10/(maxScores[i]-minScores[i]);
				
//				System.out.println("Max and min: " + maxScores[i] + " " + minScores[i] + " " + lnAlpha + " " + probScale);
				// TODO: maxScore seems to give a bit more range in JS values
				// double lnAlpha = -1*maxScores[i];
				boolean isThereInfinity = false;
				
				for (int j = 0; j < dependencyStructureSet.size(); j++) {
					// double adjustedLoggedProbability = loggedStructureProbabilityTable[j][i] + lnAlpha;
					// TODO: adjusting so that overall range is approximately limited to [-2, +2]
					double adjustedLoggedProbability = (loggedStructureProbabilityTable[j][i] + lnAlpha)*probScale; 
					double temp = Math.exp(adjustedLoggedProbability);
					
					structureProbabilityTable[j][i] = temp;
					
					if (Double.isInfinite(temp))
						isThereInfinity = true;
				}
				
				// Adjust the case of infinity
				if (isThereInfinity) {
					for (int j = 0; j < dependencyStructureSet.size(); j++) {
						if (Double.isInfinite(structureProbabilityTable[j][i]))
							structureProbabilityTable[j][i] = 1;
						else
							structureProbabilityTable[j][i] = 0;
					}
				}
			}
			else {	// If there are MB variables, sum the probabilities of all structures that has the "core" as a sub-structure.
				for (int j = 0; j < dependencyStructureSet.size(); j++) {
					DependencyStructure s = dependencyStructureSet.get(j);
					structureProbabilityTable[j][i] = evaluateBDeuScoreWithMB(s, classMarkovBlanketUnion[i], classData[i]);
					
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.println("Structure " + (j + 1) + ": " + structureProbabilityTable[j][i]);
				}
			}
		}
		
		endtime = System.currentTimeMillis() - starttime;
//		System.out.println("Total time computing scores: " + endtime + "ms \n");
		
		starttime = System.currentTimeMillis();
		
		// Normalize the probability table so that the sum of each column equals 1.
		for (int i = 0; i < sampleClass.numberOfClass; i++) {
			double sumProbability = 0;
			
			for (int j = 0; j < dependencyStructureSet.size(); j++)
				sumProbability += structureProbabilityTable[j][i];
			
			for (int j = 0; j < dependencyStructureSet.size(); j++) {
				structureProbabilityTable[j][i] /= sumProbability;
//				System.out.println("Structure " + (j + 1) + ": " + structureProbabilityTable[j][i]);
			}
			
			if (RuntimeConfiguration.IS_DEBUG_MODE) {
				System.out.println("Normalized probabilities for class " + (i + 1) + ":");
				
				for (int j = 0; j < dependencyStructureSet.size(); j++)
					System.out.println("Structure " + (j + 1) + ": " + structureProbabilityTable[j][i]);
			}
		}
		
		
		// Transpose the table before return.
		distribution = new double[sampleClass.numberOfClass][structureProbabilityTable.length];
		
		for (int i = 0; i < sampleClass.numberOfClass; i++) {
			for (int j = 0; j < structureProbabilityTable.length; j++)
				distribution[i][j] = structureProbabilityTable[j][i];
		}
		
		endtime = System.currentTimeMillis() - starttime;
//		System.out.println("Total time normalization: " + endtime + "ms \n");
//		System.out.println("Final collected structures 2: " + dependencyStructureSet.size());

		return distribution;
	}
	
	public double evaluateBDeuScoreWithMB(DependencyStructure s, VariableSet MBVariable, Data d) {
		DependencyStructure iterationStructure = s.copy();
		iterationStructure.extendWithMB(MBVariable);
		double sumProbability = iterationStructure.evaluateBDeuScore(d, equivalentSampleSize);
		iterationStructure = iterationStructure.nextMBIterationStructure();
		
		while (iterationStructure != null) {
			sumProbability += iterationStructure.evaluateBDeuScore(d, equivalentSampleSize);
			iterationStructure = iterationStructure.nextMBIterationStructure();
		}
		
		return sumProbability;
	}
	
	private void initializePriorMap() {
		String geneSetName = targetGeneSetVariable.getName();
		String geneSetFileName = "PRIORS/"+geneSetName+".prior";
		String geneName1;
		String geneRelationship;
		String geneName2;
		int nodeIndex1;
		int nodeIndex2;
		int numberOfLines=0;
		int dataType;
		
		try {
			StreamTokenizer st = new StreamTokenizer(new FileReader(geneSetFileName));
			st.ordinaryChars(32, 127);
			st.wordChars(32, 127);
			st.whitespaceChars('\t', '\t');
			st.eolIsSignificant(true);
			dataType = 75;
			while (dataType != StreamTokenizer.TT_EOF) {
				if (dataType == StreamTokenizer.TT_EOL)
				numberOfLines++;
			
				dataType = st.nextToken();
			}
		} catch (FileNotFoundException e)
			{
			System.out.println("PRIOR FILE " + geneSetFileName + "NOT FOUND.  PROCEEDING WITHOUT PRIOR.");
			return;
			}
		catch(Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		try {
		StreamTokenizer st = new StreamTokenizer(new FileReader(geneSetFileName));
		st.ordinaryChars(32, 127);
		st.wordChars(32, 127);
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		dataType = 75;	// Read out the header for variables.	
		for (int i = 0; i < numberOfLines; i++) {
			st.nextToken();
			geneName1 = st.sval;
			st.nextToken();
			geneRelationship = st.sval;
			st.nextToken();
			geneName2 = st.sval;		
			nodeIndex1 = targetGeneSetVariable.priorIndexOf(geneName1);
			nodeIndex2 = targetGeneSetVariable.priorIndexOf(geneName2);
			if (!geneRelationship.equals("neighbor-of") || includeNeighbor==true) {
				priorMap.map[nodeIndex1][nodeIndex2] = 1;
				if (!priorDirectionality) priorMap.map[nodeIndex2][nodeIndex1] = 1;
			}
			dataType = st.nextToken();
		}
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
	}

	
}

