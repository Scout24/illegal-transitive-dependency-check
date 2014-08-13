package de.is24.maven.enforcer.rules.testtypes;


import java.util.Arrays;
import java.util.List;

public class ClassInDirectDependency {
  public enum EnumInClassInDirectDependency {
    ONE,
    TWO,
    THREE
  }

  public EnumInClassInDirectDependency getOne() {
      final List<String> list = Arrays.asList("1", "2", "3");
      list.forEach(element -> Integer.parseInt(element));
      long countOnes = list.stream().filter(element -> "1".equals(element)).count();

      return EnumInClassInDirectDependency.ONE;
  }
}
