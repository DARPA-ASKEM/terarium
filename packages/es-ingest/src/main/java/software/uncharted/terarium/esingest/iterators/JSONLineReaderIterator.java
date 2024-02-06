package software.uncharted.terarium.esingest.iterators;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONLineReaderIterator<T> implements IInputIterator<T> {

	ObjectMapper mapper = new ObjectMapper();
	Queue<Path> files;
	BufferedReader reader;
	long batchSize;
	BiFunction<List<T>, T, Boolean> batcher;
	List<T> results = new ArrayList<>();

	Class<T> classType;

	public JSONLineReaderIterator(Path inputPath, Class<T> classType, long batchSize) throws IOException {
		this.batchSize = batchSize;
		this.classType = classType;
		this.files = getFilesInDir(inputPath);
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.reader = Files.newBufferedReader(files.poll());
	}

	public JSONLineReaderIterator(Path inputPath, Class<T> classType, BiFunction<List<T>, T, Boolean> batcher)
			throws IOException {
		this.classType = classType;
		this.batcher = batcher;
		this.files = getFilesInDir(inputPath);
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.reader = Files.newBufferedReader(files.poll());
	}

	private Queue<Path> getFilesInDir(Path dir) throws IOException {
		Queue<Path> files = new LinkedList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path file : stream) {
				if (Files.isRegularFile(file)) {
					log.info("Adding file to input iterator: {}", file.toString());
					files.add(file);
				}
			}
		}
		return files;
	}

	private List<T> returnAndClearResults(T toAddAfterClear) {
		if (results.isEmpty()) {
			return null; // signal there is no more data
		}
		List<T> ret = new ArrayList<>(results);
		results = new ArrayList<>();
		if (toAddAfterClear != null) {
			results.add(toAddAfterClear);
		}
		return ret;
	}

	private List<T> returnAndClearResults() {
		return returnAndClearResults(null);
	}

	public List<T> getNext() throws IOException {
		while (true) {
			if (reader == null) {
				// done
				return returnAndClearResults();
			}

			for (String line; (line = reader.readLine()) != null;) {
				T doc = mapper.readValue(line, this.classType);
				if (batcher != null) {
					// check if we need to split the batch yet

					boolean splitBatch = batcher.apply(results, doc);
					if (splitBatch) {
						return returnAndClearResults(doc);
					}
					results.add(doc);
				} else {
					// static batch size
					results.add(doc);
					if (results.size() == batchSize) {
						return returnAndClearResults();
					}
				}
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
