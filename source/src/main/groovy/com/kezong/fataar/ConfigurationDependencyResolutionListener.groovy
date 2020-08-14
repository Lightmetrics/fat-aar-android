package com.kezong.fataar

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

class ConfigurationDependencyResolutionListener implements DependencyResolutionListener {

    private final Project project

    private final Configuration configuration

    ConfigurationDependencyResolutionListener(Project project, Configuration configuration) {
        this.project = project
        this.configuration = configuration
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

//                project.dependencies.add('compileOnly', dependencyClone)

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
                project.dependencies.add('compileOnly', modifiedDependency)

            } else {
//                project.dependencies.add('compileOnly', dependency)

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
                project.dependencies.add('compileOnly', modifiedDependency)
            }
        }

        project.gradle.removeListener(this)
    }

    @Override
    void afterResolve(ResolvableDependencies resolvableDependencies) {
    }
}