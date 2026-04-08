variable "aws_region" {
  default = "us-east-1"
}

variable "cluster_name" {
  default = "banking-eks-cluster"
}

variable "cluster_version" {
  default = "1.30"
}

variable "environment" {
  default = "production"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}