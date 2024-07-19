package software.uncharted.terarium.hmiserver.models.authority;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	@Column(length = 512)
	private String description;

	@OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@JoinColumn(name = "ROLE_ID")
	private Set<AuthorityInstance> authorities = new HashSet<>();

	/** True if this role is inherited from a group */
	@Transient
	private boolean inherited;
}
