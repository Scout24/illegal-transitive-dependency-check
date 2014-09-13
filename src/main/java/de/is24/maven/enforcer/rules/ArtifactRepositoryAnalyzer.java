package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * ArtifactRepositoryAnalyzer
 *
 * @author aschubert
 */
final class ArtifactRepositoryAnalyzer {
  private static final String CLASS_SUFFIX = ".class";
  private static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.+\\.(jar|war|JAR|WAR)$");

  private final Log logger;
  private final boolean analyzeDependencies;
  private final String[] regexIgnoredClasses;

  private ArtifactRepositoryAnalyzer(Log logger, boolean analyzeDependencies, String... regexIgnoredClasses) {
    this.logger = logger;
    this.analyzeDependencies = analyzeDependencies;
    this.regexIgnoredClasses = regexIgnoredClasses;
  }

  static ArtifactRepositoryAnalyzer analyzeArtifacts(Log logger, boolean analyzeDependencies,
                                                     String... ignoreClasses) {
    return new ArtifactRepositoryAnalyzer(logger, analyzeDependencies, ignoreClasses);
  }

  Repository analyzeArtifacts(Iterable<Artifact> artifacts) {
    final Repository repository = new Repository(logger, regexIgnoredClasses);

    for (Artifact artifact : artifacts) {
      final File artifactFile = artifact.getFile();
      if (artifactFile == null) {
        logger.info("Artifact '" + artifact + "' has no associated file, skip it.");
        continue;
      }

      if (artifactFile.isDirectory()) {
        analyzeClassesDirectory(repository, artifactFile);
      } else {
        final String absolutePath = artifactFile.getAbsolutePath();
        if (JAR_FILE_PATTERN.matcher(absolutePath).matches()) {
          analyzeJar(repository, artifactFile);
        } else {
          logger.info("Artifact '" + artifact + "' associated file '" + absolutePath + "', is skipped.");
        }
      }
    }
    return repository;
  }

  private void analyzeJar(Repository repository, File jar) {
    final ClassVisitor classVisitor = new ClassDependencyResolvingVisitor(repository, logger);

    try(ZipFile zipFile = new ZipFile(jar.getAbsolutePath())) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final String fileName = entry.getName();
        if (fileName.endsWith(CLASS_SUFFIX)) {
          logger.debug("Analyze class '" + fileName + "' in JAR '" + jar + "'.");

          final ClassReader classReader = new ClassReader(zipFile.getInputStream(entry));

          if (analyzeDependencies) {
            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
          } else {
            repository.addType(classReader.getClassName().replace('/', '.'));
          }
        }
      }
    } catch (IOException e) {
      final String error = "Unable to read classes from artifact '" + jar + "'.";
      logger.error(error, e);
      throw new IllegalStateException(error, e);
    }
  }

  private void analyzeClassesDirectory(Repository repository, File classesDirectory) {
    final ClassVisitor classVisitor = new ClassDependencyResolvingVisitor(repository, logger);
    analyzeClassesDirectory(repository, classesDirectory, classVisitor);
  }

  private void analyzeClassesDirectory(Repository repository, File directory, ClassVisitor classVisitor) {
    if (directory.isDirectory()) {
      final String[] entries = directory.list();
      for (String entry : entries) {
        analyzeClassesDirectory(repository, new File(directory, entry), classVisitor);
      }
    }

    final String path = directory.getPath();
    if (path.endsWith(CLASS_SUFFIX)) {
      logger.debug("Analyze class '" + path + "'.");

      try(FileInputStream classFileStream = new FileInputStream(directory)) {
        final ClassReader classReader = new ClassReader(classFileStream);
        if (analyzeDependencies) {
          classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        } else {
          repository.addType(classReader.getClassName().replace('/', '.'));
        }
      } catch (IOException e) {
        final String error = "Unable to read class from file '" + directory + "'.";
        logger.error(error, e);
        throw new IllegalStateException(error, e);
      }
    }

  }

}
