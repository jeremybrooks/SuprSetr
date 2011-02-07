package net.jeremybrooks.suprsetr.dto;

import java.util.ArrayList;
import java.util.List;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.dto.SearchParameters;
import net.jeremybrooks.suprsetr.SSConstants;

/**
 *
 * @author jeremyb
 */
public class SSSearch extends SearchParameters {

    private List<String> negativeTagMatch;

    public SSSearch() {
	this.negativeTagMatch = new ArrayList<String>();

    }

    @Override
    public void	setTags(List<String> tags) {
	List<String> newTags = new ArrayList<String>();

	// First remove the tags that begin with -
	for (String tag : tags) {
	    if (tag.startsWith("-")) {
		this.negativeTagMatch.add(tag.substring(1));
	    } else {
		newTags.add(tag);
	    }
	}

	// If there are negative tag matches, we need to get the tags for
	// each photo returned
	if (this.negativeTagMatch.size() > 0) {
	    setExtras(JinxConstants.EXTRAS_TAGS);
	}

	super.setTags(newTags);
    }


    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder(this.getClass().getName());
	sb.append(": [ ").append(this.getNegativeTagMatch().toString());
	sb.append(" ").append(super.toString());
	sb.append(" ]");

	return sb.toString();
    }


    /**
     * @return the negativeTagMatch
     */
    public List<String> getNegativeTagMatch() {
	return negativeTagMatch;
    }


    /**
     * @param negativeTagMatch the negativeTagMatch to set
     */
    public void setNegativeTagMatch(List<String> negativeTagMatch) {
	this.negativeTagMatch = negativeTagMatch;
    }
}
