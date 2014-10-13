package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.junit.Test;
import java.io.File;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ArtifactRepositoryAnalyzerTest {
  @Test
  public void analyzeEmptyArtifact() {
    final LogStub logger = new LogStub();
    final ArtifactRepositoryAnalyzer analyzer = ArtifactRepositoryAnalyzer.analyzeArtifacts(
      logger,
      false,
      false);

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
      false);

    final Artifact artifact = makeArtifact(new File("pom.xml"));

    final Repository repository = analyzer.analyzeArtifacts(
      Collections.singleton(artifact));

    assertThat(repository.getTypes().isEmpty(), is(true));
    assertThat(repository.getDependencies().isEmpty(), is(true));
    assertThat(logger.getInfoLog(), containsString("pom.xml', is skipped"));
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
