package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LogStub implements Log {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTest.class);

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public void debug(CharSequence content) {
    LOG.info(String.valueOf(content));
  }

  @Override
  public void debug(CharSequence content, Throwable error) {
    LOG.info(String.valueOf(content), error);
  }

  @Override
  public void debug(Throwable error) {
    LOG.info("", error);
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public void info(CharSequence content) {
    LOG.info(String.valueOf(content));
  }

  @Override
  public void info(CharSequence content, Throwable error) {
    LOG.info(String.valueOf(content), error);
  }

  @Override
  public void info(Throwable error) {
    LOG.info("", error);
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public void warn(CharSequence content) {
    LOG.info(String.valueOf(content));
  }

  @Override
  public void warn(CharSequence content, Throwable error) {
    LOG.info(String.valueOf(content), error);
  }

  @Override
  public void warn(Throwable error) {
    LOG.info("", error);
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public void error(CharSequence content) {
    LOG.info(String.valueOf(content));
  }

  @Override
  public void error(CharSequence content, Throwable error) {
    LOG.info(String.valueOf(content), error);
  }

  @Override
  public void error(Throwable error) {
    LOG.info("", error);
  }
}
