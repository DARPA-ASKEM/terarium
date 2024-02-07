package software.uncharted.terarium.esingest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

	public static String getFilenameWithoutExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex > 0) {
			filename = filename.substring(0, dotIndex);
		}
		return filename;
	}

	public static String getFilenameWithoutExtension(Path path) {
		return getFilenameWithoutExtension(path.getFileName().toString());
	}

	public static List<Path> getJSONLineFilesInDir(Path dir) throws IOException {
		try (Stream<Path> stream = Files.walk(dir)) {
			List<Path> files = stream
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".jsonl"))
					.collect(Collectors.toCollection(LinkedList::new));
			return files;
		}
	}

	public static List<Path> getJsonFiles(Path dir) throws IOException {
		List<Path> jsonFiles = new ArrayList<>();
		Files.walk(dir)
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".json"))
				.forEach(jsonFiles::add);
		return jsonFiles;
	}

}
