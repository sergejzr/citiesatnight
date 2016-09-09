package sz.de.l3s.features.util;

import java.io.File;

public interface TraverseListener {

	public void directoryFound(File examine);

	public void fileFound(File examine);

}
