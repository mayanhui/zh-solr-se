package zh.solr.se.searcher.quality;

import java.util.List;

public class AveragePrecision extends ResultListComparator {

	// Package-private constructor, since we only want to expose this internally
	AveragePrecision() {
	}

	private static final String NAME = "Average Precision";

	@Override
	public String name() {
		return NAME;
	}

	//From ~ http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-ranked-retrieval-results-1.html
	@Override
	protected double compareTitles(List<String> expectedTitles, List<String> actualTitles) {
		if (expectedTitles.isEmpty()) {
			return NOT_APPLICABLE;
		}

		int numRelevant = expectedTitles.size();
		double sumPrecision = 0;
		for (int k = 0; k < numRelevant; k++) {
			String docK = expectedTitles.get(k);
			if (actualTitles.contains(docK)) {
				sumPrecision += Precision.calculatePrecision(expectedTitles, actualTitles.subList(0, actualTitles.indexOf(docK) + 1));
			}
		}
		return sumPrecision / numRelevant;
	}
}
