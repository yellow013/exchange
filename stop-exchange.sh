#!/bin/bash

kill_process () {
  if [ -n "$1" ] ; then
    echo "attempting to kill process $1"
    kill -9 "$1"
  fi
}

kill_uis () {
  pm2 kill
}

REST_API_PID="$(jps | grep rest-api.jar | awk '{print $1}')"
EXCHANGE_NODE_PID="$(jps | grep exchange-node.jar | awk '{print $1}')"
FIX_GATEWAY_PID="$(jps | grep fix-gateway.jar | awk '{print $1}')"
ADMIN_API_PID="$(jps | grep admin-api.jar | awk '{print $1}')"
MARKET_DATA_PID="$(jps | grep market-data.jar | awk '{print $1}')"

kill_process "${REST_API_PID}"
kill_process "${EXCHANGE_NODE_PID}"
kill_process "${FIX_GATEWAY_PID}"
kill_process "${ADMIN_API_PID}"
kill_process "${MARKET_DATA_PID}"
pkill -9 geth
kill_uis