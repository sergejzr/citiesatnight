package de.l3s.util;


import java.util.Comparator;
import java.util.Hashtable;

public class AssociativeComparator implements Comparator<Object> {

	

	

	private Hashtable<? extends Object, ? extends Comparable> hashtab;

	public AssociativeComparator(Hashtable<?extends Object, ? extends Comparable> scores) {
		this.hashtab=scores;
		
	
	}

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		return hashtab.get(o1).compareTo(hashtab.get(o2));
	}


/*
	@Override
	public int compare(T o1, T o2) {
	
		K result2 = (K) hashtab.get(o2);
		
		return hashtab.get(o1).compareTo(result2);
	}

	*/

	

	

	

}

