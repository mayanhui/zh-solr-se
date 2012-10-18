package zh.solr.se.indexer.util;

import java.util.ArrayList;
import java.util.List;

/**
 * There are some lists that get processed for the keyword index that should not
 * have entries in both lists.  This class filters those values.
 */
public class KeywordSiteFilter {
    /**
     * Remove the entries from base that are in both base and removeThese.  If
     * base is null an empty List will be returned.  If removeThese is null
     * base will be returned.
     *
     * @param base list of values to use as the base
     * @param removeThese list of values to be removed from base
     * @return list of elements found in base with those found in removeThese removed
     */
    public List<String> remove(List<String> base, List<String> removeThese) {
        if(base == null) {
            return new ArrayList<String>();
        }
        if(removeThese == null) {
            return base;
        }

        List<String> filteredBase = new ArrayList<String>(base);
        filteredBase.removeAll(removeThese);

        return filteredBase;
    }
}
