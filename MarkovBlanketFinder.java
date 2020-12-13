package eddy;

import statisticaltest.ChiSquareTest;

public class MarkovBlanketFinder {
	public Variable target;
	public double pValueThresholdForIndependence;
	public VariableSet allVariable;
	public MarkovBlanket MB;
	public VariableSet S;	// This is a set of potential spouses.
	public VariableSet[][] cacheZ;

	public MarkovBlanketFinder(Variable target, VariableSet allVariable, double pValueThresholdForIndependence) {
		// TODO Auto-generated constructor stub
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Searching Markov Blanket of " + target.name + "...");
		
		this.target = target;
		this.allVariable = allVariable;
		this.pValueThresholdForIndependence = pValueThresholdForIndependence;
		MB = new MarkovBlanket();
		S = new VariableSet();
		cacheZ = new VariableSet[allVariable.size()][allVariable.size()];
		
		// Initialize "cacheZ" with empty sets.
		for (int i = 0; i < allVariable.size(); i++) {
			for (int j = 0; j < allVariable.size(); j++) {
				cacheZ[i][j] = new VariableSet();
			}
		}
	}
	
	public MarkovBlanket findMB() {
		// 1. PC(T) <- HITON-PC(T)
		ParentChildFinder pcFinder_target = new ParentChildFinder(target, allVariable, pValueThresholdForIndependence, cacheZ);
		ParentChildSet PC_T = pcFinder_target.findParentChild();
		
		if (PC_T.size() == 0) {	// If "target" does not have any parent or child, then it's Markov Blanket is an empty set.
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("MB(" + target.name + ") = empty.");
			
			return MB;
		}
		
		// 2. For every variable Y in PC(T), PC(Y) <- HITON-PC(Y)
		ParentChildSet[] PC_Y = new ParentChildSet[PC_T.size()];
		
		for (int i = 0; i < PC_T.size(); i++) {
			Variable Y = PC_T.get(i);
			ParentChildFinder pcFinder_Y = new ParentChildFinder(Y, allVariable, pValueThresholdForIndependence, cacheZ);
			PC_Y[i] = pcFinder_Y.findParentChild();
		}
		
		// 3. Initialize MB. MB(T) <- PC(T)
		for (int i = 0; i < PC_T.size(); i++) {
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("Adding " + PC_T.get(i).name + " to MB(" + target.name + ").");
			
			MB.add(PC_T.get(i));
		}
		
		// 4. Initialize S. S <- {Union of PC(Y)}/{PC(T) union {T}}.
		for (int i = 0; i < PC_Y.length; i++) {
			for (int j = 0; j < PC_Y[i].size(); j++) {
				if (!S.contains(PC_Y[i].get(j)))
					S.add(PC_Y[i].get(j));
			}
		}
		
		S.remove(target);
		
		for (int i = 0; i < PC_T.size(); i++)
			S.remove(PC_T.get(i));
		
		// 5. For every variable X in S,
		//	a. Retrieve Z s.t. I(X, T| Z)
		//	b. For every variable Y in PC(T) s.t. X is in PC(Y),
		//	c. If not I(X, T | Z union {Y}), X is a spouse of T
		//		So, insert X into MB
		for (int i = 0; i < S.size(); i++) {
			Variable X = S.get(i);
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print("Testing " + X.name + " for MB(" + target.name + ")...");
			
			VariableSet Z = cacheZ[allVariable.indexOf(X)][allVariable.indexOf(target)];
			
			for (int j = 0; j < PC_T.size(); j++) {
				Variable Y = PC_T.get(j);
				
				if (PC_Y[j].contains(X)) {	// Y is a potential common child of T and X.
					String[][] conditionalVariableValue = new String[Z.size() + 1][];
					
					for (int k = 0; k < Z.size(); k++)
						conditionalVariableValue[k] = Z.get(k).getData();
					
					conditionalVariableValue[Z.size()] = Y.getData();
					int[][][] conditionalContingencyTable = ChiSquareTest.buildConditionalContingencyTable(X.getData(), target.getData(), conditionalVariableValue);
					double pValue = ChiSquareTest.conditionalIndependence(conditionalContingencyTable);
					
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.print(" " + pValue);
					
					if (pValue < pValueThresholdForIndependence)	{	// Adding Y to Z made X and T be dependent given Z. Y is a common child.
						if (RuntimeConfiguration.IS_DEBUG_MODE)
							System.out.println("Adding " + X.name + " to MB(" + target.name + ").");
						
						MB.add(X);
						break;
					}
				}
			}
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("");
		}
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.print("MB(" + target.name + ") = ");
			
			for (int i = 0; i < MB.size() - 1; i++)
				System.out.print(MB.get(i).name + ", ");
			
			if (MB.size() > 0)
				System.out.println(MB.get(MB.size() - 1));
			else
				System.out.println("");
		}
		
		return MB;
	}

}
