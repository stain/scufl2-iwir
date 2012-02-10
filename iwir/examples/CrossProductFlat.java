package org.shiwa.fgi.iwir.examples;

import java.io.IOException;

import org.shiwa.fgi.iwir.CollectionType;
import org.shiwa.fgi.iwir.Constraint;
import org.shiwa.fgi.iwir.IWIR;
import org.shiwa.fgi.iwir.InputPort;
import org.shiwa.fgi.iwir.LoopElement;
import org.shiwa.fgi.iwir.OutputPort;
import org.shiwa.fgi.iwir.ParallelForEachTask;
import org.shiwa.fgi.iwir.SimpleType;
import org.shiwa.fgi.iwir.Task;

public class CrossProductFlat {
	
	public static void main(String[] args) throws IOException {
		IWIR crossProduct = new CrossProductFlat().build();

		System.out.println(crossProduct.asXMLString());

	}

	public IWIR build() {
		IWIR i = new IWIR("flatt");

		ParallelForEachTask forEach1 = new ParallelForEachTask("foreach1");
		forEach1.addInputPort(new InputPort("collB", new CollectionType(
				SimpleType.FILE)));
		forEach1.addLoopElement(new LoopElement("collA", new CollectionType(
				SimpleType.FILE)));

		ParallelForEachTask forEach2 = new ParallelForEachTask("foreach2");
		forEach2.addInputPort(new InputPort("elementA", SimpleType.FILE));
		forEach2.addLoopElement(new LoopElement("collB", new CollectionType(
				SimpleType.FILE)));

		Task a = new Task("A", "consumer");
		a.addInputPort(new InputPort("elementA", SimpleType.FILE));
		a.addInputPort(new InputPort("elementB", SimpleType.FILE));
		a.addOutputPort(new OutputPort("res", SimpleType.FILE));

		forEach2.addTask(a);
		forEach2.addOutputPort(new OutputPort("res", new CollectionType(
				SimpleType.FILE)));
		forEach2.addLink(forEach2.getPort("elementA"), a.getPort("elementA"));
		forEach2.addLink(forEach2.getPort("collB"), a.getPort("elementB"));
		forEach2.addLink(a.getPort("res"), forEach2.getPort("res"));

		forEach1.addTask(forEach2);
		OutputPort out = new OutputPort("res", new CollectionType(SimpleType.FILE));
		out.addConstraint(new Constraint("flatten-collection", "1"));
		forEach1.addOutputPort(out);
		
		forEach1.addLink(forEach1.getPort("collA"),
				forEach2.getPort("elementA"));
		forEach1.addLink(forEach1.getPort("collB"), forEach2.getPort("collB"));
		forEach1.addLink(forEach2.getPort("res"), forEach1.getPort("res"));

		i.setTask(forEach1);

		return i;
	}

}
