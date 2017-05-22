/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example;

import com.example.api.DummyService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class DummyModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(DummyService.class, DummyServiceImpl.class);
    bindClient(Swagger1Api.class);
    bindClient(Swagger2Api.class);
  }
}
