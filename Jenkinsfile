import java.nio.file.Path
import java.nio.file.Files

pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                sh "./gradlew assemble"
            }
        }
        stage("Check") {
            steps {
                sh "./gradlew test"
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
        stage("Archive") {
            steps {
                sh "./gradlew build"
                javadoc javadocDir: "build/docs/javadoc", keepAll: true
                archiveArtifacts artifacts: 'build/libs/*.jar,benchmark/build/libs/*.jar',
                                 allowEmptyArchive: true,
                                 fingerprint: true,
                                 onlyIfSuccessful: true
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publish"
            }
        }
    }
}

