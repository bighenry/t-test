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

## Limitations

* Simple ‘echo’ behaviour to any client connecting only.  No command parsing
* No configuration of port, connection limit
* No room support
* Given the use of socket / client -- limited scope for scaling.  This is in line with the usage requirements however

