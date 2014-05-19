The 'Illegal' Transitive Dependency Check Rule
==============================================

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
            <version>1.1</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <id>enforce</id>
            <configuration>
              <rules>
                <illegalTransitiveDependencyCheck implementation="de.is24.maven.enforcer.rules.IllegalTransitiveDependencyCheck">
                  <reportOnly>false</reportOnly>
                  <regexIgnoredClasses>
                      <regexIgnoredClass>javax\..+</regexIgnoredClass>
                      <regexIgnoredClass>org\.hibernate\..+</regexIgnoredClass>
                  </regexIgnoredClasses>
                </illegalTransitiveDependencyCheck>
              </rules>
            </configuration>
            <goals>
              <goal>enforce</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

The rule itself can be configured to only report violations or to signal the enforcer-plugin to break the build by 
specifying the attribute `reportOnly`. You may also exclude classes or packages from analysis by providing 
regex-patterns to parameter `regexIgnoredClasses` (e.g. `my\.suppressed\.Type`).


