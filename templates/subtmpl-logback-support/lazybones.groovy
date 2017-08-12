@GrabResolver(name = 'utils', root = 'file:.lazybones/repo')
@Grab(group = '@groupId@', module = '@artifactId@', version = '@version@', transitive = false)

import com.github.dkorotych.lazybones.templates.maven.Dependency
import com.github.dkorotych.lazybones.templates.maven.PomProcessor
import groovy.xml.MarkupBuilder

def script = new GroovyScriptEngine('.lazybones').with {
    loadScriptByName('utils.groovy')
}
this.metaClass.mixin script

class Slf4JDependency extends Dependency {
    Slf4JDependency() {
        this.groupId = 'org.slf4j'
        this.version = '${slf4j.version}'
        asRuntime()
    }
}

// Create dependencies list
slf4jApi = new Slf4JDependency(artifactId: 'slf4j-api', scope: 'compile')
dependencies = [
        slf4jApi,
        new Slf4JDependency(artifactId: 'jul-to-slf4j'),
        new Slf4JDependency(artifactId: 'jcl-over-slf4j'),
        new Slf4JDependency(artifactId: 'log4j-over-slf4j'),
        new Dependency(groupId: 'ch.qos.logback', artifactId: 'logback-classic', version: '1.2.3',
                exclusions: [slf4jApi]).asRuntime()
]

pomProcessor = new PomProcessor(dependencies, this)
if (pomProcessor.processing) {
    MarkupBuilder builder = pomProcessor.createMarkupBuilder()
    builder.project {
        delegate.'properties' {
            'slf4j.version'("1.7.25")
        }
    }
    pomProcessor.process()

    // Create new dependencies as part of pom.xml
    builder = pomProcessor.createMarkupBuilder()
    pomProcessor.addDependencies(builder)
    pomProcessor.process()

    pomProcessor.save()
    log.info "Add logback and slf4j as project dependencies"

    // Copy configuration files
    copyDirectoryFromTemplate('src')
    log.info "Generate simple configuration files"
}
