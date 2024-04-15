package software.uncharted.terarium.hmiserver.annotations;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class AMRPropertyNamingStrategy extends PropertyNamingStrategy {

    private final PropertyNamingStrategy delegate;

    public AMRPropertyNamingStrategy(PropertyNamingStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        if (field.getDeclaringClass().isAnnotationPresent(AMRSchemaType.class)) {
            // don't mess with how the keys are named in the schema
            return defaultName;
        } else {
            return delegate.nameForField(config, field, defaultName);
        }
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        if (method.getDeclaringClass().isAnnotationPresent(AMRSchemaType.class)) {
            // don't mess with how the keys are named in the schema
            return defaultName;
        } else {
            return delegate.nameForGetterMethod(config, method, defaultName);
        }
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        if (method.getDeclaringClass().isAnnotationPresent(AMRSchemaType.class)) {
            // don't mess with how the keys are named in the schema
            return defaultName;
        } else {
            return delegate.nameForSetterMethod(config, method, defaultName);
        }
    }
}
