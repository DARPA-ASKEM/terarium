package software.uncharted.terarium.hmiserver.utils;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

/**
 * Helper to simplify accessing i18n messages in code.
 *
 * <p>This finds messages automatically found from src/main/resources (files named messages_*.properties)
 *
 * <p>For now, we are using a hard-coded English locale.
 */
@Component
public class Messages {

	private final MessageSourceAccessor accessor;

	public Messages(final MessageSource messageSource) {
		this.accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
	}

	public String get(final String code) {
		return accessor.getMessage(code);
	}
}
