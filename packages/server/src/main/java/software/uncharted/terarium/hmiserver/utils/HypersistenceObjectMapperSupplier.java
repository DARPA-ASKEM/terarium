package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperSupplier;

/**
 * This overrides the default ObjectMapper used in hypersistence.
 *
 * <p>We don't need any specialized behavior as of yet, but this will make changing that trivial in the future.
 */
public class HypersistenceObjectMapperSupplier implements ObjectMapperSupplier {

	@Override
	public ObjectMapper get() {
		return new ObjectMapper();
	}
}
