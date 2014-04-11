package de.is24.maven.enforcer.rules.testtypes;


public class ClassInDirectDependency {
  public enum EnumInClassInDirectDependency {
    ONE,
    TWO,
    THREE
  }

  public EnumInClassInDirectDependency getOne() {
    return EnumInClassInDirectDependency.ONE;
  }
}
