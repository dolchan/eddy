package eddy;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class CallableEDDY implements Callable {

	EDDY3	anEDDY;
    double[] statistic;
    // Where statistic[0] == JS value from runTest()
    //   and statistic[1] == p-value from runTest()
    //       statistic[2] == actual # of permutations, from runTest()
	
	public CallableEDDY(Data inputData, VariableSet targetGene, SampleClass sampleClass, 
			int amountOfPermutation, int numberOfProbableStructure, 
			double lambda, int maxParentAmount, double pValueThreshold, double priorWeight,
			boolean isSilent, boolean quickPermutationEnabled, boolean edgeDirFixed, boolean includeNeighbor, 
			boolean priorDirectionality, boolean approximatePermutations, double resamplingRate) {
		
		anEDDY = new EDDY3(inputData, targetGene, sampleClass, 
				amountOfPermutation, numberOfProbableStructure, 
				lambda, maxParentAmount, pValueThreshold, priorWeight, 
				isSilent, edgeDirFixed, includeNeighbor, priorDirectionality,
				approximatePermutations, resamplingRate);
		
		anEDDY.setQuickPermutationEnabled(quickPermutationEnabled);
	}

	public String call() {

		System.out.println("\nThread # " + (Thread.currentThread().getId()) + " for permutation test for: " + anEDDY.targetGeneSetVariable);

		statistic = anEDDY.runTest();
		
		// reporting & debugging
		System.out.println(this.toString());

		return this.toString();
	}
	
    public String toString() {
        // Returns String representations of VariableSet info. in the form of String array:
        // info[0] == gene set label data (collection, url, size, etc.)
        // info[1] == list of genes

        String[] info = anEDDY.targetGeneSetVariable.toStringLog();
        String labels = info[0];
        String genes = info[1];

        // Concatenates/returns (labels for gene sets) + JS + pValue + (list of genes) to single tab-delimited line.
        String s = labels + "\t";
        s += statistic[0] + "\t"; // adds JS value
        s += statistic[1] + "\t"; // adds p-value
        s += statistic[2] + "\t"; // actual # of permutations done
        s += genes + "\n";
        return s;
    }
}
