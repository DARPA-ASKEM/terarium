package software.uncharted.terarium.hmiserver.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserIdConverter implements AttributeConverter<UserId, Long> {

	@Override
	public Long convertToDatabaseColumn(UserId userId) {
		if (userId == null) {
			return null;
		}
		return userId.toLong();
	}

	@Override
	public UserId convertToEntityAttribute(Long dbData) {
		if (dbData == null) {
			return null;
		}
		return new UserId(dbData);
	}
}
