package eddy;

// This class implements the HITON-PC algorithm
public class ParentChildFinder {
	public ParentChildSet parentChild;
	public Variable target;
	public VariableSet allVariable;
	public PrioritizedList OPEN;
	public double pValueThresholdForIndependence;
	public VariableSet[][] cacheZ = null;

	public ParentChildFinder(Variable tar, VariableSet allVar, double pVal) {
		// TODO Auto-generated constructor stub
		target = tar;
		allVariable = allVar;
		pValueThresholdForIndependence = pVal;
		initializeParentChild();
		OPEN = new PrioritizedList();
		initializeOPEN();
	}
	
	public ParentChildFinder(Variable tar, VariableSet allVar, double pVal, VariableSet[][] cacheZ) {
		// TODO Auto-generated constructor stub
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Searching PC(" + tar.name + ")...");
		
		target = tar;
		allVariable = allVar;
		pValueThresholdForIndependence = pVal;
		this.cacheZ = cacheZ;
		initializeParentChild();
		OPEN = new PrioritizedList();
		initializeOPEN();
	}
	
	private void initializeParentChild() {
		parentChild = new ParentChildSet();
	}
	
	private void inclusion() {
		if (!OPEN.isEmpty()) {
			Variable v = (Variable) OPEN.removeTop();
			parentChild.add(v);
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("Adding " + v.name + " tp PC(" + target.name + ").");
		}
	}
	
	private void initializeOPEN() {
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Initializing candidates for PC(" + target.name + ")...");
		
		int numberOfVariable = allVariable.size();
		
		for (int i = 0; i < numberOfVariable; i++) {
			Variable X = allVariable.get(i);
			
			if (X.equals(target))	// Skip "target" itself.
				continue;
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print(X.name + "...");
			
			double pValueForIndependence = target.associationWith(X, "ChiSquareTest");
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.print(pValueForIndependence);
			
			Double score = new Double(1 - pValueForIndependence);	// For more association, they have the smaller p-value.
			
			if (pValueForIndependence < pValueThresholdForIndependence) {	// Put variables while ignoring independent ones.
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.print(". Candidate.");
				
				OPEN.putInDescendingOrder(score, X);
			}
			
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("");
		}
	}
	
	private void elimination() {
		if (OPEN.isEmpty()) {	// Do the final check.
			for (int i = 0; i < parentChild.size(); i++) {
				Variable X = parentChild.get(i);
				IndependencyConditioningVariableSetFinder ZFinder = new IndependencyConditioningVariableSetFinder(X, target, parentChild, pValueThresholdForIndependence);
				VariableSet Z = ZFinder.findIndependencyZ();
				
				if (Z != null) {	// Eliminate X from "parentChild" if there is Z s.t. I(X, T| Z).
					parentChild.remove(i);
					i--;
					
					if (cacheZ != null) {	// If Z cache is available, keep this Z for (X, target).
						int indexX = allVariable.indexOf(X);
						int indexT = allVariable.indexOf(target);
						cacheZ[indexX][indexT] = Z;
						cacheZ[indexT][indexX] = Z;
					}
				}
			}
		}
		else {	// Check for the last variable.
			if (parentChild.size() > 0) {
				Variable X = parentChild.get(parentChild.size() - 1);
				IndependencyConditioningVariableSetFinder ZFinder = new IndependencyConditioningVariableSetFinder(X, target, parentChild, pValueThresholdForIndependence);
				VariableSet Z = ZFinder.findIndependencyZ();
				
				if (Z != null)	{	// Eliminate X from "parentChild" if there is Z s.t. I(X, T| Z).
					parentChild.remove(parentChild.size() - 1);
					
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.println("Removing " + X.name + " from PC(" + target.name + ").");
					
					if (cacheZ != null) {	// If Z cache is available, keep this Z for (X, target).
						int indexX = allVariable.indexOf(X);
						int indexT = allVariable.indexOf(target);
						cacheZ[indexX][indexT] = Z;
						cacheZ[indexT][indexX] = Z;
					}
				}
			}
		}
	}
	
	private void beginSearch() {
		while (!OPEN.isEmpty()) {
			inclusion();
			elimination();
		}
	}
	
	public ParentChildSet findParentChild() {
		beginSearch();
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.print("PC(" + target.name + ") = ");
			
			for (int i = 0; i < parentChild.size() - 1; i++)
				System.out.print(parentChild.get(i).name + ", ");
			
			if (parentChild.size() != 0)
				System.out.println(parentChild.get(parentChild.size() - 1).name);
			else
				System.out.println("");
		}
		
		return parentChild;
	}

}
