package com.github.dkorotych.lazybones.templates.maven

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
@CompileStatic
@EqualsAndHashCode
@ToString
@TupleConstructor
class Dependency {
    String groupId
    String artifactId
    String version
    String scope
    String type
    String classifier
    boolean optional = false
    List<Dependency> exclusions

    Dependency asRuntime() {
        scope = 'runtime'
        this
    }

    Dependency asOptional() {
        optional = true
        this
    }

    Dependency asTest() {
        scope = 'test'
        this
    }
}
