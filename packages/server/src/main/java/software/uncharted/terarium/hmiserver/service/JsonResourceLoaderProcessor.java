package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JsonResourceLoaderProcessor implements ResourceLoaderAware, BeanPostProcessor {

	private final ObjectMapper mapper;

	private ResourceLoader resourceLoader;

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(JsonResource.class)) {
				JsonResource annotation = field.getAnnotation(JsonResource.class);
				try {
					ClassLoader cl = this.getClass().getClassLoader();
					ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
					Resource[] resources = resolver.getResources(annotation.value());
					Object fieldValue = null;
					if (resources.length == 0) {
						setField(fieldValue, field, bean);
					} else if (resources.length == 1) {
						String resourceString = loadResourceToString(resources[0].getURI().toString());
						fieldValue = mapper.readValue(resourceString, field.getType());
						setField(fieldValue, field, bean);
					} else {
						final Object deserializedResources = Array.newInstance(field.getType().getComponentType(), resources.length);
						for (int i = 0; i < resources.length; i++) {
							String resourceString = loadResourceToString(resources[i].getURI().toString());
							Array.set(deserializedResources, i, mapper.readValue(resourceString, field.getType().getComponentType()));
						}
						setField(deserializedResources, field, bean);
					}
				} catch (IllegalAccessException | IOException e) {
					e.printStackTrace();
					throw new BeanInitializationException("Exception loading resource: " + annotation.value(), e);
				}
			}
		}
		return bean;
	}

	private void setField(Object value, Field field, Object bean) throws IllegalAccessException {
		if (!field.canAccess(bean)) {
			field.setAccessible(true);
			field.set(bean, value);
			field.setAccessible(false);
		} else {
			field.set(bean, value);
		}
	}

	private String loadResourceToString(String resourceLocation) throws IOException {
		Resource resource = resourceLoader.getResource(resourceLocation);
		return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
	}

	@Override
	public void setResourceLoader(final org.springframework.core.io.ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
