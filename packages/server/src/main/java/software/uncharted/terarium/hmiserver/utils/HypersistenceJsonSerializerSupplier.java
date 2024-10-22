package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
				final Object firstElement = findFirstNonNullElement((Collection<?>) object);
				if (firstElement != null) {
					JavaType type;
					if (firstElement instanceof ArrayNode || firstElement instanceof JsonNode) {
						type = TypeFactory.defaultInstance().constructParametricType(object.getClass(), JsonNode.class);
					} else {
						type = TypeFactory.defaultInstance().constructParametricType(object.getClass(), firstElement.getClass());
					}

					return objectMapperWrapper.fromBytes(objectMapperWrapper.toBytes(object), type);
				}
			} else if (object instanceof Map) {
				final Map.Entry<?, ?> firstEntry = this.findFirstNonNullEntry((Map<?, ?>) object);
				if (firstEntry != null) {
					final Object key = firstEntry.getKey();
					final Object value = firstEntry.getValue();

					JavaType type;
					if (value instanceof ArrayNode || value instanceof JsonNode) {
						type = TypeFactory.defaultInstance()
							.constructParametricType(object.getClass(), key.getClass(), JsonNode.class);
					} else {
						type = TypeFactory.defaultInstance()
							.constructParametricType(object.getClass(), key.getClass(), value.getClass());
					}
					return (T) objectMapperWrapper.fromBytes(objectMapperWrapper.toBytes(object), type);
				}
			} else if (object instanceof JsonNode) {
				return (T) ((JsonNode) object).deepCopy();
			}

			return jsonClone(object);
		}

		private Object findFirstNonNullElement(final Collection<?> collection) {
			for (final java.lang.Object element : collection) {
				if (element != null) {
					return element;
				}
			}
			return null;
		}

		private Map.Entry<?, ?> findFirstNonNullEntry(final Map<?, ?> map) {
			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					return entry;
				}
			}
			return null;
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
