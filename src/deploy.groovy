pipeline {
  agent{
    kubernetes{
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
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
        sh 'which git'
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
