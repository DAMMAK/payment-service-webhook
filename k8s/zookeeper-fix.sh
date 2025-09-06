#!/bin/bash
# zookeeper-fix.sh - Diagnose and fix Zookeeper issues

set -euo pipefail

NAMESPACE="payment-webhook"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Zookeeper Diagnostic Report ===${NC}\n"

echo -e "${YELLOW}1. Current Zookeeper Pod Status:${NC}"
kubectl get pods -n ${NAMESPACE} -l app=zookeeper -o wide

echo -e "\n${YELLOW}2. Zookeeper Pod Events:${NC}"
kubectl describe pod -n ${NAMESPACE} -l app=zookeeper | grep -A 15 "Events:"

echo -e "\n${YELLOW}3. Zookeeper Logs (last 50 lines):${NC}"
kubectl logs -n ${NAMESPACE} -l app=zookeeper --tail=50

echo -e "\n${YELLOW}4. Previous Container Logs (if available):${NC}"
kubectl logs -n ${NAMESPACE} -l app=zookeeper --previous --tail=30 2>/dev/null || echo "No previous logs available"

echo -e "\n${YELLOW}5. Node Resources:${NC}"
kubectl top nodes 2>/dev/null || echo "Metrics server not available"

# Common fixes
echo -e "\n${BLUE}=== Applying Common Fixes ===${NC}"

echo -e "${GREEN}Fix 1: Deleting failed Zookeeper pod to force restart${NC}"
kubectl delete pod -n ${NAMESPACE} -l app=zookeeper

echo -e "${GREEN}Fix 2: Applying improved Zookeeper configuration${NC}"

# Create improved zookeeper configuration
cat << 'EOF' > /tmp/zookeeper-fixed.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
  namespace: payment-webhook
  labels:
    app.kubernetes.io/name: zookeeper
    app.kubernetes.io/managed-by: argocd
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: zookeeper
  template:
    metadata:
      labels:
        app: zookeeper
        app.kubernetes.io/name: zookeeper
    spec:
      containers:
        - name: zookeeper
          image: confluentinc/cp-zookeeper:7.4.0
          ports:
            - containerPort: 2181
              name: client
          env:
            - name: ZOOKEEPER_CLIENT_PORT
              value: "2181"
            - name: ZOOKEEPER_TICK_TIME
              value: "2000"
            - name: ZOOKEEPER_SERVER_ID
              value: "1"
            - name: ZOOKEEPER_INIT_LIMIT
              value: "5"
            - name: ZOOKEEPER_SYNC_LIMIT
              value: "2"
            - name: ZOOKEEPER_MAX_CLIENT_CNXNS
              value: "60"
            - name: ZOOKEEPER_AUTOPURGE_SNAP_RETAIN_COUNT
              value: "3"
            - name: ZOOKEEPER_AUTOPURGE_PURGE_INTERVAL
              value: "24"
            - name: ZOOKEEPER_LOG_LEVEL
              value: "INFO"
          volumeMounts:
            - name: zookeeper-data
              mountPath: /var/lib/zookeeper/data
            - name: zookeeper-logs
              mountPath: /var/lib/zookeeper/log
            - name: tmp
              mountPath: /tmp
          # More lenient health checks
          livenessProbe:
            exec:
              command:
                - sh
                - -c
                - "echo ruok | nc localhost 2181 | grep imok"
            initialDelaySeconds: 60
            periodSeconds: 30
            timeoutSeconds: 10
            failureThreshold: 5
          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - "echo ruok | nc localhost 2181 | grep imok"
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 10
            failureThreshold: 5
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          securityContext:
            runAsUser: 1000
            runAsGroup: 1000
            allowPrivilegeEscalation: false
      volumes:
        - name: zookeeper-data
          emptyDir: {}
        - name: zookeeper-logs
          emptyDir: {}
        - name: tmp
          emptyDir: {}

---
apiVersion: v1
kind: Service
metadata:
  name: zookeeper-service
  namespace: payment-webhook
  labels:
    app.kubernetes.io/name: zookeeper
    app.kubernetes.io/managed-by: argocd
spec:
  type: ClusterIP
  ports:
    - name: client
      port: 2181
      targetPort: 2181
      protocol: TCP
  selector:
    app: zookeeper
EOF

# Apply the fixed configuration
kubectl apply -f /tmp/zookeeper-fixed.yaml

echo -e "\n${GREEN}Applied improved Zookeeper configuration. Waiting for pod to start...${NC}"

# Wait for the new pod
sleep 10

# Monitor the new pod
for i in {1..20}; do
    POD_STATUS=$(kubectl get pods -n ${NAMESPACE} -l app=zookeeper --no-headers | awk '{print $3}' | head -1)
    echo "Attempt $i/20 - Zookeeper status: $POD_STATUS"

    if [[ "$POD_STATUS" == "Running" ]]; then
        if kubectl wait --for=condition=ready pod -l app=zookeeper -n ${NAMESPACE} --timeout=30s; then
            echo -e "${GREEN}SUCCESS: Zookeeper is now running and ready!${NC}"
            break
        fi
    elif [[ "$POD_STATUS" == "CrashLoopBackOff" ]]; then
        echo -e "${RED}Still crashing. Checking logs...${NC}"
        kubectl logs -n ${NAMESPACE} -l app=zookeeper --tail=20

        if [[ $i -eq 5 ]]; then
            echo -e "${YELLOW}Trying alternative Zookeeper image...${NC}"
            kubectl patch deployment zookeeper -n ${NAMESPACE} -p '{"spec":{"template":{"spec":{"containers":[{"name":"zookeeper","image":"zookeeper:3.8"}]}}}}'
        fi
    fi

    if [[ $i -eq 20 ]]; then
        echo -e "${RED}Zookeeper still not working after 20 attempts${NC}"
        echo -e "${YELLOW}Final logs:${NC}"
        kubectl logs -n ${NAMESPACE} -l app=zookeeper --tail=30
        break
    fi

    sleep 15
done

# Clean up temp file
rm -f /tmp/zookeeper-fixed.yaml

echo -e "\n${BLUE}=== Current Status ===${NC}"
kubectl get pods -n ${NAMESPACE} -l app=zookeeper