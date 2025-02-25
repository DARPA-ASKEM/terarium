package software.uncharted.terarium.hmiserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

/**
 * This annotation can be used on methods and classes to determine if the current user has access to the project with
 * the specified id.  For example, consider the following class and method:
 *
 *
 *  @HasProjectAccess("#project.getId()")
 *  public void doSomething(Project project) {
 *    // Do something
 *  }
 *
 *  The above annotation will check if the current user has access to the project with the id specified in the
 *  {@link software.uncharted.pantera.model.project.Project} object passed to the method.  If the user does not have
 *  access, an exception will be thrown.  It can be considered equivalent to the following code:
 *
 *  public void doSomething(Project project) {
 *    if (!projectService.hasAccess(project.getId(), currentUserService.getCurrentUser())) {
 *      throw new AccessDeniedException(...);
 *    }
 *    // Do something
 *  }
 *
 *  Additionally, classes can be annotated with this annotation, and all methods in the class will be checked for
 *  access. If both a class and a method is annotated, the method annotation will take precedence.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface HasProjectAccess {
	String value() default "#id";

	Schema.Permission level() default Schema.Permission.READ;
//TODO: Migrate this from Pantera or make decision to use our existing Schema.Permission. Update comment if needed. -dvince
//ProjectPermissionLevel level() default ProjectPermissionLevel.READ;
}
