package com.github.dkorotych.lazybones.templates.maven

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class PomProcessor {
    final GPathResult pom
    boolean processing
    final String indent
    XmlSlurper slurper
    private File pomFile
    private Writer writer
    private List<Dependency> dependencies
    private Script script

    PomProcessor(List<Dependency> dependencies, Script script) {
        this.dependencies = dependencies
        this.script = script

        // Read pom.xml
        slurper = new XmlSlurper(false, false)
        slurper.setKeepIgnorableWhitespace(true)
        pomFile = script.fileInProject('pom.xml')
        pom = slurper.parse(pomFile)

        // Any dependency already exists in pom.xml?
        processing = pom.dependencies.'*'.find { dependency ->
            return dependencies.find {
                return "${it.groupId}" == "${dependency.groupId}" && "${it.artifactId}" == "${dependency.artifactId}"
            }
        }.isEmpty()

        if (!processing) {
            processing = script.askBoolean("Some of the required dependencies were already found in the file. Are you sure you want to continue?", "no")
            if (processing) {
                println('Attention! You will need to manually remove some duplicate dependencies')
            }
        }

        // Read indent from editor settings file
        indent = script.readIndentAsString()
    }

    MarkupBuilder createMarkupBuilder() {
        writer = new StringWriter()
        return new MarkupBuilder(new IndentPrinter(writer, indent))
    }

    MarkupBuilder addDependencies(MarkupBuilder builder) {
        builder.project {
            dependencyManagement {
                dependencies {
                    dependencies.each { Dependency item ->
                        dependency {
                            groupId(item.groupId)
                            artifactId(item.artifactId)
                            version(item.version)
                            if (item.scope != 'compile') {
                                scope(item.scope)
                            }
                            if (item.exclusions) {
                                exclusions {
                                    item.exclusions.each { Dependency exclusionItem ->
                                        exclusion {
                                            groupId(exclusionItem.groupId)
                                            artifactId(exclusionItem.artifactId)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            dependencies {
                dependencies.each { Dependency item ->
                    dependency {
                        groupId(item.groupId)
                        artifactId(item.artifactId)
                    }
                }
            }
        }
        return builder
    }

    void process() {
        if (processing) {
            // Insert new dependencies to pom.xml
            GPathResult changes = slurper.parseText(writer.toString())
            changes.properties.'*'.each {
                pom.properties << indent
                pom.properties << it
                pom.properties << "\n${indent}"
            }
            changes.dependencyManagement.dependencies.'*'.each {
                pom.dependencyManagement.dependencies << "\n${indent * 3}"
                pom.dependencyManagement.dependencies << it
                pom.dependencyManagement.dependencies << "\n${indent * 2}"
            }
            changes.dependencies.'*'.each {
                pom.dependencies << "\n${indent * 2}"
                pom.dependencies << it
                pom.dependencies << "\n${indent}"
            }
            if (!(changes.build.extensions.isEmpty())) {
                if (pom.build.extensions.isEmpty()) {
                    pom.build << "${indent}"
                    pom.build << changes.build.extensions
                    pom.build << "\n${indent}"
                } else {
                    changes.build.extensions.'*'.each {
                        pom.build.extensions << "${indent * 3}"
                        pom.build.extensions << it
                        pom.build.extensions << "\n${indent * 3}"
                    }
                }
            }
            changes.build.pluginManagement.plugins.'*'.each {
                pom.build.pluginManagement.plugins << "${indent}"
                pom.build.pluginManagement.plugins << it
                pom.build.pluginManagement.plugins << "\n${indent * 3}"
            }
            changes.build.plugins.'*'.each {
                pom.build.plugins << "${indent}"
                pom.build.plugins << it
                pom.build.plugins << "\n${indent * 2}"
            }
        }
    }

    void save() {
        if (processing) {
            XmlUtil.serialize(pom, pomFile.newWriter(script.fileEncoding))
        }
    }
}
