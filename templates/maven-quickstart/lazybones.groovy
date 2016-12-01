@Grab(group = "com.squareup.okhttp3", module = "okhttp", version = "3.4.1")
@Grab(group = "uk.co.cacoethes", module = "groovy-handlebars-engine", version = "0.2")
@Grab(group = "ch.qos.logback", module = "logback-classic", version = "1.1.7")

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import uk.co.cacoethes.handlebars.HandlebarsTemplateEngine
import uk.co.cacoethes.util.NameType

import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat

def askPredefined(String message, String defaultValue, List<String> answers, String property) {
    message = "${message}. Choices are \'${answers.join('\', \'')}\' [${defaultValue}]: "
    answers = answers.each {
        it.toLowerCase()
    }
    answer = ''
    while (!answers.contains(answer)) {
        answer = ask(message, defaultValue, property).toLowerCase()
    }
    return answer
}

// Disable debug messages from HandlebarsTemplateEngine
context = (LoggerContext) LoggerFactory.getILoggerFactory();
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
""".getBytes(StandardCharsets.UTF_8)).withStream {configStream ->
        context.reset();
        configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(configStream);
    }
StatusPrinter.printInCaseOfErrorsOrWarnings(context);

// Replace standard template engine to Handlebars
registerDefaultEngine new HandlebarsTemplateEngine()

String username = System.properties['user.name']

Map properties = [:]
properties.groupId = "com.github.${username}"
properties.groupId = ask("Define value for 'groupId' [${properties.groupId}]: ", "${properties.groupId}", "groupId")

properties.artifactId = transformText(projectDir.name, from: NameType.HYPHENATED, to: NameType.PROPERTY)
properties.artifactId = ask("Define value for 'artifactId' [${properties.artifactId}]: ", "${properties.artifactId}", "artifactId")

properties.version = "0.1-SNAPSHOT"
properties.version = ask("Define value for 'version' [${properties.version}]: ", "${properties.version}", "version")

properties.packaging = ask("Define value for 'packaging' [jar]: ", "jar", "packaging")

properties.projectName = transformText(properties.artifactId, from: NameType.PROPERTY, to: NameType.NATURAL)
properties.projectCapitalizedName = properties.projectName.capitalize()

properties.packageName = "${properties.groupId}.${properties.artifactId}"
properties.packageName = ask("Define value for 'packageName' [${properties.packageName}]: ", "${properties.packageName}", "packageName")

properties.source = ask("Define value for 'source version' [1.8]: ", "1.8", "source")

properties.inceptionYear = new SimpleDateFormat("YYYY").format(new Date())

defaultValue = "${properties.groupId.toLowerCase().contains('github') ? '[Y/n]' : 'N/y'}"
properties.github = ask("Project will be placed on GitHub? ${defaultValue}: ", "${defaultValue}", "github")
if (properties.github == '[Y/n]' || properties.github == 'Y' || properties.github == 'y') {
    properties.github = true
    def repo = "https://github.com/${username}/${properties.artifactId}"
    properties.url = repo
    properties.issueManagement = [
        "url"   : "${repo}/issues",
        "system": "GitHub"
    ]
    properties.scm = [
        "url"                : "${repo}.git",
        "developerConnection": "scm:git:git@github.com:${username}/${properties.artifactId}.git"
    ]
} else {
    properties.github = false
}

properties.checkstyleConfig = askPredefined("Define value for checkstyle configuration", 'custom', ['custom', 'sun', 'google'], "checkstyleConfig")
switch (properties.checkstyleConfig) {
    case 'custom':
        properties.checkstyle = [
            'configLocation'      : '${project.basedir}/config/checkstyle/checkstyle.xml',
            'suppressionsLocation': '${project.basedir}/config/checkstyle/checkstyle-suppressions.xml'
        ]
        break
    case 'sun':
        properties.checkstyle = [
            'configLocation': 'sun_checks.xml'
        ]
        break
    case 'google':
        properties.checkstyle = [
            'configLocation': 'google_checks.xml'
        ]
        break
}
if (properties.checkstyleConfig != 'custom') {
    new File(projectDir, 'config/checkstyle').deleteDir()
}

// Create sources directories
["main", "test"].each { parent ->
    ["java", "resources"].each { dir ->
        new File(projectDir, "src/${parent}/${dir}").mkdirs()
    }
}
javaSourcesPath = 'src/main/java'
testSourcesPath = 'src/test/java'

// Remove some files if source version less then Java 8
source = (properties.source as String).replace("1.", "") as Integer
if (source < 8) {
    ["CharSequenceUtils", "CollectionUtils"].each { name ->
        new File(projectDir, "${javaSourcesPath}/utils/${name}.java").delete()
        new File(projectDir, "${testSourcesPath}/utils/${name}Test.java").delete()
    }
    properties.useCheckstyleBackport = true
}

// Replace template files
['pom.xml', "${javaSourcesPath}/**/*.java", "${testSourcesPath}/**/*.java"].each {
    processTemplates it, properties
}

packagePath = properties.packageName.replace('.' as char, '/' as char)

// Move exists sources and tests to correct package
sources = new File(projectDir, javaSourcesPath)
tests = new File(projectDir, testSourcesPath)
[sources, tests].each {
    def path = it.toPath()
    Files.walkFileTree(path, new FileVisitor<Path>() {
        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            relative = "${path.relativize(file.parent)}"
            directory = new File("${path}/${packagePath}/${relative}")
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

// For projects with git support generate .gitignore
if (scmExclusionsFile) {
    request = new Request.Builder()
        .url("https://www.gitignore.io/api/intellij+iml,maven,gradle,netbeans,eclipse,git,java,jetbrains,sonar,sublimetext,linux,windows,macos")
        .build();
    response = new OkHttpClient().newCall(request).execute();
    if (response.isSuccessful()) {
        scmExclusions(response.body().string().trim())
    } else {
        log.severe(response)
    }
}
