package software.uncharted.terarium.hmiserver.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypescriptOptional {
}
