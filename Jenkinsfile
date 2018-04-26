pipeline {
  agent any
  stages {
    stage('Build') {
      agent any
      steps {
        sh 'maven clean install'
      }
    }
  }
}