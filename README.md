An Event Driven Simulation Framework for Java
See [user-manual](./user-manual.pdf) in this directory for details.

Licensed under AGPL V 3.0

Requires Java 20 and Maven 3.83_

To build:

     cd <top level directory>
     mvn clean install

### Prime Mover Runtime

The artifact required for simulation runtime

     <dependency>
         <groupId>com.hellblazer</groupId>
         <artifactId>primeMover</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>

### Prime Mover Maven Plugin

The plugin required to transform simulation code

     <plugin>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>maven.plugin</artifactId>
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
              <artifactId>maven.plugin</artifactId>
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
