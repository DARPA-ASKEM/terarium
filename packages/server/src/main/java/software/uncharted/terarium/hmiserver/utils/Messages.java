package software.uncharted.terarium.hmiserver.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Helper to simplify accessing i18n messages in code.
 *
 * This finds messages automatically found from src/main/resources (files named messages_*.properties)
 *
 * For now, we are using a hard-coded English locale.
 */
@Component
public class Messages {

	private final MessageSourceAccessor accessor;

	public Messages(MessageSource messageSource) {
		this.accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
	}

	public String get(final String code) {
		return accessor.getMessage(code);
	}
}
