package software.uncharted.terarium.hmiserver.aspects;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class HasProjectAccessAspect {

	private final ProjectService projectService;
	private final CurrentUserService currentUserService;

	private static final ExpressionParser expressionParser = new SpelExpressionParser();

	/**
	 * This aspect will be called around all methods of classes that have the HasProjectAccess annotation.
	 * If the method itself is annotated, we'll let the method annotation take precedence.
	 * @param joinPoint   The join point
	 * @param annotation  The {@link HasProjectAccess} annotation
	 * @return            The proceeding join point if the user has access, otherwise an exception will be thrown
	 * @throws Throwable  If the user does not have access
	 */
	@Around("@within(annotation)")
	public Object classHasProjectAccess(ProceedingJoinPoint joinPoint, HasProjectAccess annotation) throws Throwable {
		// If the method we're invoking has the annotation as well, we should proceed here
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		if (method.isAnnotationPresent(HasProjectAccess.class)) {
			return joinPoint.proceed();
		}

		return hasProjectAccess(joinPoint, annotation);
	}

	/**
	 * This aspect will be called around all methods that are annotated with the HasProjectAccess annotation.
	 * @param joinPoint   The join point
	 * @param annotation  The {@link HasProjectAccess} annotation
	 * @return            The proceeding join point if the user has access, otherwise an exception will be thrown
	 * @throws Throwable  If the user does not have access
	 */
	@Around("@annotation(annotation)")
	public Object methodHasProjectAccess(ProceedingJoinPoint joinPoint, HasProjectAccess annotation) throws Throwable {
		return hasProjectAccess(joinPoint, annotation);
	}

	/**
	 * This method will check if the user has access to the project specified in the annotation.
	 * @param joinPoint   The join point
	 * @param annotation  The {@link HasProjectAccess} annotation
	 * @return            The proceeding join point if the user has access, otherwise an exception will be thrown
	 * @throws Throwable  If the user does not have access
	 */
	private Object hasProjectAccess(ProceedingJoinPoint joinPoint, HasProjectAccess annotation) throws Throwable {
		// Use the spel string to get the id
		String spel = annotation.value();
		String projectId = null;
		try {
			StandardEvaluationContext context = new StandardEvaluationContext();
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			for (int i = 0; i < signature.getParameterNames().length; i++) {
				context.setVariable(signature.getParameterNames()[i], joinPoint.getArgs()[i]);
			}
			Expression expression = expressionParser.parseExpression(spel);
			final Object projectIdObj = expression.getValue(context, Object.class);
			if (projectIdObj != null) {
				projectId = projectIdObj.toString();
			}
		} catch (Exception e) {
			log.error("Unable to parse expression: " + spel);
			throw e;
		}
		if (projectId == null) {
			throw new RuntimeException("Unable to match function arguments with SpEL: " + spel);
		}

		// Get the project
		final Optional<Project> project = projectService.getProject(UUID.fromString(projectId));
		if (project.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find project: " + projectId);
		}

		// Validate we have access
		final User user = currentUserService.get();
		if (!projectService.hasPermission(project.get(), user, annotation.level())) {
			throw new AccessDeniedException(
				"User " +
				user.getId() +
				" does not have permission to access project " +
				projectId +
				" at level " +
				annotation.level()
			);
		}

		return joinPoint.proceed();
	}
}
