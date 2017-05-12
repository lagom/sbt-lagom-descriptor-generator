[![Build Status](https://travis-ci.org/lagom/sbt-lagom-descriptor-generator.svg?branch=master)](https://travis-ci.org/lagom/sbt-lagom-descriptor-generator)

# sbt-lagom-descriptor-generator

An `sbt` plugin to generate Lagom Service Descriptor code (and related entities) provided a Swagger/OpenAPI specification.

Usage
-----

Include the plugin dependency on your `project/plugins.sbt` file:

```
addSbtPlugin("com.lightbend.lagom" % "lagom-descriptor-generator-sbt-plugin" % "0.0.2")
```

and enable the plugin on your project in `build.sbt`:

```
lazy val ``service-api = (project in file("service-api"))
  .enablePlugins(LagomJava && LagomOpenApiPlugin)
  .settings(
    libraryDependencies ++=
      Seq(
        lagomJavadslApi
      )
  )
```

The plugin will trigger the Java or Scala code generation depending on the dependencies included in your code.



Project Status
--------------

**DISCLAIMER** Currently the build job in Travis fails. Solve [#20](https://github.com/lagom/sbt-lagom-descriptor-generator/issues/20) to fix that.

This plugin is not ready for production. It currently works in most happy scenarios when given one or many JSON or YAML OpenAPI V2 specification files. There is a minimal set of `scripted` tests ([1](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/lagom-descriptor-generator-sbt-plugin/src/sbt-test/plugin/petstore-java/test), [2](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/lagom-descriptor-generator-sbt-plugin/src/sbt-test/plugin/multiclient-java/test), [3](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/lagom-descriptor-generator-sbt-plugin/src/sbt-test/plugin/petstore-scala/test)) demonstrating the basic functionality available.


Project Structure
--------------

This project provides an `sbt` plugin that will generate the Lagom Service Descriptor given a 3rd party API specification. It works by detecting the dependency of the project on Lagom and also the Java vs Scala API. Once the plugin is activated it will locate all 3rd party specification files and proceed with the code genreations.

 * `lagom-descriptor-generator-sbt-plugin`: the code for the `sbt` plugin. This project also contains the `scripted` tests.

 * `runner/`: an entry point to all valid combinations of specs and generated formats. See for example how the actual sbt plugin code [uses](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/b6f4f9eae39f949580a81feab8671303c0b70561/lagom-descriptor-generator-sbt-plugin/src/main/scala/com/lightbend/lagom/spec/sbt/LagomOpenApiGenerator.scala#L51-L59) the `LagomGenerators`. `runner/` also includes integration tests which run faster than `scripted`.

 * `spec-parsers/`: contains each of the 3rd party specicication parsers. Implementors of a new format should create a project in this directory providing an implementation of [`Parser[T]`](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/c68e929840cbbd025a23e36519f070b93cf95d9a/generator-api/src/main/scala/com/lightbend/lagom/spec/parser/SpecParser.scala#L14-L25).

 * `lagom-renderers/`: given an intermediate representation of [an API](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/generator-api/src/main/scala/com/lightbend/lagom/spec/model/DescriptorModelling.scala#L6-L52) and the [models](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/generator-api/src/main/scala/com/lightbend/lagom/spec/model/Type.scala#L6-L25) used in it these generate LagomJava and LagomScala code.

 * `generator-api/`: all modelling required for the generator. The key part is `generate()` which is a function of type `InputStream => Spec => Service => Service => Output`. That is a [4 step tranformation](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/c68e929840cbbd025a23e36519f070b93cf95d9a/generator-api/src/main/scala/com/lightbend/lagom/spec/LagomGenerator.scala#L26-L36): going through the specification file to the 3rd party specification representation, then the intermediate representation ([API](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/generator-api/src/main/scala/com/lightbend/lagom/spec/model/DescriptorModelling.scala#L6-L52) and [models](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/generator-api/src/main/scala/com/lightbend/lagom/spec/model/Type.scala#L6-L25)) and finally the `Output` which represents the filenames and code contents before persisting into actual files. The intermediate step `Service => Service` is meant to allow users to inject transformations over the 
intermediate representation (e.g. to map JSON Schema's `dateTime` to `org.joda.time.Instant` instead of `java.time.Instant`). See other potential applications of this transformation on the [issues](https://github.com/lagom/sbt-lagom-descriptor-generator/issues/25) [list](https://github.com/lagom/sbt-lagom-descriptor-generator/issues/24).


### Supported Specs

**OpenAPI V2** placing JSON of YAML OpenAPI V2 files in a project's `src/main/openapi` will generate the required code to represent that spec using Lagom code. See the `scripted` tests for an [example](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/1c312cee7e2aa61fd4206b277d87d15309c5d2dd/lagom-descriptor-generator-sbt-plugin/src/sbt-test/plugin/multiclient-java/dummy-impl/src/main/openapi/swagger1.yaml#L27-L26).

Supporting new spec formats would require:

 - [ ] a new project on `spec-parsers/` providing an implementation of [`Parser[T]`](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/c68e929840cbbd025a23e36519f070b93cf95d9a/generator-api/src/main/scala/com/lightbend/lagom/spec/parser/SpecParser.scala#L14-L25)
 - [ ] convenience methods for java and scala code generation on [`LagomGenerators`](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/c68e929840cbbd025a23e36519f070b93cf95d9a/runner/src/main/scala/com/lightbend/lagom/spec/LagomGenerators.scala#L12)
 - [ ] a comprehensive test set (unit tests on parsing, integration tests in `runner/` and `scripted` tests demonstrating the complete run of the parser in an sbt project on the `lagom-descriptor-generator-sbt-plugin` project)


Contributions & Maintainers
---------------------------

*This project does not have contributors, it only has maintainers—frequent and infrequent—and everyone helps out.*
This repo loves new maintainers as well as old maintainers. :-)
The Lagom core team keeps an eye on the project to assure its overall coherence but does not fully support this sbt plugin.

Contributions are very welcome, see [CONTRIBUTING.md](https://github.com/lagom/sbt-lagom-descriptor-generator/blob/master/CONTRIBUTING.md) or skim [existing tickets](https://github.com/lagom/sbt-lagom-descriptor-generator/issues) to see where you could help out.
