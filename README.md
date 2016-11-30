# Test project

## Description

Implementation of an IRC-like chat server.

## Implementation Plan

* Get a simple server socket example handing off to 1-1 thread processor running
* Implement object model for room use
* Implement signalling between threads for room broadcast messages
* Implement tests
* Consider optimisations [IO selectors -- although really beyond usage needs]

## Description

* A Spring Boot application acting as a very simple chat server.
* Up to 100 clients can connect, each client messages are relayed to all other clients
* Server is hard coded to listen on port 8081

## Build and Execution

The server is tested and built using `gradle`, e.g.

```
[henry@little-ben ~]$ gradle clean build
:clean
:compileJava
:processResources
:classes
:findMainClass
:jar
:bootRepackage
:assemble
:compileTestJava
:processTestResources UP-TO-DATE
:testClasses
:test
:check
:build

BUILD SUCCESSFUL

Total time: 3.91 secs
```

It can then be run as a standalone jar:

```
[henry@little-ben ~]$ java -jar build/libs/gs-spring-boot-0.1.0.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.4.2.RELEASE)

2016-11-30 22:56:58,832 [main] INFO           Application.logStarting - Starting Application on little-ben.local with PID 28321 (/Users/henry/Downloads/test/t-test/build/libs/gs-spring-boot-0.1.0.jar started by henry in /Users/henry/Downloads/test/t-test)
2016-11-30 22:56:58,833 [main] DEBUG          Application.logStarting - Running with Spring Boot v1.4.2.RELEASE, Spring v4.3.4.RELEASE
2016-11-30 22:56:58,833 [main] INFO           Application.logStartupProfileInfo - No active profile set, falling back to default profiles: default
2016-11-30 22:56:58,877 [main] INFO  AnnotationConfigApplicationContext.prepareRefresh - Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@26f0a63f: startup date [Wed Nov 30 22:56:58 GMT 2016]; root of context hierarchy
2016-11-30 22:56:59,290 [Thread-1] INFO  ConnectionAcceptorService.lambda$acceptConnections$0 - Started connecton acceptor on port: 8081
2016-11-30 22:56:59,355 [main] INFO  AnnotationMBeanExporter.afterSingletonsInstantiated - Registering beans for JMX exposure on startup
2016-11-30 22:56:59,365 [main] INFO           Application.logStarted - Started Application in 0.764 seconds (JVM running for 1.329)
``` 

## Limitations

* Simple ‘echo’ behaviour to any client connecting only.  No command parsing
* No configuration of port, connection limit
* No room support
* Given the use of socket / client -- limited scope for scaling.  This is in line with the usage requirements however

