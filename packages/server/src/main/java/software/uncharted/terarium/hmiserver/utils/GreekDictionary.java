package software.uncharted.terarium.hmiserver.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A class to store the Greek alphabet in a BiMap for easy translation between English and Greek.
 */
public class GreekDictionary {

	private static final BiMap<String, String> englishGreek = HashBiMap.create();

	static {
		englishGreek.put("alpha", "α");
		englishGreek.put("beta", "β");
		englishGreek.put("gamma", "γ");
		englishGreek.put("delta", "δ");
		englishGreek.put("epsilon", "ϵ");
		englishGreek.put("varepsilon", "ε");
		englishGreek.put("zeta", "ζ");
		englishGreek.put("eta", "η");
		englishGreek.put("theta", "θ");
		englishGreek.put("vartheta", "ϑ");
		englishGreek.put("iota", "ι");
		englishGreek.put("kappa", "κ");
		englishGreek.put("varkappa", "ϰ");
		englishGreek.put("lambda", "λ");
		englishGreek.put("mu", "μ");
		englishGreek.put("nu", "ν");
		englishGreek.put("xi", "ξ");
		englishGreek.put("omicron", "ο");
		englishGreek.put("pi", "π");
		englishGreek.put("varpi", "ϖ");
		englishGreek.put("rho", "ρ");
		englishGreek.put("varrho", "ϱ");
		englishGreek.put("sigma", "σ");
		englishGreek.put("varsigma", "ς");
		englishGreek.put("tau", "τ");
		englishGreek.put("upsilon", "υ");
		englishGreek.put("phi", "φ");
		englishGreek.put("varphi", "ϕ");
		englishGreek.put("chi", "χ");
		englishGreek.put("psi", "ψ");
		englishGreek.put("omega", "ω");
		englishGreek.put("Gamma", "Γ");
		englishGreek.put("Delta", "Δ");
		englishGreek.put("Theta", "Θ");
		englishGreek.put("Kappa", "κ");
		englishGreek.put("Lambda", "Λ");
		englishGreek.put("Xi", "Ξ");
		englishGreek.put("Pi", "Π");
		englishGreek.put("Sigma", "Σ");
		englishGreek.put("Upsilon", "ϒ");
		englishGreek.put("Phi", "Φ");
		englishGreek.put("Psi", "Ψ");
		englishGreek.put("Omega", "Ω");
	}

	public static String englishToGreek(String english) {
		return englishGreek.get(english);
	}

	public static String greekToEnglish(String greek) {
		return englishGreek.inverse().get(greek);
	}
}
