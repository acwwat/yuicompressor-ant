YUICompressorAntTask
====================
This project wraps the YUI Compressor with an Ant task to provide better
integration of JavaScript and CSS compression to an Ant-based build process.
It provides more control with batch processing and output location through
file sets and mapper in Ant over the command-line interface. 

Requirements
------------

YUICompressorAntTask works with the following:

* J2SE 5.0 or later
* Apache Ant 1.7.0 or later: <http://ant.apache.org/>
* YUI Compressor 2.4.8:
<http://www.yuiblog.com/blog/2013/05/16/yuicompressor-2-4-8-released/>

Building
--------

Use the included Ant build file, `build.xml`, to build the project. The build
artifact `YUICompressorAntTask-2.4.8.jar` will be created in the `dist`
directory.

Usage
-----

In your Ant build file, use the `<taskdef>` task to define the
`<yuicompressor>` Ant task (change the JAR file locations as needed):

	<taskdef name="yuicompressor"
	         classname="com.github.acwwat.ant.taskdefs.YUICompressTask">
		<classpath>
			<pathelement location="YUICompressorAntTask-2.4.8.jar" />
			<pathelement location="yuicompressor-2.4.8.jar" />
		</classpath>
	</taskdef>

Refer to the JavaDoc description of the `YUICompressorTask` class for usage of
the Ant task.