package eddy;

import java.util.Iterator;
import java.util.Vector;

public class DependencyStructureSet {
	public Vector<DependencyStructure> structureSet;

	public DependencyStructureSet() {
		// TODO Auto-generated constructor stub
		structureSet = new Vector<DependencyStructure>();
	}
	
	public void add(DependencyStructure s) {
		structureSet.add(s);
	}
	
	public boolean contains(DependencyStructure s) {
		return structureSet.contains(s);
	}
	
	public int size() {
		return structureSet.size();
	}
	
	public DependencyStructure get(int i) {
		return structureSet.get(i);
	}

	public String toString() {
		String s = "";
		
		Iterator<DependencyStructure> iter = structureSet.iterator();
		
		while (iter.hasNext()) {
			s += (iter.next());
			
			s += "\n";
		}
		
		return s;
	}
}
