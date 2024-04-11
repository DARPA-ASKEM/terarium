package software.uncharted.terarium.esingest.iterators;

import java.io.IOException;
import java.util.List;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

public interface IInputIterator<T extends IInputDocument> {

  public List<T> getNext() throws IOException;
}
