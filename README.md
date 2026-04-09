# 🏦 GitOps Banking Platform on AWS EKS

![AWS](https://img.shields.io/badge/AWS-EKS-orange?logo=amazon-aws)
![Terraform](https://img.shields.io/badge/IaC-Terraform-purple?logo=terraform)
![ArgoCD](https://img.shields.io/badge/GitOps-ArgoCD-blue?logo=argo)
![Jenkins](https://img.shields.io/badge/CI-Jenkins-red?logo=jenkins)
![Kubernetes](https://img.shields.io/badge/Kubernetes-1.30-blue?logo=kubernetes)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Helm](https://img.shields.io/badge/Package-Helm-blue?logo=helm)

A production-grade GitOps platform deploying a Banking Microservices application on AWS EKS with full CI/CD pipeline, observability, security scanning, and TLS certificate management.

---

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Prerequisites](#prerequisites)
- [Infrastructure Setup](#infrastructure-setup)
- [CI Pipeline - Jenkins](#ci-pipeline---jenkins)
- [CD Pipeline - ArgoCD](#cd-pipeline---argocd)
- [Monitoring & Observability](#monitoring--observability)
- [TLS & Security](#tls--security)
- [Live Endpoints](#live-endpoints)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CI PIPELINE                                │
│                                                                     │
│  Code Push → Gitleaks → Build & Test → SonarQube → Quality Gate    │
│      → Docker Build → Push to ECR → Update Helm Values             │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │ Git Push (Helm values updated)
┌─────────────────────────────────▼───────────────────────────────────┐
│                          CD PIPELINE                                │
│                                                                     │
│  GitHub (GitOps Repo) → ArgoCD Auto-Sync → Kubernetes Deploy       │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                        AWS EKS CLUSTER                              │
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │   banking    │  │  monitoring  │  │    argocd    │             │
│  │  namespace   │  │  namespace   │  │  namespace   │             │
│  │              │  │              │  │              │             │
│  │ account-svc  │  │ prometheus   │  │  argocd      │             │
│  │ transaction  │  │ grafana      │  │  server      │             │
│  │ transfer-svc │  │ alertmanager │  │              │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐                               │
│  │ cert-manager │  │ingress-nginx │                               │
│  │  namespace   │  │  namespace   │                               │
│  │              │  │              │                               │
│  │ cert-manager │  │nginx ingress │                               │
│  │              │  │ controller   │                               │
│  └──────────────┘  └──────────────┘                               │
└─────────────────────────────────────────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                         AWS SERVICES                                │
│                                                                     │
│   Route53 (DNS) │ ECR (Images) │ EBS (Storage) │ IAM (IRSA)       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
GITOPS_PROJECT_BANKING/
│
├── EKS-Banking/                              # Terraform Infrastructure
│   ├── main.tf                               # Providers configuration
│   ├── vpc.tf                                # VPC, subnets, NAT Gateway
│   ├── eks.tf                                # EKS cluster, addons, access entries
│   ├── nodes.tf                              # On-demand + Spot node groups
│   ├── iam.tf                                # IAM roles, OIDC provider, IRSA
│   ├── outputs.tf                            # Output values
│   ├── variables.tf                          # Variable definitions
│   └── terraform.tfvars                      # Variable values
│
└── banking-microservices/                    # Application & GitOps
    │
    ├── services/                             # Microservices source code
    │   ├── account-service/                  # Account management service
    │   │   ├── src/main/java/com/banking/
    │   │   │   ├── AccountApplication.java
    │   │   │   ├── AccountController.java
    │   │   │   └── Account.java
    │   │   ├── src/main/resources/
    │   │   │   └── application.yaml
    │   │   ├── Dockerfile
    │   │   └── pom.xml
    │   │
    │   ├── transaction-service/              # Transaction handling service
    │   │   ├── src/main/java/com/banking/
    │   │   │   ├── TransactionApplication.java
    │   │   │   ├── TransactionController.java
    │   │   │   └── Transaction.java
    │   │   ├── src/main/resources/
    │   │   │   └── application.yaml
    │   │   ├── Dockerfile
    │   │   └── pom.xml
    │   │
    │   └── transfer-service/                 # Transfer processing service
    │       ├── src/main/java/com/banking/
    │       │   ├── TransferApplication.java
    │       │   ├── TransferController.java
    │       │   └── Transfer.java
    │       ├── src/main/resources/
    │       │   └── application.yaml
    │       ├── Dockerfile
    │       └── pom.xml
    │
    ├── helm-charts/
    │   ├── banking-app/                      # Main application Helm chart
    │   │   ├── Chart.yaml
    │   │   ├── values.yaml
    │   │   └── templates/
    │   │       ├── deployment.yaml
    │   │       ├── service.yaml
    │   │       ├── ingress.yaml
    │   │       ├── configmap.yaml
    │   │       └── hpa.yaml
    │   │
    │   └── platforms-tools/                  # Infrastructure Helm charts
    │       ├── argocd/                        # ArgoCD installation
    │       ├── argocd-ingress/                # ArgoCD HTTPS ingress
    │       ├── cert-manager/                  # TLS certificate manager
    │       ├── cluster-configs/               # Cluster-level configs
    │       ├── ingress-nginx/                 # Nginx ingress controller
    │       ├── monitoring-ingress/            # Grafana/Prometheus ingress
    │       └── prometheus-stack/             # Prometheus + Grafana stack
    │
    ├── argocd/
    │   ├── projects/
    │   │   └── banking-project.yaml          # ArgoCD project definition
    │   └── applications/
    │       ├── banking-app.yaml              # Banking app ArgoCD application
    │       ├── cert-manager.yaml             # Cert-manager ArgoCD application
    │       ├── argocd-ingress.yaml           # ArgoCD ingress application
    │       └── monitoring-ingress.yaml       # Monitoring ingress application
    │
    └── jenkins/
        └── Jenkinsfile                       # CI pipeline definition
```

---

## 🛠️ Tech Stack

| Category | Tool | Version |
|---|---|---|
| Cloud | AWS EKS | - |
| IaC | Terraform | >= 1.0 |
| Kubernetes | EKS | 1.30 |
| CI | Jenkins | Latest |
| CD / GitOps | ArgoCD | v2.10.4 |
| Package Manager | Helm | v3 |
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.0 |
| Container Registry | AWS ECR | - |
| Monitoring | Prometheus + Grafana | kube-prometheus-stack 58.0.0 |
| Ingress | Nginx Ingress Controller | 1.10.0 |
| TLS | Cert-Manager | v1.14.0 |
| DNS | AWS Route53 | - |
| Storage | AWS EBS CSI Driver | v1.57.1 |
| Code Quality | SonarQube | Latest |
| Secret Scanning | Gitleaks | Latest |

---

## 🏦 Microservices

### Account Service
Manages bank accounts — create, retrieve accounts.

**Port:** `8080`

| Endpoint | Method | Description |
|---|---|---|
| `/api/accounts` | POST | Create a new account |
| `/api/accounts` | GET | Get all accounts |
| `/api/accounts/{id}` | GET | Get account by ID |
| `/api/accounts/health` | GET | Health check |

### Transaction Service
Handles financial transactions — credits and debits.

**Port:** `8081`

| Endpoint | Method | Description |
|---|---|---|
| `/api/transactions` | POST | Create a transaction |
| `/api/transactions` | GET | Get all transactions |
| `/api/transactions/{id}` | GET | Get transaction by ID |
| `/api/transactions/account/{id}` | GET | Get transactions by account |
| `/api/transactions/health` | GET | Health check |

### Transfer Service
Processes fund transfers between accounts.

**Port:** `8082`

| Endpoint | Method | Description |
|---|---|---|
| `/api/transfers` | POST | Create a transfer |
| `/api/transfers` | GET | Get all transfers |
| `/api/transfers/{id}` | GET | Get transfer by ID |
| `/api/transfers/from/{id}` | GET | Get transfers by source account |
| `/api/transfers/health` | GET | Health check |

---

## ✅ Prerequisites

- AWS Account with appropriate IAM permissions
- Terraform >= 1.0
- kubectl
- Helm v3
- AWS CLI v2
- Java 21 JDK
- Maven 3.9+
- Docker
- Git

---

## 🚀 Infrastructure Setup

### 1. Clone the repository
```bash
git clone https://github.com/zoumana001/GITOPS_PROJECT_BANKING.git
cd GITOPS_PROJECT_BANKING/EKS-Banking
```

### 2. Configure AWS credentials
```bash
aws configure
# Or use IAM instance role if running on EC2
```

### 3. Deploy EKS Cluster
```bash
terraform init
terraform plan
terraform apply
```

### 4. Configure kubectl
```bash
aws eks update-kubeconfig --region us-east-1 --name banking-eks-cluster
kubectl get nodes
```

### 5. Install Platform Tools

**ArgoCD:**
```bash
helm upgrade --install argocd \
  banking-microservices/helm-charts/platforms-tools/argocd \
  --namespace argocd \
  --create-namespace \
  --values banking-microservices/helm-charts/platforms-tools/argocd/values.yaml

# Get admin password
kubectl get secret argocd-initial-admin-secret \
  -n argocd \
  -o jsonpath="{.data.password}" | base64 -d
```

**Prometheus Stack:**
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm upgrade --install prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.adminPassword=your-password \
  --set grafana.service.type=LoadBalancer \
  --skip-crds
```

### 6. Apply ArgoCD Applications
```bash
kubectl apply -f banking-microservices/argocd/projects/
kubectl apply -f banking-microservices/argocd/applications/
```

---

## 🔄 CI Pipeline - Jenkins

The Jenkins pipeline runs these stages automatically on every code push:

```
Declarative: Tool Install
        ↓
    Checkout
        ↓
  Gitleaks Scan          ← Secret detection
        ↓
  Build & Test           ← Maven build + unit tests
        ↓
 SonarQube Analysis      ← Code quality analysis
        ↓
  Quality Gate           ← Pass/fail based on quality rules
        ↓
Build Docker Images      ← Build all 3 service images
        ↓
  Push to ECR            ← Push to AWS Elastic Container Registry
        ↓
Update Helm Values       ← Update image tags in values.yaml
        ↓
   Git Push              ← Triggers ArgoCD sync
```

### Jenkins Configuration

**Required credentials:**
- `aws-credentials` — AWS Access Key ID + Secret Access Key
- `git-credentials` — GitHub username + token
- `sonar-token` — SonarQube authentication token

**Required tools:**
- JDK: `jdk21` pointing to `/usr/lib/jvm/java-21-openjdk-amd64`
- Maven: `maven` version 3.9.x

---

## 🔁 CD Pipeline - ArgoCD

ArgoCD watches the GitHub repository and automatically syncs any changes to the cluster.

**GitOps Flow:**
```
Jenkins updates image tag in values.yaml
        ↓
Git commit + push to main branch
        ↓
ArgoCD detects change (polling every 3 minutes)
        ↓
ArgoCD applies Helm chart to Kubernetes
        ↓
New pods rolling deploy with zero downtime
```

**Applications managed by ArgoCD:**

| Application | Namespace | Description |
|---|---|---|
| banking-app | banking | Banking microservices |
| cert-manager | cert-manager | TLS certificate management |
| argocd-ingress | argocd | ArgoCD HTTPS ingress |
| monitoring-ingress | monitoring | Grafana/Prometheus ingress |
| ingress-nginx | ingress-nginx | Nginx ingress controller |
| cluster-configs | cert-manager | ClusterIssuers for Let's Encrypt |

---

## 📊 Monitoring & Observability

### Prometheus
Scrapes metrics from all cluster components and services.

**Targets monitored:**
- Kubernetes API servers
- Kubernetes nodes
- CoreDNS
- Kubelet
- kube-proxy
- Alertmanager
- Grafana
- All application pods via `/actuator/prometheus`

### Grafana
Pre-configured dashboards for:
- Kubernetes cluster overview
- Node resource usage
- Pod metrics
- API server performance
- Custom banking service dashboards

**Default credentials:** `admin / admin123`

### Spring Boot Actuator
All services expose metrics at `/actuator/prometheus` for Prometheus scraping.

---

## 🔒 TLS & Security

### Certificate Management
Cert-Manager automatically provisions and renews TLS certificates from Let's Encrypt.

**DNS01 Challenge** used with AWS Route53 for wildcard support.

**ClusterIssuers:**
- `letsencrypt-staging` — for testing
- `letsencrypt-prod` — for production

### IRSA (IAM Roles for Service Accounts)
Services use dedicated IAM roles instead of node-level permissions:

| Service | IAM Role | Permissions |
|---|---|---|
| EBS CSI Driver | `production-ebs-csi-role` | EBS volume management |
| Cert-Manager | `production-cert-manager-irsa-role` | Route53 DNS updates |

### Security Scanning
- **Gitleaks** — scans every commit for leaked secrets/credentials
- **SonarQube** — static code analysis, enforces quality gates
- **Private node groups** — worker nodes not directly accessible from internet

---

## 🌐 Live Endpoints

| Service | URL | Status |
|---|---|---|
| Account Service | `https://banking.zoumanas.com/api/accounts/health` | ✅ |
| Transaction Service | `https://banking.zoumanas.com/api/transactions/health` | ✅ |
| Transfer Service | `https://banking.zoumanas.com/api/transfers/health` | ✅ |
| Grafana | `https://grafana.zoumanas.com` | ✅ |
| Prometheus | `https://prometheus.zoumanas.com` | ✅ |
| ArgoCD | `https://argocd.zoumanas.com` | ✅ |

### Test the APIs
```bash
# Health checks
curl https://banking.zoumanas.com/api/accounts/health
curl https://banking.zoumanas.com/api/transactions/health
curl https://banking.zoumanas.com/api/transfers/health

# Create an account
curl -X POST https://banking.zoumanas.com/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"owner":"John Doe","type":"SAVINGS"}'

# Create a transaction
curl -X POST https://banking.zoumanas.com/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":"<account-id>","type":"CREDIT","amount":1000.00}'

# Create a transfer
curl -X POST https://banking.zoumanas.com/api/transfers \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId":"<id>","toAccountId":"<id>","amount":500.00}'
```

---

## 🔧 Troubleshooting

This section documents real issues encountered and their solutions during this project.

### 1. Terraform state conflicts
**Error:** `ResourceAlreadyExistsException` for CloudWatch Log Group, OIDC Provider

**Cause:** Resources already exist in AWS but not tracked in Terraform state.

**Fix:**
```bash
terraform import 'module.eks.aws_cloudwatch_log_group.this[0]' /aws/eks/cluster-name
terraform import 'module.eks.aws_iam_openid_connect_provider.oidc_provider[0]' <oidc-arn>
```

---

### 2. EKS cluster unreachable after creation
**Error:** `the server has asked for the client to provide credentials`

**Cause:** EC2 role not added to cluster access entries.

**Fix:**
```bash
aws eks create-access-entry \
  --cluster-name <cluster-name> \
  --principal-arn arn:aws:iam::<account>:role/<role-name>

aws eks associate-access-policy \
  --cluster-name <cluster-name> \
  --principal-arn arn:aws:iam::<account>:role/<role-name> \
  --policy-arn arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy \
  --access-scope type=cluster
```

---

### 3. EBS CSI Driver CrashLoopBackOff
**Error:** Controller pods crashing with permission errors

**Cause:** Missing IRSA role for EBS CSI driver.

**Fix:** Create dedicated IAM role with OIDC trust policy:
```bash
# Create trust policy with OIDC condition for ebs-csi-controller-sa
# Attach AmazonEBSCSIDriverPolicy
# Annotate service account with role ARN
kubectl annotate serviceaccount ebs-csi-controller-sa \
  -n kube-system \
  eks.amazonaws.com/role-arn=arn:aws:iam::<account>:role/ebs-csi-role
```

---

### 4. Kyverno blocking all deployments
**Error:** `admission webhook denied the request: label 'app' and 'environment' are required`

**Cause:** Kyverno ClusterPolicy enforcing labels on all pods.

**Fix:** Exclude system namespaces from policy:
```yaml
exclude:
  any:
  - resources:
      namespaces:
      - kube-system
      - monitoring
      - logging
      - kyverno
```

---

### 5. Prometheus CRD annotation too large
**Error:** `metadata.annotations: Too long: must have at most 262144 bytes`

**Cause:** kube-prometheus-stack CRDs exceed Kubernetes annotation limit.

**Fix:** Install CRDs with server-side apply, then use `skipCrds: true` in ArgoCD:
```bash
kubectl apply --server-side --force-conflicts \
  -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_prometheuses.yaml
```

```yaml
# In ArgoCD application
spec:
  source:
    helm:
      skipCrds: true
  syncPolicy:
    syncOptions:
      - ServerSideApply=true
```

---

### 6. Cert-Manager DNS01 challenge failing
**Error:** `AccessDenied: not authorized to perform route53:ChangeResourceRecordSets`

**Cause:** cert-manager IRSA role missing Route53 permissions.

**Fix:**
```bash
aws iam put-role-policy \
  --role-name <cert-manager-irsa-role> \
  --policy-name cert-manager-route53 \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Action": [
        "route53:GetChange",
        "route53:ChangeResourceRecordSets",
        "route53:ListHostedZones",
        "route53:ListResourceRecordSets"
      ],
      "Resource": ["arn:aws:route53:::hostedzone/*", "arn:aws:route53:::change/*"]
    }]
  }'
```

---

### 7. Spring Boot pods CrashLoopBackOff
**Error:** Readiness probe failing immediately

**Cause:** Probe firing before Spring Boot finished starting (Spring Boot needs ~20s to start).

**Fix:** Use actuator health endpoint and increase delays:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health    # not custom /health endpoint
    port: 8080
  initialDelaySeconds: 60     # Spring Boot needs time to start
  periodSeconds: 15
  failureThreshold: 5
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 45
  periodSeconds: 10
  failureThreshold: 5
```

---

### 8. Jenkins Maven build failing with Java version error
**Error:** `Fatal error compiling: error: release version 21 not supported`

**Cause:** Jenkins using wrong JDK version.

**Fix:**
```bash
# Install Java 21 JDK (not just JRE)
sudo apt install -y openjdk-21-jdk

# Configure in Jenkins Global Tool Configuration:
# Name: jdk21
# JAVA_HOME: /usr/lib/jvm/java-21-openjdk-amd64
# Uncheck "Install automatically"
```

---

### 9. ArgoCD app path does not exist
**Error:** `Manifest generation error: app path does not exist`

**Cause:** ArgoCD application pointing to wrong path in repository.

**Fix:** Verify exact path structure in GitHub and update application manifest:
```bash
git ls-files helm-charts/platforms-tools/monitoring-ingress/
# Use exact path shown in git ls-files output
```

---

### 10. ArgoCD HTTPS not working
**Error:** `PR_END_OF_FILE_ERROR` in browser

**Cause:** ArgoCD server running with TLS but nginx also doing TLS termination.

**Fix:** Run ArgoCD in insecure mode (nginx handles TLS):
```bash
kubectl patch configmap argocd-cmd-params-cm -n argocd \
  --type merge \
  -p '{"data":{"server.insecure":"true"}}'

kubectl rollout restart deployment/argocd-server -n argocd
```

---

## 📝 Environment Variables

| Variable | Description | Example |
|---|---|---|
| `AWS_REGION` | AWS deployment region | `us-east-1` |
| `ECR_REGISTRY` | ECR registry URL | `064032723992.dkr.ecr.us-east-1.amazonaws.com` |
| `IMAGE_TAG` | Docker image tag | `${BUILD_NUMBER}` |
| `CLUSTER_NAME` | EKS cluster name | `banking-eks-cluster` |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 👨‍💻 Author

**Zoumana** — DevOps / DevSecOps Engineer

- GitHub: [@zoumana001](https://github.com/zoumana001)
- LinkedIn: [Connect with me](https://linkedin.com)

---

## 📄 License

This project is licensed under the MIT License.

---

*Built with ❤️ to showcase production-grade DevOps practices*
