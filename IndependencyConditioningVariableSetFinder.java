package eddy;

import statisticaltest.ChiSquareTest;

public class IndependencyConditioningVariableSetFinder {
	public Variable X;
	public Variable target;
	public ParentChildSet parentChild;
	public PrioritizedList candidate;
	public VariableSet Z;
	double independencyPValueThreshold;

	public IndependencyConditioningVariableSetFinder(Variable v, Variable t, ParentChildSet pc, double independencyPValThr) {
		// TODO Auto-generated constructor stub
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Searching Z that makes I(" + v.name + ", " + t.name + "|Z)...");
		
		X = v;
		target = t;
		parentChild = pc;
		independencyPValueThreshold = independencyPValThr;
		candidate = new PrioritizedList();
		Z = new VariableSet();
		initializePrioritizedList();
	}
	
	public void initializePrioritizedList() {
		double originalAssociation = 1 - X.associationWith(target, "ChiSquareTest");
		
		for (int i = 0; i < parentChild.size(); i++) {
			Variable v = parentChild.get(i);
			
			if (v.equals(X))	// This will be tested on PC/{X}.
				continue;
			
			String[][] conditionalVariableValue = new String[1][];
			conditionalVariableValue[0] = v.getData();
			int[][][] contingencyTable = ChiSquareTest.buildConditionalContingencyTable(X.getData(), target.getData(), conditionalVariableValue);
			double pValue = ChiSquareTest.conditionalIndependence(contingencyTable);
			double associationGivenV = 1 - pValue;
			double reducedAssociationAmount = originalAssociation - associationGivenV;
			candidate.putInDescendingOrder(new Double(reducedAssociationAmount), v);
		}
	}
	
	public VariableSet findIndependencyZ() {	// Finds Z that makes X and "target" independent when Z is given. If there is no such Z, returns null.
		while (!candidate.isEmpty()) {
			Variable ct = (Variable) candidate.removeTop();
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print("Adding " + ct.name + " to Z.");
			
			Z.add(ct);
			
			// Collect data from the variables in Z.
			String[][] dataZ = new String[Z.size()][];
			
			for (int i = 0; i < Z.size(); i++)
				dataZ[i] = Z.get(i).getData();
			
			// Conditional independency test
			int[][][] conditionalContingencyTable = ChiSquareTest.buildConditionalContingencyTable(X.getData(), target.getData(), dataZ);
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print(" Testing...");
			
			double pValue = ChiSquareTest.conditionalIndependence(conditionalContingencyTable);
			
			if (RuntimeConfiguration.IS_DEBUG_MODE) {
				if (pValue >= independencyPValueThreshold) {
					System.out.print(pValue + ". Independent with Z = (");
					
					for (int i = 0; i < Z.size() - 1; i++)
						System.out.print(Z.get(i).name + ", ");
					
					if (Z.size() > 0)
						System.out.println(Z.get(Z.size() - 1).name + ")");
					else
						System.out.println(")");
				}
			}
			
			if (pValue >= independencyPValueThreshold)	// X and "target" are independent given Z!
				return Z;
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("");
		}
		
		return null;
	}

}
