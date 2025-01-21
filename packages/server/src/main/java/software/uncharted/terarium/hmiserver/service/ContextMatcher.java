package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.mira.DKG;

public class ContextMatcher {

	@Value
	static class CuratedGrounding {

		Map<String, String> identifiers;
		Map<String, String> context;
	}

	private static final String CONFIG_FILE = "curated-context.json";
	private static final Map<String, CuratedGrounding> configData = loadConfig();

	private static Map<String, CuratedGrounding> loadConfig() {
		try (InputStream inputStream = ContextMatcher.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			if (inputStream == null) {
				throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
			}
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(inputStream, new TypeReference<>() {});
		} catch (IOException e) {
			throw new RuntimeException("Failed to load configuration file", e);
		}
	}

	@Value
	static class SearchMatch {
		
		String key;
		double score;
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
	 * Calculate the score of a key based on a single search term.
	 * @param key The key to compare against.
	 * @param term The search term.
	 * @return A SearchMatch object with the key, score, and matched terms.
	 */
	private static SearchMatch calculateScore(String key, String term) {
    double score = 0; 

    int distance = levenshteinDistance(term.toLowerCase(), key.toLowerCase());
    boolean matched = distance <= Math.min(3, key.length() / 2); 

    if (matched) {
        score = 1.0 / (1.0 + distance); 
    }

    return new SearchMatch(key, score);
}

	/**
	 * Search for the given terms in the configuration file, with a minimum score of 0.5
	 * @param term the term to search for
	 * @return the first result, or null if no results were found
	 */
	public static List<Grounding> search(String term) {
		List<SearchMatch> matches = new ArrayList<>();
		for (String key : configData.keySet()) {
			SearchMatch match = calculateScore(key, term);
			if (match.getScore() >= 0.5) {
				matches.add(match);
			}
		}

		matches.sort(Comparator.comparing(SearchMatch::getScore).reversed());

		List<Grounding> groundings = new ArrayList<>();
		for (SearchMatch match : matches) {
			CuratedGrounding curatedGrounding = configData.get(match.getKey());
			ArrayList<DKG> identifiers = curatedGrounding
				.getIdentifiers()
				.entrySet()
				.stream()
				.map(e -> new DKG(e.getKey() + ":" + e.getValue()))
				.collect(Collectors.toCollection(ArrayList::new));
			JsonNode context = new ObjectMapper().valueToTree(curatedGrounding.getContext());

			Grounding grounding = new Grounding();
			grounding.setIdentifiers(identifiers);
			grounding.setContext(context);
			groundings.add(grounding);
		}
		return groundings;
	}

	public static Grounding searchBest(String term) {
		List<Grounding> groundings = search(term);
		if (groundings.isEmpty()) {
			return null;
		}
		return groundings.get(0);
	}
}
