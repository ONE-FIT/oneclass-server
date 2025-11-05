# main.tf

# AWS 제공자 설정
provider "aws" {
  region = "ap-northeast-2"  # 서울 리전
}

# EC2 인스턴스 리소스 정의
resource "aws_instance" "example" {
  ami           = "ami-0a71e3eb8b23101ed"  # 사용할 AMI ID (이건 예시로, 실제 AMI ID를 사용해야 함)
  instance_type = "t3.micro"  # EC2 인스턴스 유형

  # EC2 인스턴스의 태그
  tags = {
    Name = "oneclass-server"
  }
}
