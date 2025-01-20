package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.mira.DKG;

public class ContextMatcher {

	private static final String CONFIG_FILE = "curated-context.json";
	private static final JsonNode configData;

	@Value
	static class SearchMatch {

		String key;
		double score;
		List<String> matches;
	}

	/* Load the configuration file */
	static {
		try (InputStream inputStream = ContextMatcher.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			if (inputStream == null) {
				throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
			}
			ObjectMapper mapper = new ObjectMapper();
			configData = mapper.readTree(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load configuration file", e);
		}
	}

	/**
	 * Calculate the Levenshtein distance between two strings
	 * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">...</a>
	 * <a href="https://en.wikipedia.org/wiki/Approximate_string_matching">...</a>
	 */
	private static int levenshteinDistance(String a, String b) {
		int[][] matrix = new int[b.length() + 1][a.length() + 1];

		for (int i = 0; i <= b.length(); i++) {
			matrix[i][0] = i;
		}
		for (int j = 0; j <= a.length(); j++) {
			matrix[0][j] = j;
		}

		for (int i = 1; i <= b.length(); i++) {
			for (int j = 1; j <= a.length(); j++) {
				if (b.charAt(i - 1) == a.charAt(j - 1)) {
					matrix[i][j] = matrix[i - 1][j - 1];
				} else {
					matrix[i][j] = Math.min(matrix[i - 1][j - 1] + 1, Math.min(matrix[i][j - 1] + 1, matrix[i - 1][j] + 1));
				}
			}
		}

		return matrix[b.length()][a.length()];
	}

	/**
	 * Calculate the score of a key based on the search terms
	 */
	private static SearchMatch calculateScore(String key, List<String> searchTerms) {
		String[] keyTerms = key.toLowerCase().split("_");
		double totalScore = 0;
		List<String> matches = new ArrayList<>();

		for (String term : searchTerms) {
			int bestDistance = Integer.MAX_VALUE;
			boolean matched = false;
			String termLower = term.toLowerCase();

			for (String keyTerm : keyTerms) {
				int distance = levenshteinDistance(termLower, keyTerm);
				if (distance < bestDistance) {
					bestDistance = distance;
					if (distance <= Math.min(3, keyTerm.length() / 2)) {
						matched = true;
					}
				}
			}

			if (matched) {
				matches.add(term);
				totalScore += 1.0 / (1.0 + bestDistance);
			}
		}

		return new SearchMatch(key, totalScore / searchTerms.size(), matches);
	}

	/**
	 * Search for the given terms in the configuration file, with a minimum score
	 */
	public static List<Grounding> multiSearch(List<String> searchTerms, double minScore) {
		List<String> validTerms = searchTerms.stream().filter(term -> !term.isEmpty()).collect(Collectors.toList());

		List<SearchMatch> matches = new ArrayList<>();
		Iterator<String> fieldNames = configData.fieldNames();

		while (fieldNames.hasNext()) {
			String key = fieldNames.next();
			SearchMatch match = calculateScore(key, validTerms);
			if (match.getScore() >= minScore) {
				matches.add(match);
			}
		}

		matches.sort(Comparator.comparing(SearchMatch::getScore).reversed());

		List<Grounding> groundings = new ArrayList<>();
		for (SearchMatch match : matches) {
			Grounding grounding = new Grounding();
			grounding.setIdentifiers(List.of(new DKG(match.getKey())));
			groundings.add(grounding);
		}
		return groundings;
	}

	/**
	 * Quick search for the given terms in the configuration file
	 */
	public static List<Grounding> multiSearch(List<String> searchTerms) {
		return multiSearch(searchTerms, 0.3);
	}

	/**
	 * Search for the given term in the configuration file
	 */
	public static List<Grounding> search(String searchTerm) {
		return multiSearch(List.of(searchTerm));
	}
}
