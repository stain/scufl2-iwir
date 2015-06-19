package no.s11.scufl2.iwir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.shiwa.fgi.iwir.AbstractDataPort;
import org.shiwa.fgi.iwir.AbstractLink;
import org.shiwa.fgi.iwir.AbstractPort;
import org.shiwa.fgi.iwir.AbstractTask;
import org.shiwa.fgi.iwir.BlockScope;
import org.shiwa.fgi.iwir.DataLink;
import org.shiwa.fgi.iwir.IWIR;
import org.shiwa.fgi.iwir.InputPort;
import org.shiwa.fgi.iwir.OutputPort;
import org.shiwa.fgi.iwir.Task;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;

public class TestIwirWriter {

	private static final String HELLOANYONE_WFBUNDLE = "/helloanyone.wfbundle";
	public static final String APPLICATION_VND_SHIWA_IWIR_XML = "application/vnd.shiwa.iwir+xml";
	public static final String APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE = "application/vnd.taverna.scufl2.workflow-bundle";
	protected WorkflowBundle workflowBundle;
	protected WorkflowBundleIO bundleIO = new WorkflowBundleIO();

	@Before
	public void getExampleWorkflow() throws ReaderException, IOException {
		URL resource = getClass().getResource(HELLOANYONE_WFBUNDLE);
		assertNotNull("Could not find " + HELLOANYONE_WFBUNDLE, resource);
		workflowBundle = bundleIO.readBundle(resource,
				APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
	}

	@Test
	public void writeBundleToFile() throws Exception {
		File bundleFile = tempFile();
		bundleFile.delete();
		assertFalse(bundleFile.exists());
		bundleIO.writeBundle(workflowBundle, bundleFile,
				APPLICATION_VND_SHIWA_IWIR_XML);
		assertTrue(bundleFile.exists());
	}

	@Test
	public void verifyBundleFile() throws Exception {
		File bundleFile = tempFile();
		bundleIO.writeBundle(workflowBundle, bundleFile,
				APPLICATION_VND_SHIWA_IWIR_XML);
		IWIR iwir = new IWIR(bundleFile);		
		verifyBundle(iwir);
	}

	@Test
	public void verifyBundleStream() throws Exception {
		File bundleFile = tempFile();
		OutputStream out = new FileOutputStream(bundleFile);
		bundleIO.writeBundle(workflowBundle, out,
				APPLICATION_VND_SHIWA_IWIR_XML);
		out.flush();
		out.close();
		// FIXME: IWIR can't read from string?
		IWIR iwir = new IWIR(bundleFile);		
		verifyBundle(iwir);
	}
	
	private void verifyBundle(IWIR iwir) {
		assertEquals(
				"http://ns.taverna.org.uk/2010/workflowBundle/01348671-5aaa-4cc2-84cc-477329b70b0d/",
				iwir.getWfname());
		BlockScope wf = (BlockScope) iwir.getTask();
		assertEquals("Hello_Anyone", wf.getName());
		assertEquals(1, wf.getAllInputPorts().size());
		AbstractDataPort name = wf.getAllInputPorts().get(0);
		assertEquals("name", name.getName());
		assertTrue(name instanceof InputPort);
		// FIXME: SimpleType.equals() does not work
		//assertEquals(SimpleType.STRING, name.getType());

		assertEquals(1, wf.getAllOutputPorts().size());
		AbstractDataPort greeting = wf.getAllOutputPorts().get(0);
		assertEquals("greeting", greeting.getName());

		assertTrue(greeting instanceof OutputPort);
		// FIXME: SimpleType.equals() does not work
		//assertEquals(SimpleType.STRING, greeting.getType());

		assertEquals(2, wf.getBodyTasks().size());

		Task hello = null;
		Task concatenate_two_strings = null;

		for (AbstractTask task : wf.getBodyTasks()) {
			if (task.getName().equals("hello")) {
				hello = (Task) task;
			} else if (task.getName().equals("Concatenate_two_strings")) {
				concatenate_two_strings = (Task) task;
			}
		}
		assertNotNull("Could not find 'Hello'", hello);
		assertNotNull("Could not find 'Concatenate_two_strings",
				concatenate_two_strings);

		assertEquals("http://ns.taverna.org.uk/2010/activity/constant",
				hello.getTasktype());
		assertEquals(0, hello.getInputPorts().size());
		assertEquals(1, hello.getOutputPorts().size());
		assertEquals("value", hello.getOutputPorts().get(0).getName());
		// FIXME: SimpleType.equals() does not work
//		assertEquals(SimpleType.STRING, hello.getOutputPorts().get(0).getType());

		assertEquals("http://ns.taverna.org.uk/2010/activity/beanshell",
				concatenate_two_strings.getTasktype());
		assertEquals(2, concatenate_two_strings.getInputPorts().size());
		Set<String> expectedInputs = new HashSet<String>(Arrays.asList(
				"string1", "string2"));
		Set<String> foundInputs = new HashSet<String>();
		for (AbstractPort port : concatenate_two_strings.getInputPorts()) {
			// FIXME: SimpleType.equals() does not work
//			assertEquals(SimpleType.STRING, port.getType());
			foundInputs.add(port.getName());
		}
		assertEquals("Did not find expected input ports", expectedInputs,
				foundInputs);


		assertEquals(1, concatenate_two_strings.getOutputPorts().size());
		assertEquals("output", concatenate_two_strings.getOutputPorts().get(0).getName());
		// FIXME: SimpleType.equals() does not work
//		assertEquals(SimpleType.STRING, concatenate_two_strings.getOutputPorts().get(0).getType());

		
		// links
		Map<String,String> expectedLinks = new HashMap<String,String>();
		expectedLinks.put("name", "string2");
		expectedLinks.put("output", "greeting");
		expectedLinks.put("value", "string1");
		
		Map<String,String> foundLinks = new HashMap<String,String>();
		for (AbstractLink link : wf.getLinks()) {
			DataLink dl = (DataLink)link;
			foundLinks.put(dl.getFromPort().getName(), dl.getToPort().getName());			
		}
		assertEquals(expectedLinks, foundLinks);
	}

	public File tempFile() throws IOException {
		File bundleFile = File.createTempFile("test", ".xml");
		 bundleFile.deleteOnExit();
		// System.out.println(bundleFile);
		return bundleFile;
	}

}
