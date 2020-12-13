package eddy;

import java.util.Random;

public class ProbableStructureProposer {
	public VariableSet targetVariableSet;
	public Data data;
	public double[][] MI;
	public double[][] connectionProposalProbability;
	public double probabilityPowerFactor = 1;
	public int maxParentAmount = -1;	// -1 represents no limitation.
	public boolean edgeDirFixed = false;
	public boolean priorDirectionality = true;

	public ProbableStructureProposer(VariableSet targetVariableSet, Data data) {
		// TODO Auto-generated constructor stub
		this.targetVariableSet = targetVariableSet;
		this.data = data;
		MI = new double[targetVariableSet.size()][targetVariableSet.size()];
		connectionProposalProbability = new double[targetVariableSet.size()][targetVariableSet.size()];
		targetVariableSet.reloadData(data);
		init();
	}
	
	public ProbableStructureProposer(VariableSet targetVariableSet, Data data, double probabilityPowerFactor, int maxParentAmount, boolean edgeDirFixed, boolean priorDirectionality) {
		this.targetVariableSet = targetVariableSet;
		this.data = data;
		this.probabilityPowerFactor = probabilityPowerFactor;
		MI = new double[targetVariableSet.size()][targetVariableSet.size()];
		connectionProposalProbability = new double[targetVariableSet.size()][targetVariableSet.size()];
		targetVariableSet.reloadData(data);
		this.maxParentAmount = maxParentAmount;
		this.edgeDirFixed = edgeDirFixed;
		this.priorDirectionality = priorDirectionality;
		init();
	}
	
	// Compute MI table and connectionProposalProbability table.
	/*private void init() {
		double maxMI = 0;
		
		// Compute MI.
		for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++) {
				if (i != j) {
					Variable vi = targetVariableSet.get(i);
					Variable vj = targetVariableSet.get(j);
					MI[i][j] = MI[j][i] = vi.associationWith(vj, "MutualInformation");
					
					if (MI[i][j] > maxMI)
						maxMI = MI[i][j];
				}
			}
		}
		
		// Set connectionProposalProbability.
		// Probabilities proportional to MIs are given, where maxMI corresponds to a probability 1 and 0 MI corresponds to a probability 0.
		for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++)
				connectionProposalProbability[i][j] = connectionProposalProbability[j][i] = MI[i][j]/maxMI;
		}
	}*/
	
	// New version of init(). Setting probabilities with 1 - p-value.
	private void init() {		
		// Compute MI.
		/*for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++) {
				if (i != j) {
					Variable vi = targetVariableSet.get(i);
					Variable vj = targetVariableSet.get(j);
					MI[i][j] = MI[j][i] = vi.associationWith(vj, "MutualInformation");
					
					if (MI[i][j] > maxMI)
						maxMI = MI[i][j];
				}
			}
		}*/
		
		// Set connectionProposalProbability.
		// Probabilities proportional to MIs are given, where maxMI corresponds to a probability 1 and 0 MI corresponds to a probability 0.
		for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++) {	
				Variable vi = targetVariableSet.get(i);
				Variable vj = targetVariableSet.get(j);
//				System.out.print(targetVariableSet.name + "Indexes " + i + " and " + j + "\n");
				double p = 1 - vi.associationWith(vj, "ChiSquareTest");
				connectionProposalProbability[i][j] = connectionProposalProbability[j][i] = Math.pow(p, probabilityPowerFactor);
//				System.out.format("%d %d %f \n",i,j,connectionProposalProbability[j][i]);
			}
		}
//		System.out.print("\n");
	}
	
	public DependencyStructure proposeBest(double probabilityThreshold, double priorWeightFactor, DependencyStructureMap priorMap) {
		Random rd = new Random();
		DependencyStructure candidateBest;
		double priorEdge, priorEdge2;
		int priorIndex1, priorIndex2;
		
		if (maxParentAmount != -1)
			candidateBest = new DependencyStructure(targetVariableSet, maxParentAmount);
		else
			candidateBest = new DependencyStructure(targetVariableSet);
		
		for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++) {
				//double p = rd.nextDouble();
				priorIndex1 = targetVariableSet.priorIndexOf(targetVariableSet.get(i).name);
				priorIndex2 = targetVariableSet.priorIndexOf(targetVariableSet.get(j).name);
				priorEdge = priorMap.map[priorIndex1][priorIndex2];
				priorEdge2 = priorMap.map[priorIndex2][priorIndex1];

//				System.out.printf("Gene1 " + i + " Gene2 " + j + " " + connectionProposalProbability[i][j] 
//						+ "\n");
//				System.out.printf("Gene1 " + targetVariableSet.get(i).name + " Gene2 " + targetVariableSet.get(j).name + " Prob: " + connectionProposalProbability[i][j] + " Prior Edge: " + priorEdge + "\n");
				if ((priorEdge==0) && (priorEdge2==0)) {   //data-derived edges only here
				if (connectionProposalProbability[i][j] >= probabilityThreshold) {	// Connect i->j or j<-i.
					Variable parent, child;
					
					if (edgeDirFixed || rd.nextInt(2) == 0) {  // either 0 or 1
						parent = targetVariableSet.get(i);
						child = targetVariableSet.get(j);
					}
					else {
						parent = targetVariableSet.get(j);
						child = targetVariableSet.get(i);
					}
//					System.out.printf("Gene1 " + i + " Gene2 " + j + " " + connectionProposalProbability[i][j] 	+ " B1 \n");
//					System.out.printf("Gene1 " + targetVariableSet.get(i).name + " Gene2 " + targetVariableSet.get(j).name + " Prob: " + connectionProposalProbability[i][j] + " Prior Edge: " + priorEdge + "\n");

					candidateBest.connect(parent.name, child.name);
				}
				}
				else {
				// This check is for prior directionality AND if the prior edge exists.  
					if (priorEdge==1) {
						if (connectionProposalProbability[i][j] >= probabilityThreshold*(1 - priorWeightFactor)) {	// Connect i->j or j<-i.
							Variable parent, child;
							
							if (edgeDirFixed || rd.nextInt(2) == 0) {  // either 0 or 1
								parent = targetVariableSet.get(i);
								child = targetVariableSet.get(j);
//								System.out.printf("Gene1 " + i + " Gene2 " + j + " " + connectionProposalProbability[i][j] 
//									+ " B2 \n");	
//							System.out.printf("Gene1 " + targetVariableSet.get(i).name + " Gene2 " + targetVariableSet.get(j).name + " Prob: " + connectionProposalProbability[i][j] + " Prior Edge: " + priorEdge + "\n");

								}
							else {
								parent = targetVariableSet.get(j);
								child = targetVariableSet.get(i);
							}
							
							candidateBest.connect(parent.name, child.name);
						}
						}
					if (priorDirectionality && priorEdge2==1) { // if we don't have prior directionality, the edge will go one way
						if (connectionProposalProbability[i][j] >= probabilityThreshold*(1 - priorWeightFactor)) {	// Connect i->j or j<-i.
							Variable parent, child;
					
							if (edgeDirFixed || rd.nextInt(2) == 0) {  // either 0 or 1
								parent = targetVariableSet.get(j);
								child = targetVariableSet.get(i);
//								System.out.printf("Gene1 " + i + " Gene2 " + j + " " + connectionProposalProbability[i][j] 
//									+ " B3 \n");	
//								System.out.printf("Gene1 " + targetVariableSet.get(i).name + " Gene2 " + targetVariableSet.get(j).name + " Prob: " + connectionProposalProbability[i][j] + " Prior Edge: " + priorEdge + "\n");

								}
							
							else {
								parent = targetVariableSet.get(i);
								child = targetVariableSet.get(j);
							}
					
							candidateBest.connect(parent.name, child.name);
						}
					}
				}
			}
		}
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.println("Proposing a top structure: ");
			candidateBest.structureMap.printMap();
		}
		
		return candidateBest;
	}
	
	public DependencyStructure propose() {
		Random rd = new Random();
		DependencyStructure candidate;
		
		if (maxParentAmount != -1)
			candidate = new DependencyStructure(targetVariableSet, maxParentAmount);
		else
			candidate = new DependencyStructure(targetVariableSet);
		
		for (int i = 0; i < targetVariableSet.size() - 1; i++) {
			for (int j = i + 1; j < targetVariableSet.size(); j++) {
				double p = rd.nextDouble();
				
				if (connectionProposalProbability[i][j] >= p) {	// Connect i->j or j<-i.
					Variable parent, child;
					
					if (edgeDirFixed || rd.nextInt(2) == 0) {
						parent = targetVariableSet.get(i);
						child = targetVariableSet.get(j);
					}
					else {
						parent = targetVariableSet.get(j);
						child = targetVariableSet.get(i);
					}
					
					candidate.connect(parent.name, child.name);
				}
			}
		}
		
		/*if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.println("Proposing a structure: ");
			candidate.structureMap.printMap();
		}*/
		
		return candidate;
	}

}
