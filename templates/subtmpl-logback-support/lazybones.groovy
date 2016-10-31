import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil
import org.apache.commons.io.FileUtils

def class Dependency {
    String groupId = 'org.slf4j'
    String artifactId
    String version = '${slf4j.version}'
    String scope = 'runtime'
    List<Dependency> exclusions
}

def askBoolean(String message, String defaultValue) {
    positiveAnswer = ['yes', 'true', 'ok', 'y']
    String answer = ask("${message} ${positiveAnswer}: ", defaultValue).toLowerCase()
    return positiveAnswer.find {
        return it == answer
    }
}

// Read pom.xml
slurper = new XmlSlurper(false, false)
slurper.setKeepIgnorableWhitespace(true)
pomFile = new File(projectDir, 'pom.xml')
pom = slurper.parse(pomFile)

// Create dependencies list
slf4jApi = new Dependency(artifactId: 'slf4j-api', scope: 'compile')
dependencies = [
        slf4jApi,
        new Dependency(artifactId: 'jul-to-slf4j'),
        new Dependency(artifactId: 'jcl-over-slf4j'),
        new Dependency(artifactId: 'log4j-over-slf4j'),
        new Dependency(groupId: 'ch.qos.logback', artifactId: 'logback-classic', version: '1.1.7', exclusions: [slf4jApi])
]

// Any logback dependency already exists in pom.xml?
processing = pom.dependencies.'*'.find { dependency ->
    return dependencies.find {
        return "${it.groupId}" == "${dependency.groupId}" && "${it.artifactId}" == "${dependency.artifactId}"
    }
}.isEmpty()

if (!processing) {
    processing = askBoolean("Some of the required dependencies were already found in the file. Are you sure you want to continue?", "no")
    if (processing) {
        println('Attention! You will need to manually remove some duplicate dependencies')
    }
}

if (processing) {
    // Read indent from editor settings file
    indent = '  '
    new File(projectDir, '.editorconfig').readLines(fileEncoding).each {
        it.find(~/indent_size\s+=\s+(\d+)/) {
            indent = ' ' * (it[1] as Integer)
        }
    }

    // Create new dependencies as part of pom.xml
    writer = new StringWriter(1950)
    new MarkupBuilder(new IndentPrinter(writer, indent)).project {
        delegate.'properties' {
            'slf4j.version'("1.7.21")
        }
        dependencyManagement {
            dependencies {
                dependencies.each { Dependency item ->
                    dependency {
                        groupId(item.groupId)
                        artifactId(item.artifactId)
                        version(item.version)
                        if (item.scope != 'compile') {
                            scope(item.scope)
                        }
                        if (item.exclusions) {
                            exclusions {
                                item.exclusions.each { Dependency exclusionItem ->
                                    exclusion {
                                        groupId(exclusionItem.groupId)
                                        artifactId(exclusionItem.artifactId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        dependencies {
            dependencies.each { Dependency item ->
                dependency {
                    groupId(item.groupId)
                    artifactId(item.artifactId)
                }
            }
        }
    }

    // Insert new dependencies to pom.xml
    changes = slurper.parseText(writer.toString())
    changes.properties.'*'.each {
        pom.properties << indent
        pom.properties << it
        pom.properties << "\n${indent}"
    }
    changes.dependencyManagement.dependencies.'*'.each {
        pom.dependencyManagement.dependencies << "\n${indent * 3}"
        pom.dependencyManagement.dependencies << it
        pom.dependencyManagement.dependencies << "\n${indent * 2}"
    }
    changes.dependencies.'*'.each {
        pom.dependencies << "\n${indent * 2}"
        pom.dependencies << it
        pom.dependencies << "\n${indent}"
    }
    XmlUtil.serialize(pom, pomFile.newWriter(fileEncoding))
    log.info "Add logback and slf4j as project dependencies"

    // Copy configuration files
    FileUtils.copyDirectoryToDirectory(new File(templateDir, "src"), projectDir)
    log.info "Generate simple configuration files"
}
