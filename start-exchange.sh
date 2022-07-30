#!/bin/bash


DIR="$( cd "$( dirname "$0" )" && pwd )"

mkdir -p "$DIR/logs"

runInBackground() {
    eval "$@" & disown;
}

start_svc () {
  svc=$1
  echo starting "$svc"

  case $svc in
    exchange-node)
      runInBackground /usr/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
                      --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
                      "$DIR/core/build/exchange-node.jar" 0 cluster-node-config.test.yaml &> "$DIR/logs/exchange-node.log" &
      ;;
    rest-api)
      runInBackground /usr/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 \
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
                        "$DIR/$svc/build/$svc.jar" 0 cluster-node-config.test.yaml "$DIR/rest-api/src/main/resources/openapi.yaml" \
                        &> "$DIR/logs/$svc.log" &
      ;;
    market-data)
      runInBackground /usr/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 \
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
                        "$DIR/market-data/build/market-data.jar" 0 cluster-node-config.test.yaml &> "$DIR/logs/market-data.log" &
      ;;
    admin-api)
      runInBackground /usr/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5008 \
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
                        "$DIR/admin-api/build/admin-api.jar" 0 cluster-node-config.test.yaml &> "$DIR/logs/admin-api.log" &
      ;;
    fix-gateway)
      runInBackground /usr/bin/java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009  \
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
                        "$DIR/fix-gateway/build/fix-gateway.jar" 0 cluster-node-config.test.yaml &> "$DIR/logs/fix-gateway.log" &
      ;;
   trading-ui)
      cd "$DIR/trading-ui/src/main/js" && BROWSER=none PORT=3000 pm2 start "npm start" --name "Trading UI"
      ;;
    admin-ui)
      cd "$DIR/admin-ui/src/main/js" && BROWSER=none PORT=3001 pm2 start "npm start" --name "Admin UI"
      ;;
    *)
      echo Service "$svc" is unknown
      ;;
  esac
}

if [[ $# -eq 0 ]] ; then
    echo 'starting all services'
    start_svc exchange-node
    start_svc rest-api
    start_svc market-data
    start_svc admin-api
    start_svc fix-gateway
    start_svc trading-ui
    start_svc admin-ui
else
  for svc in "$@"
  do
      start_svc "$svc"
  done
fi


sleep 5
exit 0
