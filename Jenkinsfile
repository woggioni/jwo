import java.nio.file.Path
import java.nio.file.Files

pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                sh "./gradlew clean build"
                junit testResults: "build/test-results/test/*.xml"
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

