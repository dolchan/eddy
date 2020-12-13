package eddy;

import java.util.Vector;

public class PrioritizedList {
	public Vector<Double> score;
	public Vector<Object> list;

	public PrioritizedList() {
		// TODO Auto-generated constructor stub
		score = new Vector<Double>();
		list = new Vector<Object>();
	}
	
	public void putInDescendingOrder(Double s, Object o) {
		int numberOfObject = list.size();
		
		if (numberOfObject == 0) {
			score.add(s);
			list.add(o);
		}
		else {
			for (int i = 0; i < numberOfObject; i++) {
				if (score.get(i).compareTo(s) < 0) {
					score.add(i, s);
					list.add(i, o);
					break;
				}
			}
		}
	}
	
	public Object removeTop() {
		score.remove(0);
		return list.remove(0);
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
}
