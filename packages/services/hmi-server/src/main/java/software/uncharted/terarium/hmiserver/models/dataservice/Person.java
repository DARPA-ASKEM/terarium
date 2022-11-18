package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Person {

	@JsonbProperty("id")
	public String id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("email")
	public String email;

	@JsonbProperty("org")
	public String organization;

	@JsonbProperty("website")
	public String website;

	@JsonbProperty("is_registered")
	public Boolean isRegistered;

	public Person(final String name, final String email, final String organization, final String website, final Boolean isRegistered) {
		this.name = name;
		this.email = email;
		this.organization = organization;
		this.website = website;
		this.isRegistered = isRegistered;
	}
}
