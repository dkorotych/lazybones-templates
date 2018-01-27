@Grab(group = "ch.qos.logback", module = "logback-classic", version = "1.1.7")
@Grab(group = "uk.co.cacoethes", module = "groovy-handlebars-engine", version = "0.2")

import org.apache.commons.io.FileUtils

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import uk.co.cacoethes.handlebars.HandlebarsTemplateEngine

import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class Utils {
    File fileInProject(name) {
        return new File(projectDir, name)
    }

    File fileFromTemplateDirectory(name) {
        return new File(templateDir, name)
    }

    void copyDirectoryFromTemplate(String directory) {
        FileUtils.copyDirectoryToDirectory(fileFromTemplateDirectory(directory), projectDir)
    }

    void copyFileFromTemplate(String fileName) {
        FileUtils.copyFileToDirectory(fileFromTemplateDirectory(fileName), projectDir)
    }

    String packageNameToPackagePath(String packageName) {
        return packageName.replace('.' as char, '/' as char)
    }

    def askBoolean(String message, String defaultValue, String property) {
        def positiveAnswer = ['yes', 'true', 'ok', 'y']
        String answer = ask("${message} Positive answer is one of \'${positiveAnswer.join('\', \'')}\' [${defaultValue}]: ", defaultValue, property).toLowerCase()
        return positiveAnswer.find {
            return it == answer
        } != null
    }

    def askPredefined(String message, String defaultValue, List<String> answers, String property,
                      boolean showAnswers = true) {
        if (showAnswers) {
            message = "${message} ${answers}: "
        }
        answers = answers.each {
            it.toLowerCase()
        }
        def answer = ''
        while (!answers.contains(answer)) {
            answer = ask(message, defaultValue, property).toLowerCase()
        }
        return answer
    }

    /**
     * Read indent from editor settings file
     */
    def readIndent() {
        def indent = 2
        fileInProject('.editorconfig').readLines(fileEncoding).each {
            it.find(~/indent_size\s+=\s+(\d+)/) {
                indent = it[1] as Integer
            }
        }
        return indent
    }

    /**
     * Read indent from editor settings file
     */
    def readIndentAsString() {
        return ' ' * readIndent()
    }

    /**
     * Move exists sources and tests to correct package
     * @param packageName The name of package
     * @param toFile Converter from path as String to File object
     * @param paths Path to sources
     */
    void moveTemplateSourcesToCorrectPackagePath(String packageName, Closure<File> toFile, String... paths) {
        def packagePath = packageNameToPackagePath(packageName)
        paths.each {
            def path = toFile.call(it).toPath()
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    return FileVisitResult.CONTINUE
                }

                @Override
                FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    def relative = "${path.relativize(file.parent)}"
                    def directory = new File("${path}/${packagePath}/${relative}")
                    directory.mkdirs()
                    file.toFile().renameTo(new File(directory, "${file.fileName}"))
                    def parent = file.parent.toFile()
                    if (parent.list().length == 0) {
                        parent.deleteDir()
                    }
                    return FileVisitResult.CONTINUE
                }

                @Override
                FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.TERMINATE
                }

                @Override
                FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE
                }
            })
        }
    }

    void registerHandlebarsAsDefaultTemplateEngine() {
        // Disable debug messages from HandlebarsTemplateEngine
        def context = (LoggerContext) LoggerFactory.getILoggerFactory();
        new ByteArrayInputStream(
            """
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
""".getBytes(StandardCharsets.UTF_8)).withStream { configStream ->
            context.reset();
            def configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(configStream);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        // Replace standard template engine to Handlebars
        registerDefaultEngine new HandlebarsTemplateEngine()
    }
}
