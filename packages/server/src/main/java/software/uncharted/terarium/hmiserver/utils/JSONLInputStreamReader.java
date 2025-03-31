package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/**
 * Reads a JSONL input stream and produces batches of documents as a list of strings
 */
@RequiredArgsConstructor
public class JSONLInputStreamReader {

	final int bufferSize;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public void read(InputStream inputStream, BatchProducer<List<JsonNode>> producer) {
		byte[] buffer = new byte[bufferSize];
		String leftOver = "";
		int bytesRead;
		try {
			while ((bytesRead = inputStream.readNBytes(buffer, 0, bufferSize)) > 0) {
				final String stringBuffer = new String(buffer, 0, bytesRead);

				// The last document in the buffer may be incomplete, so we need to find the last complete document
				int lastDocumentIndex = stringBuffer.lastIndexOf("}\n") + 1;
				if (lastDocumentIndex == 0) {
					// Either this is the last document in the stream and there is no newline, or the buffer is too small
					// to contain a complete document.  Try to parse the buffer as a single document and see if it works.
					// If so, return the document and reset the buffer.  If not, throw an error.
					try {
						final JsonNode document = objectMapper.readTree(leftOver + stringBuffer);
						producer.onBatch(Collections.singletonList(document));
						leftOver = "";
						buffer = new byte[bufferSize];
						continue;
					} catch (IOException e) {
						// The buffer is too small to contain a complete document
						throw new IOException("Could not find a complete document in the buffer");
					}
				}

				// Convert the buffer to a list of documents
				final String documentBuffer = leftOver + stringBuffer.substring(0, lastDocumentIndex);
				leftOver = stringBuffer.substring(lastDocumentIndex);
				final List<String> documents = Stream.of(documentBuffer.split("\n")).filter(s -> !s.isBlank()).toList();

				final List<JsonNode> documentList = new ArrayList<>();
				for (String document : documents) {
					try {
						documentList.add(objectMapper.readTree(document));
					} catch (IOException e) {
						producer.onError(e);
					}
				}
				producer.onBatch(documentList);

				// reset the buffer
				buffer = new byte[bufferSize];
			}
			if (!leftOver.isBlank()) {
				producer.onBatch(Collections.singletonList(objectMapper.readTree(leftOver)));
			}
		} catch (IOException e) {
			producer.onError(e);
		}
	}
}
