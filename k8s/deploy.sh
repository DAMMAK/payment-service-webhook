#!/bin/bash
# ==========================================
# deploy.sh - Deployment script for Payment Webhook Service
# ==========================================

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="payment-webhook"
ARGOCD_NAMESPACE="argocd"
APP_NAME="payment-webhook-service"
REPO_URL="https://github.com/DAMMAK/payment-service-webhook"
BRANCH="main"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi

    # Check if ArgoCD CLI is installed
    if ! command -v argocd &> /dev/null; then
        log_warning "argocd CLI is not installed. Some operations may not work."
    fi

    # Check cluster connectivity
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi

    log_success "Prerequisites check passed"
}

create_namespace() {
    log_info "Creating namespace if it doesn't exist..."
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
    log_success "Namespace ${NAMESPACE} ready"
}

deploy_with_kubectl() {
    log_info "Deploying with kubectl..."

    # Apply resources in order
    log_info "Applying namespace..."
    kubectl apply -f namespace.yaml

    log_info "Applying secrets..."
    kubectl apply -f secrets.yaml

    log_info "Applying configmaps..."
    kubectl apply -f configmap.yaml

    log_info "Applying resource quotas..."
    kubectl apply -f resource-quota.yaml

    log_info "Applying PVCs..."
    kubectl apply -f postgres.yaml --selector="v1=PersistentVolumeClaim" || true

    log_info "Applying database services..."
    kubectl apply -f postgres.yaml
    kubectl apply -f redis.yaml

    log_info "Waiting for database to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgres -n ${NAMESPACE} --timeout=300s
    kubectl wait --for=condition=ready pod -l app=redis -n ${NAMESPACE} --timeout=300s

    log_info "Applying message queue services..."
    kubectl apply -f zookeeper-deployment.yaml

    log_info "Waiting for Zookeeper to be ready..."
    kubectl wait --for=condition=ready pod -l app=zookeeper -n ${NAMESPACE} --timeout=300s

    kubectl apply -f kafka-deployment.yaml

    log_info "Waiting for Kafka to be ready..."
    kubectl wait --for=condition=ready pod -l app=kafka -n ${NAMESPACE} --timeout=300s

    log_info "Applying main application..."
    kubectl apply -f deployment.yaml
    kubectl apply -f service.yaml

    log_info "Applying additional resources..."
    kubectl apply -f pod-disruption-budget.yaml
    kubectl apply -f horizontal-pod-autoscaler.yaml

    log_info "Waiting for application to be ready..."
    kubectl wait --for=condition=available deployment/payment-service-webhook -n ${NAMESPACE} --timeout=300s

    log_success "Deployment completed successfully"
}

deploy_with_kustomize() {
    log_info "Deploying with Kustomize..."
    kubectl apply -k .
    log_success "Kustomize deployment completed"
}

deploy_with_argocd() {
    log_info "Deploying with ArgoCD..."

    # Check if ArgoCD is installed
    if ! kubectl get namespace ${ARGOCD_NAMESPACE} &> /dev/null; then
        log_error "ArgoCD namespace not found. Please install ArgoCD first."
        exit 1
    fi

    # Apply the ArgoCD application
    kubectl apply -f argocd-application.yaml

    # Wait for sync
    log_info "Waiting for ArgoCD to sync the application..."
    sleep 10

    # Check application status
    if command -v argocd &> /dev/null; then
        argocd app sync ${APP_NAME} --server-side-apply
        argocd app wait ${APP_NAME} --health
    else
        log_info "ArgoCD CLI not available. Check the ArgoCD UI for sync status."
    fi

    log_success "ArgoCD deployment initiated"
}

check_deployment() {
    log_info "Checking deployment status..."

    echo "=== Namespace ==="
    kubectl get namespace ${NAMESPACE}

    echo -e "\n=== Pods ==="
    kubectl get pods -n ${NAMESPACE} -o wide

    echo -e "\n=== Services ==="
    kubectl get services -n ${NAMESPACE}

    echo -e "\n=== Deployments ==="
    kubectl get deployments -n ${NAMESPACE}

    echo -e "\n=== PVCs ==="
    kubectl get pvc -n ${NAMESPACE}

    echo -e "\n=== HPA ==="
    kubectl get hpa -n ${NAMESPACE}

    echo -e "\n=== Application Health ==="
    if kubectl get pods -n ${NAMESPACE} -l app=payment-service-webhook --field-selector=status.phase=Running | grep -q payment-service-webhook; then
        log_success "Payment service is running"

        # Get service endpoint
        SERVICE_IP=$(kubectl get service payment-service-webhook -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
        if [[ "$SERVICE_IP" != "Pending" && -n "$SERVICE_IP" ]]; then
            log_info "Service available at: http://${SERVICE_IP}/actuator/health"
        else
            log_info "Service IP still pending. Use port-forward for testing: kubectl port-forward -n ${NAMESPACE} service/payment-service-webhook 8080:80"
        fi
    else
        log_warning "Payment service pods are not in running state"
    fi
}

cleanup() {
    log_info "Cleaning up deployment..."

    read -p "Are you sure you want to delete the entire deployment? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # Delete ArgoCD application first if it exists
        kubectl delete -f argocd-application.yaml --ignore-not-found=true

        # Delete namespace (this will delete all resources)
        kubectl delete namespace ${NAMESPACE} --ignore-not-found=true

        log_success "Cleanup completed"
    else
        log_info "Cleanup cancelled"
    fi
}

port_forward() {
    log_info "Setting up port forwarding..."
    echo "Access the application at: http://localhost:8080"
    echo "Access health endpoint at: http://localhost:8080/actuator/health"
    echo "Press Ctrl+C to stop port forwarding"

    kubectl port-forward -n ${NAMESPACE} service/payment-service-webhook 8080:80
}

logs() {
    local component=${1:-payment-service-webhook}
    log_info "Showing logs for ${component}..."
    kubectl logs -n ${NAMESPACE} -l app=${component} --tail=100 -f
}

# Main script logic
case "${1:-deploy}" in
    "prerequisites"|"pre")
        check_prerequisites
        ;;
    "deploy"|"d")
        check_prerequisites
        create_namespace
        case "${2:-kubectl}" in
            "kubectl"|"k")
                deploy_with_kubectl
                ;;
            "kustomize"|"kust")
                deploy_with_kustomize
                ;;
            "argocd"|"argo")
                deploy_with_argocd
                ;;
            *)
                log_error "Unknown deployment method: $2"
                echo "Usage: $0 deploy [kubectl|kustomize|argocd]"
                exit 1
                ;;
        esac
        check_deployment
        ;;
    "status"|"check"|"s")
        check_deployment
        ;;
    "cleanup"|"clean"|"delete")
        cleanup
        ;;
    "port-forward"|"pf")
        port_forward
        ;;
    "logs"|"l")
        logs ${2:-payment-service-webhook}
        ;;
    "help"|"h"|"-h"|"--help")
        cat << EOF
Payment Webhook Service Deployment Script

Usage: $0 [COMMAND] [OPTIONS]

Commands:
    prerequisites, pre     Check if all prerequisites are installed
    deploy, d             Deploy the application
        kubectl, k          Deploy using kubectl (default)
        kustomize, kust     Deploy using kustomize
        argocd, argo        Deploy using ArgoCD
    status, check, s      Check deployment status
    cleanup, clean        Clean up the deployment
    port-forward, pf      Set up port forwarding for local access
    logs, l               Show application logs
        [component]         Show logs for specific component (default: payment-service-webhook)
    help, h               Show this help message

Examples:
    $0 prerequisites
    $0 deploy kubectl
    $0 deploy argocd
    $0 status
    $0 port-forward
    $0 logs kafka
    $0 cleanup

Environment Variables:
    NAMESPACE             Kubernetes namespace (default: payment-webhook)
    ARGOCD_NAMESPACE      ArgoCD namespace (default: argocd)
    APP_NAME              ArgoCD application name (default: payment-webhook-service)
    REPO_URL              Git repository URL
    BRANCH                Git branch (default: main)
EOF
        ;;
    *)
        log_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac