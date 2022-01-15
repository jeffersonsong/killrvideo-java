Get latest commit.
```
git log -n 1
```
Build docker image
```
docker build -t a88eaf1044a8456fcd05bddece7594f44d04e890 -t killrvideo-java-local .
```

```
mkdir k8s
cd k8s
```

Convert docker-compose.yaml to k8s.
```
kompose convert -f ../docker-compose.yml
```

modify backend-deployment.yaml
```
    image: killrvideo-java-local:latest
    + imagePullPolicy: Never
```

Start minikube
```
minikube start
```

Install local docker image to minikube

```
minikube image load killrvideo-java-local:latest
```

```
kubectl apply -f k8s
```

```
minikube stop
```
```
minikube delete
```
