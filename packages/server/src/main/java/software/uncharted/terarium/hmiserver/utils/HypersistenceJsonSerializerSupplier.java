package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.util.JsonSerializer;
import io.hypersistence.utils.hibernate.type.util.JsonSerializerSupplier;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import java.util.Collection;
import java.util.Map;

/**
 * This overrides the default JsonSerializer used in hypersistence.
 *
 * <p>
 * The default JsonSerializer will clone `Serializable` objects by _not_ using
 * jackson to serialize and deserialize
 * the object.
 *
 * <p>
 * This causes unwanted behavior for classes that we specifically override to
 * behave a particular way with jackson.
 * Ex. classes that extend SupportAdditionalProperties.
 *
 * <p>
 * This code is coped from
 * https://github.com/vladmihalcea/hypersistence-utils/blob/master/hypersistence-utils-hibernate-62/src/main/java/io/hypersistence/utils/hibernate/type/util/ObjectMapperJsonSerializer.java
 * and modified to omit the specialized Serializable behavior.
 */
public class HypersistenceJsonSerializerSupplier implements JsonSerializerSupplier {

	public static class HypersistenceJsonSerializer implements JsonSerializer {

		private final ObjectMapperWrapper objectMapperWrapper = new ObjectMapperWrapper(
			new HypersistenceObjectMapperSupplier().get()
		);

		@Override
		@SuppressWarnings("unchecked")
		public <T> T clone(final T object) {
			if (object instanceof String) {
				return object;
			} else if (object instanceof Collection) {
				try {
					return objectMapperWrapper
						.getObjectMapper()
						.readValue(objectMapperWrapper.toBytes(object), new TypeReference<>() {});
				} catch (final Exception e) {
					throw new IllegalArgumentException(e);
				}
			} else if (object instanceof Map) {
				try {
					return objectMapperWrapper
						.getObjectMapper()
						.readValue(objectMapperWrapper.toBytes(object), new TypeReference<>() {});
				} catch (final Exception e) {
					throw new IllegalArgumentException(e);
				}
			} else if (object instanceof JsonNode) {
				return (T) ((JsonNode) object).deepCopy();
			}

			return jsonClone(object);
		}

		@SuppressWarnings("unchecked")
		private <T> T jsonClone(final T object) {
			return objectMapperWrapper.fromBytes(objectMapperWrapper.toBytes(object), (Class<T>) object.getClass());
		}
	}

	@Override
	public JsonSerializer get() {
		return new HypersistenceJsonSerializer();
	}
}
