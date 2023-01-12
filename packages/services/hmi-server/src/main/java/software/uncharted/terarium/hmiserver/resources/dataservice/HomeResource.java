
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

import software.uncharted.terarium.documentserver.models.xdd.Document;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger; //TODO: Delete

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



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
	public Response getHomePageInfo() {
        LOG.info("--- Home Resource --- ");
        List<Project> allProjects = getAllProjectIDs(); //grab the ID in the project
        
        LOG.info("ID:" + allProjects.get(0).getID());
        
        //Get project's related documents and add them to the project.
        //Currently related documents is really stupid. It just grabs the first publication in the project and will get related documents of that publication.
        for (int i = 0; i < allProjects.size(); i++){
            LOG.info("Start getting related");
            String projectAssets = projectProxy.getAssets(allProjects.get(i).getID()).readEntity(String.class);
            //LOG.info(i + projectAssets);
            // If the project has no publications dont try to find related articles...
            //it will have a length of 0 as after the split that will just mean it has at least an empty list
            if (projectAssets.split("\"publications\":")[1].split("\"xdd_uri\":").length > 1){ 
                String projectFirstPublication = projectAssets.split("\"publications\":")[1].split("\"xdd_uri\":")[1].split(",")[0].replace("\"",""); //.split(",")[0]; //.split("\"xdd_uri\":")[1].replace("\"","");
                LOG.info("project's first publication: " + projectFirstPublication);    
                String relatedDocumentsString = documentProxy.getRelatedDocuments("xdd-covid-19",projectFirstPublication).readEntity(String.class); 
                Document relatedDocuments = stringToDocument(relatedDocumentsString);
                
                LOG.info("Related Documents Length: " + relatedDocuments.getID());
                allProjects.get(i).setRelatedDocuments(relatedDocuments);
            }
        }
        
        //TODO Remove this log check
        LOG.info("Post processing all projects:");
        for (int i = 0; i < allProjects.size(); i++){
            LOG.info(allProjects.get(i).toString());
        } //TODO Remove this log check

        return Response.ok("Tom").build();
		//return Response.ok("Tom").build();
	}

    public List<Project> getAllProjectIDs(){
        Integer pageSize = 5;
        Integer page = 0;
        List<Project> allProjects = new ArrayList<>();
        // LOG.info("Date:");
        // LOG.info(projectProxy.getProjects(pageSize, page).getDate());
        // LOG.info("Data:");
        // LOG.info(projectProxy.getProjects(pageSize, page).readEntity(String.class)); 
        
        String allProjectsString = projectProxy.getProjects(pageSize, page).readEntity(String.class);
        LOG.info("All Projects String: " + allProjectsString);
        
        Pattern p = Pattern.compile("\\{.*?\\}"); //Start with { have anything then end with } 
        Matcher m = p.matcher(allProjectsString);
        while(m.find()){
            String currentProject = m.group();
            LOG.info(currentProject);
            String currentID = currentProject.split("\"id\":")[1].split(",")[0];
            String currentName = currentProject.split("\"name\":")[1].split(",")[0];
            String currentDescription = currentProject.split("\"description\":")[1].split(",")[0];
            // String currentTimestamp = currentProject.split("\"timestamp\":")[1].split(",")[0];
            // String currentActive = currentProject.split("\"active\":")[1].split(",")[0];
            // String currentConcept = currentProject.split("\"concept\":")[1].split(",")[1];
            LOG.info("Current ID: " + currentID);
            LOG.info("Current Name: " + currentName);
            LOG.info("Current Description: " + currentDescription);

            Project aProject = new Project(currentID,currentName,currentDescription);
            allProjects.add(aProject);
            LOG.info("New project " + aProject.toString());
            // LOG.info("Current timestamp: " + currentTimestamp);
            // LOG.info("Current Active: " + currentActive);
            // LOG.info("Current Concept: " + currentConcept);
        }

        return allProjects;
    }

    //Take a string representation of a document (massive json crap) and parse it into distinct fields
    private Document stringToDocument(String aDocument){
        String currentProject = m.group();
        LOG.info(aDocument);
        String gddId = aDocument.split("\"gddId\":")[1].split(",")[0];
        String title = aDocument.split("\"title\":")[1].split(",")[0];
        String abstractText = aDocument.split("\"abstractText\":")[1].split(",")[0];
        String journal = aDocument.split("\"journal\":")[1].split(",")[0];
        
        LOG.info("Current ID: " + gddId);
        LOG.info("Title: " + title);
        LOG.info("Abstract Text: " + abstractText);
        LOG.info("Journal: " + journal);

        Document result = new Document(gddId,title,abstractText,journal);

        return result;
    }
	
}
