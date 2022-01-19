Get latest commit.
```
git log -n 1
```
Build docker image
```
docker build -t acb18b697f474b2cd7deceda655c32e5af54fc7e -t killrvideo-java-local .
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
minikube start --profile killrvideo
```

Install local docker image to minikube

```
minikube image load killrvideo-java-local:latest
```

```
kubectl apply -f k8s
```

```
kubectl delete pods,rs,deploy,svc -l io.kompose.service=web
kubectl delete pods,rs,deploy,svc -l io.kompose.service=backend
kubectl delete pods -l io.kompose.service=dse-config
kubectl delete pods,rs,deploy,svc -l io.kompose.service=redis
kubectl delete pods,rs,deploy,svc -l io.kompose.service=dse
```

```
minikube stop --profile killrvideo
```
```
minikube delete
```
