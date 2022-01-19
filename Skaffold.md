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
docker tag killrvideo-java-local:latest registry.local:5000/killrvideo-java-local:latest
docker push registry.local:5000/killrvideo-java-local:latest
skaffold dev