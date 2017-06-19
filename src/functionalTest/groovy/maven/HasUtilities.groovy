package maven

import spock.lang.Timeout
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class HasUtilities extends MavenQuickstartTests {

    @Unroll
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "has utilities. Lazybones(#lazybones), template(#version), source(#source), exists(#exists)"() {
        setup:
        createProject(lazybones, version, ['source': source])

        expect:
        ["CharSequenceUtils", "CollectionUtils"].each { name ->
            new File(projectDir, "src/main/java/com/github/lazybones/app/utils/${name}.java").exists() == exists
            new File(projectDir, "src/test/java/com/github/lazybones/app/utils/${name}Test.java").exists() == exists
        }

        where:
        [source, exists, lazybones, version] << {
            def parameters = []
            def valid = getValidVersionMatrix()
            def sources = ['1.6', '1.7', '1.8']
            def index = sources.indexOf('1.8')
            sources.eachWithIndex { source, sourceIndex ->
                valid.each {
                    def line = [source, (sourceIndex >= index) as boolean]
                    it.each {
                        line << it
                    }
                    parameters << line
                }
            }
            parameters
        }.call()
    }
}
