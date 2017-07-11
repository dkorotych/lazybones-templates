package maven

import groovy.transform.Memoized
import groovy.util.slurpersupport.GPathResult

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
abstract class MavenQuickstartTests extends AbstractLazybonesTests {
    protected static final List<String> SUPPORTED_LAZYBONES_VERSIONS = ['0.8.1', '0.8.2', '0.8.3']
    protected static final int CORRECT_LAZYBONES_VERSION_INDEX = SUPPORTED_LAZYBONES_VERSIONS.indexOf('0.8.3')
    protected static final List<String> TEMPLATE_VERSIONS = ['1.0', '1.1', '1.2', '1.2.1', '1.3', '1.3.1', '1.4']
    protected static final int CORRECT_TEMPLATE_VERSION_INDEX = TEMPLATE_VERSIONS.indexOf('1.2.1')

    protected static final ERROR_MESSAGE = 'Post install script caused an exception, project might be corrupt: ' +
            'No signature of method: java.io.ByteArrayInputStream.withCloseable()'

    protected static String javaVersion

    def setupSpec() {
        javaVersion = System.getProperty('java.version').replaceFirst(/^1\.(\d+).+$/, '$1')
    }

    protected ProcessBuilder getMavenQuickstartBuilder(String lazybonesVersion, String templateVersion) {
        getMavenQuickstartBuilder(lazybonesVersion, templateVersion, null)
    }

    protected ProcessBuilder getMavenQuickstartBuilder(String lazybonesVersion, String templateVersion,
                                                       Map<String, String> properties,
                                                       String javaSource = javaVersion) {
        List<String> commands = createCommands(templateVersion, properties)
        getLazybonesBuilder(false, javaSource, lazybonesVersion, commands)
    }

    protected List<String> createCommands(String templateVersion, Map<String, String> properties) {
        List<String> commands = []
        if (localTemplateExists(templateVersion)) {
            commands = ['create', 'maven-quickstart', getLocalTemplateString(templateVersion), '.']
        } else {
            commands = ['create', getRemoteTemplateString(templateVersion), '.']
        }
        if (!properties?.isEmpty()) {
            properties.each {
                commands << "-P${it.key}=${it.value}".toString()
            }
        }
        return commands
    }

    protected void createProject(String lazybonesVersion, String templateVersion, Map<String, String> options) {
        getMavenQuickstartBuilder(lazybonesVersion, templateVersion, options)
                .start()
                .waitForProcessOutput(System.out, System.err)
    }

    protected void createProject(String lazybonesVersion, String templateVersion) {
        createProject(lazybonesVersion, templateVersion, null)
    }

    protected GPathResult getPom() {
        new XmlSlurper(false, false).parse(new File(projectDir, 'pom.xml'))
    }

    protected GPathResult getMavenCheckstylePlugin(GPathResult pom) {
        return pom.build.pluginManagement.plugins.'*'.find { plugin ->
            plugin.artifactId == 'maven-checkstyle-plugin'
        }
    }

    @Memoized
    protected static getValidVersionMatrix() {
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

    @MaxMemoized
    private boolean localTemplateExists(String templateVersion) {
        return new File("${System.getProperty("user.home")}/.lazybones/templates/maven-quickstart-"
                + "${templateVersion}.zip").
                exists()
    }

    @MaxMemoized
    private String getLocalTemplateString(String templateVersion) {
        return "${templateVersion}".toString()
    }

    @MaxMemoized
    private String getRemoteTemplateString(String templateVersion) {
        return "https://dl.bintray.com/dkorotych/lazybones-templates/maven-quickstart-template-${templateVersion}.zip".
                toString()
    }
}
