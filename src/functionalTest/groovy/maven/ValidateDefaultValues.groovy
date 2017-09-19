package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateDefaultValues extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate default values. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        def pom = getPom()

        expect:
        pom.groupId == 'com.github.root'
        pom.artifactId == 'app'
        pom.version == '0.1-SNAPSHOT'
        pom.packaging == 'jar'
        pom.name == 'App'
        pom.description == 'App'
        pom.inceptionYear == GregorianCalendar.getInstance().get(Calendar.YEAR) as String
        pom.url == 'https://github.com/root/app'
        pom.scm.url == 'https://github.com/root/app.git'
        pom.scm.developerConnection == 'scm:git:git@github.com:root/app.git'
        pom.issueManagement.url == 'https://github.com/root/app/issues'
        pom.issueManagement.system == 'GitHub'
        pom.properties.'maven.compiler.source' == '1.8'
        pom.properties.'maven.compiler.target' == '1.8'
        pom.properties.'project.build.sourceEncoding' == 'UTF-8'
        pom.properties.'project.reporting.outputEncoding' == 'UTF-8'
        def mavenCheckstylePlugin = getMavenCheckstylePlugin(pom)
        mavenCheckstylePlugin.configuration.configLocation == '${project.basedir}/config/checkstyle/checkstyle.xml'
        mavenCheckstylePlugin.configuration.suppressionsLocation ==
                '${project.basedir}/config/checkstyle/checkstyle-suppressions.xml'

        where:
        [lazybones, version] << getValidVersionMatrix()
    }
}
