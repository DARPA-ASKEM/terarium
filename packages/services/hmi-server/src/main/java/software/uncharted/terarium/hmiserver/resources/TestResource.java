package software.uncharted.terarium.hmiserver.resources;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.jwt.JsonWebToken;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam;


import io.quarkus.security.Authenticated;
import software.uncharted.terarium.hmiserver.services.UserEventService;

@RequestScoped
@Authenticated
@Path("/api/test")
public class TestResource {
    @Inject
    JsonWebToken accessToken;

    @Inject
    UserEventService ues;

    @GET
    @NoCache
    public User me(@HeaderParam("Authorization") String auth) {
        // sub":"930b9501-ab07-4baf-b5c8-d4095dc82b3b"

        if (ues == null) {
            if (this.accessToken == null) {
                return new User("Idenity is NULL: " + auth);
            } else {
                String sub = this.accessToken.getClaim("sub");
                return new User("accessToken: sub: " + sub + " - " + accessToken.getSubject());
            }
        } else {
            return new User("UES: " + ues.getUser().getUsername());
        }
    }

    public static class User {

        private final String userName;

        User(String message) {
            this.userName = message;
        }

        public String getUserName() {
            return userName;
        }
    } 
}
