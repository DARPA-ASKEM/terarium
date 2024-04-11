package software.uncharted.terarium.esingest.iterators;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.util.FileUtil;

@Slf4j
public class JSONKeyIterator<T extends IInputDocument> implements IInputIterator<T> {

  ObjectMapper mapper;
  Queue<Path> files;
  long batchSize;
  Class<T> classType;
  ObjectNode currentNode;

  public JSONKeyIterator(Path inputPath, Class<T> classType, long batchSize) throws IOException {
    this.batchSize = batchSize;
    this.classType = classType;
    this.files = new LinkedList<>(FileUtil.getJsonFiles(inputPath));
    if (files.isEmpty()) {
      throw new IOException("No input files found for path: " + inputPath.toString());
    }
    this.mapper = new ObjectMapper();
    this.mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
    this.currentNode = (ObjectNode) this.mapper.readTree(Files.readString(files.poll()));
  }

  public List<T> getNext() throws IOException {

    List<T> results = new ArrayList<>();

    while (true) {
      if (currentNode == null) {
        // done
        if (results.isEmpty()) {
          return null;
        }
        return results;
      }

      Iterator<String> keys = currentNode.fieldNames();
      List<String> keysToRemove = new ArrayList<>();

      while (keys.hasNext()) {
        String key = keys.next();

        T doc = mapper.readValue(currentNode.get(key).toString(), classType);
        if (doc.getId() == null || doc.getId().isEmpty()) {
          doc.setId(key);
        }
        results.add(doc);

        if (results.size() == batchSize) {
          // done this batch

          // remove the keys we have processed
          for (String k : keysToRemove) {
            currentNode.remove(k);
          }
          return results;
        }

        // If you want to remove this key, add it to keysToRemove
        keysToRemove.add(key);
      }

      // we are done this node
      this.currentNode = null;

      // load next node
      if (!files.isEmpty()) {
        this.currentNode = (ObjectNode) mapper.readTree(Files.readString(files.poll()));
      }
    }
  }
}
