package com.example.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface DummyService extends Service {

    ServiceCall<akka.NotUsed, String> dummy();

    default Descriptor descriptor() {
        return named("dummy").withCalls(
                restCall(Method.GET, "/v1/dummy", this::dummy)
        ).withAutoAcl(true);
    }
}