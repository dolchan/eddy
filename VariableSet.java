package eddy;

import java.util.Vector;

public class VariableSet {
	public Vector<Variable> set;
	public Vector<String> priorSet;
    int size;
    int priorSize;
	String collection;
    String url;
    String name;

	public VariableSet() {
		// TODO Auto-generated constructor stub
		set = new Vector<Variable>();
		priorSet = new Vector<String>();
	}
	
	public void beUnionWith(VariableSet another) {
		for (int i = 0; i < another.size(); i++) {
			Variable v = another.get(i);
			
			if (!set.contains(v)) {
				set.add(v);
			}
		}
	}

	
	public boolean equals(Object s) {
		
		VariableSet another = (VariableSet) s;
		return equals(another);

/*
		if (set.size() != another.size())
			return false;
		
		for (int i = 0; i < set.size(); i++) {
			Variable thisVariable = set.get(i);
			Variable anotherVariable = another.get(i);
			
			if (!thisVariable.equals(anotherVariable))
				return false;
		}
		return true;
*/
	}
	
	public boolean equals(VariableSet another) {
		if (set.size() != another.size())
			return false;
		
		for (int i = 0; i < set.size(); i++) {
			Variable thisVariable = set.get(i);
			Variable anotherVariable = another.get(i);
			
			if (!thisVariable.equals(anotherVariable))
				return false;
		}
		
		return true;
	}
	
	public VariableSet recreate(VariableSet allVariable) {
		
        Vector<String> targetGeneSetVariableName = new Vector<String>();

        int sz = set.size();
        for (int i = 0; i < sz; i++) {
        	targetGeneSetVariableName.add(set.get(i).name);
        }
        
        set.removeAllElements();
        
        sz = 0;
        for (int i = 0; i < targetGeneSetVariableName.size(); i++) {
            String variableName = targetGeneSetVariableName.get(i);
            Variable targetVariable = allVariable.get(variableName);

            
            if (targetVariable != null) {
                this.add(targetVariable);
                sz++;
            }
        }
        this.setSize(sz);
                
        return this;
	}
	
//	public VariableSet recreate(VariableSet allVariable) {
//		
//		VariableSet targetGeneSetVariable = new VariableSet();
//        Vector<String> targetGeneSetVariableName = new Vector<String>();
//
//        targetGeneSetVariable.setCollection(this.getCollection());
//        targetGeneSetVariable.setName(this.getName());
//        targetGeneSetVariable.setUrl(this.getUrl());
//        
//        int sz = set.size();
//        for (int i = 0; i < sz; i++) {
//        	targetGeneSetVariableName.add(set.get(i).name);
//        }
//        sz = 0;
//        for (int i = 0; i < targetGeneSetVariableName.size(); i++) {
//            String variableName = targetGeneSetVariableName.get(i);
//            Variable targetVariable = allVariable.get(variableName);
//
//            if (targetVariable != null) {
//                targetGeneSetVariable.add(targetVariable);
//                sz++;
//            }
//        }
//        targetGeneSetVariable.setSize(sz);
//        
//        // empty this before return;  needed (???)
//        this.set.removeAllElements();
//        
//        return targetGeneSetVariable;
//	}
	
	
	
	public void reloadData(Data data) {
		int temp = 0;
		for (int i = 0; i < set.size(); i++) {
			set.get(i).reloadData(data);
			
			/* this is temporary for debugging */
			if (i == 0)
				temp = set.get(i).getData().length;
			else {
				if (temp != set.get(i).getData().length)
				{
					Variable v = set.get(i);
					System.out.println(v);
					System.out.println("[[ " + i + " ]] " + temp + " | " + set.get(i).getData().length);
				}
			}
		}
	}
	
	public int size() {return set.size();}
	public int priorSize() {return priorSize;}
	
	public Variable get(int index) {
		return set.get(index);
	}
	public String priorGet(int index) {
		return priorSet.get(index);
	}
	
	public void add(Variable v) {
		set.add(v);
	}
	
	public void priorAdd(String v) {
		priorSet.add(v);
	}
	
	public Variable remove(int index) {
		return set.remove(index);
	}
	
	public boolean contains(Variable v) {
		return set.contains(v);
	}
	
	public boolean remove(Variable v) {
		return set.remove(v);
	}
	
	public int indexOf(Variable v) {
		return set.indexOf(v);
	}
	
	public int indexOf(String variableName) {
		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).name.equals(variableName)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public int priorIndexOf(String variableName) {
		for (int i = 0; i < priorSet.size(); i++) {
			if (priorSet.get(i).equals(variableName)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public Variable get(String variableName) {
		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).name.equals(variableName)) {
				return set.get(i);
			}
		}
		
		return null;
	}

    public String toString() {
        String s = "";

        s += ("\nName: " + this.name);
        s += ("\nCollection: " + this.collection);
        s += ("\nSize: " + this.size);
        s += ("\nURL: " + this.url);
        String genes = "";
        for (int j = 0; j < set.size(); j++) {
            genes +=  set.get(j).name + " ";
        }
        s += ("\nList of genes: " + genes + "\n");

        return s;
    }

    // Constructs String as a single tab-delimited line for output to log file.
    // e.g. "chr15q	C1	10	http://www.broadinstitute.org/gsea/msigdb/cards/chr15q	0.687146871	 0
    //                          PEX11A	KBTBD13	AQP9	RPS17	NEDD4	LCS1	AP3B2	PIAS1	OCA2	HYT2"
    public String[] toStringLog() {

        String[] s = new String[2];
        String labels = (this.name + "\t");
        labels += (this.collection + "\t");
        labels += (this.size + "\t");
        labels += (this.url);

        String genes = "";
        for (int j = 0; j < set.size() - 1; j++) {
            genes += set.get(j).name + "\t";
        }
        
        // the last one does not add '\t' character.
        if (set.size() > 0)
        	genes += set.get(set.size()-1).name;
        
        s[0] = labels;
        s[1] = genes;

        return s;
    }


	public String getName() {
		return name;
	}

    public void setName(String name) {
        this.name = name;
    }

    public String getCollection() {
        return collection;
    }
    
    public void setCollection(String collection) {
        this.collection = collection;
    }

    public int getSize() {
		return size;
	}

    public void setSize (int size) {
        this.size = size;
    }

    public void setPriorSize (int size) {
        this.priorSize = size;
    }

    public void setSize() {
        setSize(this.set.size());
    }

    public String getUrl () {
        return url;
    }
    
    public void setUrl (String url) {
        this.url = url;
    }
}
