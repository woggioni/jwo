import java.nio.file.Path
import java.nio.file.Files

pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                sh "./gradlew build"
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                    junit 'jmath/build/test-results/test/*.xml'
                }
                success {
                    jacoco(
                        execPattern: '**/build/jacoco/*.exec',
                        classPattern: '**/build/classes/java/main',
                        sourcePattern: '**/src/main'
                    )
                    javadoc javadocDir: "build/docs/javadoc", keepAll: true
                    archiveArtifacts artifacts: '**/build/libs/*.jar',
                                     allowEmptyArchive: false,
                                     fingerprint: true,
                                     onlyIfSuccessful: true
                }
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publish"
            }
        }
    }
}

