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
        getLazybonesBuilder(false, javaVersion, lazybones, ['generate', 'logback-support'], projectDir).
                start().
                waitFor()
        def pom = getPom()

        expect:
        ['slf4j-api', 'jul-to-slf4j', 'jcl-over-slf4j', 'log4j-over-slf4j', 'logback-classic'].each { artifactId ->
            assert pom.dependencies.'*'.find({
                it.artifactId == artifactId
            }).isEmpty() == false : "$artifactId not found"
        }
        new File(projectDir, 'src/main/resources/logback.xml').exists()
        new File(projectDir, 'src/test/resources/logback-test.xml').exists()

        where:
        [lazybones, version] << {
            def index = TEMPLATE_VERSIONS.indexOf('1.1')
            def parameters = []
            def data = getValidVersionMatrix()
            data.each { line ->
                if (TEMPLATE_VERSIONS.indexOf(line[1]) >= index) {
                    parameters << line
                }
            }
            parameters
        }.call()
    }
}
