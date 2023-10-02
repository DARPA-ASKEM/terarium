package software.uncharted.terarium.hmiserver.models.authority;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Accessors(chain = true)
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	@JoinColumn(name = "ROLE_ID")
	private Set<AuthorityInstance> authorities = new HashSet<>();
}
