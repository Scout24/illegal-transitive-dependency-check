package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.Context;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


final class EnforcerRuleHelperWrapper implements EnforcerRuleHelper {
  private final EnforcerRuleHelper wrappedEnforcerRuleHelper;
  private final Map<String, Object> components = new HashMap<>();

  private final PlexusContainerWrapper plexusContainerWrapper;
  private final LogStub logStub = new LogStub();

  private Artifact projectArtifact;
  private Artifact directDependencyArtifact;
  private Set<Artifact> transitiveDependencyArtifacts;

  public Artifact getDirectDependencyArtifact() {
    return directDependencyArtifact;
  }

  public void setDirectDependencyArtifact(Artifact directDependencyArtifact) {
    this.directDependencyArtifact = directDependencyArtifact;
  }

  public Set<Artifact> getTransitiveDependencyArtifacts() {
    return transitiveDependencyArtifacts;
  }

  public void setTransitiveDependencyArtifacts(Set<Artifact> transitiveDependencyArtifacts) {
    this.transitiveDependencyArtifacts = transitiveDependencyArtifacts;
  }

  public Artifact getProjectArtifact() {
    return projectArtifact;
  }

  public void setProjectArtifact(Artifact projectArtifact) {
    this.projectArtifact = projectArtifact;
  }

  EnforcerRuleHelperWrapper(EnforcerRuleHelper wrappedEnforcerRuleHelper) {
    this.wrappedEnforcerRuleHelper = wrappedEnforcerRuleHelper;
    plexusContainerWrapper = new PlexusContainerWrapper(wrappedEnforcerRuleHelper.getContainer());
  }

  void addComponent(Object component, Class<?> key) {
    components.put(key.getName(), component);
  }

  @Override
  public LogStub getLog() {
    return logStub;
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
  public PlexusContainerWrapper getContainer() {
    return plexusContainerWrapper;
  }

  @Override
  public Object evaluate(String expression) throws ExpressionEvaluationException {
    return wrappedEnforcerRuleHelper.evaluate(expression);
  }

  @Override
  public File alignToBaseDirectory(File file) {
    return wrappedEnforcerRuleHelper.alignToBaseDirectory(file);
  }

  public static final class PlexusContainerWrapper implements PlexusContainer {
    private final PlexusContainer plexusContainer;

    private final Map<String, Object> objects = new HashMap<String, Object>();

    private PlexusContainerWrapper(PlexusContainer plexusContainer) {
      this.plexusContainer = plexusContainer;
    }

    @Override
    public Object lookup(String role) throws ComponentLookupException {
      return plexusContainer.lookup(role);
    }

    @Override
    public Object lookup(String role, String roleHint) throws ComponentLookupException {
      return plexusContainer.lookup(role, roleHint);
    }

    @Override
    public <T> T lookup(Class<T> type) throws ComponentLookupException {
      return plexusContainer.lookup(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> type, String roleHint) throws ComponentLookupException {
      final T object = (T) objects.get(type.getCanonicalName() + "#" + roleHint);
      return (object != null) ? object : plexusContainer.lookup(type, roleHint);
    }

    @Override
    public <T> T lookup(Class<T> type, String role, String roleHint) throws ComponentLookupException {
      return plexusContainer.lookup(type, role, roleHint);
    }

    @Override
    public <T> T lookup(ComponentDescriptor<T> componentDescriptor) throws ComponentLookupException {
      return plexusContainer.lookup(componentDescriptor);
    }

    @Override
    public List<Object> lookupList(String role) throws ComponentLookupException {
      return plexusContainer.lookupList(role);
    }

    @Override
    public List<Object> lookupList(String role, List<String> roleHints) throws ComponentLookupException {
      return plexusContainer.lookupList(role, roleHints);
    }

    @Override
    public <T> List<T> lookupList(Class<T> type) throws ComponentLookupException {
      return plexusContainer.lookupList(type);
    }

    @Override
    public <T> List<T> lookupList(Class<T> type, List<String> roleHints) throws ComponentLookupException {
      return plexusContainer.lookupList(type, roleHints);
    }

    @Override
    public Map<String, Object> lookupMap(String role) throws ComponentLookupException {
      return plexusContainer.lookupMap(role);
    }

    @Override
    public Map<String, Object> lookupMap(String role, List<String> roleHints) throws ComponentLookupException {
      return plexusContainer.lookupMap(role, roleHints);
    }

    @Override
    public <T> Map<String, T> lookupMap(Class<T> type) throws ComponentLookupException {
      return plexusContainer.lookupMap(type);
    }

    @Override
    public <T> Map<String, T> lookupMap(Class<T> type, List<String> roleHints) throws ComponentLookupException {
      return plexusContainer.lookupMap(type, roleHints);
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptor(String role) {
      return plexusContainer.getComponentDescriptor(role);
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptor(String role, String roleHint) {
      return plexusContainer.getComponentDescriptor(role, roleHint);
    }

    @Override
    public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> type, String role, String roleHint) {
      return plexusContainer.getComponentDescriptor(type, role, roleHint);
    }

    @Override
    public Map<String, ComponentDescriptor<?>> getComponentDescriptorMap(String role) {
      return plexusContainer.getComponentDescriptorMap(role);
    }

    @Override
    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap(Class<T> type, String role) {
      return plexusContainer.getComponentDescriptorMap(type, role);
    }

    @Override
    public List<ComponentDescriptor<?>> getComponentDescriptorList(String role) {
      return plexusContainer.getComponentDescriptorList(role);
    }

    @Override
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> type, String role) {
      return plexusContainer.getComponentDescriptorList(type, role);
    }

    @Override
    public void addComponentDescriptor(ComponentDescriptor<?> componentDescriptor)
                                throws CycleDetectedInComponentGraphException {
      plexusContainer.addComponentDescriptor(componentDescriptor);
    }

    @Override
    public void release(Object component) throws ComponentLifecycleException {
      plexusContainer.release(component);
    }

    @Override
    public void releaseAll(Map<String, ?> components) throws ComponentLifecycleException {
      plexusContainer.releaseAll(components);
    }

    @Override
    public void releaseAll(List<?> components) throws ComponentLifecycleException {
      plexusContainer.releaseAll(components);
    }

    @Override
    public boolean hasComponent(String role) {
      return plexusContainer.hasComponent(role);
    }

    @Override
    public boolean hasComponent(String role, String roleHint) {
      return plexusContainer.hasComponent(role, roleHint);
    }

    @Override
    public boolean hasComponent(Class<?> type) {
      return plexusContainer.hasComponent(type);
    }

    @Override
    public boolean hasComponent(Class<?> type, String roleHint) {
      return plexusContainer.hasComponent(type, roleHint);
    }

    @Override
    public boolean hasComponent(Class<?> type, String role, String roleHint) {
      return plexusContainer.hasComponent(type, role, roleHint);
    }

    @Override
    public void dispose() {
      plexusContainer.dispose();
    }

    @Override
    public void addContextValue(Object key, Object value) {
      plexusContainer.addContextValue(key, value);
    }

    @Override
    public Context getContext() {
      return plexusContainer.getContext();
    }

    @Override
    public ClassRealm getContainerRealm() {
      return plexusContainer.getContainerRealm();
    }

    @Override
    public void registerComponentDiscoveryListener(ComponentDiscoveryListener listener) {
      plexusContainer.registerComponentDiscoveryListener(listener);
    }

    @Override
    public void removeComponentDiscoveryListener(ComponentDiscoveryListener listener) {
      plexusContainer.removeComponentDiscoveryListener(listener);
    }

    @Override
    public List<ComponentDescriptor<?>> discoverComponents(ClassRealm childRealm)
                                                    throws PlexusConfigurationException,
                                                           CycleDetectedInComponentGraphException {
      return plexusContainer.discoverComponents(childRealm);
    }

    @Override
    public List<ComponentDescriptor<?>> discoverComponents(ClassRealm realm, Object data)
                                                    throws PlexusConfigurationException,
                                                           CycleDetectedInComponentGraphException {
      return plexusContainer.discoverComponents(realm, data);
    }

    @Override
    public ClassRealm createChildRealm(String id) {
      return plexusContainer.createChildRealm(id);
    }

    @Override
    public ClassRealm getComponentRealm(String realmId) {
      return plexusContainer.getComponentRealm(realmId);
    }

    @Override
    public void removeComponentRealm(ClassRealm componentRealm) throws PlexusContainerException {
      plexusContainer.removeComponentRealm(componentRealm);
    }

    @Override
    public ClassRealm getLookupRealm() {
      return plexusContainer.getLookupRealm();
    }

    @Override
    public ClassRealm setLookupRealm(ClassRealm realm) {
      return plexusContainer.setLookupRealm(realm);
    }

    @Override
    public ClassRealm getLookupRealm(Object component) {
      return plexusContainer.getLookupRealm(component);
    }

    @Override
    public void addComponent(Object component, String role) throws CycleDetectedInComponentGraphException {
      plexusContainer.addComponent(component, role);
    }

    @Override
    public <T> void addComponent(T component, Class<?> role, String roleHint) {
      objects.put(role.getCanonicalName() + "#" + roleHint, component);
    }
  }
}
