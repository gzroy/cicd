pipeline {
  agent{
    kubernetes{
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          volumes:
          - name: maven-pv-storage
            persistentVolumeClaim:
              claimName: maven-repo-storage
          containers:
          - name: maven
            image: maven:3.8.3-openjdk-17
            tty: true
            imagePullPolicy: "IfNotPresent"
            volumeMounts:
            - mountPath: '/root/.m2/repository' 
              name: maven-pv-storage
            command:
            - cat
        '''
    }
  }
  triggers {
    GenericTrigger(
      genericVariables: [
        [key: 'action', value: '$.action', expressionType: 'JSONPath'],
        [key: 'clone_url', value: '$.pull_request.base.repo.clone_url', expressionType: 'JSONPath'],
        [key: 'ref', value: '$.pull_request.head.ref', expressionType: 'JSONPath'],
        [key: 'sha', value: '$.pull_request.head.sha', expressionType: 'JSONPath'],
        [key: 'number', value: '$.number', expressionType: 'JSONPath'],
        [key: 'comments_url', value: '$.pull_request.comments_url', expressionType: 'JSONPath']
      ],
      token: 'abc'
    )
  }
  environment {
    CREDENTIAL = credentials("${CREDENTIAL_ID}")
  }
  stages{
    stage("git checkout") {
      when {
        expression {
          return action=="opened" || action=="synchronize"
        }
      }
      steps {
        script {
          git(
            url: clone_url,
            credentialsId: CREDENTIAL_ID,
            branch: ref
          )
        }
      }
    }
    stage("test"){
      when {
        expression {
          return action=="opened" || action=="synchronize"
        }
      }
      steps{
        container('maven') {
          script{
            sh 'mvn test '
          }
        }
      }
      post {
        failure {
          sh """
          (curl -L -X POST \
          -H \"Accept: application/vnd.github+json\" \
          -H \"Authorization: Bearer ${env.CREDENTIAL_PSW}\" \
          -H \"X-GitHub-Api-Version: 2022-11-28\" \
          ${comments_url} \
          -d \'{\"body\": \"UT test failure for commit ${sha}\"}\')
          """
        }
        success {
          sh """
          (curl -L -X POST \
          -H \"Accept: application/vnd.github+json\" \
          -H \"Authorization: Bearer ${env.CREDENTIAL_PSW}\" \
          -H \"X-GitHub-Api-Version: 2022-11-28\" \
          ${comments_url} \
          -d \'{\"body\": \"UT test success for commit ${sha}\"}\')
          """
        }
      }
    }
  }
}