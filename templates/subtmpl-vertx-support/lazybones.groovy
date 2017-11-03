@GrabResolver(name = 'utils', root = 'file:.lazybones/repo')
@Grab(group = '@groupId@', module = '@artifactId@', version = '@version@', transitive = false)

import com.github.dkorotych.lazybones.templates.maven.Dependency
import com.github.dkorotych.lazybones.templates.maven.PomProcessor
import com.github.dkorotych.lazybones.templates.maven.RuleSetProcessor
import groovy.xml.MarkupBuilder

def script = new GroovyScriptEngine('.lazybones').with {
    loadScriptByName('utils.groovy')
}
this.metaClass.mixin script

def versions = [
        'vertx'    : '3.5.0',
        'netty'    : '4.1.15.Final',
        'shade'    : '3.1.0',
        'exec'     : '1.6.0',
        'javassist': '3.12.1.GA',
        'os'       : '1.5.0.Final'
]
def parameters = binding.variables.parentParams
def packageName = parameters.packageName
def mainVerticleClass = "${packageName}.MainVerticle"
parameters['mainVerticleClass'] = mainVerticleClass

class VertXDependency extends Dependency {
    static final String VERTX_GROUP_ID = 'io.vertx'

    VertXDependency() {
        this.groupId = VERTX_GROUP_ID
    }
}

class NettyDependency extends Dependency {
    NettyDependency() {
        this.groupId = 'io.netty'
        asRuntime()
    }
}

dependencies = [
        new VertXDependency(artifactId: 'vertx-core'),
        new VertXDependency(artifactId: 'vertx-unit').asTest(),
        new NettyDependency(artifactId: 'netty-transport-native-epoll', version: '${netty.version}',
                classifier: '${os.detected.classifier}'),
        new Dependency(groupId: 'javassist', artifactId: 'javassist', version: versions['javassist']).asRuntime()
]

codegen = askBoolean('Need to add support for code generation?', 'yes', 'codegen')
if (codegen) {
    dependencies << new VertXDependency(artifactId: 'vertx-codegen').asOptional()
}

if (askBoolean('Need to add support for service proxy?', 'yes', 'service')) {
    dependencies << new VertXDependency(artifactId: 'vertx-service-proxy', classifier: 'processor')
    if (!codegen) {
        dependencies << new VertXDependency(artifactId: 'vertx-codegen').asOptional()
    }
}

if (askBoolean('Need to add support for web services?', 'yes', 'web')) {
    dependencies << new VertXDependency(artifactId: 'vertx-web-client')
    dependencies << new NettyDependency(artifactId: 'netty-tcnative-boringssl-static')
}

pomProcessor = new PomProcessor(dependencies, this)
if (pomProcessor.processing) {
    MarkupBuilder builder = pomProcessor.createMarkupBuilder()
    builder.project {
        delegate.'properties' {
            'main.verticle'(mainVerticleClass)
            'netty.version'(versions['netty'])
        }
        dependencyManagement {
            dependencies {
                dependency {
                    groupId(VertXDependency.VERTX_GROUP_ID)
                    artifactId('vertx-dependencies')
                    version(versions['vertx'])
                    type('pom')
                    scope('import')
                }
            }
        }
        dependencies {
            dependencies.each { Dependency item ->
                dependency {
                    groupId(item.groupId)
                    artifactId(item.artifactId)
                    if (item.version) {
                        version(item.version)
                    }
                    if (item.scope) {
                        scope(item.scope)
                    }
                    if (item.classifier) {
                        classifier(item.classifier)
                    }
                    if (item.optional) {
                        optional(item.optional)
                    }
                }
            }
        }
        build {
            extensions {
                extension {
                    groupId('kr.motd.maven')
                    artifactId('os-maven-plugin')
                    version(versions['os'])
                }
            }
            pluginManagement {
                plugins {
                    plugin {
                        groupId('org.apache.maven.plugins')
                        artifactId('maven-shade-plugin')
                        version(versions['shade'])
                    }
                    plugin {
                        groupId('org.codehaus.mojo')
                        artifactId('exec-maven-plugin')
                        version(versions['exec'])
                    }
                }
            }
            plugins {
                plugin {
                    groupId('org.apache.maven.plugins')
                    artifactId('maven-shade-plugin')
                    executions {
                        execution {
                            phase('package')
                            goals {
                                goal('shade')
                            }
                        }
                    }
                    configuration {
                        transformers {
                            transformer(implementation: "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer") {
                                manifestEntries {
                                    'Main-Class'('io.vertx.core.Launcher')
                                    'Main-Verticle'('${main.verticle}')
                                }
                            }
                            transformer(implementation: "org.apache.maven.plugins.shade.resource.AppendingTransformer") {
                                resource('META-INF/services/io.vertx.core.spi.VerticleFactory')
                            }
                        }
                        artifactSet {
                        }
                        outputFile('${project.build.directory}/${project.artifactId}-${project.version}-fat.jar')
                    }
                }
                plugin {
                    groupId('org.codehaus.mojo')
                    artifactId('exec-maven-plugin')
                    configuration {
                        mainClass('io.vertx.core.Launcher')
                        arguments {
                            argument('run')
                            argument('${main.verticle}')
                        }
                    }
                }
            }
        }
    }
    pomProcessor.process()
    pomProcessor.save()
    System.out.println "pom.xml was successfully modified"

    // Add verticle example and test example
    processTemplates "src/**/*.java", parameters
    moveTemplateSourcesToCorrectPackagePath(packageName, {
        fileFromTemplateDirectory(it)
    }, 'src/main/java', 'src/test/java')
    copyDirectoryFromTemplate('src')
    System.out.println "Added main verticle and test"

    // Add utility scripts
    ['bat', 'sh'].each {
        def name = "redeploy.$it"
        processTemplates "$name", parameters
        copyFileFromTemplate(name)
    }
    System.out.println "Added utility scripts"

    ruleSetProcessor = new RuleSetProcessor(this)
    builder = ruleSetProcessor.createMarkupBuilder()
    builder.ruleset {
        rules {
            rule(groupId: 'com.fasterxml.jackson.core', artifactId: 'jackson-core') {
                ignoreVersions {
                    ignoreVersion(type: 'regex', '.*')
                }
            }
            rule(groupId: 'com.fasterxml.jackson.core', artifactId: 'jackson-databind') {
                ignoreVersions {
                    ignoreVersion(type: 'regex', '.*')
                }
            }
            rule(groupId: 'io.netty') {
                ignoreVersions {
                    ignoreVersion(type: 'regex', '.*')
                }
            }
        }
    }
    ruleSetProcessor.process()
    ruleSetProcessor.save()
    System.out.println "${ruleSetProcessor.file.name} was successfully modified"
}
