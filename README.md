# exchange

This project presents a reference implementation of an exchange. It leverages a number of technologies / libraries:

* [Aeron](https://github.com/real-logic/aeron): Aeron Cluster, Archive and Aeron transport protocol.
* [Aeronic](https://github.com/lob-software/aeronic): Proxy generation for Aeron.
* [Artio](https://github.com/real-logic/artio): FIX Gateway
* [Vert.x](https://vertx.io/): Market Data WS API + REST API
* [React](https://reactjs.org/): Admin UI + Trading UI
* [Selenide](https://selenide.org/): UI acceptance tests

## Quickstart

In order to build the project locally run:

```shell
./build.sh
```

This will build required artifacts to the start all the services

```shell
./start-exchange.sh
```

To stop the exchange and kill underlying processes, run:

```shell
./stop-exchange.sh
```

## Testing

The project has a deep testing suite, which consists of unit tests, as well as acceptance tests that deploy some or all of the services. Before running all the tests, make sure that exchange is stopped to avoid, as the same ports will be used. To run tests:

```shell
./gradlew test
```