package eddy;

import bayesiannetwork.DiscreteBN;
import bayesiannetwork.DiscreteNode;

import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.Vector;

import bayesiannetwork.DiscreteData;
import bayesiannetwork.ParentRestriction;

public class DependencyStructure extends DiscreteBN {
	public VariableSet targetGeneSetVariable;
	public DependencyStructureMap structureMap;
	public int numberOfCoreVariable;	// Variables of this amount in "targetGeneSetVariable" are core variables. The rest of them are MB variables.
	public String[] dataSampleName;
	public int maxParentAmount = -1;	// -1 represents no limitation.

	// 
	// shound't this be replaced with this(targetGeneSetVariable, -1) ???
	//
	public DependencyStructure(VariableSet targetGeneSetVariable) {
		// TODO Auto-generated constructor stub
		super();
		
		// this.targetGeneSetVariable = targetGeneSetVariable;

		this.targetGeneSetVariable = new VariableSet();
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++)
			this.targetGeneSetVariable.add(new Variable(targetGeneSetVariable.get(i).name));
		for (int i = 0; i < targetGeneSetVariable.priorSize(); i++)
			this.targetGeneSetVariable.priorAdd(targetGeneSetVariable.priorGet(i));
		
		this.targetGeneSetVariable.setName(targetGeneSetVariable.getName());
		this.targetGeneSetVariable.setCollection(targetGeneSetVariable.getCollection());
		this.targetGeneSetVariable.setUrl(targetGeneSetVariable.getUrl());
		this.targetGeneSetVariable.setSize(targetGeneSetVariable.getSize());
		this.targetGeneSetVariable.setPriorSize(targetGeneSetVariable.priorSize());
		
		structureMap = new DependencyStructureMap(this.targetGeneSetVariable.size());
		numberOfCoreVariable = this.targetGeneSetVariable.size();
		initializeDiscreteNode();
	}
	
	public DependencyStructure(VariableSet targetGeneSetVariable, int maxParentAmount) {
		super();
		//this.targetGeneSetVariable = targetGeneSetVariable;
		this.targetGeneSetVariable = new VariableSet();
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++)
			this.targetGeneSetVariable.add(targetGeneSetVariable.get(i));
		for (int i = 0; i < targetGeneSetVariable.priorSize(); i++)
			this.targetGeneSetVariable.priorAdd(targetGeneSetVariable.priorGet(i));
		
		this.targetGeneSetVariable.setName(targetGeneSetVariable.getName());
		this.targetGeneSetVariable.setCollection(targetGeneSetVariable.getCollection());
		this.targetGeneSetVariable.setUrl(targetGeneSetVariable.getUrl());
		this.targetGeneSetVariable.setSize(targetGeneSetVariable.getSize());
		this.targetGeneSetVariable.setPriorSize(targetGeneSetVariable.priorSize());


		structureMap = new DependencyStructureMap(this.targetGeneSetVariable.size(), maxParentAmount);
		numberOfCoreVariable = this.targetGeneSetVariable.size();
		this.maxParentAmount = maxParentAmount;
		initializeDiscreteNode();
		
		
		if (maxParentAmount != -1)
			setMaxParentAmountToNode();
	}
	
	private void setMaxParentAmountToNode() {
		Vector<String> targetGeneSetName = new Vector<String>();
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++)
			targetGeneSetName.add(targetGeneSetVariable.get(i).name);
		
		for (int i = 0; i < node.size(); i ++) {
			DiscreteNode n = node.get(i);
			ParentRestriction pr = new ParentRestriction(n.name, maxParentAmount);
			pr.candidateParentName = targetGeneSetName;
			n.restriction = pr;
		}
	}
	
	public void reloadData(Data data) {
		// reload data values to targetGeneSetVariable from data
		targetGeneSetVariable.reloadData(data);
		
		dataSampleName = data.sampleName;
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++) {
			Variable v = targetGeneSetVariable.get(i);
			String[] possibleValue = v.variableData.possibleValue;
			DiscreteNode n = node.get(i);
			n.value = possibleValue;
		}
	}
	
	private DiscreteData generateDiscreteData() {
		String[] entityName = new String[targetGeneSetVariable.size()];
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++)
			entityName[i] = targetGeneSetVariable.get(i).name;
		
		DiscreteData dData = new DiscreteData(entityName, dataSampleName);
		
		for (int i = 0; i < targetGeneSetVariable.size(); i++) {
			Variable v = targetGeneSetVariable.get(i);
				
			dData.setEntityData(v.getData(), i);
			dData.setEntityValue(v.getPossibleValue(), i);
		}
		
		return dData;
	}
	
	public double evaluateBDeuScore(Data data, double eqSampleSize) {
		reloadData(data);
		DiscreteData dData = generateDiscreteData();
		double loggedProbability = evaluateScore(eqSampleSize, dData);
		return Math.exp(loggedProbability);
	}
	
	public double evaluateLnBDeuScore(Data data, double eqSampleSize) {
		reloadData(data);
		DiscreteData dData = generateDiscreteData();
		double loggedProbability = evaluateScore(eqSampleSize, dData);
		return loggedProbability;
	}
	
	public void extendWithMB(VariableSet unionedMB) {
		// Add the variables in "unionedMB" to "targetGeneSetVariable".
		for (int i = 0; i < unionedMB.size(); i++) {
			Variable MBVariable = unionedMB.get(i);
			targetGeneSetVariable.add(MBVariable);
		}
		
		// Reset "structureMap".
		DependencyStructureMap extendedMap = new DependencyStructureMap(targetGeneSetVariable.size());
		
		for (int i = 0; i < structureMap.map.length; i++) {
			for (int j = 0; j < structureMap.map[0].length; j++)
				extendedMap.map[i][j] = structureMap.map[i][j];
		}
		
		structureMap = extendedMap;
		
		// Add nodes corresponding to the variables in "unionedMB".
		for (int i = 0; i < unionedMB.size(); i++) {
			Variable v = unionedMB.get(i);
			DiscreteNode node = new DiscreteNode(v.name);
			node.value = v.variableData.possibleValue;
			node.ID = numberOfCoreVariable + i;
			this.addNode(node);
		}
	}
	
	public boolean equals(Object s) {
		DependencyStructure another = (DependencyStructure) s;
		
		if (targetGeneSetVariable.equals(another.targetGeneSetVariable) && structureMap.equals(another.structureMap) && (numberOfCoreVariable == another.numberOfCoreVariable))
			return true;
		
		return false;
	}
	
	public boolean equals(DependencyStructure another) {
		if (targetGeneSetVariable.equals(another.targetGeneSetVariable) && structureMap.equals(another.structureMap) && (numberOfCoreVariable == another.numberOfCoreVariable))
			return true;
		
		return false;
	}
	
	private void initializeDiscreteNode() {
		for (int i = 0; i < targetGeneSetVariable.size(); i++) {
			Variable v = targetGeneSetVariable.get(i);
			DiscreteNode node = new DiscreteNode(v.name);
			node.value = v.variableData.possibleValue;
			node.ID = i;	// Each node will have an ID equal to the index from "targetGeneSetVariable".
			
//			if (maxParentAmount > -1) {
//				ParentRestriction pr = new ParentRestriction();
//				pr.maxParent = maxParentAmount;
//				node.setParentRestriction(pr);
//			}
			
			this.addNode(node);
		}
	}
	
	public boolean connect(String parentName, String childName) {
		int parentIndex = targetGeneSetVariable.indexOf(parentName);
		int childIndex = targetGeneSetVariable.indexOf(childName);
		boolean isSuccessfullyConnected = super.connect(parentName, childName);
		
		if (isSuccessfullyConnected)
			structureMap.map[parentIndex][childIndex] = 1;
		
		return isSuccessfullyConnected;
	}
	
	public void naiveConnect(String parentName, String childName) {
		int parentIndex = targetGeneSetVariable.indexOf(parentName);
		int childIndex = targetGeneSetVariable.indexOf(childName);
		structureMap.map[parentIndex][childIndex] = 1;
		super.naiveConnect(parentName, childName);
	}
	
	public boolean disconnect(String nodeName1, String nodeName2) {
		int nodeIndex1 = targetGeneSetVariable.indexOf(nodeName1);
		int nodeIndex2 = targetGeneSetVariable.indexOf(nodeName2);
		structureMap.map[nodeIndex1][nodeIndex2] = 0;
		structureMap.map[nodeIndex2][nodeIndex1] = 0;
		return super.disconnect(nodeName1, nodeName2);
	}
	
	public boolean reverse(String nodeName1, String nodeName2) {
		int nodeIndex1 = targetGeneSetVariable.indexOf(nodeName1);
		int nodeIndex2 = targetGeneSetVariable.indexOf(nodeName2);
		boolean result = super.reverse(nodeName1, nodeName2);
		
		if (result) {
			int temp = structureMap.map[nodeIndex1][nodeIndex2];
			structureMap.map[nodeIndex1][nodeIndex2] = structureMap.map[nodeIndex2][nodeIndex1];
			structureMap.map[nodeIndex2][nodeIndex1] = temp;
		}
		
		return result;
	}
	
	public DependencyStructure copy() {
		DependencyStructure newCopy = new DependencyStructure(targetGeneSetVariable, maxParentAmount);
		newCopy.structureMap = this.structureMap.copy();
		newCopy.numberOfCoreVariable = numberOfCoreVariable;
		DiscreteBN newBNComponentCopy = deepCopy();
		newCopy.node = newBNComponentCopy.node;
		newCopy.edge = newBNComponentCopy.edge;
		newCopy.structureRestriction = newBNComponentCopy.structureRestriction;
		newCopy.lnBDeuScore = newBNComponentCopy.lnBDeuScore;
		newCopy.loggedScore = newBNComponentCopy.loggedScore;
		newCopy.cache = newBNComponentCopy.cache;
		return newCopy;
	}
	
	public DependencyStructure nextIterationStructure() {
		DependencyStructure nextStructure = copy();	// Initialize the next structure with a copy of "this".
		Vector<int[]> modifiedMapCellLocation = nextStructure.structureMap.modifySelfToNextMap();	// It checks cycle.
		
		if (modifiedMapCellLocation == null)
			return null;
		
		// Modify the structure
		for (int i = 0; i < modifiedMapCellLocation.size(); i++) {
			int[] location = modifiedMapCellLocation.get(i);

			if (i == modifiedMapCellLocation.size() - 1)	// The last modification is adding an edge.
				nextStructure.naiveConnect(targetGeneSetVariable.get(location[0]).name, targetGeneSetVariable.get(location[1]).name);
			else	// Remove this connection.
				nextStructure.disconnect(targetGeneSetVariable.get(location[0]).name, targetGeneSetVariable.get(location[1]).name);
		}
		
		return nextStructure;
	}
	
	public DependencyStructure nextMBIterationStructure() {
		DependencyStructure nextStructure = copy();	// Initialize the next structure with a copy of "this".
		Vector<int[]> modifiedMapCellLocation = nextStructure.structureMap.modifySelfToNextMapWithCoreKept(0, numberOfCoreVariable - 1);	// It checks cycle.
		
		if (modifiedMapCellLocation == null)
			return null;
		
		// Modify the structure
		for (int i = 0; i < modifiedMapCellLocation.size(); i++) {
			int[] location = modifiedMapCellLocation.get(i);

			if (i == modifiedMapCellLocation.size() - 1)	// The last modification is adding an edge.
				nextStructure.naiveConnect(targetGeneSetVariable.get(location[0]).name, targetGeneSetVariable.get(location[1]).name);
			else	// Remove this connection.
				nextStructure.disconnect(targetGeneSetVariable.get(location[0]).name, targetGeneSetVariable.get(location[1]).name);
		}
		
		return nextStructure;
	}
	
	public String toString() {		
		return ((targetGeneSetVariable) + "\n" + (structureMap));
	}
}	
