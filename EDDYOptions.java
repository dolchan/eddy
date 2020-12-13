package eddy;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class EDDYOptions {

	public Option logfile;
	public Option argDataFile;
//	public Option argGeneSetFile;
	public Option argMultiGeneSetFile;
//	public Option argMSigDBGeneSetFile;
	public Option argClassFile;
//	public Option optArgProbPowerFactor;
	public Option optArgMaxParents;
	public Option optArgMultiThreads;
//	public Option optMarkovBlanket;
	public Option optArgNumberOfStructures;
	public Option optArgNumberOfPermutations;
	public Option optArgPValueThreshold;
//	public Option optArgEquivSampleSize;
    public Option optArgMinGeneSetSize;
    public Option optArgMaxGeneSetSize;
//	public Option optArgSelectedCollections;
    public Option optArgQuickPermutation;
    public Option optArgEdgeDirFixed;
    public Option optArgIncludeNeighbor;
    public Option optArgPriorDirectionality;
    public Option optArgPriorWeight;
    public Option optArgApproximatePermutations;
    public Option optArgResamplingRate;


	Options options;
	
	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public EDDYOptions() {		
		
		// create the Options
		options = new Options();
		
    	argDataFile = OptionBuilder.withArgName("file")
    			                   .withLongOpt("data")
    			                   .isRequired()
    			                   .hasArg()
    			                   .withDescription("file name for gene expression data")
    			                   .create("d");
    	options.addOption(argDataFile);

//
// TODO: no longer supported
//
//    	argGeneSetFile = OptionBuilder.withArgName("file")
//    			                      .withLongOpt("single-gene-set")
//    			                      .hasArg()
//    			                      .withDescription("file name for a single gene set [deprecated]")
//    			                      .create("t");
//    	options.addOption(argGeneSetFile);

    	argMultiGeneSetFile = OptionBuilder.withArgName("file")
    			                           .withLongOpt("gene-sets")
    			                           .isRequired()
    			                           .hasArg()
    			                           .withDescription("file name for multiple gene sets")
    			                           .create("g");
    	options.addOption(argMultiGeneSetFile);


//
// TODO: no longer needed as -T option is default for gmt format
//
//    	argMSigDBGeneSetFile = OptionBuilder.withArgName("msigdbfile")
//                                            .withLongOpt("msigdb-gmt")
//                                            .hasArg()
//                                            .withDescription("file name for multiple gene sets (MSigDB .gmt). "
//                                            		       + "[Note] one of t, T, or M should be specified.")
//                                            .create("M");
//    	options.addOption(argMSigDBGeneSetFile);

    	argClassFile = OptionBuilder.withArgName("file")
    			                    .withLongOpt("class")
    			                    .hasArg()
    			                    .isRequired()
    			                    .withDescription("file name for class labels")
    			                    .create("c");
    	options.addOption(argClassFile);

//
// TODO: no longer needed as lamda is fixed at 1.
//    	
//    	optArgProbPowerFactor = OptionBuilder.withArgName("double")
//    			                          .withLongOpt("prob-power-factor")
//    			                          .hasArg()
//    			                          .withDescription("probability power factor, lamda [default = 1]")
//    			                          .create("l");
//    	options.addOption(optArgProbPowerFactor);
        
    	optArgMaxParents = OptionBuilder.withArgName("SIZE")
    			                     .withLongOpt("max-parents")
    			                     .hasArg()
    			                     .withDescription("maximum number of parents for each node")
    			                     .create("mp");
    	options.addOption(optArgMaxParents);
    	
    	optArgMultiThreads = OptionBuilder.withArgName("N")
    			                       .withLongOpt("multi-threads")
    			                       .hasArg()
    			                       .withDescription("number of threads to use")
    			                       .create("mt");
    	options.addOption(optArgMultiThreads);

//
// TODO: not used anymore, due to too much computational load
//
//    	optMarkovBlanket = OptionBuilder.withArgName("markovblanket")
//    								  	.withLongOpt("markov-blanket")
//    								  	.withDescription("consider Markov Blanket if given")
//    								  	.create();
//    	options.addOption(optMarkovBlanket);

    	optArgNumberOfStructures = OptionBuilder.withArgName("N")
								    			.withLongOpt("number-of-structures")
								    			.hasArg()
								    			.withDescription("number of network structures to consider."
								    						   + "  [default = LOO (-1)]")
								    			.create("s");
    	options.addOption(optArgNumberOfStructures);

    	optArgNumberOfPermutations = OptionBuilder.withArgName("N")
    											  .withLongOpt("number-of-permutations")
    											  .hasArg()
    											  .withDescription("number of permutations for "
    													         + "statistical significance testing."
    													  	     + " [default = 1000]")
    											  .create("r");
    	options.addOption(optArgNumberOfPermutations);

    	optArgPValueThreshold = OptionBuilder.withArgName("double")
    										 .withLongOpt("pvalue-threshold")
    										 .hasArg()
    										 .withDescription("pvalue threshold for independece testing."
    												 		+ "  [default = 0.05]")
    										 .create("p");
    	options.addOption(optArgPValueThreshold);

    	optArgPriorWeight = OptionBuilder.withArgName("double")
				 .withLongOpt("prior-weight")
				 .hasArg()
				 .withDescription("weight to give prior."
						 		+ "  [default = 0.0]")
				 .create("pw");
    	options.addOption(optArgPriorWeight);

    	optArgResamplingRate = OptionBuilder.withArgName("double")
				 .withLongOpt("resampling-rate")
				 .hasArg()
				 .withDescription("resampling rate."
						 		+ "  [default = 0.8 when number of network structures is specified]")
				 .create("rs");
    	options.addOption(optArgResamplingRate);

//
// TODO: fixed to 4;  do not support from CLI
//
//    	optArgEquivSampleSize = OptionBuilder.withArgName("equivsamplesize")
//    										 .withLongOpt("equiv-sample-size")
//    										 .hasArg()
//    										 .withDescription("equivalent sample size"
//		                                              		+ "  [default = 4]")
//		                                     .create("e");
//    	options.addOption(optArgEquivSampleSize);
//
        optArgMinGeneSetSize = OptionBuilder.withArgName("SIZE")
                                            .withLongOpt("min-gene-set")
                                            .hasArg()
                                            .withDescription("minimum gene set size for analysis."
                                                         + "  [default = -1 for no limit]")
                                            .create("m");
        options.addOption(optArgMinGeneSetSize);

        optArgMaxGeneSetSize = OptionBuilder.withArgName("SIZE")
                                            .withLongOpt("max-gene-set")
                                            .hasArg()
                                            .withDescription("maximum gene set size for anlaysis."
                                                    + "  [default = -1 for no limit]")
                                            .create("M");
        options.addOption(optArgMaxGeneSetSize);

//
// TODO: no longer used, as the new gene set format is now used.
//
//        optArgSelectedCollections = OptionBuilder.withArgName("selectedcollections")
//                .withLongOpt("selected-collections")
//                .hasArgs()
//                .withDescription("listed collections (e.g. \"C2 C7 C3\" separated by spaces) to select gene sets by.")
//                .create("co");
//        options.addOption(optArgSelectedCollections);
        
        optArgQuickPermutation = OptionBuilder.withArgName("boolean")
			  	.withLongOpt("quick-permutations")
			  	.hasArg()
			  	.withDescription("Quick permutations enabled -- permutation stops" 
			  				   + "when there is no possibility to yield p-value less than given threshold.")
			  	.create("qp");
        options.addOption(optArgQuickPermutation);

        optArgEdgeDirFixed = OptionBuilder.withArgName("boolean")
			  	.withLongOpt("edge-dir-fixed")
			  	.hasArg()
			  	.withDescription("Edge direction prefixed -- earlier gene -> later gene.")
			  	.create("edf");
        options.addOption(optArgEdgeDirFixed);

        optArgIncludeNeighbor = OptionBuilder.withArgName("boolean")
			  	.withLongOpt("include-neighbor")
			  	.hasArg()
			  	.withDescription("Include neighbor-of interactions from prior.")
			  	.create("in");
        options.addOption(optArgIncludeNeighbor);
        
        optArgPriorDirectionality = OptionBuilder.withArgName("boolean")
			  	.withLongOpt("prior-directionality")
			  	.hasArg()
			  	.withDescription("Use directionality of prior edges.")
			  	.create("pd");
        options.addOption(optArgPriorDirectionality);
 
        optArgApproximatePermutations = OptionBuilder.withArgName("boolean")
			  	.withLongOpt("approximate-permutations")
			  	.hasArg()
			  	.withDescription("Approximate permutations with beta distribution.")
			  	.create("ap");
        options.addOption(optArgApproximatePermutations);
	}
}
