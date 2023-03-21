package software.uncharted.terarium.hmiserver.models.petrinet;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.proxies.mira.MiraProxy;

import lombok.Data;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.smallrye.jwt.config.ConfigLogging.log;

@Data
public class Ontology implements Serializable {
	private final String name;
	private final String curie;
	private String title;
	private String description;
	private URL url;

	@RestClient
	MiraProxy proxy;

	public Ontology (String input) {
		// input = "('identity', 'ido:0000514')"
		final Matcher matcher = Pattern.compile("\\(\\'(.+?)\\', \\'(.+?)\\'\\)").matcher(input);
		final Boolean result = matcher.find();
		int i = matcher.groupCount();
		if (result && i > 0) {
			this.name = matcher.group(1);
			this.curie = matcher.group(2);
		} else {
			this.name = this.curie = null;
		}

		Response entity = proxy.getEntity(this.curie);

		this.title = "title";
		this.description = "description";
		try {
			this.url = new URL("github.io");
		} catch (Exception e) {
			log.error(e);
		}
	}
}
