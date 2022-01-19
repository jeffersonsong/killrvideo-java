minikube start --profile killrvideo

skaffold init (first time)
skaffold dev
minikube stop --profile killrvideo

Jib

eval $(minikube docker-env)
./gradlew jibDockerBuild