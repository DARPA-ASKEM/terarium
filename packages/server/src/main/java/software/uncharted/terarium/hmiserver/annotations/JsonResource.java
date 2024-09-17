package software.uncharted.terarium.hmiserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a JSON resource into a Spring bean. The value of the annotation is the path to the resource and
 * can be a classpath resource or a file path. The resource will be loaded as a JSON object and injected into the bean
 * according to the field type. A serialization exception will be thrown if the resource cannot be loaded or if the
 * resource cannot be deserialized into the field type.
 *
 * <p>Example: @JsonResource("classpath:mock_admin.json") private User mockAdmin;
 *
 * <p>Will load the resource mock_admin.json from the classpath and deserialize it into a User object.
 *
 * <p>It is also possible to load multiple resources into an array: @JsonResource("classpath*:/mock-users/*.json")
 * private User[] mockUsers;
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonResource {
	String value();
}
