package software.uncharted.terarium.hmiserver.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;

@Slf4j
public class MultipartFileService {

	/**
	 * Parse a multipart request into a map of field name to byte array
	 *
	 * <p>NOTE: The body of getItemIterator can only be executed once. If you need to access the metadata of the form on
	 * a per-field basis (eg/ to get the content type of a file) you will need to use the JakartaFileUpload directly or
	 * refactor this method to return a custom model that includes the metadata.
	 *
	 * @param request the request to parse
	 * @return a map of form field name to the contents of the field as a byte array
	 */
	public static Map<String, byte[]> parse(final HttpServletRequest request) {
		final Map<String, byte[]> result = new HashMap<>();
		try {
			final JakartaServletFileUpload upload = new JakartaServletFileUpload();
			upload
				.getItemIterator(request)
				.forEachRemaining(item -> {
					result.put(item.getFieldName(), item.getInputStream().readAllBytes());
				});
		} catch (final Exception e) {
			log.error("Error parsing multipart file", e);
		}
		return result;
	}
}
