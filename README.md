This project is end of live and not any longer maintained!
==========================================================


The 'Illegal' Transitive Dependency Check Rule
==============================================

[![Build Status](https://travis-ci.org/ImmobilienScout24/illegal-transitive-dependency-check.svg?branch=master)](https://travis-ci.org/ImmobilienScout24/illegal-transitive-dependency-check)
[![Coverage Status](https://img.shields.io/coveralls/ImmobilienScout24/illegal-transitive-dependency-check.svg?branch=master)](https://coveralls.io/r/ImmobilienScout24/illegal-transitive-dependency-check)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.is24.maven.enforcer.rules/illegal-transitive-dependency-check/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.is24.maven.enforcer.rules/illegal-transitive-dependency-check/)

The `IllegalTransitiveDependencyCheck` is an additional rule for the `maven-enforcer-plugin`. The rule checks if
all classes in a certain artifact references only classes that are provided by explicitly declared dependencies.
Thus the rule will list (or complain about) all classes that are only available through transitive dependencies.

You can run the check by configuring the maven-enforcer-plugin to make use of the additional rule:

```xml
<project>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>1.3.1</version>
        <dependencies>
          <dependency>
            <groupId>de.is24.maven.enforcer.rules</groupId>
            <artifactId>illegal-transitive-dependency-check</artifactId>
            <version>1.7.4</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <id>enforce</id>
            <phase>verify</phase>
            <goals>
              <goal>enforce</goal>
            </goals>
            <configuration>
              <rules>
                <illegalTransitiveDependencyCheck implementation="de.is24.maven.enforcer.rules.IllegalTransitiveDependencyCheck">
                  <reportOnly>false</reportOnly>
                  <useClassesFromLastBuild>true</useClassesFromLastBuild>
                  <suppressTypesFromJavaRuntime>true</suppressTypesFromJavaRuntime>
                  <regexIgnoredClasses>
                      <regexIgnoredClass>javax\..+</regexIgnoredClass>
                      <regexIgnoredClass>org\.hibernate\..+</regexIgnoredClass>
                  </regexIgnoredClasses>
                  <listMissingArtifacts>false</listMissingArtifacts>
                </illegalTransitiveDependencyCheck>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

The rule itself can be configured to only report violations or even to signal the enforcer-plugin to break the build by
specifying the attribute `reportOnly`. You may also exclude classes or packages from analysis by providing
regex-patterns to parameter `regexIgnoredClasses` (e.g. `my\.suppressed\.Type`).

In addition to these exclusions types from packages `javax.*`,`sun.*`, `jdk.*`, `org.*` and `com.sun.*` that are available through the current
Java runtime can be excluded automatically by setting parameter `suppressTypesFromJavaRuntime`.

By default the rule will resolve the currently analyzed artifact in the Maven repository. In case the enforcer-plugin
runs in a phase compiled classes are available in the target folder (e.g. `verify`) artifact-resolving can be avoided
by setting parameter `useClassesFromLastBuild` to `true`.

(Since version 1.7.4 the `regexIngoredClasses` filtering is also applied to the classes of the artifact currently
analyzed. Thus direct dependencies of that classes will not be considered. See request [#29](https://github.com/ImmobilienScout24/illegal-transitive-dependency-check/issues/29))

If not only the classes but also the transitively used artifacts should be listed the parameter `listMissingArtifacts`
 can be set to `true`. **Caution: This option is really slow!**

Releases are available [here](http://repo1.maven.org/maven2/de/is24/maven/enforcer/rules/illegal-transitive-dependency-check/) in Maven's central repository.

