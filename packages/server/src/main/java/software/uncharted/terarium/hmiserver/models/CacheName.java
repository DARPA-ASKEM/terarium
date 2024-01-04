package software.uncharted.terarium.hmiserver.models;

import lombok.ToString;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ToString
public abstract class CacheName {
	public static final String EXAMPLE = "EXAMPLE";

	public static List<String> getAll() {
		return Arrays.stream(CacheName.class.getDeclaredFields())
			.map(Field::getName)
			.collect(Collectors.toList());
	}
}
