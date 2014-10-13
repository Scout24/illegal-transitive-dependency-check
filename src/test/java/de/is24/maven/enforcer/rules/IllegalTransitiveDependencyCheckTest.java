package de.is24.maven.enforcer.rules;

import de.is24.maven.enforcer.rules.testtypes.ClassInAnotherTransitiveDependency;
import de.is24.maven.enforcer.rules.testtypes.ClassInDirectDependency;
import de.is24.maven.enforcer.rules.testtypes.ClassInMavenProjectSource;
import de.is24.maven.enforcer.rules.testtypes.ClassInTransitiveDependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.plugin.testing.stubs.StubArtifactResolver;
import org.apache.maven.plugins.enforcer.EnforcerTestUtils;
import org.apache.maven.plugins.enforcer.MockProject;
import org.apache.maven.plugins.enforcer.utils.TestEnforcerRuleUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class IllegalTransitiveDependencyCheckTest {
  private static final String ARTIFACT_ID = "some-artifact";
  private static final String DEPENDENCY_ARTIFACT_ID = "dependency-artifact";
  private static final String TRANSITIVE_DEPENDENCY_ARTIFACT_ID = "transitive-dependency-artifact";
  private static final String GROUP_ID = "some-group";
  private static final String ARTIFACT_VERSION = "1.0";

  private enum ArtifactFileType {
    JAR,
    TARGET_CLASSES,
    NOTHING
  }

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  private ArtifactStubFactory factory;

  @Before
  public void prepareArtifactStubFactory() throws IOException {
    factory = new ArtifactStubFactory();
    factory.setWorkingDir(folder.newFolder("repository"));
    factory.setCreateFiles(true);
  }

  @Test
  public void ruleIsNotCacheable() {
    assertThat(new IllegalTransitiveDependencyCheck().isCacheable(), is(false));
    assertThat(new IllegalTransitiveDependencyCheck().getCacheId(), nullValue());
    assertThat(new IllegalTransitiveDependencyCheck().isResultValid(null), is(false));
  }

  @Test
  public void ruleFiresExceptionOnTransitiveDependency() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies(ArtifactFileType.JAR);

    final EnforcerRule rule = new IllegalTransitiveDependencyCheck();

    TestEnforcerRuleUtils.execute(rule, helper, true);
  }

  @Test
  public void ruleLogsOnlyTransitiveDependency() throws IOException {
    final EnforcerRuleHelperWrapper helper = prepareProjectWithIllegalTransitiveDependencies(ArtifactFileType.JAR);

    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();
    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });

    TestEnforcerRuleUtils.execute(rule, helper, false);

    assertNumberOfIllegalTransitiveDependencies(helper, 7);
    assertNonJdkDependenciesAreListed(helper);
    assertJdkDependenciesAreListed(helper);
  }

  @Test
  public void tryToUseExistingTargetClassesDirectory() throws IOException {
    final EnforcerRuleHelperWrapper helper = prepareProjectWithIllegalTransitiveDependencies(
      ArtifactFileType.TARGET_CLASSES);
    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();

    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });
    rule.setUseClassesFromLastBuild(true);

    TestEnforcerRuleUtils.execute(rule, helper, false);

    assertNumberOfIllegalTransitiveDependencies(helper, 5);
    assertJdkDependenciesAreListed(helper);
  }

  @Test
  public void tryToUseMissingTargetClassesDirectory() throws IOException {
    final EnforcerRuleHelperWrapper helper = prepareProjectWithIllegalTransitiveDependencies(ArtifactFileType.NOTHING);
    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();

    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });
    rule.setUseClassesFromLastBuild(true);

    TestEnforcerRuleUtils.execute(rule, helper, false);

    assertThat(helper.getLog().getDebugLog(),
      containsString("No target/classes directory found"));

    assertThat(helper.getLog().getInfoLog(),
      containsString("Nothing to analyze in 'some-group:some-artifact:jar:1.0'"));
  }

  @Test
  public void suppressTypesFromJavaRuntime() throws IOException {
    final EnforcerRuleHelperWrapper helper = prepareProjectWithIllegalTransitiveDependencies(ArtifactFileType.JAR);
    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();

    rule.setReportOnly(true);
    rule.setSuppressTypesFromJavaRuntime(true);
    rule.setRegexIgnoredClasses(new String[] { "" });

    TestEnforcerRuleUtils.execute(rule, helper, false);

    // we expect the illegal dependencies to be printed into error log!

    assertThat(helper.getLog().getWarnLog(),
      containsString("Project's output directory has not been set, skip writing!"));

    assertThat(helper.getLog().getDebugLog(),
      containsString("Suppress type 'com.sun.management.DiagnosticCommandMBean', it's in current Java runtime"));

    assertNumberOfIllegalTransitiveDependencies(helper, 3);
    assertNonJdkDependenciesAreListed(helper);
  }

  private void assertNumberOfIllegalTransitiveDependencies(EnforcerRuleHelperWrapper helper, int number) {
    assertThat(helper.getLog().getErrorLog(),
      containsString(
        "Found " + number + " illegal transitive type dependencies in artifact 'some-group:some-artifact:jar:1.0"));
  }

  private void assertNonJdkDependenciesAreListed(EnforcerRuleHelperWrapper helper) {
    assertThat(helper.getLog().getErrorLog(),
      containsString("de.is24.maven.enforcer.rules.testtypes.ClassInAnotherTransitiveDependency"));
    assertThat(helper.getLog().getErrorLog(),
      containsString("de.is24.maven.enforcer.rules.testtypes.ClassInTransitiveDependency"));
    assertThat(helper.getLog().getErrorLog(),
      containsString("de.is24.maven.enforcer.rules.testtypes.ClassInTransitiveDependency$SomeUsefulAnnotation"));
  }

  private void assertJdkDependenciesAreListed(EnforcerRuleHelperWrapper helper) {
    assertThat(helper.getLog().getErrorLog(),
      containsString("com.sun.management.DiagnosticCommandMBean"));
    assertThat(helper.getLog().getErrorLog(),
      containsString("javax.sql.DataSource"));
    assertThat(helper.getLog().getErrorLog(),
      containsString("jdk.Exported"));
    assertThat(helper.getLog().getErrorLog(),
      containsString("org.w3c.dom.Text"));
  }

  private EnforcerRuleHelperWrapper prepareProjectWithIllegalTransitiveDependencies(ArtifactFileType artifactFileType)
                                                                             throws IOException {
    final MockProject project = new MockProject() {
      private Build build;

      @Override
      public void setBuild(Build build) {
        this.build = build;
      }

      @Override
      public Build getBuild() {
        return build;
      }
    };

    final EnforcerRuleHelperWrapper helper = new EnforcerRuleHelperWrapper(EnforcerTestUtils.getHelper(project));
    helper.addComponent(new StubArtifactResolver(factory, false, false), ArtifactResolver.class);


    final Artifact artifact = factory.createArtifact(GROUP_ID, ARTIFACT_ID, ARTIFACT_VERSION);
    project.setArtifact(artifact);
    project.setArtifactId(ARTIFACT_ID);
    project.setGroupId(GROUP_ID);
    project.setVersion(ARTIFACT_VERSION);

    switch (artifactFileType) {
      case JAR: {
        ClassFileReference.makeArtifactJarFromClassFile(artifact, ClassInMavenProjectSource.class);
        break;
      }

      case TARGET_CLASSES: {
        ClassFileReference.prepareArtifactTargetClassesDirectory(project, ClassInMavenProjectSource.class);
        break;
      }

      case NOTHING: {
        artifact.setFile(null);
        break;
      }

      default: {
        throw new IllegalStateException("Unexpected type " + artifactFileType);
      }
    }

    final Artifact dependency = factory.createArtifact(GROUP_ID, DEPENDENCY_ARTIFACT_ID, ARTIFACT_VERSION);

    // add the direct dependency and it's children
    ClassFileReference.makeArtifactJarFromClassFile(dependency,
      ClassInDirectDependency.class,
      ClassInDirectDependency.EnumInClassInDirectDependency.class);


    final Artifact transitiveDependency = factory.createArtifact(GROUP_ID,
      TRANSITIVE_DEPENDENCY_ARTIFACT_ID,
      ARTIFACT_VERSION);

    // add the transitive dependency and the enclosed annotation
    ClassFileReference.makeArtifactJarFromClassFile(transitiveDependency,
      ClassInTransitiveDependency.class,
      ClassInTransitiveDependency.SomeUsefulAnnotation.class);

    final Artifact anotherTransitiveDependency = factory.createArtifact(GROUP_ID,
      TRANSITIVE_DEPENDENCY_ARTIFACT_ID + "2",
      ARTIFACT_VERSION);

    ClassFileReference.makeArtifactJarFromClassFile(anotherTransitiveDependency,
      ClassInAnotherTransitiveDependency.class,
      ClassInAnotherTransitiveDependency.EnumInClassInAnotherTransitiveDependency.class);

    // set projects direct dependencies
    project.setDependencyArtifacts(Collections.singleton(dependency));

    // set projects direct and transitive dependencies
    final Set<Artifact> dependencies = new HashSet<>();
    dependencies.add(dependency);
    dependencies.add(transitiveDependency);
    dependencies.add(anotherTransitiveDependency);
    project.setArtifacts(dependencies);
    return helper;
  }
}
