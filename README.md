rcjp
====

Instrument Java bytecode using ASM library, implementing a simple reference counting model to test Java programs.

Description
---
rcjp is short for reference counting Java program.

This project is aimed to discover some detailed properties existing in Whole-Part relationships from Java program. An instrumenting program is built to trace behaviors of instances in Java program and produce tracing files. It can modify Java bytecode while certain class is loading into JVM. Relationship analysis is based on tracing files. 

In analysis, three normal properties of Whole-Part relationship are introduced. They are lifetime, exclusivity and multiplicity. 

Results of analysis are demonstrated through some pie charts and line charts, as well as an analysis_result.txt. The program, tomcat, is tested.


What Are They
---

Report.pdf is my final report of this project.

All source code is organised in eclipse, a common IDE for Java. 


There are three sub-folders:
api: Include all Jar packages in this project.
	ASMInstrument.jar is the Jar package implement algorithm in this project.
	asm-3.0.jar is used in ASMInstrument.jar for instrument with Java bytecode.
	jcommon-1.0.16.jar and jfreechart-1.0.14.jar are used for drawing charts in this project.
	dacapo-9.12-bach.jar is the Jar package which has been tested by this project.
pro: The configuration file, fyp.properties, exists in this folder.
result: A folder to keep result produced by this program.


To execute the program in this project:

* Step1. Editing configuration file. Modifing OutputPath to the current absolute path of result folder.
* Step2. Make api folder as the working directory in terminal, add tested program into this directory.
* Step3. Instrument tested program. Add javaagent option in command line when instrument the given tested program. For example, to instrument with xalan in Dacapo Benchmark Suite:  #java -javaagent:ASMInstrument.jar  -jar dacapo-9.12-bach.jar xalan
* Step4. Analyze trace files. 

`$java -classpath ./:ASMInstrument.jar:jfreechart-1.0.14.jar:jcommon-1.0.16.jar  fyp.analysis.Scanner`

* Step5. Check result files in result direcory. 

	
#License

rcjp is published under the [MIT License](http://www.opensource.org/licenses/mit-license.php).