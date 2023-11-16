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

  }
}
