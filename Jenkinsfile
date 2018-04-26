pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args '-v /root/.m2:/root/.m2'
    }

  }
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