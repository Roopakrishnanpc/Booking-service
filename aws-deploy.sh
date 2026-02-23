# 1. Build Docker Image
docker build -t booking-service .

# 2. Tag for ECR
docker tag booking-service:latest <your-account-id>.dkr.ecr.ap-south-1.amazonaws.com/booking-service:latest

# 3. Login to ECR
aws ecr get-login-password --region ap-south-1 | \
docker login --username AWS --password-stdin <your-account-id>.dkr.ecr.ap-south-1.amazonaws.com

# 4. Push to ECR
docker push <your-account-id>.dkr.ecr.ap-south-1.amazonaws.com/booking-service:latest

# 5. Deploy to EKS
kubectl apply -f k8s/
	#•	ECR → stores Docker images
	#•	EKS → runs Kubernetes cluster
	#•	ELB → created automatically for LoadBalancer service
	#•	RDS → production MySQL
	#•	IAM Roles → secure pod access
	#•	CloudWatch → centralized logging
# Build Image
docker build -t booking-service .

# Login to Azure Container Registry
az acr login --name youracrname

# Tag
docker tag booking-service youracrname.azurecr.io/booking-service:latest

# Push
docker push youracrname.azurecr.io/booking-service:latest

# Deploy to AKS
kubectl apply -f k8s/

	#•	ACR → container registry
	#•	AKS → Kubernetes cluster
	#•	Azure Load Balancer → auto created
	#•	Azure SQL → managed DB
	#•	Azure Monitor → logging
	#•	Managed Identity → secure access