package software.uncharted.terarium.hmiserver.models.authority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumEntity;

@Data
@Entity
@Accessors(chain = true)
@Table(
	name = "groups" // "group" is a reserved word in many db engines
)
@TSModel
public class Group extends TerariumEntity {

	@Column(unique = true)
	private String name;

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
