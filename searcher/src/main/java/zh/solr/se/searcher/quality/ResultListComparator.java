package zh.solr.se.searcher.quality;

import com.answers.nlp.atlas.common.result.Result;

import java.util.*;

public abstract class ResultListComparator {
	public static final double NOT_APPLICABLE = -1.0;
	public abstract String name();

	public double compare(List<Result> expectedResults, List<Result> actualResults) {
		//copy results to list of titles
		List<String> expectedTitles = getTitles(expectedResults);
		List<String> actualTitles = getTitles(actualResults);
		return compareTitles(expectedTitles, actualTitles);
	}

	protected abstract double compareTitles(List<String> expectedTitles, List<String> actualTitles);

	private static List<String> getTitles(List<Result> expectedResults) {
		List<String> titles = new LinkedList<String>();
		for (Result result : expectedResults) {
			titles.add(result.getType() + ':' + result.getUniqueTitle());
		}
		return titles;
	}

	private static final List<ResultListComparator> COMPARATORS = Collections.unmodifiableList(new ArrayList<ResultListComparator>() {{
		add(new Precision());
		add(new AveragePrecision());
		add(new WeightedMRR());
	}});

	public static List<ResultListComparator> getComparators() {
		return COMPARATORS;
	}
}
