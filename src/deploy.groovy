pipeline {
  agent{
    kubernetes{
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: terraform
            image: hashicorp/terraform:1.6
            tty: true
            imagePullPolicy: "IfNotPresent"
            command:
            - cat
          serviceAccountName: "jenkins-sa"
      '''
    }
  }
  tools {
    git "Default"
  }
  stages{
    stage("git checkout") {
      steps {
        script {
          git(
            url: 'https://github.com/gzroy/cicd.git',
            credentialsId: CREDENTIAL_ID,
            branch: "master"
          )
        }
      }
    }
    stage("terraform init") {
      steps {
        container('terraform') {
          script {
            sh 'terraform init'
          }
        }
      }
    }
    stage("terraform plan") {
      steps {
        container('terraform') {
          script {
            sh 'terraform plan'
          }
        }
      }
    }
    stage("terraform apply") {
      steps {
        container('terraform') {
          script {
            sh 'terraform apply -input=false -auto-approve'
          }
        }
      }
    }
  }
}
