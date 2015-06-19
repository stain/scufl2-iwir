# scufl2-iwir

Convert Taverna Scufl2 workflow bundle to SHIWA IWIR description

See:

* http://www.dps.uibk.ac.at/~kassian/shiwa/iwir/iwirTool-1.1.4/
* https://www.shiwa-workflow.eu/documents/10753/55350/IWIR+v1.1+Specification
* http://www.myexperiment.org/workflows/2649
* http://taverna.incubator.apache.org/documentation/scufl2/
* https://github.com/apache/incubator-taverna-language/

Example from converting `helloanyone.wfbundle`:

Author: [Stian Soiland-Reyes](http://orcid.org/0000-0001-9842-9718), University of Manchester
License: [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0) -- Note: Dependency on [IWIR Tool](http://sourceforge.net/projects/iwirtool/) which is licensed as [GNU LGPL License 3.0](https://www.gnu.org/licenses/gpl-3.0.html)
	
	
```xml  
	<IWIR version="1.1" wfname="http://ns.taverna.org.uk/2010/workflowBundle/01348671-5aaa-4cc2-84cc-477329b70b0d/" xmlns="http://shiwa-workflow.eu/IWIR">
	  <blockScope name="Hello_Anyone">
	    <inputPorts>
	      <inputPort name="name" type="string"/>
	    </inputPorts>
	    <body>
	      <task name="Concatenate_two_strings" tasktype="http://ns.taverna.org.uk/2010/activity/beanshell">
	        <inputPorts>
	          <inputPort name="string1" type="string"/>
	          <inputPort name="string2" type="string"/>
	        </inputPorts>
	        <outputPorts>
	          <outputPort name="output" type="string"/>
	        </outputPorts>
	      </task>
	      <task name="hello" tasktype="http://ns.taverna.org.uk/2010/activity/constant">
	        <inputPorts/>
	        <outputPorts>
	          <outputPort name="value" type="string"/>
	        </outputPorts>
	      </task>
	    </body>
	    <outputPorts>
	      <outputPort name="greeting" type="string"/>
	    </outputPorts>
	    <links>
	      <link from="Hello_Anyone/name" to="Concatenate_two_strings/string2"/>
	      <link from="Concatenate_two_strings/output" to="Hello_Anyone/greeting"/>
	      <link from="hello/value" to="Concatenate_two_strings/string1"/>
	    </links>
	  </blockScope>
	</IWIR>
```
