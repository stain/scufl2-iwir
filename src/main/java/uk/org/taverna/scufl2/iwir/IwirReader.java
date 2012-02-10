package uk.org.taverna.scufl2.iwir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.shiwa.fgi.iwir.AbstractTask;
import org.shiwa.fgi.iwir.BlockScope;
import org.shiwa.fgi.iwir.IWIR;
import org.shiwa.fgi.iwir.Task;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Ported;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleReader;
import uk.org.taverna.scufl2.api.port.AbstractGranularDepthPort;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

public class IwirReader implements WorkflowBundleReader {

	public Set<String> getMediaTypes() {
		return Collections.singleton(IwirWriter.APPLICATION_VND_SHIWA_IWIR_XML);
	}

	public WorkflowBundle readBundle(File bundleFile, String mediaType)
			throws ReaderException, IOException {
		IWIR iwir = new IWIR(bundleFile);
		return readIwir(iwir);
	}

	public WorkflowBundle readBundle(InputStream inputStream, String mediaType)
			throws ReaderException, IOException {
		// IWIR can't read streams, so make a temporary file
		File tmpFile = File.createTempFile("iwir", "xml");
		tmpFile.deleteOnExit();
		try {
			FileUtils.copyInputStreamToFile(inputStream, tmpFile);
			IWIR iwir = new IWIR(tmpFile);
			return readIwir(iwir);
		} finally {
			tmpFile.delete();
		}

	}

	public WorkflowBundle readIwir(IWIR iwir) throws ReaderException {
		WorkflowBundle workflowBundle = new WorkflowBundle();
		if (iwir.getWfname().startsWith("http://")
				|| iwir.getWfname().startsWith("https://")) {
			URI uri = URI.create(iwir.getWfname());
			workflowBundle.setGlobalBaseURI(uri);
		}
		workflowBundle.setName(workflowBundle.getName());

		AbstractTask task = iwir.getTask();
		if (!(task instanceof BlockScope)) {
			// TODO: Top-level workflow implied
			throw new ReaderException(
					"Not implemented: Top level task is not BlockScope, but "
							+ task.getClass());
		}
		BlockScope blockScope = (BlockScope) task;
		readBlockScope(blockScope, workflowBundle);

		return workflowBundle;
	}

	protected void readBlockScope(BlockScope blockScope,
			WorkflowBundle workflowBundle) throws ReaderException {
		Workflow wf = new Workflow();
		wf.setName(blockScope.getName());
		readPorts(blockScope, wf);
		readTasks(blockScope, wf);
		readLinks(blockScope, wf);		
		
	}

	protected void readTasks(BlockScope blockScope, Workflow wf) {
		for (AbstractTask abstTask : blockScope.getBodyTasks()) {
			if (!(abstTask instanceof Task)) {
				throw new ReaderException("R");
			}
			Task task = (Task) abstTask;
		}

	}

	protected void readLinks(BlockScope blockScope, Workflow wf) {
		// TODO Auto-generated method stub
		
	}

	protected void readPorts(AbstractTask blockScope, Ported ported) throws ReaderException {
		// or getAllInputPorts() ?
		for (org.shiwa.fgi.iwir.InputPort inputPort : blockScope
				.getInputPorts()) {
			InputPort port;
			if (ported instanceof Workflow) {
				port = new InputWorkflowPort((Workflow) ported,
						inputPort.getName());
			} else if (ported instanceof Processor) {
				port = new InputProcessorPort((Processor) ported,
						inputPort.getName());
			} else if (ported instanceof Activity) {
				port = new InputActivityPort((Activity) ported,
						inputPort.getName());
			} else {
				throw new ReaderException("Unknown Ported subclass "
						+ ported.getClass());
			}
			port.setDepth(inputPort.getType().getNestingLevel());
		}

		for (org.shiwa.fgi.iwir.OutputPort outputPort : blockScope
				.getOutputPorts()) {
			OutputPort port;
			if (ported instanceof Workflow) {
				port = new OutputWorkflowPort((Workflow) ported,
						outputPort.getName());
			} else if (ported instanceof Processor) {
				port = new OutputProcessorPort((Processor) ported,
						outputPort.getName());
			} else if (ported instanceof Activity) {
				port = new OutputActivityPort((Activity) ported,
						outputPort.getName());
			} else {
				throw new ReaderException("Unknown Ported subclass "
						+ ported.getClass());
			}
			if (port instanceof AbstractGranularDepthPort) {
				AbstractGranularDepthPort depthPort = (AbstractGranularDepthPort)port;
				depthPort.setDepth(outputPort.getType().getNestingLevel());				
				depthPort.setGranularDepth(outputPort.getType().getNestingLevel());				
			}			
		}
	}

	public String guessMediaTypeForSignature(byte[] firstBytes) {
		String firstChars;
		try {
			firstChars = new String(firstBytes, "latin1");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		if (firstChars.contains("<IWIR")
				&& firstChars.contains("http://shiwa-workflow.eu/IWIR")) {
			return IwirWriter.APPLICATION_VND_SHIWA_IWIR_XML;
		}
		return null;
	}

}
