package eddy;

import similarity.MutualInformation;
import statisticaltest.ChiSquareTest;

public class Variable {
	public String name;	// This is the key of a Variable object.
	public VariableData variableData;

	public Variable(String nam) {
		// TODO Auto-generated constructor stub
		name = nam;
	}
	
	public void reloadData(Data data) {	// Change "variableData" to the one in "data".
		variableData = data.getVariableData(name);
	}
	
	public double associationWith(Variable T, String method) {
		// Returns the association score Assoc(this, T) using "method"
		long starttime, endtime;
		if (method.toLowerCase().equals("mutualinformation"))
			return MutualInformation.evaluateStringArrayMI(this.getData(), T.getData());
		else if (method.toLowerCase().equals("chisquaretest")) {
			starttime = System.nanoTime();	
			int[][] contingencyTable = ChiSquareTest.buildContingencyTable(getData(), T.getData());
			endtime = System.nanoTime() - starttime;
//			System.out.println("Total time build contingency table:" + endtime/1000.0 + "us.  \n");
			starttime = System.nanoTime();	
			double pValue = ChiSquareTest.independence(contingencyTable);
			endtime = System.nanoTime() - starttime;
//			System.out.println("Total time independence test:" + endtime/1000.0 + "us.  \n");	
//			System.out.println(this.name + " " + T.name + " P-value:" + pValue + " \n");	
			return pValue;
		}
		else {
			System.out.println("Error: Method " + method + " is not supported by Variable.associationWith().");
			return -1;
		}
	}
	
	public String[] getData() {
		return variableData.value;
	}
	
	public String[] getPossibleValue() {
		return variableData.possibleValue;
	}
	
	public void setData(String[] d) {
		variableData = new VariableData();
		variableData.setData(d);
	}
	
	public void setData(VariableData d) {
		variableData = d;
	}
	
	public boolean equals(Variable another) {
		return name.equals(another.name);
	}
	
	public boolean equals(Object another) {
		return name.equals(((Variable) another).name);
	}

}
