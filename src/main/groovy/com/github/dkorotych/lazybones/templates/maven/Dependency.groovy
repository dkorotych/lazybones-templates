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
    List<Dependency> exclusions
}
