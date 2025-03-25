package software.uncharted.terarium.hmiserver.models;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.authority.Role;

@Data
@Entity
@Accessors(chain = true)
@Table(
	name = "groups" // "group" is a reserved word in many db engines
)
@TSModel
public class Group implements Serializable {

	@Id
	private String id = UUID.randomUUID().toString();

	@Column(unique = true)
	private String name;

	private Long createdAtMs;

	@Column(length = 512)
	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	private Collection<Role> roles;

	@PostLoad
	public void postLoad() {
		if (roles != null) {
			roles.forEach(role -> role.setInherited(true));
		}
	}

	public Collection<Role> getRoles() {
		return roles != null ? roles : new HashSet<>();
	}
}
