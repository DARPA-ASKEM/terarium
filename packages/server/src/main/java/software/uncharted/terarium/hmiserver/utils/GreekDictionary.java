package software.uncharted.terarium.hmiserver.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A class to store the Greek alphabet in a BiMap
 *
 * <p>englishToGreek("beta")); // Output: β greekToEnglish("β")); // Output: beta
 */
public class GreekDictionary {

	private static BiMap<String, String> englishGreek = HashBiMap.create();

	static {
		englishGreek.put("alpha", "α");
		englishGreek.put("beta", "β");
		englishGreek.put("gamma", "γ");
		englishGreek.put("delta", "δ");
		englishGreek.put("epsilon", "ε");
		englishGreek.put("zeta", "ζ");
		englishGreek.put("eta", "η");
		englishGreek.put("theta", "θ");
		englishGreek.put("iota", "ι");
		englishGreek.put("kappa", "κ");
		englishGreek.put("lambda", "λ");
		englishGreek.put("mu", "μ");
		englishGreek.put("nu", "ν");
		englishGreek.put("xi", "ξ");
		englishGreek.put("omicron", "ο");
		englishGreek.put("pi", "π");
		englishGreek.put("rho", "ρ");
		englishGreek.put("sigma", "σ");
		englishGreek.put("tau", "τ");
		englishGreek.put("upsilon", "υ");
		englishGreek.put("phi", "φ");
		englishGreek.put("chi", "χ");
		englishGreek.put("psi", "ψ");
		englishGreek.put("omega", "ω");
	}

	public static String englishToGreek(String english) {
		return englishGreek.get(english);
	}

	public static String greekToEnglish(String greek) {
		return englishGreek.inverse().get(greek);
	}
}
