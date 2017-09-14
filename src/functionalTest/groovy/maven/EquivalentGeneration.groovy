package maven

import org.junit.Assert
import spock.lang.Timeout
import spock.lang.Unroll

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class EquivalentGeneration extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "create project by template(1.3.2) should be equals create project by template(1.4). Lazybones(#lazybones)"() {
        setup:
        File directoryFor132 = createProjectByTemplate(lazybones, '1.3.2')
        File directoryFor14 = createProjectByTemplate(lazybones, '1.4')

        expect:
        verifyDirectories(directoryFor132, directoryFor14)

        where:
        lazybones << SUPPORTED_LAZYBONES_VERSIONS
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "logback support by template(1.0.1) should be equals logback support by template(1.0.2). Lazybones(#lazybones)"() {
        setup:
        File directoryFor132 = createProjectByTemplate(lazybones, '1.3.2', true)
        File directoryFor14 = createProjectByTemplate(lazybones, '1.4', true)

        expect:
        verifyDirectories(directoryFor132, directoryFor14)

        where:
        lazybones << SUPPORTED_LAZYBONES_VERSIONS
    }

    private File createProjectByTemplate(String lazybones, String version, boolean withLogback = false) {
        File directory = File.createTempDir()
        List<String> commands = createCommands(version, null)
        startProcess getLazybonesBuilder(false, javaVersion, lazybones, commands, directory)
        if (withLogback) {
            executeGeneration(lazybones, LOGBACK_SUPPORT, javaVersion, directory)
        }
        return directory
    }

    private static void verifyDirectories(File expected, File generated) throws IOException {
        Files.walkFileTree(expected.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
                if (!(path.toFile().name == '.lazybones')) {
                    FileVisitResult result = super.preVisitDirectory(path, attributes)
                    Path relativize = expected.toPath().relativize(path)
                    File directory = generated.toPath().resolve(relativize).toFile()
                    Assert.assertEquals("Path doesn't contain same file",
                            Arrays.toString(path.toFile().list().sort()),
                            Arrays.toString(directory.list().sort()))
                    return result
                } else {
                    return FileVisitResult.SKIP_SUBTREE
                }
            }

            @Override
            FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                FileVisitResult result = super.visitFile(path, attributes)
                Path relativize = expected.toPath().relativize(path)
                File file = generated.toPath().resolve(relativize).toFile()
                String expectedContents = path.toFile().text
                String generatedContents = file.text
                Assert.assertEquals(expectedContents, generatedContents)
                return result
            }
        })
    }
}
