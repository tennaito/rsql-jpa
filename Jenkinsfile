pipeline {
  agent any
  stages {
    stage('Build') {
      agent {
        docker {
          args '-v /root/.m2:/root/.m2'
          image 'maven:3-alpine'
        }

      }
      steps {
        sh 'maven clean install'
      }
    }
  }
}