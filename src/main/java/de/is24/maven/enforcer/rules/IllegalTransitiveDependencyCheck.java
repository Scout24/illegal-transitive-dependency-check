package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * IllegalTransitiveDependencyCheck
 *
 * @author aschubert
 */
public class IllegalTransitiveDependencyCheck implements EnforcerRule {
  private static final String NO_CACHE_ID_AVAILABLE = null;

  private ArtifactResolver resolver;

  private ArtifactRepository localRepository;

  private List<ArtifactRepository> remoteRepositories;

  private String outputDirectory;

  private MavenProject project;

  private Log logger;

  private boolean reportOnly;

  private String[] regexIgnoredClasses;

  @Override
  public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
    logger = helper.getLog();

    if (reportOnly) {
      logger.info("Flag 'reportOnly' is set. Exceptions from rule will only be reported!");
    }

    initializeArtifactResolver(helper);

    initializeProject(helper);

    final Artifact artifact = resolveArtifact();

    final Repository artifactClassesRepository = ArtifactRepositoryAnalyzer.analyzeArtifacts(logger,
      true, regexIgnoredClasses)
      .analyzeArtifacts(Collections.singleton(artifact));

    final Set<Artifact> dependencies = resolveDirectDependencies(artifact);

    final Repository dependenciesClassesRepository = ArtifactRepositoryAnalyzer.analyzeArtifacts(logger,
      false, regexIgnoredClasses)
      .analyzeArtifacts(dependencies);

    logger.debug("Artifact's type dependencies are: " + artifactClassesRepository.getDependencies());
    logger.debug("Classes defined in direct dependencies are: " + dependenciesClassesRepository.getTypes());

    final List<String> unresolvedTypes = new ArrayList<>(artifactClassesRepository.getDependencies());
    unresolvedTypes.removeAll(artifactClassesRepository.getTypes());
    unresolvedTypes.removeAll(dependenciesClassesRepository.getTypes());

    if (unresolvedTypes.isEmpty()) {
      logger.info("No illegal transitive dependencies found in '" + artifact.getId() + "'.");
    } else {
      final String message = buildOutput(artifact, unresolvedTypes);

      writeOutputFile(artifact, message);

      if (reportOnly) {
        logger.error(message);
      } else {
        throw new EnforcerRuleException(message);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Set<Artifact> resolveDirectDependencies(Artifact artifact) {
    final Set<Artifact> dependencies = new HashSet<Artifact>(project.getDependencyArtifacts());
    dependencies.remove(artifact);
    logger.debug("Direct dependencies are '" + dependencies + "'.");
    return dependencies;
  }

  @SuppressWarnings("unchecked")
  private void initializeProject(ExpressionEvaluator helper) throws EnforcerRuleException {
    try {
      project = (MavenProject) helper.evaluate("${project}");

      localRepository = (ArtifactRepository) helper.evaluate("${localRepository}");
      remoteRepositories = (List<ArtifactRepository>) helper.evaluate("${project.remoteArtifactRepositories}");

      outputDirectory = (String) helper.evaluate("${project.build.directory}");

    } catch (ExpressionEvaluationException e) {
      throw new EnforcerRuleException("Unable to locate Maven project and/or repositories!", e);
    }

    logger.debug("Analyze project '" + project + "'.");
  }

  private void initializeArtifactResolver(EnforcerRuleHelper helper) throws EnforcerRuleException {
    try {
      resolver = (ArtifactResolver) helper.getComponent(ArtifactResolver.class);
    } catch (ComponentLookupException e) {
      throw new EnforcerRuleException("Unable to lookup artifact resolver!", e);
    }
  }

  private Artifact resolveArtifact() throws EnforcerRuleException {
    final Artifact artifact = project.getArtifact();
    logger.info("Analyze dependencies of artifact '" + artifact.getId() + "'.");

    enforceArtifactResolution(artifact);
    return artifact;
  }

  private String buildOutput(Artifact artifact, List<String> unresolvedTypes) {
    Collections.sort(unresolvedTypes);

    final StringBuilder illegalDependencies = new StringBuilder();
    illegalDependencies.append("Found ")
    .append(unresolvedTypes.size())
    .append(" illegal transitive type dependencies in artifact '")
    .append(artifact.getId())
    .append("':\n");

    int k = 1;
    for (String illegalDependency : unresolvedTypes) {
      illegalDependencies.append(k).append(".) ").append(illegalDependency).append("\n");
      k++;
    }
    return illegalDependencies.toString();
  }

  private void writeOutputFile(Artifact artifact, String output) throws EnforcerRuleException {
    if (outputDirectory == null) {
      logger.warn("Project's output directory has not been set, skip writing!");
      return;
    }

    final String fileName = outputDirectory + (outputDirectory.endsWith("/") ? "" : "/") +
      "itd-" + artifact.getId().replace(':', '-') + ".txt";

    final File outputFile = new File(fileName);
    final File targetFolder = outputFile.getParentFile();
    if (!targetFolder.exists() && !targetFolder.mkdirs()) {
      final String error = "Unable to create directory '" + targetFolder + "'!";
      logger.error(error);
      throw new EnforcerRuleException(error);
    }

    try(FileWriter resultFileWriter = new FileWriter(outputFile)) {
      resultFileWriter.write(output);
    } catch (IOException e) {
      final String error = "Unable to write output file '" + fileName + "'!";
      logger.error(error, e);
      throw new EnforcerRuleException(error, e);
    }
  }

  private void enforceArtifactResolution(Artifact artifact) throws EnforcerRuleException {
    try {
      resolver.resolve(artifact, remoteRepositories, localRepository);
    } catch (AbstractArtifactResolutionException e) {
      final String error = "Unable to resolve artifact '" + artifact.getId() + "'!";
      logger.error(error, e);
      throw new EnforcerRuleException(error, e);
    }
  }

  @Override
  public boolean isCacheable() {
    return false;
  }

  @Override
  public boolean isResultValid(EnforcerRule enforcerRule) {
    return false;
  }

  @Override
  public String getCacheId() {
    return NO_CACHE_ID_AVAILABLE;
  }

  /**
   * This parameter determines if the rule will break the build or not...
   */
  public boolean isReportOnly() {
    return reportOnly;
  }

  public void setReportOnly(boolean reportOnly) {
    this.reportOnly = reportOnly;
  }

  /**
   * A reggular expression that matches classes to be excluded from analyzes (e.g. 'de\.foo\.Foo.*')
   */
  public String[] getRegexIgnoredClasses() {
    return regexIgnoredClasses;
  }

  public void setRegexIgnoredClasses(String[] regexIgnoredClasses) {
    this.regexIgnoredClasses = regexIgnoredClasses;
  }
}
