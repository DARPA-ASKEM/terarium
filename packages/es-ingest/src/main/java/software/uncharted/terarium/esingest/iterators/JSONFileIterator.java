package software.uncharted.terarium.esingest.iterators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.util.FileUtil;

@Slf4j
public class JSONFileIterator<T extends IInputDocument> implements IInputIterator<T> {

	ObjectMapper mapper;
	Queue<Path> files;
	long batchSize;
	BiFunction<List<T>, T, Boolean> batcher;
	List<T> results = new ArrayList<>();
	Class<T> classType;

	public JSONFileIterator(Path inputPath, Class<T> classType, long batchSize) throws IOException {
		this.batchSize = batchSize;
		this.classType = classType;
		this.files = new LinkedList<>(FileUtil.getJsonFiles(inputPath));
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.mapper = new ObjectMapper();
		this.mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
	}

	public JSONFileIterator(Path inputPath, Class<T> classType, BiFunction<List<T>, T, Boolean> batcher)
			throws IOException {
		this.batcher = batcher;
		this.classType = classType;
		this.files = new LinkedList<>(FileUtil.getJSONLineFilesInDir(inputPath));
		if (files.isEmpty()) {
			throw new IOException("No input files found for path: " + inputPath.toString());
		}
		this.mapper = new ObjectMapper();
		this.mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
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
			if (files.isEmpty()) {
				// done
				return returnAndClearResults();
			}

			Path file = files.poll();
			String content = Files.readString(file);

			T doc = mapper.readValue(content, classType);
			if (doc.getId() == null || doc.getId().isEmpty()) {
				doc.setId(FileUtil.getFilenameWithoutExtension(file.getFileName().toString()));
			}
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
	}
}
