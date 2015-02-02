package de.is24.maven.enforcer.rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;


final class Repository {

  private final Set<String> types = new HashSet<String>();
  private final Set<String> dependencies = new HashSet<String>();

  private final ClassFilter filter;

  Repository(ClassFilter filter) {
    this.filter = filter;
  }

  Set<String> getTypes() {
    return Collections.unmodifiableSet(types);
  }

  Set<String> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  void addType(String type) {
    filter.addFiltered(types, type);
  }

  void addDependency(String type) {
    filter.addFiltered(dependencies, type);
  }

  @Override
  public String toString() {
    return format("Repository{types=%s, dependencies=%s}", types, dependencies);
  }
}
