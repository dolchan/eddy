package eddy;

import java.io.BufferedWriter;

import flanagan.analysis.Stat;

import java.io.FileWriter;

import similarity.JensenShannon;

public class EDDY3 {
	
	final static double log2 = Math.log(2);
	
	Data data;
	VariableSet allVariable;
	SampleClass sampleClass;
	VariableSet targetGeneSetVariable;
	int finalTargetGeneAmount;

	boolean isConsideringMarkovBlanket = false;
	double independencyPValueThreshold = 0.05;   // not used any more
	
	boolean isConsideringAllStructure = false;
	int numberOfProbableStructure;
	double equivalentSampleSize = 4;
	double probabilityPowerFactor;
	int maxParentAmount;

	boolean isSilent = true;
	boolean networkToBeWritten = true;
	boolean quickPermutationEnabled = true;
	boolean edgeDirFixed = true;
	boolean includeNeighbor = false;
	boolean priorDirectionality = true;
	boolean approximatePermutations = false;

	double JS;
	double pValue = 1;
	double alpha, beta, JSmean, JSvariance;
	double betaderivedpValue;
	
	int numberOfPermutation;
	int permutedAmount;
	double pValueThreshold = 0.05;
	double priorWeight = 1.0;
	double resamplingRate = 0.8;

	long starttime,endtime;
	
	public EDDY3(Data inputData, VariableSet targetGene, SampleClass sampleClass,
			int amountOfPermutation,
			int numberOfProbableStructure,
			double lambda, int maxParentAmount, double pValueThreshold, double priorWeight,
			boolean isSilent, boolean edgeDirFixed, boolean includeNeighbor, boolean priorDirectionality,
			boolean approximatePermutations, double resamplingRate) {
		// TODO Auto-generated constructor stub

		this.numberOfPermutation = amountOfPermutation;
		// this.isConsideringMarkovBlanket = isConsideringMarkovBlanket;
		// this.isConsideringAllStructure = isConsideringAllStructure;
		// this.independencyPValueThreshold = independencyPValueThreshold;
		this.numberOfProbableStructure = numberOfProbableStructure;
		// this.equivalentSampleSize = equivalentSampleSize;
		this.probabilityPowerFactor = lambda;
		this.maxParentAmount = maxParentAmount;
		this.isSilent = isSilent;
		this.pValueThreshold = pValueThreshold;
		this.priorWeight = priorWeight;
		this.resamplingRate = resamplingRate;
		this.edgeDirFixed = edgeDirFixed;
		this.includeNeighbor = includeNeighbor;
		this.priorDirectionality = priorDirectionality;
		this.approximatePermutations = approximatePermutations;
		
		this.sampleClass = sampleClass;
		this.data = inputData;		
	
		// this.allVariable = this.data.buildAllVariable();
		this.targetGeneSetVariable = targetGene;
		
		// this.finalTargetGeneAmount = this.targetGeneSetVariable.size();
	}
	
	public void setToWriteNetworkFile(boolean networkToBeWritten, double pValueThreshold) {
		this.pValueThreshold  = pValueThreshold;
		this.networkToBeWritten = networkToBeWritten;
	}

	public double[] runTest() {
		
		// initialize local instances of some variables
		allVariable = data.buildAllVariable();
//		targetGeneSetVariable = targetGeneSetVariable.recreate(allVariable);
		targetGeneSetVariable.recreate(allVariable);

		finalTargetGeneAmount = targetGeneSetVariable.size();
		
		// Collect structure distributions
		starttime = System.currentTimeMillis();
		StructureProbabilityDistributionEvaluator evaluator = new StructureProbabilityDistributionEvaluator(isConsideringMarkovBlanket, isConsideringAllStructure, independencyPValueThreshold, data, allVariable, targetGeneSetVariable, sampleClass, numberOfProbableStructure, equivalentSampleSize, probabilityPowerFactor, maxParentAmount, priorWeight, edgeDirFixed, includeNeighbor, priorDirectionality, resamplingRate);
		double[][] distribution = evaluator.getProbabilityDistribution();
		
//		System.out.println("Calculating JS divergence\n"); 

		JS = JensenShannon.divergence(distribution[0], distribution[1]) / log2;  // JS = [0, 1];
		endtime = System.currentTimeMillis() - starttime;
		System.out.println("Total time:" + endtime + "ms"); 	
//		System.out.println(JS);
		
		double[] permutedJS = new double[numberOfPermutation];
		int caseLargerThanEqualToJS = 0;
		int nPermutationExit = (int)Math.ceil(numberOfPermutation * pValueThreshold * 2);  // 2 is just safety factor

		if (!isSilent) {
			System.out.println("JS:\t" + JS);
			System.out.println("Running permutation...");
		}
		
		// Collect JS divergences with randomly permuted sample labels.
		if (RuntimeConfiguration.IS_WRITING_LOG) {
			try {
				// TODO: need to re-write this part to be consistent with the other part.
				BufferedWriter bw = new BufferedWriter(new FileWriter("log.txt"));
				int permuted = 0;

				for (int i = 0; i < numberOfPermutation; i++) {
					SampleClass permutedClass = sampleClass.permutedCopy();
					StructureProbabilityDistributionEvaluator permutationEvaluator = new StructureProbabilityDistributionEvaluator(isConsideringMarkovBlanket, isConsideringAllStructure, independencyPValueThreshold, data, allVariable, targetGeneSetVariable, permutedClass, numberOfProbableStructure, equivalentSampleSize, probabilityPowerFactor, maxParentAmount, priorWeight, edgeDirFixed, includeNeighbor, priorDirectionality, resamplingRate);
					double[][] permutedDistribution = permutationEvaluator.getProbabilityDistribution();
					permutedJS[i] = JensenShannon.divergence(permutedDistribution[0], permutedDistribution[1]) / log2;
					
					if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
						System.out.print("Permutation " + (i + 1) + ":\t" + permutedJS[i]);
					
					bw.write("Permutation " + (i + 1) + ":\t" + permutedJS[i] + "\n");

					if (permutedJS[i] >= JS) {
						caseLargerThanEqualToJS++;

						if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
							System.out.println("\t" + caseLargerThanEqualToJS + "th Hit");
					} else {
						if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
							System.out.println("");
					}
					
					permuted++;
				}

				pValue = ((double) caseLargerThanEqualToJS)/((double) permuted);
				permutedAmount = permuted;
				System.out.println("Statistic:\t" + JS + "\tp-value:\t" + pValue);
				bw.write("Statistic:\t" + JS + "\tp-value:\t" + pValue + "\n");
				bw.close();
			} catch (Exception e) {
				System.out.println(e);
				System.exit(-1);
			}
		} else {
			int permuted = 0;
			for (int i = 0; i < numberOfPermutation; i++) {
				SampleClass permutedClass = sampleClass.permutedCopy();
				StructureProbabilityDistributionEvaluator permutationEvaluator = new StructureProbabilityDistributionEvaluator(isConsideringMarkovBlanket, isConsideringAllStructure, independencyPValueThreshold, data, allVariable, targetGeneSetVariable, permutedClass, numberOfProbableStructure, equivalentSampleSize, probabilityPowerFactor, maxParentAmount, priorWeight, edgeDirFixed, includeNeighbor, priorDirectionality, resamplingRate);
				double[][] permutedDistribution = permutationEvaluator.getProbabilityDistribution();
				permutedJS[i] = JensenShannon.divergence(permutedDistribution[0], permutedDistribution[1]) / log2;

				if (Double.isNaN(permutedJS[i])) {
					permutedJS[i]=0;
//					System.out.print("NaN JS likely from 2 identical networks\n");
				};
				
				
				if (!isSilent)
					System.out.print("Permutation " + (i + 1) + ":\t" + permutedJS[i]);
				
				permuted++;
				
				if (permutedJS[i] >= JS) {
					caseLargerThanEqualToJS++;
					
					if (!approximatePermutations & quickPermutationEnabled & permuted > 100 &
							(caseLargerThanEqualToJS > nPermutationExit))  
						break;   // permutation stops since there is no possibility to go below pValueThreshold;
					if (!isSilent)
						System.out.println("\t" + caseLargerThanEqualToJS + " hit");
				} else {
					if (!isSilent)
						System.out.println("");
				}
			}

			pValue = caseLargerThanEqualToJS/((double) permuted);
			permutedAmount = permuted;
			
			if ((permuted==numberOfPermutation) && (approximatePermutations)) {
				JSmean = Stat.mean(permutedJS);
				JSvariance = Stat.variance(permutedJS);
				alpha = (((1-JSmean)/JSvariance) - (1/JSmean))*JSmean*JSmean;
				beta = alpha*((1/JSmean)-1);
				System.out.println("JSmean:" + JSmean + " JSvariance:" + JSvariance);
				System.out.println("alpha:" + alpha + " beta:" + beta);
				betaderivedpValue = 1-Stat.betaCDF(alpha,beta,JS);
				pValue=betaderivedpValue;
				System.out.println(" beta-derived JS p-val:" + betaderivedpValue);
			}
			
			if (!isSilent)
				System.out.println("Statistic:\t" + JS + "\tp-value:\t" + pValue);
		}

		if (networkToBeWritten & (pValue < pValueThreshold))
			evaluator.writeNetworkToFile(targetGeneSetVariable.getName());

        double[] statistic = new double[3];
        statistic[0] = JS;
        statistic[1] = pValue;
        statistic[2] = permutedAmount;  // in many cases, actual # of permutations << specified #

		return statistic;
	}

	public boolean isQuickPermutationEnabled() {
		return quickPermutationEnabled;
	}

	public void setQuickPermutationEnabled(boolean quickPermutationEnabled) {
		this.quickPermutationEnabled = quickPermutationEnabled;
	}
}
