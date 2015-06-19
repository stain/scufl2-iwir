package no.s11.scufl2.iwir;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.io.ReaderException;
import org.apache.taverna.scufl2.api.io.WorkflowBundleIO;
import org.junit.Before;
import org.junit.Test;

public class TestIwirReader {

	private static final String HELLOANYONE_IWIR = "helloanyone.iwir";
	protected WorkflowBundleIO bundleIO = new WorkflowBundleIO();
	private WorkflowBundle workflowBundle;

	@Before
	public void getExampleWorkflow() throws ReaderException, IOException {
		URL resource = getClass().getResource(HELLOANYONE_IWIR);
		assertNotNull("Could not find " + HELLOANYONE_IWIR, resource);
		workflowBundle = bundleIO.readBundle(resource,
				"application/vnd.shiwa.iwir+xml");
	}

	@Test
	public void readIwir() throws Exception {

	}

}
