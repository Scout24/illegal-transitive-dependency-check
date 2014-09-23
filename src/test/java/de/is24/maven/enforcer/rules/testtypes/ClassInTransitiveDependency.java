package de.is24.maven.enforcer.rules.testtypes;

import org.junit.Ignore;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Deprecated
public interface ClassInTransitiveDependency extends Serializable {
  @Ignore("DummyValue")
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
  @interface SomeUsefulAnnotation {
    String defaultParameter() default "default";

    String[] stringArrayParameter();

    int[] intArrayParameter();

    float floatParameter();
  }
}
