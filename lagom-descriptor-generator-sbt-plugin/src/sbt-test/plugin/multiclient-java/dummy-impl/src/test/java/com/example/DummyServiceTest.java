package com.example;

import akka.NotUsed;
import com.example.api.DummyService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class DummyServiceTest {

    private static final Setup setup = defaultSetup()
            .configureBuilder(b ->
                    b.overrides(
                            bind(Swagger1Api.class).to(Swagger1Stub.class),
                            bind(Swagger2Api.class).to(Swagger2Stub.class)
                    )
            );

    @Test
    public void shouldConcatDonwnstreams() throws Exception {
        withServer(setup, server -> {
            DummyService service = server.client(DummyService.class);

            String msg = service.dummy().invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals("str1 - str2", msg);
        });
    }

    public static class Swagger1Stub implements Swagger1Api{
        @Override
        public ServiceCall<NotUsed, String> getSomething() {
            return req -> CompletableFuture.completedFuture("str1");
        }
    }

    public static class Swagger2Stub implements Swagger2Api{
        @Override
        public ServiceCall<NotUsed, String> getSomethingMore() {
            return req -> CompletableFuture.completedFuture("str2");
        }
    }

}