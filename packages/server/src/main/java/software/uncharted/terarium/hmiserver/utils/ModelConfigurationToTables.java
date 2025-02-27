package software.uncharted.terarium.hmiserver.utils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.InitialSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ParameterSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;

public class ModelConfigurationToTables {

	public static String generateCsv(Model model, ModelConfiguration modelConfiguration) {
		StringBuilder csv = new StringBuilder();

		// Add the header
		csv.append("ID,Name,Value,Units,Description,Source\n");

		// Generate Initial Values Table
		if (!modelConfiguration.getInitialSemanticList().isEmpty()) {
			final Map<String, State> states = model
				.getStates()
				.stream()
				.collect(Collectors.toMap(State::getId, Function.identity()));

			for (InitialSemantic initial : modelConfiguration.getInitialSemanticList()) {
				String id = initial.getTarget();
				String name = "";
				String expression = initial.getExpression();
				String units = "";
				String description = "";
				String source = initial.getSource();

				// Get the values from the state
				final State state = states.get(initial.getTarget());
				if (state != null) {
					name = state.getName();
					description = state.getDescription();
					units = state.getUnits().getExpression();
				}

				// Add the row to the table
				csv.append(String.format("%s,%s,%s,%s,%s,%s\n", id, name, expression, units, description, source));
			}
		}

		// Generate Parameter Table
		if (!modelConfiguration.getParameterSemanticList().isEmpty()) {
			final Map<String, ModelParameter> parameters = model
				.getParameters()
				.stream()
				.collect(Collectors.toMap(ModelParameter::getId, Function.identity()));

			for (ParameterSemantic param : modelConfiguration.getParameterSemanticList()) {
				String id = param.getReferenceId();
				String name = "";
				String value = "";
				String units = "";
				String description = "";
				String source = param.getSource();

				// Get the values from the Parameter
				final ModelParameter parameter = parameters.get(param.getReferenceId());
				if (parameter != null) {
					name = parameter.getName();
					description = parameter.getDescription();
					units = parameter.getUnits().getExpression();
				}

				// Get the value based on the distribution type
				if (param.getDistribution() != null) {
					final ModelDistribution distribution = param.getDistribution();

					if (Objects.equals(distribution.getType(), "Constant")) {
						value = distribution.getParameters().get("value").toString();
					}

					if (Objects.equals(distribution.getType(), "StandardUniform1")) {
						value =
							"Uniform(min=" +
							distribution.getParameters().get("minimum") +
							", max=" +
							distribution.getParameters().get("maximum") +
							")";
					}
				}

				// Add the row to the table
				csv.append(String.format("%s,%s,%s,%s,%s,%s\n", id, name, value, units, description, source));
			}
		}

		return csv.toString();
	}

	public static String generateLatex(Model model, ModelConfiguration modelConfiguration) {
		StringBuilder latex = new StringBuilder();

		// Start LaTeX table
		latex.append("\\documentclass{article}\n");
		latex.append("\\usepackage{array, float, booktabs, subcaption}\n");
		latex.append("\\begin{document}\n");
		latex.append("\\begin{table}\n");

		// Generate Initial Values Table
		if (!modelConfiguration.getInitialSemanticList().isEmpty()) {
			latex.append("\\begin{center}\n");
			latex.append("\\caption{Initial expressions of the model state variables}\n");
			latex.append("\\begin{tabular}{cccc>{\\small}m{8em}>{\\small}m{4em}}\n");
			latex.append("\\toprule\n");
			latex.append("ID & Name & Expression & Units & Description & Source \\\\\n");
			latex.append("\\midrule\n");

			final Map<String, State> states = model
				.getStates()
				.stream()
				.collect(Collectors.toMap(State::getId, Function.identity()));

			for (InitialSemantic initial : modelConfiguration.getInitialSemanticList()) {
				String id = escapeLatex(initial.getTarget());
				String name = "";
				String expression = escapeLatex(initial.getExpression());
				String units = "";
				String description = "";
				String source = escapeLatex(initial.getSource());

				// Get the values from the state
				final State state = states.get(initial.getTarget());
				if (state != null) {
					name = escapeLatex(state.getName());
					description = escapeLatex(state.getDescription());
					units = escapeLatex(state.getUnits().getExpression());
				}

				// Add the row to the table
				latex.append(
					String.format("%s & %s & %s & %s & %s & %s \\\\\n", id, name, expression, units, description, source)
				);
				latex.append("\\hline\n");
			}

			// remove the last \\hline
			latex.delete(latex.length() - 8, latex.length());
			latex.append("\n");

			latex.append("\\bottomrule\n");
			latex.append("\\end{tabular}\n");
			latex.append("\\end{center}\n");
		}

		// Generate Parameter Table
		if (!modelConfiguration.getParameterSemanticList().isEmpty()) {
			latex.append("\\hfill\n");
			latex.append("\\begin{center}\n");
			latex.append("\\caption{Model parameters}\n");
			latex.append("\\begin{tabular}{cccc>{\\small}m{8em}>{\\small}m{4em}}\n");
			latex.append("\\toprule\n");
			latex.append("ID & Name & Value & Units & Description & Source \\\\\n");
			latex.append("\\midrule\n");

			final Map<String, ModelParameter> parameters = model
				.getParameters()
				.stream()
				.collect(Collectors.toMap(ModelParameter::getId, Function.identity()));

			for (ParameterSemantic param : modelConfiguration.getParameterSemanticList()) {
				String id = escapeLatex(param.getReferenceId());
				String name = "";
				String value = "";
				String units = "";
				String description = "";
				String source = escapeLatex(param.getSource());

				// Get the values from the Parameter
				final ModelParameter parameter = parameters.get(param.getReferenceId());
				if (parameter != null) {
					name = escapeLatex(parameter.getName());
					description = escapeLatex(parameter.getDescription());
					units = escapeLatex(parameter.getUnits().getExpression());
				}

				// Get the value based on the distribution type
				if (param.getDistribution() != null) {
					final ModelDistribution distribution = param.getDistribution();

					if (Objects.equals(distribution.getType(), "Constant")) {
						value = distribution.getParameters().get("value").toString();
					}

					if (Objects.equals(distribution.getType(), "StandardUniform1")) {
						value =
							"Uniform(min=" +
							distribution.getParameters().get("minimum") +
							", max=" +
							distribution.getParameters().get("maximum") +
							")";
					}
				}

				// Add the row to the table
				latex.append(String.format("%s & %s & %s & %s & %s & %s \\\\\n", id, name, value, units, description, source));
				latex.append("\\hline\n");
			}

			// remove the last \\hline\n
			latex.delete(latex.length() - 8, latex.length());
			latex.append("\n");

			latex.append("\\bottomrule\n");
			latex.append("\\end{tabular}\n");
			latex.append("\\end{center}\n");
		}

		// End LaTeX table
		latex.append("\\end{table}\n");
		latex.append("\\end{document}\n");

		return latex.toString();
	}

	/**
	 * Escapes special LaTeX characters
	 */
	private static String escapeLatex(String input) {
		if (input == null) {
			return "";
		}
		return input
			.replace("\\", "\\\\")
			.replace("_", "\\_")
			.replace("%", "\\%")
			.replace("$", "\\$")
			.replace("#", "\\#")
			.replace("&", "\\&")
			.replace("{", "\\{")
			.replace("}", "\\}")
			.replace("^", "\\^{}")
			.replace("~", "\\~{}")
			.replace(">", "$>$")
			.replace("<", "$<$");
	}
}
