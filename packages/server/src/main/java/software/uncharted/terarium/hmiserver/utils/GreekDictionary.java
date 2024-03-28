package software.uncharted.terarium.hmiserver.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A class to store the Greek alphabet in a BiMap
 *
 * englishToGreek.get("beta")); // Output: β
 * greekToEnglish.get("β")); // Output: beta
 */
public class GreekDictionary {
	private static BiMap<String, String> englishToGreek = HashBiMap.create();
	static {
		englishToGreek.put("alpha", "α");
		englishToGreek.put("beta", "β");
		englishToGreek.put("gamma", "γ");
		englishToGreek.put("delta", "δ");
		englishToGreek.put("epsilon", "ε");
		englishToGreek.put("zeta", "ζ");
		englishToGreek.put("eta", "η");
		englishToGreek.put("theta", "θ");
		englishToGreek.put("iota", "ι");
		englishToGreek.put("kappa", "κ");
		englishToGreek.put("lambda", "λ");
		englishToGreek.put("mu", "μ");
		englishToGreek.put("nu", "ν");
		englishToGreek.put("xi", "ξ");
		englishToGreek.put("omicron", "ο");
		englishToGreek.put("pi", "π");
		englishToGreek.put("rho", "ρ");
		englishToGreek.put("sigma", "σ");
		englishToGreek.put("tau", "τ");
		englishToGreek.put("upsilon", "υ");
		englishToGreek.put("phi", "φ");
		englishToGreek.put("chi", "χ");
		englishToGreek.put("psi", "ψ");
		englishToGreek.put("omega", "ω");
	}

	public static String greekToEnglish(String greek) {
		return englishToGreek.inverse().get(greek);
	}
}


