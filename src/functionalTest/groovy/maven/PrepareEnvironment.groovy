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
    def "prepare functional tests environment. Lazybones #lazybones template(#version)"() {
        setup:
        createProject(lazybones, version)

        where:
        [lazybones, version] << getValidVersionMatrix().reverse()
    }
}
