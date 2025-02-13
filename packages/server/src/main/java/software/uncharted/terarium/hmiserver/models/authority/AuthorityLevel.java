package software.uncharted.terarium.hmiserver.models.authority;

import java.io.Serializable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Slf4j
@TSModel
public enum AuthorityLevel implements Serializable {
	READ(0),
	CREATE(1),
	UPDATE(2),
	DELETE(3);

	@Getter
	private final int id;

	AuthorityLevel(final int id) {
		this.id = id;
	}

	public static AuthorityLevel get(String level) {
		try {
			return valueOf(level);
		} catch (IllegalArgumentException e) {
			log.error("AuthorityLevel {} not found", level);
			return null;
		}
	}

	public byte getMask() {
		return (byte) (0x0001 << id);
	}
}
