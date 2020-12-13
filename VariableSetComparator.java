package eddy;

import java.util.Comparator;

public class VariableSetComparator implements Comparator<VariableSet> {
	@Override
	public int compare(VariableSet v1, VariableSet v2) {
		return v2.size() - v1.size();
	}
}