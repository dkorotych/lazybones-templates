package maven

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

    def ProcessBuilder getLazybonesBuilder(boolean interactive, String javaSource, String lazybonesVersion,
                                           List<String> lazybonesCommands) {
        List<String> commands = ['docker', 'run', '--rm']
        if (interactive) {
            commands << '--interactive'
            commands << '--tty'
        }
        commands << '--volume'
        commands << "${projectDir.absolutePath}:/home/lazybones/app".toString()
        commands << '--volume'
        commands << "${System.getProperty("user.home")}/.lazybones/templates:/home/lazybones/.lazybones/templates".toString()
        commands << "lazybones:${lazybonesVersion}-jre${javaSource}".toString()
        commands.addAll(lazybonesCommands)
        new ProcessBuilder(commands).
                directory(projectDir)
    }
}
