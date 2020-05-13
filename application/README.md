1. Create a docker network -> docker create network statepoc
2. Start csaasndra -> docker run -p 9042:9042 --rm --name cassandra --network statepoc -d cassandra:3.11
3. Running from SBT
3.1 Start shard -> sbt '; set javaOptions += "-Dconfig.resource=cluster-application.conf" ; run'
3.2 Start endpoint -> sbt '; set javaOptions += "-Dconfig.resource=endpoint-application.conf" ; run'
3.3 Send curl -> curl http://localhost:8082/cart/1
3.4 Run Gatling -> sbt gatling:test
4. Running from docker
4.1 Publish docker -> 
4.2 Start shard -> 
4.3 Start endpoint -> 
4.3 Send curl -> curl http://localhost:8082/cart/1
4.4 Run Gatling -> sbt gatling:test
5. Run on GCP/K8
4.1 Set docker credentials ->
4.2 Publish docker image to GCP ->
4.3 Set up cluster ->
4.4 Set cluster credentials ->
4.5 Deploy shards ->
4.6 Deploy endpoints ->
4.7 Get service endpoint ->
4.8 Run Gatling ->


