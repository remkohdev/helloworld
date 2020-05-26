# helloworld

Run on localhost,
```
mvn clean install
mvn test
mvn spring-boot:run
```

Run on localhost as container and push to docker hub,
```
mvn clean install
docker stop helloworld
docker rm helloworld
docker image build -t helloworld .
docker run -d --name helloworld -p 8081:8080 helloworld
docker ps -a
docker logs helloworld
docker login -u remkohdev
docker tag helloworld remkohdev/helloworld
docker push remkohdev/helloworld
docker run -d --name helloworld -p 8081:8080 remkohdev/helloworld
```

Create the deployment and service declarations for both the direct helloworld and the helloworld-proxy application with both a NodePort and LoadBalancer type service,
```
$ echo 'apiVersion: apps/v1
kind: Deployment
metadata:
  name: helloworld
  labels:
    app: helloworld
spec:
  replicas: 3
  selector:
    matchLabels:
      app: helloworld
  template:
    metadata:
      labels:
        app: helloworld
    spec:
      containers:
      - name: helloworld
        image: remkohdev/helloworld
        ports:
        - name: http-server
          containerPort: 8080' > helloworld-deployment.yaml
```

```
echo 'apiVersion: v1
kind: Service
metadata:
  name: helloworld
  labels:
    app: helloworld
spec:
  ports:
  - port: 8080
    targetPort: http-server
  selector:
    app: helloworld
  type: NodePort' > helloworld-service-nodeport.yaml
```

```
echo 'apiVersion: v1
kind: Service
metadata:
  name: helloworld
  labels:
    app: helloworld
spec:
  ports:
  - port: 8080
    targetPort: http-server
  selector:
    app: helloworld
  type: LoadBalancer' > helloworld-service-loadbalancer.yaml
```


```
$ echo 'apiVersion: apps/v1
kind: Deployment
metadata:
  name: helloworld-proxy
  labels:
    app: helloworld-proxy
spec:
  replicas: 3
  selector:
    matchLabels:
      app: helloworld-proxy
  template:
    metadata:
      labels:
        app: helloworld-proxy
    spec:
      containers:
      - name: helloworld-proxy
        image: remkohdev/helloworld
        ports:
        - name: http-server
          containerPort: 8080' > helloworld-proxy-deployment.yaml
```

```
echo 'apiVersion: v1
kind: Service
metadata:
  name: helloworld-proxy
  labels:
    app: helloworld-proxy
spec:
  ports:
  - port: 8080
    targetPort: http-server
  selector:
    app: helloworld-proxy
  type: NodePort' > helloworld-proxy-service-nodeport.yaml
```

```
echo 'apiVersion: v1
kind: Service
metadata:
  name: helloworld-proxy
  labels:
    app: helloworld-proxy
spec:
  ports:
  - port: 8080
    targetPort: http-server
  selector:
    app: helloworld-proxy
  type: LoadBalancer' > helloworld-proxy-service-loadbalancer.yaml
```

Login to ibmcloud and set current-context to your cluster. Set the CLUSTERNAME variable, for convenience.
```
CLUSTERNAME=remkohdev-iks116-2n-cluster
```

Create the helloworld and helloworld-proxy applications with a LoadBalancer,
```
kubectl create -f helloworld-deployment.yaml
kubectl create -f helloworld-service-loadbalancer.yaml
kubectl create -f helloworld-proxy-deployment.yaml
kubectl create -f helloworld-proxy-service-loadbalancer.yaml

kubectl get all
```

kubectl delete -f helloworld-deployment.yaml
kubectl delete -f helloworld-service-loadbalancer.yaml
kubectl delete -f helloworld-proxy-deployment.yaml
kubectl delete -f helloworld-proxy-service-loadbalancer.yaml


kubectl apply -f helloworld-deployment.yaml
kubectl apply -f helloworld-service-loadbalancer.yaml
kubectl apply -f helloworld-proxy-deployment.yaml
kubectl apply -f helloworld-proxy-service-loadbalancer.yaml

test the helloworld and the helloworld-proxy app,
```
curl --location --request POST 'http://169.60.156.139:30856/api/messages' \
--header 'Content-Type: application/json' \
--header 'Content-Type: text/plain' \
--data-raw '{
"sender": "remko"
}'

curl --location --request POST 'http://169.60.156.140:30422/proxy/api/messages' \
--header 'Content-Type: application/json' \
--header 'Content-Type: text/plain' \
--data-raw '{
"sender": "remko",
"host": "helloworld"
}'
```

## Add Network Policy
Zero trust network: disallow all traffic to all pods, only allow external traffic to the proxy
Only allow traffic from helloworld-proxy to helloworld

