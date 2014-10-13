package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;


final class LogStub implements Log {
  private final StringBuilder debugLog = new StringBuilder();
  private final StringBuilder infoLog = new StringBuilder();
  private final StringBuilder warnLog = new StringBuilder();
  private final StringBuilder errorLog = new StringBuilder();

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public void debug(CharSequence content) {
    debugLog.append(content).append('\n');
  }

  @Override
  public void debug(CharSequence content, Throwable error) {
    debugLog.append(content).append(error).append('\n');
  }

  @Override
  public void debug(Throwable error) {
    debugLog.append(error).append('\n');
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public void info(CharSequence content) {
    infoLog.append(content).append('\n');
  }

  @Override
  public void info(CharSequence content, Throwable error) {
    infoLog.append(content).append(error).append('\n');
  }

  @Override
  public void info(Throwable error) {
    infoLog.append(error).append('\n');
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public void warn(CharSequence content) {
    warnLog.append(content).append('\n');
  }

  @Override
  public void warn(CharSequence content, Throwable error) {
    warnLog.append(content).append(error).append('\n');
  }

  @Override
  public void warn(Throwable error) {
    warnLog.append(error).append('\n');
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public void error(CharSequence content) {
    errorLog.append(content).append('\n');
  }

  @Override
  public void error(CharSequence content, Throwable error) {
    errorLog.append(content).append(error).append('\n');
  }

  @Override
  public void error(Throwable error) {
    errorLog.append(error).append('\n');
  }

  public String getDebugLog() {
    return debugLog.toString();
  }

  public String getInfoLog() {
    return infoLog.toString();
  }

  public String getWarnLog() {
    return warnLog.toString();
  }

  public String getErrorLog() {
    return errorLog.toString();
  }
}
