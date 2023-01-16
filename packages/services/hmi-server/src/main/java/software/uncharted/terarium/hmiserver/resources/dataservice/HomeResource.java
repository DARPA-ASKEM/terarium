
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

	@GET
    /*
     * 1) Get all projects
     * 2) get all assets for each project
     * 3) Get all related articles for the first asset of each project
     * Return all projects + their related projects for the homepage to display
     */
	public Response getHomePageInfo() {
        List<Project> allProjects = getAllProjects(); //grab the ID in the project

        //Get project's related documents and add them to the project.
        //Currently related documents is really stupid. It just grabs the first publication in the project and will get related documents of that publication.
        for (int i = 0; i < allProjects.size(); i++){            
            String projectAssets = projectProxy.getAssets(allProjects.get(i).getID()).readEntity(String.class);

            // If project has publication, grab the first one and get its related documents
            String[] projectPublication  = projectAssets.split("\"publications\":")[1].split("\"xdd_uri\":");
            if (projectPublication.length > 1){ 
                String projectFirstPublication = projectPublication[1].split(",")[0].replace("\"","");
                String relatedDocumentsString = documentProxy.getRelatedDocuments("xdd-covid-19",projectFirstPublication).readEntity(String.class); 
                List<Document> relatedDocuments = stringToDocument(relatedDocumentsString);
                
                allProjects.get(i).setRelatedDocuments(relatedDocuments); //update corresponding project
            }
        }

        return Response
            .status(Response.Status.OK)
            .entity(allProjects)
            .type(MediaType.APPLICATION_JSON)
            .build();
	}

    //Using proxy get all of the projects. 
    //Parse the nonesense into Project type
    private List<Project> getAllProjects(){
        Integer pageSize = 5;
        Integer page = 0;
        List<Project> allProjects = new ArrayList<>();
        
        String allProjectsString = projectProxy.getProjects(pageSize, page).readEntity(String.class);
        
        Pattern p = Pattern.compile("\\{.*?\\}"); //Start with { have anything then end with } 
        Matcher m = p.matcher(allProjectsString);
        while(m.find()){
            String currentProject = m.group();
            String currentID = currentProject.split("\"id\":")[1].split(",")[0].replace("\"","");
            String currentName = currentProject.split("\"name\":")[1].split(",")[0].replace("\"","");
            String currentDescription = currentProject.split("\"description\":")[1].split(",")[0].replace("\"","");
            String currentTimestamp = currentProject.split("\"timestamp\":")[1].split(",")[0].replace("\"","");

            Project aProject = new Project(currentID,currentName,currentDescription,currentTimestamp);
            allProjects.add(aProject);
        }

        return allProjects;
    }

    //Take a string representation of documents (massive json) and parse it into distinct fields
    //Parse for each distinct gddid (doc id)
    private List<Document> stringToDocument(String documentJSON){
        List<Document> result = new ArrayList<>();
        for (int i = 1; i < documentJSON.split("\"_gddid\":").length; i++){
            String gddId = documentJSON.split("\"_gddid\":")[i].split(",")[0].replace("\"","");
            String title = documentJSON.split("\"title\":")[i].split(",")[0].replace("\"","");
            String abstractText = documentJSON.split("\"abstract\":")[i].split(",")[0].replace("\"","");
            String journal = documentJSON.split("\"journal\":")[i].split(",")[0];
            String currentPublisher = documentJSON.split("\"publisher\":")[1].split(",")[0].replace("\"","");
            String currentAuthor = documentJSON.split("\"author\":")[1].split(",")[1].split("\"name\":")[1].replace("\"",""); 
            currentAuthor = currentAuthor.substring(0,currentAuthor.indexOf("}")); 
            String currentIdentifier = documentJSON.split("\"identifier\":")[1].split(",")[1].split("\"id\":")[1].replace("\"","");
            currentIdentifier = currentIdentifier.substring(0,currentIdentifier.indexOf("}")); //Cut out the extra crap from id

            List<Map<String, String>> identifier = new ArrayList<>();
            identifier.add(Map.of("type", "doi","id", currentIdentifier));
            List<Map<String, String>> author = new ArrayList<>();
            author.add(Map.of("name", currentAuthor));

            Document newDocument = new Document(gddId,title,abstractText,journal,currentPublisher,author,identifier);
            result.add(newDocument);
        }
        
        return result;
    }
	
}
