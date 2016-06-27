package annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import de.l3s.util.datatypes.comparators.AssociativeComparator;

public class Score {
	Hashtable<String, Double> scores = new Hashtable<>();
	List<String> sorted = new ArrayList<>();
	private Vector<String> users;

	public Score(Hashtable<String, Integer> scores,Vector<String> vector) {
users=vector;
		sorted.addAll(scores.keySet());
		Collections.sort(sorted, new AssociativeComparator(scores));
		Collections.reverse(sorted);

		Double sum = 0.;
		for (String key : scores.keySet()) {
			sum += scores.get(key);
		}

		for (String key : scores.keySet()) {
			Double score = scores.get(key).doubleValue();
			double per = score / sum;

			this.scores.put(key, per);
		}

	}


	public List<String> getlLabels() {
		return sorted;
	}

	public Double getScore(String label) {
		return scores.get(label);
	}

	public String getValidScore(int i, double d) {
		if(users.size()<i) return null;
		
		String highlab = sorted.get(0);
		Double highscore = scores.get(highlab);
		if(highscore>=d) return (highlab);
		return null;
	}
}
