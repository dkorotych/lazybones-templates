@Grab(group = "com.squareup.okhttp3", module = "okhttp", version = "3.4.1")

import okhttp3.OkHttpClient
import okhttp3.Request
import uk.co.cacoethes.util.NameType

import java.text.SimpleDateFormat

def script = new GroovyScriptEngine(".lazybones").with {
    loadScriptByName('utils.groovy')
}
this.metaClass.mixin script

def askChoices(String message, String defaultValue, List<String> answers, String property) {
    return askPredefined("${message}. Choices are \'${answers.join('\', \'')}\' [${defaultValue}]: ",
        defaultValue, answers, property, false)
}

registerHandlebarsAsDefaultTemplateEngine()
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

defaultValue = "${properties.groupId.toLowerCase().contains('github') ? 'yes' : 'no'}"
properties.github = askBoolean('Project will be placed on GitHub?', defaultValue, 'github')
if (properties.github) {
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
}

properties.checkstyleConfig = askChoices("Define value for checkstyle configuration", 'custom', ['custom', 'sun', 'google'], "checkstyleConfig")
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
    fileInProject('config/checkstyle').deleteDir()
}

// Create sources directories
["main", "test"].each { parent ->
    ["java", "resources"].each { dir ->
        fileInProject("src/${parent}/${dir}").mkdirs()
    }
}
javaSourcesPath = 'src/main/java'
testSourcesPath = 'src/test/java'

// Remove some files if source version less then Java 8
source = (properties.source as String).replace("1.", "") as Integer
if (source < 8) {
    ["CharSequenceUtils", "CollectionUtils"].each { name ->
        fileInProject("${javaSourcesPath}/utils/${name}.java").delete()
        fileInProject("${testSourcesPath}/utils/${name}Test.java").delete()
    }
    def utilPackage = fileInProject("${javaSourcesPath}/utils")
    def list = utilPackage.list()
    def packageInfoFileName = 'package-info.java'
    if (list != null && (list.length == 0 || (list.length == 1 && list[0] == packageInfoFileName))) {
        utilPackage.deleteDir()
    }
    properties.useCheckstyleBackport = true
}
if (properties.useCheckstyleBackport) {
    properties.checkstyleArtifactId = 'checkstyle-backport-jre6'
    properties.checkstyleVersion = '8.7'
} else {
    properties.checkstyleArtifactId = 'checkstyle'
    properties.checkstyleVersion = '8.7'
}

// Replace template files
['pom.xml', "${javaSourcesPath}/**/*.java", "${testSourcesPath}/**/*.java"].each {
    processTemplates it, properties
}

// Move exists sources and tests to correct package
moveTemplateSourcesToCorrectPackagePath(properties.packageName, {
    fileInProject(it)
}, javaSourcesPath, testSourcesPath)

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
