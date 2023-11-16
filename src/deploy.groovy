pipeline {
  agent{
    kubernetes{
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: terraform
            image: hashicorp/terraform:latest
            tty: true
            imagePullPolicy: "IfNotPresent"
            command:
            - cat
          serviceAccountName: "jenkins-sa"
      '''
    }
  }
  stages{
    stage("git checkout") {
      steps {
        script {
          git(
            url: 'https://github.com/gzroy/cicd.git',
            credentialsId: CREDENTIAL_ID,
            branch: "main"
          )
        }
      }
    }
    stage("terraform init") {
      steps {
        sh 'terraform version'
        sh 'terraform init'
      }
    }
    stage("terraform plan") {
      steps {
        sh 'terraform plan'
      }
    }
  }
}
