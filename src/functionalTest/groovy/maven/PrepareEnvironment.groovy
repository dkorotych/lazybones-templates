package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class PrepareEnvironment extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "prepare functional tests environment. template(#version)"() {
        setup:
        String lazybones = SUPPORTED_LAZYBONES_VERSIONS.last()
        createProject(lazybones, version)

        where:
        version << TEMPLATE_VERSIONS
    }
}
