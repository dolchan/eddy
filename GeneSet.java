package eddy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.*;

/**
 * Created by nlalani on 6/20/14.
 */
public class GeneSet {
    public VariableSet allVariable;
    public String targetGeneSetFileName;
    public ArrayList<VariableSet> list;
    public String[] collections;
    public int lowerSizeLimit = -1;
    public int upperSizeLimit = -1;

    public GeneSet (VariableSet v, String file, int from, int to, String[] c) {
        allVariable = v;
        targetGeneSetFileName = file;
        list = new ArrayList<VariableSet>();
        lowerSizeLimit = from;
        upperSizeLimit = to;
        collections = c;
    }
    
    public GeneSet (VariableSet v, String file, int from, int to) {
        allVariable = v;
        targetGeneSetFileName = file;
        list = new ArrayList<VariableSet>();
        lowerSizeLimit = from;
        upperSizeLimit = to;
        collections = new String[0];
    }

    
    public GeneSet (VariableSet v, String file) {
        allVariable = v;
        targetGeneSetFileName = file;
        list = new ArrayList<VariableSet>();
    }


    public ArrayList<VariableSet> buildListOfGeneSets () {

        if (RuntimeConfiguration.IS_DEBUG_MODE_LV2)
            System.out.println("Reading the list of target variable sets from " + targetGeneSetFileName);

        try {
            Scanner sc = new Scanner(new File(targetGeneSetFileName));
            while (sc.hasNextLine())
            	list.add(parseGeneSet(sc.nextLine()));
            
        } catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        }

        // Order list by size.
        Collections.sort(list, new VariableSetComparator());

        // Select for sets by size interval.
        this.selectBySize();

        // Select for sets by collection(s).
        this.selectByCollection();

        return list;
    }

    public VariableSet parseGeneSet (String line) {
        Scanner sc = new Scanner(line);
        
        // SK: tab delimited file
        sc.useDelimiter("\t");
        VariableSet targetGeneSetVariable = new VariableSet();
        Vector<String> targetGeneSetVariableName = new Vector<String>();

        String name = sc.next();
        targetGeneSetVariable.setName(name);

        targetGeneSetVariable.setCollection(targetGeneSetFileName);

        String url = sc.next();
        targetGeneSetVariable.setUrl(url);
        
        while (sc.hasNext())
            targetGeneSetVariableName.add(sc.next());

        if (RuntimeConfiguration.IS_DEBUG_MODE_LV2) {
            System.out.println("Number of target variables: " + targetGeneSetVariableName.size());
        }

        targetGeneSetVariable.setPriorSize(targetGeneSetVariableName.size());

        for (int i = 0; i < targetGeneSetVariableName.size(); i++) {
            String variableName = targetGeneSetVariableName.get(i);
//            System.out.println("Variable name: " + variableName);
            Variable targetVariable = allVariable.get(variableName);
            targetGeneSetVariable.priorAdd(variableName);

            if (targetVariable != null)
                targetGeneSetVariable.add(targetVariable);
        }
        
        targetGeneSetVariable.setSize();

        return (targetGeneSetVariable);
        //System.out.println(targetGeneSetVariable.toString());
    }


    public void selectBySize() {

        // If no lower & upper bounds
        if ((lowerSizeLimit == -1) && (upperSizeLimit == -1)) {return;}
        // If has lower bound
        else if (lowerSizeLimit == -1) {lowerSizeLimit = 1;}
        // If has upper bound
        else if (upperSizeLimit == -1) {upperSizeLimit = Integer.MAX_VALUE;}

        ArrayList<VariableSet> newList = new ArrayList<VariableSet>();

        for (VariableSet v: list) {
            // If v's size is within the bound interval, add to list.
            if ((v.getSize() >= lowerSizeLimit) && (v.getSize() <= upperSizeLimit))
                newList.add(v);
        }

        list = newList;
    }

    public void selectByCollection() {

        if (collections.length == 0) {return;}
        ArrayList<VariableSet> newList = new ArrayList<VariableSet>();

        for (VariableSet v: list) {
            // If v's collection matches one of those selected by the user, add to list.
            if (Arrays.asList(collections).contains(v.getCollection()))
                newList.add(v);
        }

        list = newList;
    }

    // no longer used -- can safely remove this method.
    public void parseLine (String line) {
        Scanner sc = new Scanner(line);
        VariableSet targetGeneSetVariable = new VariableSet();
        Vector<String> targetGeneSetVariableName = new Vector<String>();

//        if (isMultipleGeneSet) {
            String name = sc.next();
            targetGeneSetVariable.setName(name);
            
            String collection = sc.next();
            targetGeneSetVariable.setCollection(collection);
            
            sc.next();  // # of genes (size)
            
            String url = sc.next();
            targetGeneSetVariable.setUrl(url);
//        }

        while (sc.hasNext())
            targetGeneSetVariableName.add(sc.next());

        if (RuntimeConfiguration.IS_DEBUG_MODE_LV2) {
            //System.out.println("Number of target variables: " + targetGeneSetVariableName.size());
        }

        int size = 0;
        for (int i = 0; i < targetGeneSetVariableName.size(); i++) {
            String variableName = targetGeneSetVariableName.get(i);
            Variable targetVariable = allVariable.get(variableName);

            if (targetVariable != null) {
                targetGeneSetVariable.add(targetVariable);
                size++;
            }
        }
        targetGeneSetVariable.setSize(size);
        list.add(targetGeneSetVariable);
        
        //System.out.println(targetGeneSetVariable.toString());
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < list.size(); i++)
            s += ("\nVariableSet #" + (i + 1) + list.get(i).toString());
        return s;
    }

    public ArrayList<VariableSet> getList() {return this.list;}


//    For Testing Only
    public static void main(String[] args) {
//        String geneSet = "ATP_DEPENDENT_DNA_HELICASE_ACTIVITY.txt";
        //String geneSet = "MSigDB_Tester.txt";
//        String geneSet = "MergedMSigDB_4.0_all.txt";
        String geneSet = "c2.cp.biocarta.v4.0.entrez.gmt.txt";
        String dataFile = "TCGA_ACC_mRNA_discrete_entrezID.txt";
        Data data = new Data(dataFile);
        VariableSet allVariable = data.buildAllVariable();
        // String[] collec = new String[] {"C2", "C4", "C5"};
        GeneSet g = new GeneSet(allVariable, geneSet, 0, 20);
        long start = System.currentTimeMillis()/1000;
        g.buildListOfGeneSets();
        long end = System.currentTimeMillis()/1000;
        System.out.println("Done. Elapsed Time: " + (int)(end-start));
        System.out.println(g.toString());
    }
}

















