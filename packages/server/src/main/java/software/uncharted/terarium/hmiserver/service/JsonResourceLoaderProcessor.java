package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import software.uncharted.terarium.hmiserver.annotations.JsonResource;

@Component
@RequiredArgsConstructor
public class JsonResourceLoaderProcessor implements ResourceLoaderAware, BeanPostProcessor {

	private final ObjectMapper mapper;

	private ResourceLoader resourceLoader;

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		final Field[] fields = bean.getClass().getDeclaredFields();
		for (final Field field : fields) {
			if (field.isAnnotationPresent(JsonResource.class)) {
				final JsonResource annotation = field.getAnnotation(JsonResource.class);
				try {
					final ClassLoader cl = this.getClass().getClassLoader();
					final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
					final Resource[] resources = resolver.getResources(annotation.value());
					Object fieldValue = null;
					if (resources.length == 0) {
						setField(fieldValue, field, bean);
					} else if (resources.length == 1) {
						final String resourceString = loadResourceToString(resources[0].getURI().toString());
						fieldValue = mapper.readValue(resourceString, field.getType());
						setField(fieldValue, field, bean);
					} else {
						final Object deserializedResources = Array.newInstance(
							field.getType().getComponentType(),
							resources.length
						);
						for (int i = 0; i < resources.length; i++) {
							final String resourceString = loadResourceToString(resources[i].getURI().toString());
							Array.set(deserializedResources, i, mapper.readValue(resourceString, field.getType().getComponentType()));
						}
						setField(deserializedResources, field, bean);
					}
				} catch (final IllegalAccessException | IOException e) {
					e.printStackTrace();
					throw new BeanInitializationException("Exception loading resource: " + annotation.value(), e);
				}
			}
		}
		return bean;
	}

	private static void setField(final Object value, final Field field, final Object bean) throws IllegalAccessException {
		if (!field.canAccess(bean)) {
			field.setAccessible(true);
			field.set(bean, value);
			field.setAccessible(false);
		} else {
			field.set(bean, value);
		}
	}

	private String loadResourceToString(final String resourceLocation) throws IOException {
		final Resource resource = resourceLoader.getResource(resourceLocation);
		return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
	}

	@Override
	public void setResourceLoader(final org.springframework.core.io.ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
