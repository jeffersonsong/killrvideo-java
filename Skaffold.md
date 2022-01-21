Get latest commit.
```
git log -n 1
```
Build docker image
```
docker build -t acb18b697f474b2cd7deceda655c32e5af54fc7e -t killrvideo-java-local .
```
Install local docker image to minikube

skaffold init (first time)

use minikube
minikube start --profile killrvideo
```
minikube image load killrvideo-java-local:latest
```

skaffold dev
minikube stop --profile killrvideo
minikube delete --profile killrvideo

Jib

eval $(minikube docker-env)
./gradlew jibDockerBuild

Use k3d
./k3d-create-cluster.sh
skaffold dev -p local --port-forward

k3d cluster list
k3d cluster delete k3s-local

kompose convert -f ../../docker-compose.yml

Get latest commit.
```
git log -n 1
```
Build docker image
```
docker build -t 5ebd53f4a6c990566e82dd78f0021e6a93b76eb1 -t killrvideo-java-local .
```

k3d image import killrvideo-java-local:latest --cluster k3s-local
```
kubectl apply -f k8s/base
kubectl apply -f k8s/base/dse-deployment.yaml
kubectl apply -f k8s/base/dse-service.yaml
kubectl apply -f k8s/base/redis-deployment.yaml
kubectl apply -f k8s/base/redis-service.yaml
kubectl apply -f k8s/base/dse-config-pod.yaml
kubectl apply -f k8s/base/backend-deployment.yaml
kubectl apply -f k8s/base/backend-service.yaml
```
port forward backend and dse.

with local insecure registry (not working)

➜ docker image ls
REPOSITORY                                 TAG                                                                IMAGE ID       CREATED         SIZE
5ebd53f4a6c990566e82dd78f0021e6a93b76eb1   latest                                                             f32c0773ef14   5 seconds ago   771MB
killrvideo-java-local                      latest                                                             f32c0773ef14   5 seconds ago   771MB

docker tag killrvideo-java-local:latest registry.local:5000/killrvideo-java-local:latest
docker push registry.local:5000/killrvideo-java-local:latest

docker image ls                                                                         
REPOSITORY                                  TAG                                                                IMAGE ID       CREATED         SIZE
5ebd53f4a6c990566e82dd78f0021e6a93b76eb1    latest                                                             f32c0773ef14   2 minutes ago   771MB
killrvideo-java-local                       latest                                                             f32c0773ef14   2 minutes ago   771MB
registry.local:5000/killrvideo-java-local   latest                                                             f32c0773ef14   2 minutes ago   771MB

➜ curl http://registry.localhost:5000/v2/_catalog
{"repositories":["killrvideo-java-local"]}
