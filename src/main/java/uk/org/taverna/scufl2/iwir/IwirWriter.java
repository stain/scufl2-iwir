package uk.org.taverna.scufl2.iwir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.shiwa.fgi.iwir.AbstractPort;
import org.shiwa.fgi.iwir.AbstractTask;
import org.shiwa.fgi.iwir.BlockScope;
import org.shiwa.fgi.iwir.CollectionType;
import org.shiwa.fgi.iwir.DataType;
import org.shiwa.fgi.iwir.IWIR;
import org.shiwa.fgi.iwir.SimpleType;
import org.shiwa.fgi.iwir.Task;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Ported;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.WorkflowBundleWriter;
import uk.org.taverna.scufl2.api.io.WriterException;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.Port;

public class IwirWriter implements WorkflowBundleWriter {

	WeakHashMap<Port, WeakReference<AbstractPort>> portMapping = new WeakHashMap<Port, WeakReference<AbstractPort>>();
	
	WeakHashMap<Processor, WeakReference<AbstractTask>> procMapping = new WeakHashMap<Processor, WeakReference<AbstractTask>>();
	
	
	public Set<String> getMediaTypes() {
		return Collections.singleton("application/vnd.shiwa.iwir+xml");
	}
	

	public void writeBundle(WorkflowBundle wfBundle, File file, String mediaType) throws WriterException, IOException {

		IWIR iwir = new IWIR(wfBundle.getName());
		// iwir.setVersion(bundle.getGlobalBaseURI().toASCIIString());
		Workflow wf = wfBundle.getMainWorkflow();
		BlockScope workflowTask = new BlockScope(wf.getName());
		iwir.setTask(workflowTask);

		addPorts(workflowTask, (Ported) wf);
		addProcessors(workflowTask, wf, wfBundle);
		addLinks(workflowTask, wf, wfBundle);
		addControlLinks(workflowTask, wf, wfBundle);
		
		System.out.println(iwir.asXMLString());
		iwir.asXMLFile(file);
	}

	private void addControlLinks(BlockScope workflowTask, Workflow wf,
			WorkflowBundle wfBundle) {
		for (ControlLink cl : wf.getControlLinks() ) {
			if (! (cl instanceof BlockingControlLink)) {
				continue;
			}
			BlockingControlLink bCL = (BlockingControlLink) cl;
			AbstractTask blockTask = procMapping.get(bCL.getBlock()).get();
			AbstractTask untilFinishedTask = procMapping.get(bCL.getUntilFinished()).get();
			workflowTask.addLink(untilFinishedTask, blockTask);			
		}
	}


	private void addLinks(BlockScope workflowTask, Workflow workflow,
			WorkflowBundle wfBundle) {
		for (DataLink dl : workflow.getDataLinks()) {
			if (dl.getMergePosition() != null) {
				System.err.println("Merge ports not yet supported");
				continue;
			}

			AbstractPort fromPort = portMapping.get(dl.getReceivesFrom()).get();
			AbstractPort toPort = portMapping.get(dl.getSendsTo()).get();
			
			workflowTask.addLink(fromPort, toPort);			
		}
		

	}

	private void addProcessors(BlockScope workflowTask, Workflow workflow,
			WorkflowBundle wfBundle) {
		for (Processor proc : workflow.getProcessors()) {
			Configuration config = new Scufl2Tools()
					.configurationForActivityBoundToProcessor(proc,
							wfBundle.getMainProfile());
			Activity activity = (Activity) config.getConfigures();
			String tasktype = activity.getConfigurableType().toASCIIString();
			Task procTask = new Task(proc.getName(), tasktype);
			addPorts(procTask, proc);
			workflowTask.addTask(procTask);
			procMapping.put(proc, new WeakReference<AbstractTask>(procTask));
			// TODO: Check for nested workflows and make a BlockScope instead
			// TODO: Detect while loops
			// TODO: Detect iterations
		}
	}

	private void addPorts(AbstractTask task, Ported proc) {
		for (uk.org.taverna.scufl2.api.port.InputPort inPort : proc
				.getInputPorts()) {
			// TODO: Check for known binaries
			DataType type = SimpleType.STRING;
			if (inPort.getDepth() > 0) {
				type = new CollectionType(type);
			}
			org.shiwa.fgi.iwir.InputPort inputPort = new org.shiwa.fgi.iwir.InputPort(
					inPort.getName(), type);
			task.addInputPort(inputPort);
			portMapping.put(inPort, new WeakReference<AbstractPort>(inputPort));			
		}

		for (uk.org.taverna.scufl2.api.port.OutputPort outPort : proc
				.getOutputPorts()) {
			// TODO: Check for known binaries
			DataType type = SimpleType.STRING;
			if (outPort instanceof OutputProcessorPort
					&& ((OutputProcessorPort) outPort).getDepth() > 0) {
				type = new CollectionType(type);
			}
			// FIXME: Workflow output ports depth is *calculated* - but here we
			// force them as single string
			org.shiwa.fgi.iwir.OutputPort outputPort = new org.shiwa.fgi.iwir.OutputPort(
					outPort.getName(), type);
			task.addOutputPort(outputPort);
			portMapping.put(outPort, new WeakReference<AbstractPort>(outputPort));


		}
	}

	public void writeBundle(WorkflowBundle bundle, OutputStream stream,
			String mediaType) throws WriterException, IOException {
		// TODO Auto-generated method stub

	}

}
