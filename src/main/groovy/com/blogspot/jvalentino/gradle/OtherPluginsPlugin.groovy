package com.blogspot.jvalentino.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <p>A basic gradle plugin.</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println', 'DuplicateStringLiteral'])
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
