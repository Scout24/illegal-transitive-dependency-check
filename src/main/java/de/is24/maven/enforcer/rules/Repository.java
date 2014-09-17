package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import static java.lang.String.format;


final class Repository {
  // not allowed are primitives, numerical names (for anonymous classes) and all classes in package java
  private static final String JAVA_TYPES_REGEX = "[0-9\\$]+|[BSCFIJVDL]|(java\\.[\\w\\.\\$]*)";

  // path of current Java runtime environment
  private static final String JAVA_HOME_PATH = "file:" + System.getProperty("java.home");
  private static final Pattern JAVA_RUNTIME_PACKAGES = Pattern.compile(
    "^(javax|com\\.sun|org|sun|jdk)\\..+");

  private final Set<String> types = new HashSet<>();
  private final Set<String> dependencies = new HashSet<>();

  private final Pattern ignoredClassesPattern;
  private final boolean suppressTypesFromJavaRuntime;
  private final Log logger;

  Repository(Log logger, boolean suppressTypesFromJavaRuntime, String... regexIgnoredClasses) {
    this.logger = logger;
    this.suppressTypesFromJavaRuntime = suppressTypesFromJavaRuntime;

    if ((regexIgnoredClasses == null) || (regexIgnoredClasses.length == 0)) {
      ignoredClassesPattern = Pattern.compile(JAVA_TYPES_REGEX);
    } else {
      final StringBuilder regexBuilder = new StringBuilder(JAVA_TYPES_REGEX);
      for (String regex : regexIgnoredClasses) {
        regexBuilder.append("|(").append(regex).append(")");
      }

      final String regex = regexBuilder.toString();
      logger.debug("Use type suppression pattern '" + regex + "'.");
      ignoredClassesPattern = Pattern.compile(regex);
    }
  }

  Set<String> getTypes() {
    return Collections.unmodifiableSet(types);
  }

  Set<String> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  void addType(String type) {
    addFiltered(types, type);
  }

  void addDependency(String type) {
    addFiltered(dependencies, type);
  }

  private void addFiltered(Collection<String> set, String type) {
    if (ignoredClassesPattern.matcher(type).matches()) {
      logger.debug("Suppress type '" + type + "'.");
      return;
    }

    // check if javax class comes from current Java runtime..
    if (suppressTypesFromJavaRuntime && typeFromJavaRuntime(type)) {
      return;
    }

    set.add(type);
  }

  private boolean typeFromJavaRuntime(String type) {
    if (JAVA_RUNTIME_PACKAGES.matcher(type).matches()) {
      final String classResource = type.replace('.', '/') + ".class";
      final URL it = ClassLoader.getSystemClassLoader().getResource(classResource);
      if (it != null) {
        final String sourcePath = it.getFile();
        if (sourcePath.startsWith(JAVA_HOME_PATH)) {
          logger.debug("Suppress type '" + type + "', it's in current Java runtime '" + JAVA_HOME_PATH + "'.");
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return format("Repository{types=%s, dependencies=%s}", types, dependencies);
  }
}
