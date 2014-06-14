package de.is24.maven.enforcer.rules;

import de.is24.maven.enforcer.rules.testtypes.ClassInAnotherTransitiveDependency;
import de.is24.maven.enforcer.rules.testtypes.ClassInDirectDependency;
import de.is24.maven.enforcer.rules.testtypes.ClassInMavenProjectSource;
import de.is24.maven.enforcer.rules.testtypes.ClassInTransitiveDependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.plugin.testing.stubs.StubArtifactResolver;
import org.apache.maven.plugins.enforcer.EnforcerTestUtils;
import org.apache.maven.plugins.enforcer.MockProject;
import org.apache.maven.plugins.enforcer.utils.TestEnforcerRuleUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class IllegalTransitiveDependencyCheckTest {
  private static final Logger LOG = LoggerFactory.getLogger(IllegalTransitiveDependencyCheckTest.class);
  private static final String ARTIFACT_ID = "some-artifact";
  private static final String DEPENDENCY_ARTIFACT_ID = "dependency-artifact";
  private static final String TRANSITIVE_DEPENDENCY_ARTIFACT_ID = "transitive-dependency-artifact";
  private static final String GROUP_ID = "some-group";
  private static final String ARTIFACT_VERSION = "1.0";
  private static final String CLASS_SUFFIX = ".class";

  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();

  private ArtifactStubFactory factory;

  private Set<ClassFileInJar> makeClassFilesInJarSet(Class<?>... classes) {
    final Set<ClassFileInJar> fileEntries = new HashSet<>();
    for (Class<?> clazz : classes) {
      final ClassLoader classLoader = clazz.getClassLoader();
      final String resource = clazz.getName().replace('.', '/') + CLASS_SUFFIX;

      // validate that the class file is accessible..
      final URL url = classLoader.getResource(resource);
      if (url == null) {
        final String error = "Test class file '" + resource + "' not readable!";
        LOG.error(error);
        throw new IllegalStateException(error);
      }

      final File classFile = new File(url.getFile());
      fileEntries.add(new ClassFileInJar(resource, classFile));
    }
    return fileEntries;
  }

  @Before
  public void prepareArtifactStubFactory() throws IOException {
    factory = new ArtifactStubFactory();
    factory.setWorkingDir(folder.newFolder("repository"));
    factory.setCreateFiles(true);
  }

  @Test
  public void ruleFiresExceptionOnTransitiveDependency() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies();

    final EnforcerRule rule = new IllegalTransitiveDependencyCheck();

    TestEnforcerRuleUtils.execute(rule, helper, true);
  }

  @Test
  public void ruleLogsOnlyTransitiveDependency() throws IOException {
    final EnforcerRuleHelper helper = prepareProjectWithIllegalTransitiveDependencies();

    final IllegalTransitiveDependencyCheck rule = new IllegalTransitiveDependencyCheck();
    rule.setReportOnly(true);
    rule.setRegexIgnoredClasses(new String[] { "" });

    TestEnforcerRuleUtils.execute(rule, helper, false);
  }

  private EnforcerRuleHelper prepareProjectWithIllegalTransitiveDependencies() throws IOException {
    final MockProject project = new MockProject();

    final EnforcerRuleHelperWrapper helper = new EnforcerRuleHelperWrapper(EnforcerTestUtils.getHelper(project));
    helper.addComponent(new StubArtifactResolver(factory, false, false), ArtifactResolver.class);


    final Artifact artifact = factory.createArtifact(GROUP_ID, ARTIFACT_ID, ARTIFACT_VERSION);
    project.setArtifact(artifact);
    project.setArtifactId(ARTIFACT_ID);
    project.setGroupId(GROUP_ID);
    project.setVersion(ARTIFACT_VERSION);

    makeArtifactJarFromClassFile(artifact, ClassInMavenProjectSource.class);


    final Artifact dependency = factory.createArtifact(GROUP_ID, DEPENDENCY_ARTIFACT_ID, ARTIFACT_VERSION);

    // add the direct dependency and it's children
    makeArtifactJarFromClassFile(dependency, ClassInDirectDependency.class,
      ClassInDirectDependency.EnumInClassInDirectDependency.class);


    final Artifact transitiveDependency = factory.createArtifact(GROUP_ID, TRANSITIVE_DEPENDENCY_ARTIFACT_ID,
      ARTIFACT_VERSION);

    // add the transitive dependency and the enclosed annotation
    makeArtifactJarFromClassFile(transitiveDependency, ClassInTransitiveDependency.class,
      ClassInTransitiveDependency.SomeUsefulAnnotation.class);

    final Artifact anotherTransitiveDependency = factory.createArtifact(GROUP_ID,
      TRANSITIVE_DEPENDENCY_ARTIFACT_ID + "2",
      ARTIFACT_VERSION);

    makeArtifactJarFromClassFile(anotherTransitiveDependency, ClassInAnotherTransitiveDependency.class,
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

  private void makeArtifactJarFromClassFile(Artifact artifact, Class<?>... classes) {
    artifact.setFile(replaceJarWithPacketClassFile(artifact.getFile(), makeClassFilesInJarSet(classes)));
  }

  private File replaceJarWithPacketClassFile(File jar, Set<ClassFileInJar> classFilesInJar) {
    final String fileName = jar.getAbsolutePath();
    jar.delete();

    final File newJar = new File(fileName);

    try {
      try(ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(jar))) {
        for (ClassFileInJar classFileInJar : classFilesInJar) {
          try(InputStream in = new FileInputStream(classFileInJar.getClassFile())) {
            final byte[] buffer = new byte[1024];

            zipOutputStream.putNextEntry(new ZipEntry(classFileInJar.getResource()));

            int bytesRead = in.read(buffer);
            do {
              zipOutputStream.write(buffer, 0, bytesRead);
              bytesRead = in.read(buffer);
            } while (bytesRead > 0);
            zipOutputStream.closeEntry();
          }
        }
      }
    } catch (IOException e) {
      final String error = "Unable to pack class files '" + classFilesInJar + "'!";
      LOG.error(error, e);
      throw new IllegalStateException(error, e);
    }
    return newJar;
  }

  private static final class ClassFileInJar {
    private final String resource;
    private final File classFile;

    private ClassFileInJar(String resource, File classFile) {
      this.resource = resource;
      this.classFile = classFile;
    }

    String getResource() {
      return resource;
    }

    File getClassFile() {
      return classFile;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("ClassFileInJar{");
      sb.append("resource='").append(resource).append('\'');
      sb.append(", classFile=").append(classFile);
      sb.append('}');
      return sb.toString();
    }
  }

  private static final class EnforcerRuleHelperWrapper implements EnforcerRuleHelper {
    private final EnforcerRuleHelper wrappedEnforcerRuleHelper;
    private final Map<String, Object> components = new HashMap<>();

    private EnforcerRuleHelperWrapper(EnforcerRuleHelper wrappedEnforcerRuleHelper) {
      this.wrappedEnforcerRuleHelper = wrappedEnforcerRuleHelper;
    }

    void addComponent(Object component, String key) {
      components.put(key, component);
    }

    void addComponent(Object component, Class<?> key) {
      components.put(key.getName(), component);
    }

    @Override
    public Log getLog() {
      return wrappedEnforcerRuleHelper.getLog();
    }

    @Override
    public Object getComponent(Class clazz) throws ComponentLookupException {
      return getComponent(clazz.getName());
    }

    @Override
    public Object getComponent(String componentKey) throws ComponentLookupException {
      if (components.containsKey(componentKey)) {
        return components.get(componentKey);
      }
      return wrappedEnforcerRuleHelper.getComponent(componentKey);
    }

    @Override
    public Object getComponent(String role, String roleHint) throws ComponentLookupException {
      return wrappedEnforcerRuleHelper.getComponent(role, roleHint);
    }

    @Override
    public Map getComponentMap(String role) throws ComponentLookupException {
      return wrappedEnforcerRuleHelper.getComponentMap(role);
    }

    @Override
    public List getComponentList(String role) throws ComponentLookupException {
      return wrappedEnforcerRuleHelper.getComponentList(role);
    }

    @Override
    public PlexusContainer getContainer() {
      return wrappedEnforcerRuleHelper.getContainer();
    }

    @Override
    public Object evaluate(String expression) throws ExpressionEvaluationException {
      return wrappedEnforcerRuleHelper.evaluate(expression);
    }

    @Override
    public File alignToBaseDirectory(File file) {
      return wrappedEnforcerRuleHelper.alignToBaseDirectory(file);
    }
  }
}
