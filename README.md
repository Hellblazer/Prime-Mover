# An Event Driven Simulation Framework for Java

See (way out of date) [user-manual](./user-manual.pdf) in this directory for details.

## Build Status

![Build Status](https://github.com/hellblazer/prime-mover/actions/workflows/maven.yml/badge.svg)

## License

Licensed under AGPL V 3.0

## Building

Requires Java 23+

To build:

     cd <top level directory>
     ./mvnw clean install

Please see the GitHub Action file [maven.yml](.github/workflows/maven.yml) for an example of how to set up and run your
maven build successfully.

## Status

Prime Mover is now Soot Free. The framework now uses the JVM Virtual Threads from Project Loom to provide the blocking
thread continuation mechanism required for blocking events. The Prime Mover event transform is now implemented by an ASM
based transform.

## Maven Artifacts

Currently, Prime Mover is in active development and does not publish to maven central. Rather, periodic snapshots (and
releases when they happen)
will be uploaded to the [repo-hell]() repository. If you would like to use Prime Mover maven artifacts, you'll need to
add the following repository
declarations to your pom.xml The maven coordinates for individual artifacts are found below.

    <repositories>
        <repository>
            <id>hell-repo</id>
            <url>https://raw.githubusercontent.com/Hellblazer/repo-hell/main/mvn-artifact</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>plugin-hell-repo</id>
            <url>https://raw.githubusercontent.com/Hellblazer/repo-hell/main/mvn-artifact</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>

### Prime Mover Runtime

The artifact required for simulation runtime

     <dependency>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>runtime</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>

### Prime Mover Transform

The artifact required for simulation transform. This module contains the ASM based event transformer framework. The
transformer uses ClassGraph to
provide the scanning mechanism to obtain the classes to transform.S

     <dependency>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>transform</artifactId>
         <version>0.1.0-SNAPSHOT</version>
     </dependency>

### Prime Mover Maven Plugin

The plugin required to transform simulation code. This plugin runs after compilation for either main or test classes and
will perform the simulation
transform on classes in the target output directory, overwriting these class files with the transformed class.

     <plugin>
         <groupId>com.hellblazer.primeMover</groupId>
         <artifactId>primemover-maven-plugin</artifactId>
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

Note that it is important to have the PrimeMover plugin run incrementally if you are compiling incrementally in the IDD.
Further, it needs to be hooked into both the transform and transform-test phases of the maven lifecycle.

Once this is done, the PrimeMover byte code transformation will hopefully run incrementally as you edit your code. When
you run your unit tests (and you have them, right ;) ) they will run as transformed PrimeMover simulations.

For an example of this, please see the "demo" module's pom.xml in this project.

