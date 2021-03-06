/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.iep.gov;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.netflix.governator.LifecycleInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Helper to simplify common usage of governator.
 *
 * @deprecated Use {@link com.netflix.governator.Governator} directly.
 */
public final class Governator {

  private static final Logger LOGGER = LoggerFactory.getLogger(Governator.class);

  private static final String SERVICE_LOADER = "service-loader";
  private static final String NONE = "none";

  private static final Governator INSTANCE = new Governator();

  public static Governator getInstance() {
    return INSTANCE;
  }

  /** Add a task to be executed after shutting down governator. */
  public static void addShutdownHook(final Runnable task) {
    final Runnable r = new Runnable() {
      @Override public void run() {
        try {
          getInstance().shutdown();
          task.run();
        } catch (Exception e) {
          LOGGER.warn("exception during shutdown sequence", e);
        }
      }
    };
    Runtime.getRuntime().addShutdownHook(new Thread(r, "ShutdownHook"));
  }

  /**
   * Returns a list of all guice modules in the classpath using ServiceLoader. Modules that do
   * not have a corresponding provider config will not get loaded.
   */
  public static List<Module> getModulesUsingServiceLoader() {
    ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
    List<Module> modules = new ArrayList<>();
    for (Module m : loader) {
      modules.add(m);
    }
    return modules;
  }

  private Governator() {
  }

  private LifecycleInjector injector;

  /** Return the injector used with the governator lifecycle. */
  public Injector getInjector() {
    return injector;
  }

  /** Start up governator using the list of modules from {@link #getModulesUsingServiceLoader()}. */
  public void start() throws Exception {
    start(getModulesUsingServiceLoader());
  }

  /** Start up governator with an arbitrary list of modules. */
  public void start(Collection<Module> modules) throws Exception {
    injector = com.netflix.governator.Governator.createInjector(modules);
  }

  /** Start up governator with an arbitrary list of modules. */
  public void start(Module... modules) throws Exception {
    injector = com.netflix.governator.Governator.createInjector(modules);
  }

  /** Shutdown governator. */
  public void shutdown() throws Exception {
    injector.shutdown();
  }
}
