package maven

import spock.lang.IgnoreRest
import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class TemplateNotWork extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @IgnoreRest
    def "template with version '#version' not work in Lazybones(#lazybones)"() {
        setup:
        File err = File.createTempFile(String.valueOf(lazybones), String.valueOf(version))

        when:
        getMavenQuickstartBuilder(lazybones, version).
                redirectError(err).
                start().
                waitFor()


        then:
        err.text.contains(ERROR_MESSAGE)

        where:
        [lazybones, version] << {
            def parameters = []
            def lazybones = SUPPORTED_LAZYBONES_VERSIONS.subList(0, CORRECT_LAZYBONES_VERSION_INDEX)
            def templates = TEMPLATE_VERSIONS.subList(0, CORRECT_TEMPLATE_VERSION_INDEX)
            lazybones.each { version ->
                templates.each { template ->
                    parameters << [version, template]
                }
            }
            parameters
        }.call()
    }
}
