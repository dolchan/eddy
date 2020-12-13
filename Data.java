package eddy;

import java.io.FileReader;
import java.io.StreamTokenizer;

public class Data {
	public VariableData[] variableData;
	public String[] variableName;
	public String[] sampleName;
	public int numberOfVariable;
	public int numberOfSample;

	public Data(int numberOfVariable, int numberOfSample) {
		// TODO Auto-generated constructor stub
		variableData = new VariableData[numberOfVariable];
		variableName = new String[numberOfVariable];
		sampleName = new String[numberOfSample];
		this.numberOfVariable = numberOfVariable;
		this.numberOfSample = numberOfSample;
	}
	
	public Data(String fileName) {
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Reading the data file " + fileName + "...");
		
		readFromFile(fileName);
	}
	
	public VariableData getVariableData(String vName) {
		for (int i = 0; i < numberOfVariable; i++) {
			if (variableName[i].equals(vName)) {
				return variableData[i];
			}
		}
		
		return null;
	}
	
	public VariableSet buildAllVariable() {
		if (RuntimeConfiguration.IS_DEBUG_MODE)
			System.out.println("Building all variable instances from the data...");
		
		VariableSet allVariable = new VariableSet();
		
		for (int i = 0; i < numberOfVariable; i++) {
			Variable v = new Variable(variableName[i]);
			v.setData(variableData[i]);
			allVariable.add(v);
		}
		
		return allVariable;
	}
	
	public Data getSubsetSamples(String subsetSamples[]) {
		
		int nSubsetSamples = subsetSamples.length;
		int[] subsetIndex = new int[nSubsetSamples];
		
		for (int i = 0; i < nSubsetSamples; i++) {
			for (int j = 0; j < numberOfSample; j++) {
				if (sampleName[j].equals(subsetSamples[i])) {	
					subsetIndex[i] = j;
				}
			}
		}

		return getSubsetSamples(subsetIndex);		
	}
	
	public Data getSubsetSamples(boolean includedSamples[]) {
		int nSubsetSamples = 0;
		
		if (includedSamples.length != this.numberOfSample)
			return null;
		
		for (int i = 0; i < includedSamples.length; i++)
			if (includedSamples[i])
				nSubsetSamples++;
		
		int subsetSampleID[] = new int[nSubsetSamples];
		int idx = 0;
		for (int i = 0; i < includedSamples.length; i++)
			if (includedSamples[i])
				subsetSampleID[idx++] = i;
		
		return getSubsetSamples(subsetSampleID);
	}
	
	public Data getSubsetSamples(int subsetSampleID[]) {
		
		int nSubsetSamples = subsetSampleID.length;
		
		Data data = new Data(this.numberOfVariable, subsetSampleID.length);
		
		for (int i = 0; i < nSubsetSamples; i++)
			data.sampleName[i] = this.sampleName[subsetSampleID[i]];		
		
		for (int i = 0; i < this.numberOfVariable; i++) {
			
			data.variableName[i] = this.variableName[i];
			
			VariableData vd = new VariableData();
			String[] vv = new String[nSubsetSamples];
			
			for (int j = 0; j < nSubsetSamples; j++)
				vv[j] = this.variableData[i].value[subsetSampleID[j]];
			
			vd.setData(vv);
			data.variableData[i] = vd;
		}
		
		return data;
	}
	
	public Data aCopy() {
		Data data = new Data(numberOfVariable, numberOfSample);
				
		for (int i = 0; i < numberOfSample; i++)
			data.sampleName[i] = this.sampleName[i];
		
		for (int i = 0; i < numberOfVariable; i++) {
			data.variableName[i] = this.variableName[i];
			data.variableData[i] = new VariableData();
			
			String[] vData = new String[numberOfSample];
			
			for (int j = 0; j < numberOfSample; j++) {
				vData[j] = this.variableData[i].value[j];
			}
			data.variableData[i].setData(vData);
		}

		return (data);
	}
	
	private void readFromFile(String fileName) {
		// Check the number of variables and number of samples
		try {
			StreamTokenizer st = new StreamTokenizer(new FileReader(fileName));
			st.ordinaryChars(32, 127);
			st.wordChars(32, 127);
			st.whitespaceChars('\t', '\t');
			st.eolIsSignificant(true);
			int dataType = st.nextToken();	// The first token of the first line is a header for variables.
			numberOfVariable = 0;
			numberOfSample = 0;
			dataType = st.nextToken();	// Begin to read sample names.
			
			while (dataType != StreamTokenizer.TT_EOL) {
				numberOfSample++;
				dataType = st.nextToken();
			}
			
			// The first line is gone. Count EOL as the number of variables until EOF.
			dataType = st.nextToken();
			
			while (dataType != StreamTokenizer.TT_EOF) {
				if (dataType == StreamTokenizer.TT_EOL)
					numberOfVariable++;
				
				dataType = st.nextToken();
			}
			
			//numberOfVariable++;
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		// Initialize data variables.
		sampleName = new String[numberOfSample];
		variableData = new VariableData[numberOfVariable];
		variableName = new String[numberOfVariable];
		
		// Read the data file.
		try {
			StreamTokenizer st = new StreamTokenizer(new FileReader(fileName));
			st.ordinaryChars(32, 127);
			st.wordChars(32, 127);
			st.whitespaceChars('\t', '\t');
			st.eolIsSignificant(true);
			st.nextToken();	// Read out the header for variables.
			
			for (int i = 0; i < numberOfSample; i++) {	// Read the sample names.
				st.nextToken();
				sampleName[i] = st.sval;
			}
			
			st.nextToken();	// Read out EOL.
			
			for (int i = 0; i < numberOfVariable; i++) {	// Read variable data.
				st.nextToken();	// This is a variable name.
				variableName[i] = st.sval;
				if (variableName[i]==null) {
					System.out.println("Bad format input on line " + i + " of DataFile.  First field read as null.");
					System.exit(-1);
				}
				variableData[i] = new VariableData();
				String[] vData = new String[numberOfSample];
				
				for (int j = 0; j < numberOfSample; j++) {
					st.nextToken();
					vData[j] = st.sval;
				}
				
				variableData[i].setData(vData);
				st.nextToken();	// Read out EOL.

				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println(i + "/" + numberOfVariable + ": " + variableName[i] + " read.");
			}
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.println("Number of variables: " + numberOfVariable);
			System.out.println("Number of samples: " + numberOfSample);
		}
	}
}
