#!/usr/bin/env bash
set -e

#
# functions.sh
# Some utility functions to assist in bringing up our cloudformation stacks
#

# Set some defaults
export AWS_DEFAULT_REGION=${AWS_REGION:-"eu-west-1"}
AWSPATH="aws --region ${AWS_DEFAULT_REGION}"
CF_PACKAGE="omt-all-packaged.yml"
TEST=false

die () {
    local message=$1
    echo "ERROR: $message" >&2
    echo "ABORTING" >&2
    exit 1
}

create_s3_bucket () {
    local bucket_suffix=$1
    local bucket_name="$(cat /dev/urandom | strings | grep -o '[[:alpha:]]' | tr A-Z a-z | head -n 12 | tr -d '\n').${bucket_suffix}"
    ${AWSPATH} s3api create-bucket --create-bucket-configuration LocationConstraint=${AWS_DEFAULT_REGION} --bucket ${bucket_name} >/dev/null || return 1
	  ${AWSPATH} s3api wait bucket-exists --bucket ${bucket_name} >/dev/null
    echo $bucket_name
}

delete_s3_bucket () {
    local bucket_name=$1
    ${AWSPATH} s3api head-bucket --bucket ${bucket_name} >/dev/null 2>&1 || return 0
    ${AWSPATH} s3 rm --recursive s3://${bucket_name}/
	  ${AWSPATH} s3api delete-bucket --bucket ${bucket_name}
}

clean_s3_bucket () {
  local bucket_name=$1
  local bucket_prefix=$2
  if [ -z "$bucket_name" -o -z "$bucket_prefix" ]; then
    echo "Bucket name and bucket prefix must be set when running clean_s3_bucket()!"
    return 1
  else
    ${AWSPATH} s3 rm --recursive s3://${bucket_name}/$bucket_prefix
  fi
}

validate () {
    for template in $1
    do
      echo -n "Validating $template... "
      ${AWSPATH} cloudformation validate-template --template-body file://$template 1>/dev/null
      if [ $? -eq 0 ]; then
        echo "OK."
      fi
    done
}

stack_exists () {
    local STACK_NAME=$1
    ${AWSPATH} cloudformation describe-stacks --stack-name ${STACK_NAME} > /dev/null 2>&1
}

# Package the cloudformation resources
package () {

    local TEMPLATE_ENTRY=$1
    local S3_BUCKET=$2
    local S3_BUCKET_PREFIX=$3

    OUT=$( ${AWSPATH} cloudformation package --template-file ${TEMPLATE_ENTRY} --s3-bucket ${S3_BUCKET} --s3-prefix deploy-${S3_BUCKET_PREFIX} --output-template-file $CF_PACKAGE 2>&1 )
    RES=$?

    if echo "$OUT" | grep -q "Invalid"; then
      return 1
    fi

    echo $CF_PACKAGE

}

# Create or update the cloudformation stack
create_or_update_stack () {

    local STACK_NAME=$1
    local TEMPLATE=$2
    local PARAMETERS=$3
    local ACTION="create"

    if stack_exists $STACK_NAME; then
        echo "INFO: Stack $STACK_NAME exists. Updating the stack."
        ACTION="update"
    else
        echo "INFO: Stack $STACK_NAME does not exist. Creating a new stack."
    fi

    echo "${AWSPATH} cloudformation ${ACTION}-stack \\"
    echo -e "\t--stack-name ${STACK_NAME} \\"
    echo -e "\t--template-body file://${TEMPLATE} \\"
    echo -e "\t--parameters file://${PARAMETERS} \\"
    echo -e "\t--capabilities CAPABILITY_IAM 2>&1"

    if $TEST; then
        echo "TEST MODE: Not running $ACTION"
        return 0
    fi

    ${AWSPATH} cloudformation ${ACTION}-stack \
        --stack-name ${STACK_NAME} \
        --template-body file://${TEMPLATE} \
        --parameters file://${PARAMETERS} \
        --capabilities CAPABILITY_IAM 2>&1
    RES=$?

    if [ $RES -ne 0 ] && echo "$OUT" | grep -q "No updates"; then
        echo "$OUT"
    elif [ $RES -ne 0 ]; then
        echo "$OUT" && exit $RES
    else
        echo Stack $ACTION in progress, waiting ...
        ${AWSPATH} cloudformation wait stack-$ACTION-complete \
            --stack-name ${STACK_NAME} \
            && echo Stack $ACTION complete
    fi

}

# Delete the cloudformation stack
delete_stack() {

    local STACK=$1

    if ! stack_exists $STACK; then
        echo Stack does not exist, nothing to do
        return 0
    fi

    echo -e "${AWSPATH} cloudformation delete-stack \\"
    echo -e "\t--stack-name ${STACK}"

    if $test; then
        echo TEST MODE: Not running delete
        return 0
    fi

    ${AWSPATH} cloudformation delete-stack --stack-name ${STACK}

    echo Deleting stack $STACK ...
    if ${AWSPATH} cloudformation wait stack-delete-complete --stack-name ${STACK}; then
        echo Stack $STACK deletion complete
    else
        echo Could not delete $STACK
        exit 1
    fi

}

run_task() {

  local TASK_NAME=$1
  local CLUSTER_NAME=$2

  ${AWSPATH} ecs run-task --cluster ${CLUSTER_NAME} --task-definition ${TASK_NAME}

}
