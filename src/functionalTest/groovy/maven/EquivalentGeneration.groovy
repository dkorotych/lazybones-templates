package maven

import org.junit.Assert
import spock.lang.Timeout

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

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "generation by template(1.3.1) should be equals generation by template(1.4)"() {
        setup:
        File directoryFor131 = createProjectByTemplate('1.3.1')
        File directoryFor14 = createProjectByTemplate('1.4')

        expect:
        verifyDirectories(directoryFor131, directoryFor14)
    }

    private File createProjectByTemplate(String version) {
        File directory = File.createTempDir()
        List<String> commands = createCommands(version, null)
        getLazybonesBuilder(false, javaVersion, SUPPORTED_LAZYBONES_VERSIONS.last(), commands, directory).
                start().
                waitFor()
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
