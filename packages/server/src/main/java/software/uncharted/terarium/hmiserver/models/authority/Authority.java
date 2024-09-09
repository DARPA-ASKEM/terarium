package software.uncharted.terarium.hmiserver.models.authority;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "NAME") })
public class Authority {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	@Column(length = 512)
	private String description;
}
