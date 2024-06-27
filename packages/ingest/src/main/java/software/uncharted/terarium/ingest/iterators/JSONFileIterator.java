package software.uncharted.terarium.ingest.iterators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.ingest.util.FileUtil;

@Slf4j
public class JSONFileIterator {

	ObjectMapper mapper;
	Queue<Path> files;

	public JSONFileIterator(final Path inputPath) throws IOException {
		this.files = new LinkedList<>(FileUtil.getJsonFiles(inputPath));
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.mapper = new ObjectMapper();
		this.mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
	}

	public JsonNode getNext() throws IOException {
		if (files.isEmpty()) {
			// done
			return null;
		}
		final Path file = files.poll();
		final String content = Files.readString(file);
		return mapper.readTree(content);
	}
}
