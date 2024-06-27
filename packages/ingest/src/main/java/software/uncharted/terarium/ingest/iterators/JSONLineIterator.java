package software.uncharted.terarium.ingest.iterators;

import java.io.BufferedReader;
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
public class JSONLineIterator {

	ObjectMapper mapper;
	Queue<Path> files;
	BufferedReader reader;

	public JSONLineIterator(final Path inputPath) throws IOException {
		this.files = new LinkedList<>(FileUtil.getJSONLineFilesInDir(inputPath));
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.reader = Files.newBufferedReader(files.poll());
		this.mapper = new ObjectMapper();
		this.mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
	}

	public JsonNode getNext() throws IOException {
		while (true) {
			if (reader == null) {
				// done all files
				return null;
			}

			final String line = reader.readLine();
			if (line != null) {
				// map and return
				return mapper.readTree(line);
			}

			// done with this file
			reader.close();
			reader = null;
			if (!files.isEmpty()) {
				// go to next file
				reader = Files.newBufferedReader(files.poll());
			}
		}
	}
}
