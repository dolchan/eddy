package eddy;

public class DependencyStructureContingencyTable {
	public double[][] contingencyTableProbability;
	public double[][] contingencyTable;
	public DependencyStructure[] structureValue;

	public DependencyStructureContingencyTable(int numberOfStructure, int numberOfClass) {
		// TODO Auto-generated constructor stub
		contingencyTableProbability = new double[numberOfStructure][numberOfClass];
		contingencyTable = new double[numberOfStructure][numberOfClass];
		structureValue = new DependencyStructure[numberOfStructure];
	}

}
