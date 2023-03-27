terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
  backend "s3" {
    bucket = "akanksha-tf-gurukul"
    key    = "gurukul.tfstate"
    region = "us-east-1"
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "us-east-1"
}

data "aws_key_pair" "akanksha_key" {
  key_name           = "akanksha key pair"
  include_public_key = true
}

output "fingerprint" {
  value = data.aws_key_pair.akanksha_key.fingerprint
}

output "name" {
  value = data.aws_key_pair.akanksha_key.key_name
}

output "id" {
  value = data.aws_key_pair.akanksha_key.id
}

data "aws_vpc" "selected" {
  id = "vpc-019c09a1a0c5b4f6b"
}

resource "aws_subnet" "main" {
  vpc_id     = data.aws_vpc.selected.id
  cidr_block = "10.0.0.192/28"
  tags       = {
    Name = "akanksha-gurukul"
  }
}

resource "aws_security_group" "akanksha_tls" {
  name        = "akanksha_tls"
  description = "Allow TLS inbound traffic"
  vpc_id      = data.aws_vpc.selected.id


  ingress {
    description = "TLS from VPC"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Name = "akanksha-tls"
  }
}

resource "aws_instance" "app_server" {
  ami                         = "ami-00c39f71452c08778"
  instance_type               = "t2.micro"
  key_name                    = data.aws_key_pair.akanksha_key.key_name
  vpc_security_group_ids      = [aws_security_group.akanksha_tls.id]
  subnet_id                   = aws_subnet.main.id
  associate_public_ip_address = true

  tags = {
    Name = "akanksha-gurukul-esop"
  }
}
