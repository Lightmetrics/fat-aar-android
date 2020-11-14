package com.kezong.fataar

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

class EmbedDependencyListener implements DependencyResolutionListener {

    private final Project project

    private final Configuration configuration

    private final String compileOnlyConfigName;

    EmbedDependencyListener(Project project, Configuration configuration) {
        this.project = project
        this.configuration = configuration
        String prefix = getConfigNamePrefix(configuration.name)
        if (prefix != null) {
            this.compileOnlyConfigName = prefix + "CompileOnly"
        } else {
            this.compileOnlyConfigName = "compileOnly"
        }
    }

    private String getConfigNamePrefix(String configurationName) {
        if (configurationName.endsWith(FatLibraryPlugin.CONFIG_SUFFIX)) {
            return configurationName.substring(0, configuration.name.length() - FatLibraryPlugin.CONFIG_SUFFIX.length())
        } else {
            return null
        }
    }

    @Override
    void beforeResolve(ResolvableDependencies resolvableDependencies) {
        configuration.dependencies.each { dependency ->
            if (dependency instanceof DefaultProjectDependency) {
                if (dependency.targetConfiguration == null) {
                    dependency.targetConfiguration = "default"
                }
                // support that the module can be indexed in Android Studio 4.0.0
                DefaultProjectDependency dependencyClone = dependency.copy()
                dependencyClone.targetConfiguration = null;
                // The purpose is to support the code hints
                //project.dependencies.add(compileOnlyConfigName, dependencyClone)
                                
                Utils.logAnytime('[Dependency name][' + dependencyClone.name + ']')
                Utils.logAnytime('[Dependency group][' + dependencyClone.group + ']')
                Utils.logAnytime('[Dependency version][' + dependencyClone.version + ']')

                def modifiedDependency = dependencyClone

                if (dependencyClone.group == project.parent.name) {
                    //local module dependencyClone
                    Utils.logAnytime('[local module dependencyClone detected][' + dependencyClone.name + ']')
                    def map = new HashMap<String, String>()
                    map.put("path", ':' + dependencyClone.name)
                    //this makes the dependencyClone resolve the flavour and buildType correctly
                    modifiedDependency = project.dependencies.project(map)
                }
                project.dependencies.add(compileOnlyConfigName, modifiedDependency)

            } else {
                // The purpose is to support the code hints
                //project.dependencies.add(compileOnlyConfigName, dependency)
                
                Utils.logAnytime('[Dependency name][' + dependency.name + ']')
                Utils.logAnytime('[Dependency group][' + dependency.group + ']')
                Utils.logAnytime('[Dependency version][' + dependency.version + ']')

                def modifiedDependency = dependency

                if (dependency.group == project.parent.name) {
                    //local module dependency
                    Utils.logAnytime('[local module dependency detected][' + dependency.name + ']')
                    def map = new HashMap<String, String>()
                    map.put("path", ':' + dependency.name)
                    //this makes the dependency resolve the flavour and buildType correctly
                    modifiedDependency = project.dependencies.project(map)
                }
                project.dependencies.add(compileOnlyConfigName, modifiedDependency)
                
            }
        }
        project.gradle.removeListener(this)
    }

    @Override
    void afterResolve(ResolvableDependencies resolvableDependencies) {
    }
}
