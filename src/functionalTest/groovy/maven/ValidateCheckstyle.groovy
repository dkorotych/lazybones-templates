package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateCheckstyle extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 15, unit = TimeUnit.MINUTES)
    def "validate checkstyle plugin. Lazybones(#lazybones), template(#version)"() {
        when:
        createProject(lazybones, version, ['checkstyleConfig': 'custom'])
        def pom = getPom()
        def mavenCheckstylePlugin = getMavenCheckstylePlugin(pom)

        then:
        mavenCheckstylePlugin.configuration.configLocation == '${project.basedir}/config/checkstyle/checkstyle.xml'
        mavenCheckstylePlugin.configuration.suppressionsLocation ==
                '${project.basedir}/config/checkstyle/checkstyle-suppressions.xml'
        new File(projectDir, 'config/checkstyle/checkstyle.xml').exists()
        new File(projectDir, 'config/checkstyle/checkstyle-suppressions.xml').exists()

        when:
        createProject(lazybones, version, ['checkstyleConfig': 'sun'])
        pom = getPom()
        mavenCheckstylePlugin = getMavenCheckstylePlugin(pom)

        then:
        mavenCheckstylePlugin.configuration.configLocation == 'sun_checks.xml'
        mavenCheckstylePlugin.configuration.suppressionsLocation.isEmpty()
        !(new File(projectDir, 'config/checkstyle').exists())

        when:
        createProject(lazybones, version, ['checkstyleConfig': 'google'])
        pom = getPom()
        mavenCheckstylePlugin = getMavenCheckstylePlugin(pom)

        then:
        mavenCheckstylePlugin.configuration.configLocation == 'google_checks.xml'
        mavenCheckstylePlugin.configuration.suppressionsLocation.isEmpty()
        !(new File(projectDir, 'config/checkstyle').exists())

        where:
        [lazybones, version] << {
            def parameters = []
            def templates = TEMPLATE_VERSIONS.subList(TEMPLATE_VERSIONS.indexOf('1.2'), TEMPLATE_VERSIONS.size())
            def index = templates.indexOf('1.2.1')
            SUPPORTED_LAZYBONES_VERSIONS.eachWithIndex { version, versionIndex ->
                templates.eachWithIndex { template, templateIndex ->
                    if (versionIndex >= CORRECT_LAZYBONES_VERSION_INDEX) {
                        parameters << [version, template]
                    } else {
                        if (templateIndex >= index) {
                            parameters << [version, template]
                        }
                    }
                }
            }
            parameters
        }.call()
    }
}
