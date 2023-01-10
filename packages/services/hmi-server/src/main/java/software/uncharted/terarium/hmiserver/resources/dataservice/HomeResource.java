
package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.Software;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ExternalProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.xdd.DocumentProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger; //TODO: Delete

import java.util.*;


@Path("/api/home")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Home Screen End Points")
public class HomeResource {
    @Inject
	@RestClient
    ProjectProxy projectProxy;

    @Inject
	@RestClient
    DocumentProxy documentProxy;

    private static final Logger LOG = Logger.getLogger(HomeResource.class);


	@GET
    /*
     * 1) Get all projects
     * 2) get all assets for each project
     * 3) Get all related articles for the first asset of each project
     * Return all projects + their related projects for the homepage to display
     */
	public Response getHomePageProjects(  
	) {
        Integer pageSize = 5;
        Integer page = 0;
        LOG.info("--- Home Resource --- ");
        LOG.info("Date:");
        LOG.info(projectProxy.getProjects(pageSize, page).getDate());
        LOG.info("Data:");
        LOG.info(projectProxy.getProjects(pageSize, page).readEntity(String.class)); 
        
        String allProjectsString = projectProxy.getProjects(pageSize, page).readEntity(String.class);
        LOG.info("All Projects String: " + allProjectsString);

        //TODO: This will need to have a split before it for when i get multiple projects.
        //Then this will need to be in a for loop to add the output to a list of project ids.
        //aProject = allProjectsString.split ... 
        //for projectID = aProject.split("\"id\":")[1].split(",");
            //add to list
        String[] id = allProjectsString.split("\"id\":")[1].split(","); //grab the ID in the project
        
        //TODO: Get doc id from project id
        LOG.info("ID:" + id[0]);
        // Project test = new Project(projectProxy.getProjects(pageSize, page));
        LOG.info(projectProxy.getAssets("1"));
        String projectAssets = projectProxy.getAssets("1").readEntity(String.class);
        LOG.info("Project Assets: " + projectAssets);

        
        //Need to pull new document service
        String relatedDocuments = documentProxy.getRelatedDocuments("xdd-covid-19","1").readEntity(String.class); 
        LOG.info("Related Documents:");
        //LOG.info(relatedDocuments);

        return Response.ok("Tom").build();
		//return Response.ok("Tom").build();
	}
	
}
