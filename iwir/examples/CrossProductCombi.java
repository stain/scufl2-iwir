package org.shiwa.fgi.iwir.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.shiwa.fgi.iwir.CollectionType;
import org.shiwa.fgi.iwir.IWIR;
import org.shiwa.fgi.iwir.InputPort;
import org.shiwa.fgi.iwir.LoopElement;
import org.shiwa.fgi.iwir.OutputPort;
import org.shiwa.fgi.iwir.ParallelForEachTask;
import org.shiwa.fgi.iwir.SimpleType;
import org.shiwa.fgi.iwir.Task;

//a concrete example for a cross product over a combination set
public class CrossProductCombi {
	public static void main(String[] args) throws IOException {
		IWIR crossProduct = new CrossProductCombi().build();

		// to stdout
		System.out.println(crossProduct.asXMLString());

		// to file
		crossProduct.asXMLFile(new File("crossProductCombination.xml"));

		// form file
		crossProduct = new IWIR(new File("crossProductCombination.xml"));

		// to stdout
		System.out.println(crossProduct.asXMLString());
	}

	private IWIR build() {
		IWIR i = new IWIR("crossProductCombination");

		ParallelForEachTask forEach1 = new ParallelForEachTask("forEach1");
		forEach1.addInputPort(new InputPort("collC", new CollectionType(
				SimpleType.FILE)));
		forEach1.addInputPort(new InputPort("collD", new CollectionType(
				SimpleType.FILE)));
		forEach1.addLoopElement(new LoopElement("collA", new CollectionType(
				SimpleType.FILE)));
		forEach1.addLoopElement(new LoopElement("collB", new CollectionType(
				SimpleType.FILE)));

		ParallelForEachTask forEach2 = new ParallelForEachTask("forEach2");
		forEach2.addInputPort(new InputPort("elementA", SimpleType.FILE));
		forEach2.addInputPort(new InputPort("elementB", SimpleType.FILE));
		forEach2.addLoopElement(new LoopElement("collC", new CollectionType(
				SimpleType.FILE)));
		forEach2.addLoopElement(new LoopElement("collD", new CollectionType(
				SimpleType.FILE)));

		Task b = new Task("B", "exampleTyp");
		b.addInputPort(new InputPort("elementA", SimpleType.FILE));
		b.addInputPort(new InputPort("elementB", SimpleType.FILE));
		b.addInputPort(new InputPort("elementC", SimpleType.FILE));
		b.addInputPort(new InputPort("elementD", SimpleType.FILE));
		b.addOutputPort(new OutputPort("res", SimpleType.FILE));

		forEach2.addTask(b);
		forEach2.addOutputPort(new OutputPort("res", new CollectionType(
				SimpleType.FILE)));
		forEach2.addLink(forEach2.getPort("elementA"), b.getPort("elementA"));
		forEach2.addLink(forEach2.getPort("elementB"), b.getPort("elementB"));
		forEach2.addLink(forEach2.getPort("collC"), b.getPort("elementC"));
		forEach2.addLink(forEach2.getPort("collD"), b.getPort("elementD"));
		forEach2.addLink(b.getPort("res"), forEach2.getPort("res"));

		forEach1.addTask(forEach2);
		forEach1.addOutputPort(new OutputPort("res", new CollectionType(
				new CollectionType(SimpleType.FILE))));
		forEach1.addLink(forEach1.getPort("collA"),
				forEach2.getPort("elementA"));
		forEach1.addLink(forEach1.getPort("collB"),
				forEach2.getPort("elementB"));
		forEach1.addLink(forEach1.getPort("collC"), forEach2.getPort("collC"));
		forEach1.addLink(forEach1.getPort("collD"), forEach2.getPort("collD"));
		forEach1.addLink(forEach2.getPort("res"), forEach1.getPort("res"));

		i.setTask(forEach1);
		return i;
	}
}
