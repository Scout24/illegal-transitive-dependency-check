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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class IllegalTransitiveDependencyCheckTest {
  private static final Logger LOG = LoggerFactory.getLogger(IllegalTransitiveDependencyCheckTest.class);
  private static final String ARTIFACT_ID = "some-artifact";
  private static final String DEPENDENCY_ARTIFACT_ID = "dependency-artifact";
  private static final String TRANSITIVE_DEPENDENCY_ARTIFACT_ID = "transitive-dependency-artifact";
  private static final String GROUP_ID = "some-group";
  private static final String ARTIFACT_VERSION = "1.0";

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
  public void ruleFiresExceptionOnTransitiveDependency() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies(false);

    final EnforcerRule rule = new IllegalTransitiveDependencyCheck();

    TestEnforcerRuleUtils.execute(rule, helper, true);
  }

  @Test
  public void ruleLogsOnlyTransitiveDependency() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies(false);

    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();
    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });

    TestEnforcerRuleUtils.execute(rule, helper, false);
  }

  @Test
  public void tryToUseExistingTargetClassesDirectory() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies(true);
    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();

    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });
    rule.setUseClassesFromLastBuild(true);

    TestEnforcerRuleUtils.execute(rule, helper, false);
  }

  private EnforcerRuleHelper prepareProjectWithIllegalTransitiveDependencies(boolean createTargetClassDirectory)
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

    if (createTargetClassDirectory) {
      ClassFileReference.prepareArtifactTargetClassesDirectory(project, ClassInMavenProjectSource.class);
    } else {
      ClassFileReference.makeArtifactJarFromClassFile(artifact, ClassInMavenProjectSource.class);
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

    LOG.info("Artifact [{}].", artifact);

    LOG.info("Dependencies of [{}] are {}.", artifact, project.getDependencyArtifacts());
    LOG.info("Transitive dependencies of [{}] are {}.", artifact, project.getArtifacts());
    return helper;
  }
}
