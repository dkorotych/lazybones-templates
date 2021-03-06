package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class MavenWrapper extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate maven wrapper. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        def directory = new File(projectDir, '.mvn/wrapper')

        expect:
        new File(projectDir, 'mvnw').exists()
        new File(projectDir, 'mvnw.cmd').exists()
        assert directory.isDirectory()
        assert directory.exists()
        assert directory.list({ File dir, String name ->
            return name ==~ /(?i)^maven-wrapper\.(jar|properties)$/
        }).size() == 2
        assertDependencyByArtifactId(getPom().build.plugins, 'maven-enforcer-plugin')

        where:
        [lazybones, version] << getValidVersionMatrixEqualOrGreaterThen('1.4.1')
    }
}
