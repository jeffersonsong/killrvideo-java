git log -n 1
docker build -t a88eaf1044a8456fcd05bddece7594f44d04e890 -t killrvideo-java-local .
mkdir k8s
cd k8s
kompose convert -f ../docker-compose.yml
modify backend-deployment.yaml
    image: killrvideo-java-local:latest
    + imagePullPolicy: Never
minikube start
minikube image load killrvideo-java-local:latest
kubectl apply -f k8s
