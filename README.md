## 4.10 Extending Other Plugins

​      Just like a Java (or Groovy) class can extend an existing class, a similar concept can be accomplished using a Gradle plugin. It is possible for a Gradle plugin to extend the functionality of one or more existing plugins, by applying them as a part of the plugin project. The purpose of this plugin is to demonstrate how one can extend the functionality of the “Hello World” plugin, and additionally show how custom tasks can be made to before, after and between the tasks of the plugin which is being extended.

 

### The Base Plugin

This plugin refers to the plugin that will be extended. The purpose of this project is to demonstrate how the plugin works, which is to be extended..

#### plugin-tests/hello/build.gradle

```groovy
buildscript {
  repositories {
	jcenter()
  }
  dependencies {
    classpath "org.gradle:gradle-hello-world-plugin:0.2"
  }
}

apply plugin: "org.gradle.hello-world"

```

This build applies a “Hello World” plugin which is available via most common central repositories, in this case JCenter.

#### Manually Testing the Base Plugin

```bash
plugin-tests/hello$ gradlew helloWorld

> Task :helloWorld 
Hello World!


BUILD SUCCESSFUL

```

The result of applying the “Hello World” plugin, is that a task can be called named “helloworld” that displays the expected “Hello World!” at the command-line.

 

### Working out the desired behavior

The desired task behavior is the following:

·   “a” then “b” or “b” then “a”

·   “runBefore”

·   “helloworld”

·   “runAfter”

 

Additionally, we always want “runAfter” to execute even if only “helloworld” is directly specified.

#### plugin-tests/hello-ext/build.gradle

```groovy
buildscript {
  repositories {
	jcenter()
  }
  dependencies {
    classpath "org.gradle:gradle-hello-world-plugin:0.2"
  }
}

apply plugin: "org.gradle.hello-world"

task a {
    doLast {
        println 'a'
    }
}

task b {
    doLast {
        println 'b'
    }
}

task runBefore(dependsOn:['a','b']) {
    doLast {
        println "Running before..."
    }
}
tasks.helloWorld.dependsOn 'runBefore'

task runAfter(dependsOn:'helloWorld') {
    doLast {
        println "Running after..."
    }
}
tasks.helloWorld.finalizedBy 'runAfter'

```

When dealing with integration style functionality that is unfamiliar, there are two strategies that can be used. The first is to use functional testing while the other is to use manual testing. This is demonstrating the manual strategy, where the functionality as worked out directly in the build.gradle, with the intention of later moving it into a plugin.

 

**Line 6: Classpath**

Since this build is extending the functionality of an existing plugin, that plugin has to be added as a classpath dependency.

 

**Line 10: The plugin being extended**

The plugin to be extended, must first be applied to the build so that it can be enhanced.

 

**Lines 12-22: Tasks A and B**

The tasks for “a” and “b” simply print their name to the command-line. No dependencies were set between either task, as they are intended to be run in any order.

 

**Line 24: runBefore depends**

When using “dependsOn”, this indicates that the given tasks in that list must run prior to the running of the declaring task. However, order it not guaranteed in this list. In this case, either task “a” could run first or task “b”.

 

**Line 29: Getting runBefore to run before helloworld**

The task of “helloworld” isn’t defined in this build.gradle, however it can be accessed using **tasks.helloworld**. This line makes the “helloworld” task not depend on “runBefore”.

 

**Line 31: Telling runAfter to depend on helloworld**

This line makes it so the “helloworld” task must run before the “runAfter” task.

 

**Line 36: Always run after**

Designated that the “helloworld” task is **finalizedBy** “runAfter”, means that the “runAfter” task will always run after the completion of the “helloworld” task.

Manually testing the desired behavior

```bash
plugin-tests/hello-ext$ gradlew helloWorld

> Task :a 
a

> Task :b 
b

> Task :runBefore 
Running before...

> Task :helloWorld 
Hello World!

> Task :runAfter 
Running after...


BUILD SUCCESSFUL

```

Executing the “helloWorld” task shows that the order of execution is:

·   A

·   B

·   runBefore

·   helloWord

·   runAfter

```bash
plugin-tests/hello-ext$ gradlew runAfter

> Task :a 
a

> Task :b 
b

> Task :runBefore 
Running before...

> Task :helloWorld 
Hello World!

> Task :runAfter 
Running after...


BUILD SUCCESSFUL

```

Executing the “runAfter” task directly shows the same task execution order, as expected.

 

### Turning the custom Gradle build into a plugin

This project takes the work done in the build.gradle for manually working out the desired functionality, and turns in into a Gradle plugin.

#### build.gradle

```groovy
dependencies {
    compile gradleApi()
    compile 'org.codehaus.groovy:groovy-all:2.4.12'
    compile "org.gradle:gradle-hello-world-plugin:0.2"
    
    testCompile 'org.spockframework:spock-core:1.1-groovy-2.4'
    testCompile 'cglib:cglib:3.2.6'
    testCompile 'org.objenesis:objenesis:2.6'
}

```

The plugin that must be extended, which was originally listed as a classpath dependency, now turns into a compile dependency for the project. When the library is constructed, its POM will now contain the library being extended as a compile-time dependency, to be picked up by the applying build.

 

#### src/main/groovy/com/blogspot/jvalenitno/gradle/OtherPluginsPlugin.groovy

```groovy
class OtherPluginsPlugin implements Plugin<Project> {
    void apply(Project project) {
        // https://plugins.gradle.org/plugin/org.gradle.hello-world
        project.apply plugin:'org.gradle.hello-world'

        project.task('a') { println 'a' }

        project.task('b') { println 'b' }

        project.task('runBefore', dependsOn:['a', 'b']) { 
            println 'Running before...' 
        }

        project.task('runAfter', dependsOn:'helloWorld') { 
            println 'Running after...' 
        }

        project.tasks.helloWorld.finalizedBy 'runAfter'
    }
}

```

The entirety of the build.gradle that was used for working out the functionality, can be pasted into the body of the apply method for the plugin class. The differences are that the task creation and task access must be done through the project variable.

#### plugin-tests/local/build.gradle

```groovy
buildscript {
  repositories {
	jcenter()
  }
  dependencies {
    classpath 'com.blogspot.jvalentino.gradle:other-plugins:1.0.0'
    
  }
}

apply plugin: 'other-plugins'


```

Plugin application or testing purposes is the same as within any other plugin, where we rely on settings.gradle to associate the test project with the plugin project. Decalre the library as a classpath depednency, and apply the plugin by name.

 

#### Manual Testing

```bash
plugin-tests/local$ gradlew helloWorld

> Task :a 
a

> Task :b 
b

> Task :runBefore 
Running before...

> Task :helloWorld 
Hello World!

> Task :runAfter 
Running after...


BUILD SUCCESSFUL

```

Executing the “helloWorld” task shows that the order of execution is, just as with the build.gradle we are modeling this after:

·   A

·   B

·   runBefore

·   helloWord

·   runAfter

 

```bash
plugin-tests/local$ gradlew runAfter

> Task :a 
a

> Task :b 
b

> Task :runBefore 
Running before...

> Task :helloWorld 
Hello World!

> Task :runAfter 
Running after...


BUILD SUCCESSFUL

```

The task “runAfter” also has the same task execution order.

