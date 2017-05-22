/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example;

import akka.NotUsed;
import com.example.api.DummyService;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import javax.inject.Inject;

/**
 * Implementation of the HelloService.
 */
public class DummyServiceImpl implements DummyService {

    private Swagger1Api s1;
    private Swagger2Api s2;

    @Inject
    public DummyServiceImpl(Swagger1Api s1, Swagger2Api s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public ServiceCall<NotUsed, String> dummy() {
        return req -> s1.getSomething().invoke().thenCompose(str1 ->
                s2.getSomethingMore().invoke().thenApply(str2 ->
                        str1 + " - " + str2));
    }
}
