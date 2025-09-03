# Kubernetes Deployment for Payment Service Webhook

This directory contains Kubernetes manifests for deploying the Payment Service Webhook application and its dependencies.

## Directory Structure

- `namespace.yaml` - Creates the payment-webhook namespace
- `configmap.yaml` - Configuration for the application
- `secrets.yaml` - Secrets for the application (base64 encoded)
- `deployment.yaml` - Main application deployment
- `service.yaml` - Service to expose the application internally
- `ingress.yaml` - Ingress to expose the application externally
- `kustomization.yaml` - Kustomize file to tie everything together
- `dependencies/` - Contains manifests for dependent services (PostgreSQL, Redis, Kafka)

## Prerequisites

- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl CLI
- kustomize (optional but recommended)

## Deployment Instructions

### 1. Deploy Dependencies

First, deploy the dependent services (PostgreSQL, Redis, Kafka):

```bash
kubectl apply -k dependencies/
```

### 2. Deploy the Application

Deploy the main application:

```bash
kubectl apply -k .
```

This will create:
- The payment-webhook namespace
- ConfigMaps and Secrets
- Deployment with 3 replicas
- Service for internal access
- Ingress for external access

### 3. Verify Deployment

Check that all pods are running:

```bash
kubectl get pods -n payment-webhook
```

Check services:

```bash
kubectl get svc -n payment-webhook
```

### 4. Access the Application

If using minikube:

```bash
minikube service payment-webhook-service -n payment-webhook
```

Or if using the ingress:

Add `payment-webhook.local` to your `/etc/hosts` file pointing to your cluster's IP, then access:
http://payment-webhook.local

## Configuration

### Environment Variables

The application is configured through environment variables set in the deployment. Key variables include:

- Database connection settings
- Redis connection settings
- Kafka bootstrap servers
- Security settings

### Secrets

Secrets are base64 encoded in the secrets.yaml file. To update them:

```bash
echo -n 'your-actual-password' | base64
```

Then update the value in secrets.yaml.

## Scaling

To scale the application:

```bash
kubectl scale deployment payment-webhook-deployment --replicas=5 -n payment-webhook
```

## Monitoring

Check logs:

```bash
kubectl logs -l app=payment-webhook -n payment-webhook
```

Check status:

```bash
kubectl get deployment payment-webhook-deployment -n payment-webhook
```

## Updating the Application

To update the application:

1. Build and push a new Docker image
2. Update the image tag in deployment.yaml
3. Apply the changes:

```bash
kubectl apply -k .
```

Or use rolling updates:

```bash
kubectl set image deployment/payment-webhook-deployment payment-webhook=payment-service-webhook:new-version -n payment-webhook
```