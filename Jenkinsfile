pipeline {
  agent any
  stages {
    stage('Build') {
      agent any
      steps {
        sh 'mvn clean install'
      }
    }
    stage('input') {
      steps {
        input(message: 'test', id: 'id', ok: 'ij')
      }
    }
    stage('coiso') {
      parallel {
        stage('coiso') {
          steps {
            echo 'teste'
          }
        }
        stage('error') {
          steps {
            echo 'teste2'
            sh 'exit -1'
          }
        }
      }
    }
  }
}