package de.is24.maven.enforcer.rules;

import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EnforcerRuleHelperWrapper implements EnforcerRuleHelper {
  private final EnforcerRuleHelper wrappedEnforcerRuleHelper;
  private final Map<String, Object> components = new HashMap<>();

  EnforcerRuleHelperWrapper(EnforcerRuleHelper wrappedEnforcerRuleHelper) {
    this.wrappedEnforcerRuleHelper = wrappedEnforcerRuleHelper;
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
