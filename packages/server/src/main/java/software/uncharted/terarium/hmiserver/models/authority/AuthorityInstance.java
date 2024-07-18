package software.uncharted.terarium.hmiserver.models.authority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

@Data
@Accessors(chain = true)
@Entity
@NoArgsConstructor
public class AuthorityInstance {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private byte mask;

	@ManyToOne(fetch = FetchType.EAGER)
	private Authority authority;

	public AuthorityInstance(final Authority authority, final List<AuthorityLevel> authorityLevels) {
		this.authority = authority;
		this.mask = authorityLevelsToMask(authorityLevels);
	}

	private static byte authorityLevelsToMask(final List<AuthorityLevel> authorityLevels) {
		return authorityLevels.stream().map(AuthorityLevel::getMask).reduce((l1, l2) -> (byte) (l1 | l2)).orElse((byte) 0);
	}

	@TSIgnore
	@JsonIgnore
	public List<String> getAuthoritiesAsStrings() {
		return getAuthoriyLevels()
			.stream()
			.map(authorityLevel -> authorityLevel + "_" + authority.getName())
			.collect(Collectors.toList());
	}

	@TSIgnore
	@JsonIgnore
	public List<AuthorityLevel> getAuthoriyLevels() {
		return Arrays.stream(AuthorityLevel.values()).filter(this::hasAuthorityLevel).collect(Collectors.toList());
	}

	private boolean hasAuthorityLevel(final AuthorityLevel level) {
		return (mask & (byte) Math.pow(2, level.getId())) != 0;
	}
}
