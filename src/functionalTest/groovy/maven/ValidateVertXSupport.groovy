package maven

import spock.lang.Ignore
import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateVertXSupport extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @Ignore
    def "validate vertx subtemplate. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT)
        def pom = getPom()

        expect:
        assert pom.dependencyManagement.dependencies.'*'.find {
            it.groupId == 'io.vertx' && it.artifactId == 'vertx-dependencies' && it.type == 'pom' && it.scope == 'import'
        }.isEmpty() == false
        ['vertx-core', 'vertx-unit', 'netty-transport-native-epoll', 'javassist', 'vertx-codegen', 'vertx-service-proxy',
         'vertx-web-client', 'netty-tcnative-boringssl-static'].each { artifactId ->
            assertDependencyByArtifactId(pom.dependencies, artifactId)
        }
        new File(projectDir, 'redeploy.bat').exists()
        new File(projectDir, 'redeploy.sh').exists()

        where:
        [lazybones, version] << getTestData()
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate vertx subtemplate. Codegeneration. Lazybones(#lazybones), template(#version)"() {
        setup:
        createProject(lazybones, version)

        when:
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': true])
        def pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-codegen')

        when:
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': false])
        pom = getPom()

        then:
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-codegen')

        where:
        [lazybones, version] << getTestData()
    }

    private List getTestData() {
        return getValidVersionMatrixGreaterThen('1.4')
    }
}
