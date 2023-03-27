provider "aws" {
  region = "us-east-1"
}

resource "aws_s3_bucket" "my-bucket" {
  bucket = "akanksha-tf-gurukul"

  tags = {
    Name="gurukul-a"
  }
}
