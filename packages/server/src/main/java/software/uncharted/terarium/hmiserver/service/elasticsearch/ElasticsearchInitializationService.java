package software.uncharted.terarium.hmiserver.service.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchInitializationService {

	private final ElasticsearchService elasticsearchService;

	private final ObjectMapper objectMapper;

	private final ElasticsearchConfiguration config;

	private final Environment env;

	@Value("classpath:static/es/component-templates/*.json")
	private Resource[] resourceComponentTemplates;

	@Value("classpath:static/es/index-templates/*.json")
	private Resource[] resourceIndexTemplates;

	@Value("classpath:static/es/pipelines/*.json")
	private Resource[] resourcePipelines;

	@PostConstruct
	void init() throws IOException {
		pushMissingPipelines();
		pushMissingComponentTemplates();
		pushMissingIndexTemplates();
		pushMissingIndices();
	}

	private boolean isRunningLocalProfile() {
		final String[] activeProfiles = env.getActiveProfiles();

		for (final String profile : activeProfiles) {
			if ("local".equals(profile)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * For each system template resource, add it to the cluster if it doesn't exist
	 */
	private void pushMissingComponentTemplates() throws IOException {
		for (final Resource resource : resourceComponentTemplates) {
			final String filename = resource.getFilename();
			if (filename != null) {
				final String componentTemplateName = filename.substring(0, filename.length() - 5);
				if (isRunningLocalProfile() || !elasticsearchService.containsComponentTemplate(componentTemplateName)) {
					final JsonNode templateJson;
					try {
						templateJson = objectMapper.readValue(resource.getInputStream(), JsonNode.class);
						final boolean acknowledged = elasticsearchService.putComponentTemplate(componentTemplateName,
							templateJson.toString());
						if (acknowledged) {
							log.info("Added component template: {}", componentTemplateName);
						} else {
							log.error("Error adding component template: {}", componentTemplateName);
						}
					} catch (final IOException e) {
						log.error("Error parsing component template: {}", resource.getFilename(), e);
					}
				}
			}
		}
	}

	/**
	 * For each system template resource, add it to the cluster if it doesn't exist
	 */
	private void pushMissingIndexTemplates() throws IOException {
		for (final Resource resource : resourceIndexTemplates) {
			final String filename = resource.getFilename();
			if (filename != null) {
				final String indexTemplateName = filename.substring(0, filename.length() - 5);
				if (isRunningLocalProfile() || !elasticsearchService.containsIndexTemplate(indexTemplateName)) {
					final JsonNode templateJson;
					try {
						templateJson = objectMapper.readValue(resource.getInputStream(), JsonNode.class);
						final boolean acknowledged = elasticsearchService.putIndexTemplate(indexTemplateName,
								templateJson.toString());
						if (acknowledged) {
							log.info("Added index template: {}", indexTemplateName);
						} else {
							log.error("Error adding index template: {}", indexTemplateName);
						}
					} catch (final IOException e) {
						log.error("Error parsing index template: {}", resource.getFilename(), e);
					}
				}
			}
		}
	}

	/**
	 * For each pipeline resource, add it to the cluster if it doesn't exist
	 */
	private void pushMissingPipelines() throws IOException {
		for (final Resource resource : resourcePipelines) {
			final String filename = resource.getFilename();
			if (filename != null) {
				final String pipelineName = filename.substring(0, filename.length() - 5);
				if (isRunningLocalProfile() || !elasticsearchService.containsPipeline(pipelineName)) {
					final JsonNode pipelineJson;
					try {
						pipelineJson = objectMapper.readValue(resource.getInputStream(), JsonNode.class);
						final boolean acknowledged = elasticsearchService.putPipeline(pipelineName,
								pipelineJson.toString());
						if (acknowledged) {
							log.info("Added pipeline: {}", pipelineName);
						} else {
							log.error("Error adding pipeline: {}", pipelineName);
						}
					} catch (final IOException e) {
						log.error("Error parsing pipeline: {}", resource.getFilename(), e);
					}
				}
			}
		}
	}

	/**
	 * For each index in the ElasticsearchConfiguration, add it to the cluster if it
	 * doesn't exist
	 */
	private void pushMissingIndices() throws IOException {
		final String[] indices = new String[] {
			config.getCodeIndex(),
			config.getDatasetIndex(),
			config.getDecapodesConfigurationIndex(),
			config.getDecapodesContextIndex(),
			config.getDocumentIndex(),
			config.getEquationIndex(),
			config.getModelIndex(),
			config.getModelConfigurationIndex(),
			config.getNotebookSessionIndex(),
			config.getSimulationIndex(),
			config.getWorkflowIndex()
		};
		for (final String index : indices) {
			if (!elasticsearchService.containsIndex(index)) {
				try {
					elasticsearchService.createIndex(index);
				} catch (final IOException e) {
					log.error("Error creating index {}", index, e);
				}
			}
		}
	}
}
