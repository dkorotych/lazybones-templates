package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class ValidateVertXSupport extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
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
        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['service': false, 'codegen': true])
        def pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-codegen')

        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['service': false, 'codegen': false])
        pom = getPom()

        then:
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-codegen')

        where:
        [lazybones, version] << getTestData()
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate vertx subtemplate. Service Proxy. Lazybones(#lazybones), template(#version)"() {
        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': true, 'service': true])
        def pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-codegen')
        assertDependencyByArtifactId(pom.dependencies, 'vertx-service-proxy')

        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': true, 'service': false])
        pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-codegen')
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-service-proxy')

        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': false, 'service': true])
        pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-codegen')
        assertDependencyByArtifactId(pom.dependencies, 'vertx-service-proxy')

        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['codegen': false, 'service': false])
        pom = getPom()

        then:
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-codegen')
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-service-proxy')

        where:
        [lazybones, version] << getTestData()
    }

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "validate vertx subtemplate. Web Client. Lazybones(#lazybones), template(#version)"() {
        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['web': true])
        def pom = getPom()

        then:
        assertDependencyByArtifactId(pom.dependencies, 'vertx-web-client')
        assertDependencyByArtifactId(pom.dependencies, 'netty-tcnative-boringssl-static')

        when:
        createProject(lazybones, version)
        executeGeneration(lazybones, VERTX_SUPPORT, ['web': false])
        pom = getPom()

        then:
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'vertx-web-client')
        assertDependencyNotFoundByArtifactId(pom.dependencies, 'netty-tcnative-boringssl-static')

        where:
        [lazybones, version] << getTestData()
    }

    private List getTestData() {
        return getValidVersionMatrixEqualOrGreaterThen('1.4')
    }
}
