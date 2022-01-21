kubectl delete svc,deploy -l app=web
kubectl delete svc,deploy -l app=backend
kubectl delete pods -l app=dse-config
kubectl delete svc,deploy -l app=redis
kubectl delete svc,deploy -l app=dse