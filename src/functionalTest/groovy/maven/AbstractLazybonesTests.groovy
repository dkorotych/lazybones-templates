package maven

import groovy.transform.Memoized
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
abstract class AbstractLazybonesTests extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    File projectDir

    def setupSpec() {
        def userHome = System.getProperty("user.home")
        new File("$userHome/.lazybones/templates").mkdirs()
        new File("$userHome/.groovy").mkdirs()
    }

    def setup() {
        projectDir = temporaryFolder.root
    }

    def ProcessBuilder getLazybonesBuilder(boolean interactive, String javaSource, String lazybonesVersion,
                                           List<String> lazybonesCommands, File directory = projectDir) {
        List<String> commands = ['docker', 'run', '--rm']
        if (interactive) {
            commands << '--interactive'
            commands << '--tty'
        }
        commands << '--volume'
        commands << getProjectPathVolume(directory)
        commands << '--volume'
        commands << getTemplatesVolume()
        commands << '--volume'
        commands << getGroovyCacheVolume()
        commands << getImageName(lazybonesVersion, javaSource)
        commands.addAll(lazybonesCommands)
        new ProcessBuilder(commands).
                directory(directory)
    }

    @Memoized
    private String getProjectPathVolume(File directory) {
        return "${directory.absolutePath}:/home/lazybones/app".toString()
    }

    @Memoized
    private static String getTemplatesVolume() {
        return "${System.getProperty("user.home")}/.lazybones/templates:/home/lazybones/.lazybones/templates".toString()
    }

    @Memoized
    private static String getGroovyCacheVolume() {
        return "${System.getProperty("user.home")}/.groovy:/home/lazybones/.groovy".toString()
    }

    @MaxMemoized
    private static String getImageName(String lazybonesVersion, String javaSource) {
        return "lazybones:${lazybonesVersion}-jre${javaSource}".toString()
    }
}
