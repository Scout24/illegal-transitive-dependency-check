package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;

final class ClassFileReference {
  private static final Logger LOG = LoggerFactory.getLogger(ClassFileReference.class);

  private static final String CLASS_SUFFIX = ".class";

  private final String resource;
  private final File classFile;

  private ClassFileReference(String resource, File classFile) {
    this.resource = resource;
    this.classFile = classFile;
  }

  private static File replaceJarWithPacketClassFile(File jar, Set<ClassFileReference> classFilesInJarReference) {
    final String fileName = jar.getAbsolutePath();
    jar.delete();

    final File newJar = new File(fileName);

    try {
      try(ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(jar))) {
        for (ClassFileReference classFileReference : classFilesInJarReference) {
          try(InputStream in = new FileInputStream(classFileReference.getClassFile())) {
            final byte[] buffer = new byte[1024];

            zipOutputStream.putNextEntry(new ZipEntry(classFileReference.getResource()));

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
      final String error = "Unable to pack class files '" + classFilesInJarReference + "'!";
      LOG.error(error, e);
      throw new IllegalStateException(error, e);
    }
    return newJar;
  }

  private static Set<ClassFileReference> makeClassFileSet(Class<?>... classes) {
    final Set<ClassFileReference> fileEntries = new HashSet<>();
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
      fileEntries.add(new ClassFileReference(resource, classFile));
    }
    return fileEntries;
  }

  static void prepareArtifactTargetClassesDirectory(MavenProject project, Class<?> clazz) {
    final ClassFileReference classFileReference = makeClassFileSet(clazz).iterator().next();
    final Build build = new Build();
    project.setBuild(build);
    build.setDirectory(classFileReference.getClassFile().getParentFile().getAbsolutePath());
    build.setOutputDirectory(classFileReference.getClassFile().getParentFile().getAbsolutePath());
  }

  static void makeArtifactJarFromClassFile(Artifact artifact, Class<?>... classes) {
    artifact.setFile(replaceJarWithPacketClassFile(artifact.getFile(), makeClassFileSet(classes)));
  }

  String getResource() {
    return resource;
  }

  File getClassFile() {
    return classFile;
  }

  @Override
  public String toString() {
    return format("ClassFileReference{resource='%s', classFile=%s}", resource, classFile);
  }
}
