@GrabResolver(name = 'utils', root = 'file:.lazybones/repo')
@Grab(group = '@groupId@', module = '@artifactId@', version = '@version@', transitive = false)

def script = new GroovyScriptEngine('.lazybones').with {
    loadScriptByName('utils.groovy')
}
this.metaClass.mixin script
def parameters = binding.variables.parentParams
registerHandlebarsAsDefaultTemplateEngine()

codecov = askBoolean('Do you want to add code coverage support by codecov.io?', 'yes', 'codecov')
parameters['codecov'] = codecov

def source = parameters['source']
parameters['java8'] = (source == '1.8' || source == '8')
parameters['java7'] = (source == '1.7' || source == '7')

name = '.travis.yml'
processTemplates name, parameters
copyFileFromTemplate(name)
System.out.println "Generate simple $name configuration file"
