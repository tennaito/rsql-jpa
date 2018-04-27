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
  }
}