package com.blogspot.jvalentino.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class OtherPluginsPluginTestSpec extends Specification {
    Project project
    OtherPluginsPlugin plugin

    def setup() {
        project = ProjectBuilder.builder().build()
        plugin = new OtherPluginsPlugin()
    }

    void "test plugin"() {
        when:
        plugin.apply(project)

        then:
        project.tasks.getAt(0).toString() == "task ':a'"
        project.tasks.getAt(1).toString() == "task ':b'"
        project.tasks.getAt(2).toString() == "task ':helloWorld'"
        project.tasks.getAt(3).toString() == "task ':runAfter'"
        project.tasks.getAt(4).toString() == "task ':runBefore'"
    }
}
