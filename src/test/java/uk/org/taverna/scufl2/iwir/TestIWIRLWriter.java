package uk.org.taverna.scufl2.iwir;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;

public class TestIWIRLWriter {

	public static final String APPLICATION_VND_SHIWA_IWIR_XML = "application/vnd.shiwa.iwir+xml";
	public static final String APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE = "application/vnd.taverna.scufl2.workflow-bundle";
	protected WorkflowBundle workflowBundle;
	protected WorkflowBundleIO bundleIO = new WorkflowBundleIO();

	@Before
	public void getExampleWorkflow() throws ReaderException, IOException {
		URL resource = getClass().getResource("/helloayone.wfbundle");
		assertNotNull(resource);
		workflowBundle = bundleIO.readBundle(resource, APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
	}

	@Test
	public void writeBundleToFile() throws Exception {
		File bundleFile = tempFile();
		bundleIO.writeBundle(workflowBundle, bundleFile,
				APPLICATION_VND_SHIWA_IWIR_XML);
	}


	public void verifyBundleFile() throws Exception {
		File bundleFile = tempFile();
		bundleIO.writeBundle(workflowBundle, bundleFile,
				APPLICATION_VND_SHIWA_IWIR_XML);
		
	}

	public File tempFile() throws IOException {
		File bundleFile = File.createTempFile("test", ".scufl2");
//		bundleFile.deleteOnExit();
		System.out.println(bundleFile);
		return bundleFile;
	}

}
