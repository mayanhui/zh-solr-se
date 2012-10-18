package zh.solr.se.searcher.quality;

import java.util.List;

public class Precision extends ResultListComparator {
	private static final String NAME = "Precision";

	// Package-private constructor, since we only want to expose this internally
	Precision() {
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	protected double compareTitles(List<String> expectedTitles, List<String> actualTitles) {
		if (expectedTitles.isEmpty()) {
			return NOT_APPLICABLE;
		}
		if (actualTitles.isEmpty()) {
			return 0;
		}
		return calculatePrecision(expectedTitles, actualTitles);
	}

	static double calculatePrecision(List<String> relevantDocs, List<String> foundDocs) {
		double hits = 0;
		for (String foundDoc : foundDocs) {
			if (relevantDocs.contains(foundDoc)) {
				hits++;
			}
		}
		return hits / foundDocs.size();
	}
}
