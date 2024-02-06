package software.uncharted.terarium.esingest.iterators;

import java.io.IOException;
import java.util.List;

public interface IInputIterator<T> {

	public List<T> getNext() throws IOException;

}
