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

    def setup() {
        projectDir = temporaryFolder.root
    }

    ProcessBuilder getLazybonesBuilder(boolean interactive, String javaSource, String lazybonesVersion,
                                       List<String> lazybonesCommands, File directory = projectDir) {
        setPermissions(directory)
        List<String> commands = ['docker', 'run', '--rm']
        if (interactive) {
            commands << '--interactive'
            commands << '--tty'
        }
        commands << '-v'
        commands << getProjectPathVolume(directory)
        commands << '-v'
        commands << getTemplatesVolume()
        commands << '-v'
        commands << getGroovyCacheVolume()
        commands << '--user'
        commands << 'root'
        commands << getImageName(lazybonesVersion, javaSource)
        commands.addAll(lazybonesCommands)
        new ProcessBuilder(commands).
                directory(directory)
    }

    @Memoized
    private String getProjectPathVolume(File directory) {
        return "${directory.canonicalPath}:/app".toString()
    }

    @Memoized
    private static String getTemplatesVolume() {
        return createVolume('.lazybones/templates')
    }

    @Memoized
    private static String getGroovyCacheVolume() {
        return createVolume('.groovy')
    }

    @MaxMemoized
    private static String getImageName(String lazybonesVersion, String javaSource) {
        return "dkorotych/lazybones:${lazybonesVersion}-jre${javaSource}".toString()
    }

    private static String createVolume(String path) {
        File directory = new File("${System.getProperty("user.home")}/$path")
        if (!directory.exists()) {
            directory.mkdirs()
            setPermissions(directory)
        }
        return "${directory.canonicalPath}:/root/$path".toString()
    }

    private static void setPermissions(File directory) {
        directory.setReadable(true, false)
        directory.setWritable(true, false)
        directory.setExecutable(true, false)
    }
}
