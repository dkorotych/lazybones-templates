package maven

import groovy.util.slurpersupport.GPathResult
import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class MavenQuickstartTests extends AbstractLazybonesTests {
    private static final List<String> SUPPORTED_LAZYBONES_VERSIONS = ['0.8.1', '0.8.2', '0.8.3']
    private static final int CORRECT_LAZYBONES_VERSION_INDEX = SUPPORTED_LAZYBONES_VERSIONS.indexOf('0.8.3')
    private static final List<String> TEMPLATE_VERSIONS = ['1.0', '1.1', '1.2', '1.2.1', '1.3']
    private static final int CORRECT_TEMPLATE_VERSION_INDEX = TEMPLATE_VERSIONS.indexOf('1.2.1')

    private static final ERROR_MESSAGE = 'Post install script caused an exception, project might be corrupt: ' +
            'No signature of method: java.io.ByteArrayInputStream.withCloseable()'

    private String javaVersion

    def setup() {
        javaVersion = System.getProperty('java.version').replaceFirst(/^1\.(\d+).+$/, '$1')
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "template with version '#version' not work in Lazybones(#lazybones)"() {
        setup:
        StringWriter err = new StringWriter()

        when:
        getMavenQuickstartBuilder(lazybones, version).start().waitForProcessOutput(System.out, err)


        then:
        err.toString().contains(ERROR_MESSAGE)

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

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate default values. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        def pom = getPom()

        expect:
        pom.groupId == 'com.github.lazybones'
        pom.artifactId == 'app'
        pom.version == '0.1-SNAPSHOT'
        pom.packaging == 'jar'
        pom.name == 'App'
        pom.description == 'App'
        pom.inceptionYear == GregorianCalendar.getInstance().get(Calendar.YEAR) as String
        pom.url == 'https://github.com/lazybones/app'
        pom.scm.url == 'https://github.com/lazybones/app.git'
        pom.scm.developerConnection == 'scm:git:git@github.com:lazybones/app.git'
        pom.issueManagement.url == 'https://github.com/lazybones/app/issues'
        pom.issueManagement.system == 'GitHub'
        pom.properties.'maven.compiler.source' == '1.8'
        pom.properties.'maven.compiler.target' == '1.8'
        pom.properties.'project.build.sourceEncoding' == 'UTF-8'
        pom.properties.'project.reporting.outputEncoding' == 'UTF-8'
        def mavenCheckstylePlugin = getMavenCheckstylePlugin(pom)
        mavenCheckstylePlugin.configuration.configLocation == '${project.basedir}/config/checkstyle/checkstyle.xml'
        mavenCheckstylePlugin.configuration.suppressionsLocation ==
                '${project.basedir}/config/checkstyle/checkstyle-suppressions.xml'

        where:
        [lazybones, version] << getValidVersionMatrix()
    }

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

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "has utilities. Lazybones(#lazybones), template(#version), source(#source), exists(#exists)"() {
        setup:
        createProject(lazybones, version, ['source': source])

        expect:
        ["CharSequenceUtils", "CollectionUtils"].each { name ->
            new File(projectDir, "src/main/java/com/github/lazybones/app/utils/${name}.java").exists() == exists
            new File(projectDir, "src/test/java/com/github/lazybones/app/utils/${name}Test.java").exists() == exists
        }

        where:
        [source, exists, lazybones, version] << {
            def parameters = []
            def valid = getValidVersionMatrix()
            def sources = ['1.6', '1.7', '1.8']
            def index = sources.indexOf('1.8')
            sources.eachWithIndex { source, sourceIndex ->
                valid.each {
                    def line = [source, (sourceIndex >= index) as boolean]
                    it.each {
                        line << it
                    }
                    parameters << line
                }
            }
            parameters
        }.call()
    }

    private ProcessBuilder getMavenQuickstartBuilder(String lazybonesVersion, String templateVersion) {
        getMavenQuickstartBuilder(lazybonesVersion, templateVersion, null)
    }

    private ProcessBuilder getMavenQuickstartBuilder(String lazybonesVersion, String templateVersion,
                                                     Map<String, String> properties, String javaSource = javaVersion) {
        List<String> commands = []
        def localTemplate = new File("${System.getProperty("user.home")}/.lazybones/templates/maven-quickstart-"
                + "${templateVersion}.zip")
        if (localTemplate.exists()) {
            commands = ['create', 'maven-quickstart', "${templateVersion}".toString(), '.']
        } else {
            commands = ['create', "https://dl.bintray.com/dkorotych/lazybones-templates/maven-quickstart-template-"
                    + "${templateVersion}.zip".toString(), '.']
        }
        if (!properties?.isEmpty()) {
            properties.each {
                commands << "-P${it.key}=${it.value}".toString()
            }
        }
        getLazybonesBuilder(false, javaSource, lazybonesVersion, commands)
    }

    private void createProject(String lazybonesVersion, String templateVersion, Map<String, String> options) {
        getMavenQuickstartBuilder(lazybonesVersion, templateVersion, options)
                .start()
                .waitForProcessOutput(System.out, System.err)
    }

    private void createProject(String lazybonesVersion, String templateVersion) {
        createProject(lazybonesVersion, templateVersion, null)
    }

    private GPathResult getPom() {
        new XmlSlurper(false, false).parse(new File(projectDir, 'pom.xml'))
    }

    private GPathResult getMavenCheckstylePlugin(GPathResult pom) {
        return pom.build.pluginManagement.plugins.'*'.find { plugin ->
            plugin.artifactId == 'maven-checkstyle-plugin'
        }
    }

    private static getValidVersionMatrix() {
        def parameters = []
        SUPPORTED_LAZYBONES_VERSIONS.eachWithIndex { version, versionIndex ->
            TEMPLATE_VERSIONS.eachWithIndex { template, templateIndex ->
                if (versionIndex >= CORRECT_LAZYBONES_VERSION_INDEX) {
                    parameters << [version, template]
                } else {
                    if (templateIndex >= CORRECT_TEMPLATE_VERSION_INDEX) {
                        parameters << [version, template]
                    }
                }
            }
        }
        parameters
    }
}
