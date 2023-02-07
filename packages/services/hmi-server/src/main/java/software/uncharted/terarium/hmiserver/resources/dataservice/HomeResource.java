
package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.documentserver.models.xdd.RelatedDocument;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.xdd.DocumentProxy;


import software.uncharted.terarium.documentserver.models.xdd.Document;
import software.uncharted.terarium.documentserver.responses.xdd.XDDRelatedDocumentsResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;


@Path("/api/home")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Home Screen End Points")
@Slf4j
public class HomeResource {
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 200;

	//TODO: Fix the hard coded xdd-covid-19
	private static final String DEFAULT_DOC = "xdd-covid-19";
	@Inject
	@RestClient
	ProjectProxy projectProxy;

	@Inject
	@RestClient
	DocumentProxy documentProxy;

	@GET
	/*
	 * 1) Get all projects
	 * 2) get all assets for each project
	 * 3) Get all related articles for the first asset of each project
	 * Return all projects + their related projects for the homepage to display
	 */
	public Response getHomePageInfo() {
		List<Project> allProjects;
		try {
			allProjects = projectProxy.getProjects(DEFAULT_PAGE_SIZE, DEFAULT_PAGE);
		} catch (RuntimeException e) {
			log.error("Unable to get projects", e);
			return Response
				.status(Response.Status.NO_CONTENT)
				.type(MediaType.APPLICATION_JSON)
				.build();

		}

		//Get project's related documents and add them to the project.
		//Currently related documents is really stupid. It just grabs the first publication in the project and will get related documents of that publication.
		//TODO: Make this smarter than grabbing first publication and then its related
		for (Project project : allProjects) {
			Assets assets = new Assets();
			try {
				assets = projectProxy.getAssets(project.getProjectID(), Arrays.asList(ResourceType.Type.PUBLICATIONS.type, ResourceType.Type.MODELS.type, ResourceType.Type.DATASETS.type));
				Map<String, List<String>> projectAssets = new HashMap<>();
				projectAssets.put(ResourceType.Type.MODELS.type, assets.getModels().stream().map(ResourceType::getId).collect(Collectors.toList()));
				projectAssets.put(ResourceType.Type.DATASETS.type, assets.getDatasets().stream().map(ResourceType::getId).collect(Collectors.toList()));
				projectAssets.put(ResourceType.Type.PUBLICATIONS.type, assets.getPublications().stream().map(ResourceType::getId).collect(Collectors.toList()));
				project.setAssets(projectAssets);
			} catch (RuntimeException e) {
				log.warn("Unable to access publications for project " + project.getProjectID());
				continue;
			}

			List<Publication> currentProjectPublications = assets.getPublications();

			if (currentProjectPublications.size() > 0) {

				XDDRelatedDocumentsResponse relatedDocumentResponse = documentProxy.getRelatedDocuments(DEFAULT_DOC, currentProjectPublications.get(0).getXddUri()).readEntity(XDDRelatedDocumentsResponse.class);
				List<Document> relatedDocuments = new ArrayList();
				for (RelatedDocument relatedDocument : relatedDocumentResponse.getData()) {
					relatedDocuments.add(relatedDocument.getDocument());
				}

				project.setRelatedDocuments(relatedDocuments);
			}
		}

		return Response
			.status(Response.Status.OK)
			.entity(allProjects)
			.type(MediaType.APPLICATION_JSON)
			.build();
	}

}
