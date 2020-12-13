package eddy;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Stack;
import java.util.Vector;

public class DependencyStructureMap {
	public int[][] map;	// Directed adjacency matrix.
	public int maxParentAmount = -1;	// -1 represents no limitation.

	public DependencyStructureMap(int numberOfVariable) {
		// TODO Auto-generated constructor stub
		map = new int[numberOfVariable][numberOfVariable];
	}
	
	public DependencyStructureMap(int numberOfVariable, int maxParentAmount) {
		// TODO Auto-generated constructor stub
		map = new int[numberOfVariable][numberOfVariable];
		this.maxParentAmount = maxParentAmount;
	}
	
	public int HammingDistance(DependencyStructureMap another) {
		int distance = 0;
		
		for (int i = 0; i < map.length - 1; i++) {
			for (int j = i + 1; j < map.length; j++) {
				if (map[i][j] != another.map[i][j] || map[j][i] != another.map[j][i])
					distance++;
			}
		}
		
		return distance;
	}
	
	public int undirectedHammingDistnace(DependencyStructureMap another) {
		int[][] undirectedMap = new int[map.length][map.length];
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j] == 1) {
					undirectedMap[i][j] = 1;
					undirectedMap[j][i] = 1;
				}
			}
		}
		
		int [][] anotherUndirectedMap = new int[another.map.length][another.map.length];
		
		for (int i = 0; i < another.map.length; i++) {
			for (int j = 0; j < another.map[0].length; j++) {
				if (another.map[i][j] == 1) {
					anotherUndirectedMap[i][j] = 1;
					anotherUndirectedMap[j][i] = 1;
				}
			}
		}
		
		int distance = 0;
		
		for (int i = 0; i < undirectedMap.length; i++) {
			for (int j = 0; j < undirectedMap[0].length; j++) {
				if (undirectedMap[i][j] != anotherUndirectedMap[i][j])
					distance++;
			}
		}
		
		distance /= 2;
		return distance;
	}
	
	public boolean equals(Object m) {
		DependencyStructureMap another = (DependencyStructureMap) m;
		
		if (map.length != another.map.length)
			return false;
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j] != another.map[i][j])
					return false;
			}
		}
		
		return true;
	}
	
	public boolean equals(DependencyStructureMap another) {
		if (map.length != another.map.length)
			return false;
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j] != another.map[i][j])
					return false;
			}
		}
		
		return true;
	}
	
	public DependencyStructureMap copy() {
		DependencyStructureMap newCopy = new DependencyStructureMap(map.length, maxParentAmount);
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++)
				newCopy.map[i][j] = map[i][j];
		}
		
		return newCopy;
	}
	
	// This method works like increasing a binary number.
	/*public Vector<int[]> modifySelfToNextMap() {
		Vector<int[]> modifiedCell = new Vector<int[]>();	// Keeps the call locations where all "1->0" happened. The last element is for "0 -> 1".
		boolean isModificationDone = false;
		DependencyStructureMap tempMap = copy();	// Work on the temporary map. If modification was successful on the temporary map, replace this.map with the temporary map.
		
		for (int i = 0; i < tempMap.map.length; i++) {
			for (int j = 0; j < tempMap.map[0].length; j++) {
				if (i == j)	// Skip diagonal cells.
					continue;
				
				if (tempMap.map[i][j] == 1) {
					tempMap.map[i][j] = 0;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
				}
				else {	// This cell has a value 0.
					if (tempMap.map[j][i] == 1)	// If i and j are already connected (in opposite direction), skip.
						continue;
					
					tempMap.map[i][j] = 1;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
					isModificationDone = true;
					this.map = tempMap.map;
					break;
				}
			}
			
			if (isModificationDone)
				break;
		}
		
		if (isModificationDone)
			return modifiedCell;
		else
			return null;
	}*/
	
	
	// This will be used for debugging.
	public void printMap() {
		
		System.out.println(this);
/*
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++)
				System.out.print(" " + map[i][j]);
			
			System.out.println("");
		}
*/
	}
	
	public String toString() {
		String s = "";
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++)
				s += (" " + map[i][j]);
			
			s += "\n";
		}
		
		return s; 
	}
	
	
	// This method works like increasing a binary number.
	public Vector<int[]> modifySelfToNextMap() {
		Vector<int[]> modifiedCell = new Vector<int[]>();	// Keeps the call locations where all "1->0" happened. The last element is for "0 -> 1".
		boolean isModificationDone = false;
		DependencyStructureMap tempMap = copy();	// Work on the temporary map. If modification was successful on the temporary map, replace this.map with the temporary map.
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.println("Modifying a structure from:");
			tempMap.printMap();
		}
		
		for (int i = 0; i < tempMap.map.length; i++) {
			for (int j = 0; j < tempMap.map[0].length; j++) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Testing (" + i + ", " + j + ")...");
				
				if (i == j)	// Skip diagonal cells.
					continue;
				
				if (tempMap.map[i][j] == 1) {
					tempMap.map[i][j] = 0;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
				}
				else {	// This cell has a value 0.
					if (tempMap.map[j][i] == 1)	// If i and j are already connected (in opposite direction), skip.
						continue;
					
					// If j already has max amount of parents, skip.
					if (maxParentAmount > -1) {
						if (parentAmount(j) >= maxParentAmount)
							continue;
					}
					
					if (tempMap.causeCycle(i, j))	// If this causes a cycle, skip.
						continue;
					
					tempMap.map[i][j] = 1;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
					isModificationDone = true;
					this.map = tempMap.map;
					break;
				}
			}
			
			if (isModificationDone)
				break;
		}
		
		if (isModificationDone) {
			if (RuntimeConfiguration.IS_DEBUG_MODE) {
				System.out.println("Modified to:");
				printMap();
			}
			
			return modifiedCell;
		}
		else {
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("No more structure to explore.");
			
			return null;
		}
	}
	
	// Returns the number of parents that ith row has.
	private int parentAmount(int i) {
		int prAmount = 0;
		
		for (int j = 0; j < map[i].length; j++)
			prAmount += map[i][j];
		
		return prAmount;
	}
	
	// It does not modify the map for the area of (i~j)x(i~j).
	public Vector<int[]> modifySelfToNextMapWithCoreKept(int coreBegin, int coreEnd) {
		Vector<int[]> modifiedCell = new Vector<int[]>();	// Keeps the call locations where all "1->0" happened. The last element is for "0 -> 1".
		boolean isModificationDone = false;
		DependencyStructureMap tempMap = copy();	// Work on the temporary map. If modification was successful on the temporary map, replace this.map with the temporary map.
		
		if (RuntimeConfiguration.IS_DEBUG_MODE) {
			System.out.println("Modifying a structure from (while keeping " + coreBegin + " ~ " + coreEnd + "):");
			tempMap.printMap();
		}
		
		for (int i = 0; i < tempMap.map.length; i++) {
			for (int j = 0; j < tempMap.map[0].length; j++) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Testing (" + i + ", " + j + ")...");
				
				if ((i >= coreBegin && i <= coreEnd) && (j >= coreBegin && j <= coreEnd))	// Do not touch the core area.
					continue;
				
				if (i == j)	// Skip diagonal cells.
					continue;
				
				if (tempMap.map[i][j] == 1) {
					tempMap.map[i][j] = 0;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
				}
				else {	// This cell has a value 0.
					if (tempMap.map[j][i] == 1)	// If i and j are already connected (in opposite direction), skip.
						continue;
					
					// If j already has max amount of parents, skip.
					if (maxParentAmount > -1) {
						if (parentAmount(j) >= maxParentAmount)
							continue;
					}
					
					if (tempMap.causeCycle(i, j))	// If this causes a cycle, skip.
						continue;
					
					tempMap.map[i][j] = 1;
					int[] cellLocation = new int[2];
					cellLocation[0] = i;
					cellLocation[1] = j;
					modifiedCell.add(cellLocation);
					isModificationDone = true;
					this.map = tempMap.map;
					break;
				}
			}
			
			if (isModificationDone)
				break;
		}
		
		if (isModificationDone) {
			if (RuntimeConfiguration.IS_DEBUG_MODE) {
				System.out.println("Modified to:");
				printMap();
			}
			
			return modifiedCell;
		}
		else {
			if (RuntimeConfiguration.IS_DEBUG_MODE)
				System.out.println("No more structure to explore.");
			
			return null;
		}
	}
	
	private int[] getChildIndex(int i) {
		int numberOfChildren = 0;
		
		for (int j = 0; j < map[i].length; j++)
			numberOfChildren += map[i][j];
		
		if (numberOfChildren == 0)
			return null;
		
		int[] childIndex = new int[numberOfChildren];
		int childIndexMark = 0;
		
		for (int j = 0; j < map[i].length; j++) {
			if (map[i][j] == 1) {
				childIndex[childIndexMark] = j;
				childIndexMark++;
			}
		}
		
		return childIndex;
	}
	
	public boolean causeCycle(int parentIndex, int childIndex) {
		Stack<Integer> nodeToVisit = new Stack<Integer>();
		int curNode;
		int i;
		int[] children;

		curNode = childIndex;
		children = getChildIndex(curNode);

		if (children != null) {
			for (i = 0;  i < children.length; i++) {
				if (children[i] == parentIndex) {
					if (RuntimeConfiguration.IS_DEBUG_MODE)
						System.out.println("Cycle");
					
					return true;
				}
				
				nodeToVisit.push(new Integer(children[i]));
			}
		}

		if (nodeToVisit.empty())
			return false;

		do {
			curNode = nodeToVisit.pop().intValue();

			if (curNode == parentIndex) {
				if (RuntimeConfiguration.IS_DEBUG_MODE)
					System.out.println("Cycle");
				
				return true;
			}

			children = getChildIndex(curNode);

			if (children != null) {
				for (i = 0;  i < children.length; i++)
					nodeToVisit.push(new Integer(children[i]));
			}
		} while (!nodeToVisit.empty());

		return false;
	}
}
