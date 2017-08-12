package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateLogbackSupport extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate Logback subtemplate. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        executeGeneration(lazybones, LOGBACK_SUPPORT)
        def pom = getPom()

        expect:
        ['slf4j-api', 'jul-to-slf4j', 'jcl-over-slf4j', 'log4j-over-slf4j', 'logback-classic'].each { artifactId ->
            assertDependencyByArtifactId(pom.dependencies, artifactId)
        }
        new File(projectDir, 'src/main/resources/logback.xml').exists()
        new File(projectDir, 'src/test/resources/logback-test.xml').exists()

        where:
        [lazybones, version] << getValidVersionMatrixGreaterThen('1.1')
    }
}
