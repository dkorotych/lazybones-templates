package maven

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.MemoizedASTTransformation")
/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
@interface MaxMemoized {
    int protectedCacheSize() default 100;

    int maxCacheSize() default 100;
}
