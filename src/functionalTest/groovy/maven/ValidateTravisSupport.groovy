package maven

import org.assertj.core.api.Assertions
import spock.lang.Timeout
import spock.lang.Unroll

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateTravisSupport extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate travis. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        executeGeneration(lazybones, TRAVIS)

        expect:
        Assertions.assertThat(new File(projectDir, '.travis.yml')).
                exists().
                hasSameContentAs(expectedFileContent('8', true))

        where:
        [lazybones, version] << getValidVersionMatrixEqualOrGreaterThen('1.4.4')
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate travis. Java #source. Coverage: #coverage. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version, ['source': source])
        executeGeneration(lazybones, TRAVIS, ['codecov': coverage])

        expect:
        Assertions.assertThat(new File(projectDir, '.travis.yml')).
                exists().
                hasSameContentAs(expectedFileContent(source, coverage))

        where:
        [coverage, source, lazybones, version] << {
            def parameters = []
            getValidVersionMatrixEqualOrGreaterThen('1.4.4').each { version ->
                ['1.7', '7', '1.8', '8'].each { source ->
                    [true, false].each { coverage ->
                        def line = [coverage, source]
                        version.each {
                            line << it
                        }
                        parameters << line
                    }
                }
            }
            parameters
        }.call()
    }

    private File expectedFileContent(String source, boolean coverage) {
        if (source == '1.8') {
            source = '8'
        }
        if (source == '1.7') {
            source = '7'
        }
        Paths.get(this.class.getResource("/travis/travis-$source-${coverage ? 'coverage' : 'no-coverage'}.yml").toURI()).toFile()
    }
}
