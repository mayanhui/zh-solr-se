package zh.solr.se.searcher.quality;

import com.google.common.collect.Lists;

import java.util.List;

public class WeightedMRR extends ResultListComparator {
	// Package-private constructor, since we only want to expose this internally
	WeightedMRR() {}

	private static final String NAME = "Weighted MRR";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	protected double compareTitles(List<String> expectedTitles, List<String> actualTitles) {
		if (expectedTitles.isEmpty()) {
			return NOT_APPLICABLE;
		}

		List<String> actual = Lists.newLinkedList(actualTitles); //Make sure we can remove items from the list

		double indexOfExpected = 0;
		double score = 0;
		double norm = 0; //sum 1 + 1/2 + 1/3 +...   used to normalize the result at the end of the method
		for (String expected : expectedTitles) {
			indexOfExpected++;
			if (actual.contains(expected)) {
				double indexOfActual = actual.indexOf(expected) + 1;
				score += (1 / (indexOfExpected * indexOfActual)); //The lower on the list the lower the score
				actual.remove(expected);
			}
			norm += (1.0 / indexOfExpected);
		}

		return score / norm;
	}
}
