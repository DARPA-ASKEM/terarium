
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
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;

import software.uncharted.terarium.documentserver.models.xdd.Document;
import software.uncharted.terarium.documentserver.responses.xdd.XDDRelatedDocumentsResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbDateFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;


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
        List<Project> allProjects = getAllProjects();

        //Get project's related documents and add them to the project.
        //Currently related documents is really stupid. It just grabs the first publication in the project and will get related documents of that publication.
        //TODO: Make this smarter than grabbing first publication and then its related
        for (int i = 0; i < allProjects.size(); i++){            
            List<String> param = Arrays.asList("publications");
            String currentProjectAssetsString = projectProxy.getAssets(allProjects.get(i).getProjectID(),param).readEntity(String.class);
            //Format from : { publications: [ ..... ]} to [ .... ]
            currentProjectAssetsString = currentProjectAssetsString.replace("{\"publications\":", "");
            currentProjectAssetsString = currentProjectAssetsString.substring(0,currentProjectAssetsString.length() - 1);
            
            Jsonb jsonb = JsonbBuilder.create();
            List<Publication> currentProjectPublications = jsonb.fromJson(currentProjectAssetsString, new ArrayList<Publication>(){}.getClass().getGenericSuperclass());
            
            if (currentProjectPublications.size() > 0){
                //TODO: Fix the hard coded xdd-covid-19
                XDDRelatedDocumentsResponse relatedDocumentResponse = documentProxy.getRelatedDocuments("xdd-covid-19",currentProjectPublications.get(0).getXddUri()).readEntity(XDDRelatedDocumentsResponse.class);
                List<Document> relatedDocuments = new ArrayList();
                for (int j = 0; j < relatedDocumentResponse.getData().size(); j++){
                    relatedDocuments.add(relatedDocumentResponse.getData().get(j).getDocument());
                }
                
                allProjects.get(i).setRelatedDocuments(relatedDocuments);
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
        Integer pageSize = 200; //TODO: Determine a better way of grabbing "all" of a users projects. This will cap out at 200
        Integer page = 0;
        List<Project> allProjects = projectProxy.getProjects(pageSize, page);
        
        return allProjects;
    }

}
