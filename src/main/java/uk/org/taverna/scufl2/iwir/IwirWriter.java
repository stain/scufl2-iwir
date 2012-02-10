package uk.org.taverna.scufl2.iwir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

import org.shiwa.fgi.iwir.IWIR;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.io.WorkflowBundleWriter;
import uk.org.taverna.scufl2.api.io.WriterException;

public class IwirWriter implements WorkflowBundleWriter {

	public Set<String> getMediaTypes() {
		return Collections.singleton("application/vnd.shiwa.iwir+xml");
	}

	public void writeBundle(WorkflowBundle bundle, File file, String mediaType)
			throws WriterException, IOException {

		IWIR dummy = new IWIR(bundle.getName());
		System.out.println(dummy.asXMLString());
		dummy.asXMLFile(file);
	}

	public void writeBundle(WorkflowBundle bundle, OutputStream stream, String mediaType)
			throws WriterException, IOException {
		// TODO Auto-generated method stub
		
	}

}
