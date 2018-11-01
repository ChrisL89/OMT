#!/usr/bin/env groovy

@Library('potatocannon-global')
import au.com.tabcorp.potatocannon.*

def REPO_HOST = "redbook-docker-dev.artifacts.tabdigital.com.au"
def PROD_REPO_HOST = "redbook-docker-prod.artifacts.tabdigital.com.au"
def BUILD_BOT_USER = "redbook-build-bot"
def BUILD_BOT_ENCRYPTED_PASS = "AQECAHgL/SjTOdR1KnWVv3wnf7JbKn5qd8Q77j0Ak2IhoGmaJAAAAKswgagGCSqGSIb3DQEHBqCBmjCBlwIBADCBkQYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAzift4olOfb2VBF3Q4CARCAZKMzFVDQPGJ9tit7FGXP8kShSErKUeHmhA/zGHmuWGShHccs16+dfQV1LC6RWkIhvSJrxz4tns8gf/e9B+y7LuoCPA617O522Go5fbB6gsM45YCQM/v0UyP82cymc084krTdmBU="
def AD_BOT_USER = "gen_titans_rw_ghe"
def AD_BOT_ENCRYPTED_PASS = "AQECAHhggPTequCEJZiWje2nomwraogaydeiw6VFPgL4Kmh9JQAAAGwwagYJKoZIhvcNAQcGoF0wWwIBADBWBgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDCCsrWXfe+eXVkoS7AIBEIApEnttua6ZgpUHHwtWhVGSzGvOilBmdywaEOhbiscnB2MvK3txA6DW4FY="
def IMAGE_NAME = "offer-management"
def IMAGE_TAG = "build-volatile"

node('java18') {
    try {
        checkout scm
        def plaintextBuildBotPass = kmsDecrypt { ciphertext = BUILD_BOT_ENCRYPTED_PASS }
        def plaintextAdBotPass = kmsDecrypt { ciphertext = AD_BOT_ENCRYPTED_PASS }

        addDockerAuth {
            repo = "redbook-docker-dev.artifacts.tabdigital.com.au"
            username = "${BUILD_BOT_USER}"
            password = "${plaintextBuildBotPass}"
        }

        addComposerAuth {
            packagist = "artifacts.tabdigital.com.au"
            username = "${BUILD_BOT_USER}"
            password = "${BUILD_BOT_ENCRYPTED_PASS}"
        }

        sh 'git rev-parse --abbrev-ref HEAD > branch_name'
        sh 'git remote get-url origin > git_url'
        sh 'git rev-parse HEAD > commit_long'
        sh 'git rev-parse --short HEAD > commit_short'

        def OMT_ROOTDIR="/u01/deploy/current"          // directory to deploy offer_management
        def OMT_VERSION = "${env.BRANCH_NAME}" + '-' + System.currentTimeMillis()
        def CURRENT_BUILD_NUMBER = getBuildTimestamp {}
        def CURRENT_BUILD_NAME = getBuildName {}
        def GIT_BRANCH = readFile('branch_name').trim()
        def GIT_URL = readFile('git_url').trim()
        def GIT_COMMIT = readFile('commit_long').trim()
        def GIT_COMMIT_SHORT = readFile('commit_short').trim()

        withEnv([
                "OMT_ROOTDIR=${OMT_ROOTDIR}",
                "OMT_VERSION=${OMT_VERSION}",
                "REPO_HOST=${REPO_HOST}",
                "PROD_REPO_HOST=${PROD_REPO_HOST}",
                "CURRENT_BUILD_NUMBER=${CURRENT_BUILD_NUMBER}",
                "CURRENT_BUILD_NAME=${CURRENT_BUILD_NAME}",
                "GIT_BRANCH=${GIT_BRANCH}",
                "GIT_URL=${GIT_URL}",
                "GIT_COMMIT=${GIT_COMMIT}",
                "GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT}"
        ]){
            stage('Test') {
                sh "mvn clean test"
            }

            stage('Build') {
                sh "mvn package -DskipTests"
            }

            stage('Package') {
                sh """docker build \
                    --build-arg CONTAINER_NAME='${IMAGE_NAME}' \
                    --build-arg BUILD_NUMBER='${CURRENT_BUILD_NUMBER}' \
                    --build-arg BUILD_NAME='${CURRENT_BUILD_NAME}' \
                    --build-arg GIT_REPO='${GIT_URL}' \
                    --build-arg GIT_BRANCH='${GIT_BRANCH}' \
                    --build-arg GIT_COMMIT='${GIT_COMMIT}' \
                    --build-arg GIT_COMMIT_SHORT='${GIT_COMMIT_SHORT}' \
                    -t ${IMAGE_NAME}:${IMAGE_TAG} ."""
            }

            stage('Publish') {
                // get version from maven
                def projectVersion = sh(script: "mvn help:evaluate -Dexpression=project.version -B | grep -e '^[^\\[]'", returnStdout: true).trim()

                // generate a unique image version number
                def imageVersion = projectVersion + '-' + System.currentTimeMillis()

                // publish Docker image
                sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${REPO_HOST}/${IMAGE_NAME}:${imageVersion}"
                sh "docker push  ${REPO_HOST}/${IMAGE_NAME}:${imageVersion}"

                if (env.BRANCH_NAME == 'master') {
                    stage('Promote') {

                        addDockerAuth {
                            repo = "${PROD_REPO_HOST}"
                            username = "${BUILD_BOT_USER}"
                            password = "${BUILD_BOT_ENCRYPTED_PASS}"
                        }

                        sh "docker tag ${REPO_HOST}/${IMAGE_NAME}:${imageVersion} ${PROD_REPO_HOST}/${IMAGE_NAME}:${imageVersion}"
                        sh "docker push ${PROD_REPO_HOST}/${IMAGE_NAME}:${imageVersion}"
                    }
                }
            }

            slackSuccess {
                message = "Offer Management build have passed! :hooray:"
                channel = "#omt-builds"
            }
        }
    } catch (e) {
        slackFail {
            message = "Oh noes! The Wagerplayer build failed!"
            channel = "#omt-builds"
        }
        println "THE BUILD FAILED"
        throw e
    }
}