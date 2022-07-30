#!/bin/bash


./gradlew clean
./gradlew --stacktrace --parallel \
  :core:exchangeNodeJar \
  :admin-api:adminApiJar \
  :rest-api:restApiJar \
  :fix-gateway:fixGatewayJar \
  :market-data:marketDataServerJar \
  :trading-ui:installTradingUI \
  :admin-ui:installAdminUI \
  compileTestJava
