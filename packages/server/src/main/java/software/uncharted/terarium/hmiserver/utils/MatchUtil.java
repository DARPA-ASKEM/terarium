package software.uncharted.terarium.hmiserver.utils;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class MatchUtil {

	/**
	 * Counts the number of matches/occurences of a term in a string of text
	 *
	 * @param searchTerm the term to find
	 * @param text the text to search
	 * @return number of times searchTerm occurs in text
	 */
	public static long matchCount(final String searchTerm, final String text) {
		return Pattern.compile("(" + searchTerm + ")").matcher(text).results().map(MatchResult::group).count();
	}
}
