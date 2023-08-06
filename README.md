# An Event Driven Simulation Framework for Java

See [user-manual](./user-manual.pdf) in this directory for details.

## License
Licensed under AGPL V 3.0

### Important! Requires _--enable-preview_
Prime Mover now requires the [Virtual Thread preview feature from Project Loom](https://openjdk.org/jeps/425).  Unfortunately, this means that the Maven plugin for Prime Mover ultimately
requires _--enable-preview_ to be used to run the plugin.  As the Prime Mover plugin is run during normal build processing
to test the plugin, this poses a problem of how to configure this correctly in Maven.  The answer is, of course, you **cannot** - lol.

Consequently, you _must_ have _"--enable-preview"_ in your [_MAVEN_OPTS_](https://maven.apache.org/configure.html).

Much apologies for this inconvienence.  This requirement will be eliminated soon with Java 21, as Project Loom is no longer preview, but a released feature of Java 21.

## Building
Requires Java 20 and Maven 3.83+

To build:

     cd <top level directory>
     mvn clean install
     
## Status
Prime Mover is now Soot Free.  The framework now uses the JVM Virtual Threads from Project Loom to provide the blocking 
thread continuation mechanism required for blocking events.  The Prime Mover event transform is now implemented by an ASM based tranform.  Maven artifacts are not currently 
published, so you'll have to build them until I get that sorted.

### Prime Mover Runtime

The artifact required for simulation runtime

     <dependency>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>runtime</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>

### Prime Mover Transform

The artifact required for simulation transform.  This module contains the ASM based event transformer framework.  The transformer uses ClassGraph to
provide the scanning mechanism to obtain the classes to transform.S

     <dependency>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>transform</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>

### Prime Mover Maven Plugin

The plugin required to transform simulation code.  This plugin runs after compilation for either main or test classes and will perform the simulation
transform on classes in the target output directory, overwriting these class files with the transformed class.

Note that because of the requirement for _--enable-preview_ you might run into interesting issues with the plugin unable to run using the Maven M2E feature.  If so, make sure
that maven is configured to use the workspace configuration JVM and that is also configured to enable previews as a default option.

     <plugin>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>maven-plugin</artifactId>
         <version>0.1.0-SNAPSHOT</version>
         <executions>
             <execution> 
                 <goals>
                     <goal>transform</goal> 
                     <goal>transform-test</goal> 
                 </goals>
             </execution>
         </executions>
     </plugin>
     
### Integration Into Eclipse

Because Prime Mover is a byte code rewriting framework, if you don't rewrite the bytecodes, you won't get the behavior that you're looking for.  When you're working in the IDE, such as Eclipse, it would be super cool if you didn't have to run all your tests and main() applications outside of Eclipse.   Luckily, if you use Maven and in particular, the Maven integration into Eclipse (i.e. M2E) then it turns out that it's pretty simple to integrate the PrimeMover Maven plugin into the plugin management of your pom.xml.  To do so, simply add the following in the pom of your project:

      <plugin>
        <groupId>org.eclipse.m2e</groupId>
        <artifactId>lifecycle-mapping</artifactId>
        <version>1.0.0</version>
        <configuration>
          <lifecycleMappingMetadata>
            <pluginExecutions>
              <pluginExecution>
                <pluginExecutionFilter>
                  <groupId>com.hellblazer.primeMover</groupId>
                  <artifactId>maven-plugin</artifactId>
                  <versionRange>[0.1.0-SNAPSHOT,)</versionRange>
                  <goals>
                    <goal>transform</goal>
                    <goal>transform-test</goal>
                  </goals>
                </pluginExecutionFilter>
                <action>
                  <execute>
                    <runOnIncremental>true</runOnIncremental>
                  </execute>
                </action>
              </pluginExecution>
            </pluginExecutions>
          </lifecycleMappingMetadata>
        </configuration>
      </plugin>

Note that it is important to have the PrimeMover plugin run incrementally.  Further, it needs to be hooked into both the transform and transform-test phases of the maven lifecycle.

Once this is done, the PrimeMover byte code transformation will run incrementally as you edit your code.  When you run your unit tests (and you have them, right ;) ) they will run as transformed PrimeMover simulations.

For an example of this, please see the "demo" module's pom.xml in this project.
