package eddy;

import java.io.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import static java.lang.Runtime.*;

public class MyRunEDDY {
    public static String dataFileName;
    public static String targetGeneSetFileName;
    public static String sampleClassInformationFileName;
//    public static boolean isConsideringMarkovBlanket = false;  // no longer used
//    public static boolean isConsideringAllStructure;     // no longer used

    public static Data data;
    public static VariableSet allVariable;
    public static Vector<String> targetGeneSetVariableName;
//    public static VariableSet targetGeneSetVariable;
    public static ArrayList<VariableSet> listOfTargetGeneSets;
    public static SampleClass sampleClass;
    
    public static  int numberOfProbableStructure = -1;
    public static int numberOfPermutation = 1000;
    public static double probabilityPowerFactor = 1;
    public static int maxParentAmount = 3;	// -1 indicates no limitation.
    public static int numberOfThreads = 0;
    public static int timeLimitInHour = -1;	// in hours; -1 indicates no limit
    public static long startTime;
    public static long endTime;
    public static boolean isSilent = true;
	public static boolean edgeDirFixed = true;
	public static boolean includeNeighbor = false;
	public static boolean priorDirectionality = true;
	public static boolean approximatePermutations = false;
    public static double resamplingRate = 0.8;
    public static double pValueThreshold = 0.05;   // 0 means no output, pValue threshold for JS
    public static double priorWeight = 0.0;
    public static BufferedWriter output;
    public static String[] selectedCollections = new String[0];
    public static int minGeneSetSize = -1;
    public static int maxGeneSetSize = -1;
    public static boolean quickPermutationEnabled = true;
    public static String[] commandLine;

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        startTime = System.currentTimeMillis();

        // to log
        commandLine = args;
        
        // Parse and process commandline arguments
        if (!initialize(args))
            return;

        if ( (edgeDirFixed==false) && (numberOfProbableStructure==-1)) {
        	 System.out.println("Exiting: Edge direction not fixed and LOO selected (numberOfProbableNetworks = -1).  More networks must be created, but be warned that this will increase the computational load.");
             System.exit(-1);
        }
        // Read all necessary data
        initializeData();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // Instantiates Collection<Future> to add to for each call to executor's submit() method.
        List<Future<String>> futureList = new ArrayList<Future<String>>(listOfTargetGeneSets.size());

        for (int i = 0; i < listOfTargetGeneSets.size(); i++) {
            VariableSet targetSet = listOfTargetGeneSets.get(i);
            CallableEDDY callableEDDY = new CallableEDDY(data, targetSet, sampleClass, 
            		numberOfPermutation, numberOfProbableStructure, 
            		probabilityPowerFactor, maxParentAmount, pValueThreshold, priorWeight,
            		isSilent, quickPermutationEnabled, edgeDirFixed, includeNeighbor, 
            		priorDirectionality, approximatePermutations, resamplingRate);
            
            futureList.add(executor.submit(callableEDDY));
        }

        // Waits for the returned String from each thread (represented as a "Future") &
        // writes it to output file.
        try {
            for (Future<String> f : futureList)
                printToLog(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();

        try {
            long elapsedTimeMillis = (System.currentTimeMillis() - startTime);
            String elapsedTime = String.format("%02d hrs: %02d min: %02d sec", TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis),
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis)));
            System.out.printf("Tasks completed. Elapsed Time: " + elapsedTime + "\n");
            output.write("\nElapsed Time:   " + elapsedTime);
            output.close();
        } catch (Exception e) {e.printStackTrace();}

    }


    /** Waits for given "Future" object to return String representation of gene set info.
     * When task is completed, the BufferedWriter writes this String of info. to output file.
     * as a single line. */
    public static void printToLog(Future<String> f) {
        try {
            String line;
            line = f.get();
            if (!line.equals("")) {
                output.write(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean initialize(String[] args) {

        CommandLineParser parser = new BasicParser();
//		Options optionsEDDY = createOptions();	

        EDDYOptions myOptions = new EDDYOptions();

        try {
            CommandLine cmd = parser.parse(myOptions.getOptions(), args);

            if (cmd.hasOption(myOptions.argDataFile.getOpt())) {
                dataFileName = cmd.getOptionValue(myOptions.argDataFile.getOpt());
            }

//            if (cmd.hasOption(myOptions.argGeneSetFile.getOpt())) {
//                targetGeneSetFileName = cmd.getOptionValue(myOptions.argGeneSetFile.getOpt());
//                isMultipleGeneSet = false;
//            }

            if (cmd.hasOption(myOptions.argMultiGeneSetFile.getOpt())) {
                targetGeneSetFileName = cmd.getOptionValue(myOptions.argMultiGeneSetFile.getOpt());
//                isMultipleGeneSet = true;
//                isGMT = targetGeneSetFileName.contains(".gmt");
            }

            if (cmd.hasOption(myOptions.argClassFile.getOpt())) {
                sampleClassInformationFileName = cmd.getOptionValue(myOptions.argClassFile.getOpt());
            }

//            if (cmd.hasOption(myOptions.optArgProbPowerFactor.getLongOpt())) {
//                probabilityPowerFactor = Double.parseDouble(cmd.getOptionValue(myOptions.optArgProbPowerFactor.getLongOpt()));
//            }

            if (cmd.hasOption(myOptions.optArgMaxParents.getLongOpt())) {
                maxParentAmount = Integer.parseInt(cmd.getOptionValue(myOptions.optArgMaxParents.getLongOpt()));
            }

            if (cmd.hasOption(myOptions.optArgMultiThreads.getLongOpt())) {
                numberOfThreads = Integer.parseInt(cmd.getOptionValue(myOptions.optArgMultiThreads.getLongOpt()));
            }
            
            if (cmd.hasOption(myOptions.optArgEdgeDirFixed.getLongOpt())) {
            	edgeDirFixed = Boolean.parseBoolean(cmd.getOptionValue(myOptions.optArgEdgeDirFixed.getLongOpt()));
            }
        
            if (cmd.hasOption(myOptions.optArgIncludeNeighbor.getLongOpt())) {
            	includeNeighbor = Boolean.parseBoolean(cmd.getOptionValue(myOptions.optArgIncludeNeighbor.getLongOpt()));
            }
        
            if (cmd.hasOption(myOptions.optArgPriorDirectionality.getLongOpt())) {
            	priorDirectionality = Boolean.parseBoolean(cmd.getOptionValue(myOptions.optArgPriorDirectionality.getLongOpt()));
            }

            if (cmd.hasOption(myOptions.optArgApproximatePermutations.getLongOpt())) {
            	approximatePermutations = Boolean.parseBoolean(cmd.getOptionValue(myOptions.optArgApproximatePermutations.getLongOpt()));
            }
            
//            if (cmd.hasOption(myOptions.optMarkovBlanket.getLongOpt())) {
//                isConsideringMarkovBlanket = true;
//            }

            if (cmd.hasOption(myOptions.optArgNumberOfStructures.getOpt())) {
                numberOfProbableStructure = Integer.parseInt(cmd.getOptionValue(myOptions.optArgNumberOfStructures.getOpt()));
            }

            if (cmd.hasOption(myOptions.optArgNumberOfPermutations.getOpt())) {
                numberOfPermutation = Integer.parseInt(cmd.getOptionValue(myOptions.optArgNumberOfPermutations.getOpt()));
            }

            if (cmd.hasOption(myOptions.optArgPValueThreshold.getOpt())) {
                pValueThreshold = Double.parseDouble(cmd.getOptionValue(myOptions.optArgPValueThreshold.getOpt()));
            }

            if (cmd.hasOption(myOptions.optArgResamplingRate.getOpt())) {
                resamplingRate = Double.parseDouble(cmd.getOptionValue(myOptions.optArgResamplingRate.getOpt()));
            }

            if (cmd.hasOption(myOptions.optArgPriorWeight.getOpt())) {
                priorWeight = Double.parseDouble(cmd.getOptionValue(myOptions.optArgPriorWeight.getOpt()));
            }

//            if (cmd.hasOption(myOptions.optArgEquivSampleSize.getOpt())) {
//                equivalentSampleSize = Double.parseDouble(cmd.getOptionValue(myOptions.optArgEquivSampleSize.getOpt()));
//            }

            if (cmd.hasOption(myOptions.optArgMinGeneSetSize.getOpt())) {
                minGeneSetSize = Integer.parseInt(cmd.getOptionValue(myOptions.optArgMinGeneSetSize.getOpt()));
            }

            if (cmd.hasOption(myOptions.optArgMaxGeneSetSize.getOpt())) {
                maxGeneSetSize = Integer.parseInt(cmd.getOptionValue(myOptions.optArgMaxGeneSetSize.getOpt()));
            }

//            if (cmd.hasOption(myOptions.optArgSelectedCollections.getOpt())) {
//                selectedCollections = cmd.getOptionValues(myOptions.optArgSelectedCollections.getOpt());
//            }

            if (cmd.hasOption(myOptions.optArgQuickPermutation.getOpt())) {
                quickPermutationEnabled = Boolean.parseBoolean(cmd.getOptionValue(myOptions.optArgQuickPermutation.getOpt()));
            }

            return true;

        } catch (ParseException e) {
            // print the date
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("eddy", myOptions.getOptions(), true);


            // TODO Auto-generated catch block			
            // e.printStackTrace();
            return false;
        }
    }


    static void initializeData() {

//        permutedJS = new double[numberOfPermutation];

        if (timeLimitInHour != -1) {
            endTime = startTime + timeLimitInHour * 60 * 60 * 1000;
        }

        // Read the data file.
        data = new Data(dataFileName);

        // Build all variables.
        allVariable = data.buildAllVariable();

        // Read the target gene set.
        GeneSet geneSet = new GeneSet(allVariable, targetGeneSetFileName, minGeneSetSize, maxGeneSetSize, selectedCollections);
        listOfTargetGeneSets = geneSet.buildListOfGeneSets();

        // Read sample class labels.
        readSampleClass();

        // Initialize remaining fields.
        if (numberOfThreads == 0)
            numberOfThreads = getRuntime().availableProcessors();

        // Initialize BufferedWriter with preliminary info for log file.
        String fileInfo = "EDDY OUTPUT FILE\n";
        fileInfo += ("Data File: " + dataFileName + "\n");
        fileInfo += ("Target Gene Set(s) File: " + targetGeneSetFileName + "\n");
        fileInfo += ("Class Label File: " + sampleClassInformationFileName + "\n");
        fileInfo += ("Number of Gene Sets: " + listOfTargetGeneSets.size() + "\n");
        fileInfo += ("Number of Threads: " + numberOfThreads + "\n\n");
        
        // log command line options, in verbatim 
        fileInfo += concatStrings(commandLine) + "\n\n";
        
        fileInfo += ("Name: \tCollection: \tSize: \tURL: \tJS Divergence: \tP-Value: \t#Permutations: \tGenes: \n");
        try {
        	// TODO: need to come up with a better way to assign the output file name.
            String fileName = targetGeneSetFileName.substring(0, targetGeneSetFileName.indexOf(".gmt")) + "_output.txt";
            
            File file = new File(fileName);
            
            output = new BufferedWriter(new FileWriter(file));
            output.write(fileInfo);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }
    
    static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    static String concatStrings(String[] strs) {
    	String s = strs[0];
    	
    	for ( int i=1; i <strs.length; i++ )
    		s += " " + strs[i];
    	
    	return s;
    }
    
    static void readSampleClass() {
        // Read class information.
        if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
            System.out.println("Reading class information from " + sampleClassInformationFileName + "...");

        sampleClass = new SampleClass(data.numberOfSample);

        try {
            StreamTokenizer st = new StreamTokenizer(new FileReader(sampleClassInformationFileName));
            st.ordinaryChars(32, 127);
            st.wordChars(32, 127);
            st.whitespaceChars('\t', '\t');
            st.eolIsSignificant(true);
            int dataType = st.nextToken();
            int i = 0;

            while (dataType != StreamTokenizer.TT_EOF) {
                if (dataType != StreamTokenizer.TT_EOL) {
                    sampleClass.set(st.sval, i);
                    i++;
                }

                dataType = st.nextToken();
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }

        if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
            System.out.println("Number of classes: " + sampleClass.numberOfClass);

    }

}
