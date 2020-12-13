package eddy;

import java.util.Vector;

public class VariableData {
	public String[] value;
	public String[] possibleValue;

	public VariableData() {
		// TODO Auto-generated constructor stub
	}
	
	public void setData(String[] d) {
		value = d;
		Vector<String> valueVec = new Vector<String>();
		
		for (int i = 0; i < value.length; i++) {
			if (!valueVec.contains(value[i]))
				valueVec.add(value[i]);
		}
		
		possibleValue = new String[valueVec.size()];
		possibleValue = valueVec.toArray(possibleValue);
	}

}
