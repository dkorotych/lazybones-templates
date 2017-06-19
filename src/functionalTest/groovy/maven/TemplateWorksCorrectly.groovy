package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class TemplateWorksCorrectly extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "template with version '#version' works correctly in Lazybones(#lazybones)"() {
        setup:
        StringWriter err = new StringWriter()

        when:
        getMavenQuickstartBuilder(lazybones, version).start().waitForProcessOutput(System.out, err)

        then:
        !(err.toString().contains(ERROR_MESSAGE))

        where:
        [lazybones, version] << {
            def parameters = []
            def templates = TEMPLATE_VERSIONS.subList(CORRECT_TEMPLATE_VERSION_INDEX, TEMPLATE_VERSIONS.size())
            SUPPORTED_LAZYBONES_VERSIONS.each { version ->
                templates.each { template ->
                    parameters << [version, template]
                }
            }
            parameters
        }.call()
    }
}
