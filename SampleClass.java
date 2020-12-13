package eddy;

import java.util.Random;
import java.util.Vector;

public class SampleClass {
	public String[] classLabel;
	public int numberOfClass;
	public Vector<String> uniqueClassLabel;

	public SampleClass(int numberOfSample) {
		// TODO Auto-generated constructor stub
		classLabel = new String[numberOfSample];
		uniqueClassLabel = new Vector<String>();
		numberOfClass = 0;
	}
	
	public SampleClass aCopy() {
		SampleClass newClass = new SampleClass(classLabel.length);
		newClass.numberOfClass = numberOfClass;
		newClass.uniqueClassLabel = uniqueClassLabel;
		for (int i = 0; i < classLabel.length; i++)
			newClass.classLabel[i] = classLabel[i];
		
		return newClass;
	}
	
	public SampleClass permutedCopy() {
		SampleClass newClass = new SampleClass(classLabel.length);
		newClass.numberOfClass = numberOfClass;
		newClass.uniqueClassLabel = uniqueClassLabel;
		newClass.classLabel = permute(classLabel);
		return newClass;
	}
	
	private String[] permute(String[] originalArray) {
		String[] permutedArray = new String[originalArray.length];
		Vector<Integer> originalArrayIndex = new Vector<Integer>();

		for (int i = 0; i < originalArray.length; i++)
			originalArrayIndex.add(new Integer(i));

		Random rd = new Random(System.currentTimeMillis());

		for (int i = 0; i < permutedArray.length; i++) {
			int randomlyChosenOne = rd.nextInt(originalArrayIndex.size());
			int randomlyChosenArrayIndex = originalArrayIndex.remove(randomlyChosenOne).intValue();
			permutedArray[i] = originalArray[randomlyChosenArrayIndex];
		}

		return permutedArray;
	}
	
	public String getClassName(int i) {
		return uniqueClassLabel.get(i);
	}
	
	public void set(String value, int index) {
		classLabel[index] = value;
		
		if (!uniqueClassLabel.contains(value)) {
			uniqueClassLabel.add(value);
			numberOfClass = uniqueClassLabel.size();
			if (numberOfClass > 2) {
				System.out.println("Exiting: EDDY only supports two classes.");
				System.exit(-1);
			}
		}
	}
	
	public Data getClassDataFrom(Data data, String className) {
		// Count the number of samples for class "className".
		int numberOfClassSample = 0;
		
		for (int i = 0; i < classLabel.length; i++) {
			if (classLabel[i].equals(className))
				numberOfClassSample++;
		}
		
		int[] classIndex = new int[numberOfClassSample];
		int tempIndex = 0;
		
		for (int i = 0; i < classLabel.length; i++) {
			if (classLabel[i].equals(className)) {
				classIndex[tempIndex] = i;
				tempIndex++;
			}
		}

		Data classData = data.getSubsetSamples(classIndex);
		
/*
 * TODO: remove this later
 * 
 *   		Data classData = new Data(data.numberOfVariable, numberOfClassSample);
		
		for (int i = 0; i < data.numberOfVariable; i++) {
			classData.variableName[i] = data.variableName[i];
			
			VariableData vd = new VariableData();
			String[] vv = new String[numberOfClassSample];
			
			for (int j = 0; j < numberOfClassSample; j++)
				vv[j] = data.variableData[i].value[classIndex[j]];
			
			vd.setData(vv);
			classData.variableData[i] = vd;
		}
		
		for (int i = 0; i < numberOfClassSample; i++)
			classData.sampleName[i] = data.sampleName[classIndex[i]];
*/		
		return classData;
	}

}
