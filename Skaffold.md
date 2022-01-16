minikube start --profile killrvideo

skaffold init
skaffold dev
minikube stop --profile killrvideo

Jib

eval $(minikube docker-env)
./gradlew jibDockerBuild