package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class ArtifactRepositoryAnalyzerTest {
  @Test
  public void analyzeEmptyArtifact() {
    final LogStub logger = new LogStub();
    final ArtifactRepositoryAnalyzer analyzer = ArtifactRepositoryAnalyzer.analyzeArtifacts(
      logger,
      false,
      new ClassFilter(logger, false));

    final Artifact artifact = makeArtifact(null);

    final Repository repository = analyzer.analyzeArtifacts(
      Collections.singleton(artifact));

    assertThat(repository.getTypes().isEmpty(), is(true));
    assertThat(repository.getDependencies().isEmpty(), is(true));
    assertThat(logger.getInfoLog(),
      containsString("has no associated file, skip it."));
  }

  @Test
  public void analyzePomArtifact() {
    final LogStub logger = new LogStub();
    final ArtifactRepositoryAnalyzer analyzer = ArtifactRepositoryAnalyzer.analyzeArtifacts(
      logger,
      false,
      new ClassFilter(logger, false));

    final Artifact artifact = makeArtifact(new File("pom.xml"));

    final Repository repository = analyzer.analyzeArtifacts(
      Collections.singleton(artifact));

    assertThat(repository.getTypes().isEmpty(), is(true));
    assertThat(repository.getDependencies().isEmpty(), is(true));
    assertThat(logger.getInfoLog(), containsString("pom.xml', is skipped"));
  }

  @Test
  public void invalidJarArtifact() {
    final LogStub logger = new LogStub();
    final ArtifactRepositoryAnalyzer analyzer = ArtifactRepositoryAnalyzer.analyzeArtifacts(
      logger,
      false,
      new ClassFilter(logger, false));

    final Artifact artifact = makeArtifact(new File("invalid.jar"));

    final String expectedErrorMessage = "Unable to read class(es) from artifact 'invalid.jar'.";
    try {
      analyzer.analyzeArtifacts(Collections.singleton(artifact));
      fail("IllegalStateException expected!");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is(expectedErrorMessage));
    }

    assertThat(logger.getErrorLog(), containsString(expectedErrorMessage));
  }

  @Test
  public void readTypesFromClassDirectory() {
    final LogStub logger = new LogStub();
    final ArtifactRepositoryAnalyzer analyzer = ArtifactRepositoryAnalyzer.analyzeArtifacts(
      logger,
      false,
      new ClassFilter(logger, false));

    final File classFile = getCurrentClassFile();
    final File classesDirectory = new File(classFile.getParent());
    final Artifact artifact = makeArtifact(classesDirectory);

    final Repository repository = analyzer.analyzeArtifacts(
      Collections.singleton(artifact));

    assertThat(repository.getTypes().isEmpty(), is(false));
    assertThat(repository.getDependencies().isEmpty(), is(true));
    assertThat(logger.getDebugLog(), containsString(classFile.getPath()));
  }

  private File getCurrentClassFile() {
    final String resourcePath = "/" + ArtifactRepositoryAnalyzerTest.class.getName().replace(".", "/") + ".class";
    return new File(ArtifactRepositoryAnalyzerTest.class.getResource(resourcePath).getFile());
  }

  private Artifact makeArtifact(File file) {
    final Artifact artifact = new ArtifactStub();
    artifact.setArtifactId("artifactId");
    artifact.setGroupId("groupId");
    artifact.setScope("scope");
    artifact.setVersion("0.123456789");
    artifact.setFile(file);
    return artifact;
  }
}
