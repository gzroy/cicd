pipeline {
  agent{
    kubernetes{
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          hostAliases:
          - ip: "169.254.169.254"
            hostnames:
              - metadata.google.internal
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
          - name: kaniko
            image: gcr.io/kaniko-project/executor:1.18.0-debug
            command:
            - sleep
            args:
            - 9999999
          serviceAccountName: "jenkins-sa"
        '''
    }
  }
  triggers {
    GenericTrigger(
      genericVariables: [
        [key: 'action', value: '$.action', expressionType: 'JSONPath'],
        [key: 'clone_url', value: '$.pull_request.base.repo.clone_url', expressionType: 'JSONPath'],
        [key: 'ref', value: '$.pull_request.head.ref', expressionType: 'JSONPath'],
        [key: 'name', value: '$.pull_request.base.repo.name', expressionType: 'JSONPath']
      ],
      token: 'abc'
    )
  }
  environment {
    CREDENTIAL = credentials("${CREDENTIAL_ID}")
    PACKAGE_STATUS = "success"
  }
  stages{
    stage("git checkout") {
      when {
        expression {
          return action=="closed"
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
    stage("package"){
      when {
        expression {
          return action=="closed"
        }
      }
      steps{
        container('maven') {
          script{
            sh 'mvn clean package'
            sh 'mkdir target/extracted'
            sh 'java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted'
          }
        }
      }
    }
    stage("Build image and push to registry") {
      when {
        expression {
          return action=="closed"
        }
      }
      steps {
        container('kaniko') {
          script {
            sh "/kaniko/executor --verbosity debug --context `pwd` --destination asia-southeast1-docker.pkg.dev/curious-athlete-401708/roy-repo/${name}:0.0.1"
          }
        }
      }
    }
  }
}