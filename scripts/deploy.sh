#!/usr/bin/env bash
set -e

#
# deploy.sh
# Responsible for creation and update of the OMT stack
#

# Set some defaults
LOCATION=$( dirname $0 ) # current execution directory
DEPLOY_BUCKET_SUFFIX="sunppw.in.cld" # the bucket suffix to push packaged templates to
CREATE_BUCKET=false

# Load global function library
if [ ! -f "$LOCATION/functions.sh" ]; then
    echo "$0: Cannot find library functions.sh"
    exit 1
fi
. "$LOCATION/functions.sh"

usage () {
    local message=$1
    [ -n "$message" ] && echo "INVALID PARAMETERS: $message"
    echo -e "usage: $0 [installation_name] [config_location] [deployment_bucket]"
    echo -e "installation_name: the unique identifier for the stack"
    echo -e "config_location: the path of the configuration file you'd like to deploy the stack with (defaults to './config/parameters.json')"
    echo -e "deployment_bucket (optional): persistent bucket to deploy cloudformation templates to"
    exit 1
}

cleanup () {

    echo "Cleaning up..."

    if [[ $CREATE_BUCKET == "true" ]]; then
      echo "Removing deployment bucket"
      delete_s3_bucket $S3_BUCKET_NAME
    else
      echo "Cleaning up deployment bucket"
      clean_s3_bucket $S3_BUCKET_NAME deploy-$STACK_NAME
    fi

    rm $PKG_TEMPLATE $PKG_CONFIG 2&>/dev/null

}

# Validate CLI parameters and capture STACK_NAME and CONFIG_LOCATION
if [[ -z $1 ]]; then
  usage "Argument 1: installation name must be set."
fi
if [[ -z $2 ]]; then
  usage "Argument 2: config_location must be set."
fi
if [ -z $3 ]; then
  if [ "$CREATE_BUCKET" == "false" ]; then
    usage "Argument 3: deployment_bucket must be set if CREATE_BUCKET is false"
  fi
fi
CONFIG_LOCATION=$2
STACK_NAME=$1

# Cleanup on exit (so we don't leave buckets dangling around)
trap cleanup EXIT

# Validate Templates
echo "Validating templates..."
validate ./cloudformation/\* # you have to escape "*" to perform a directory loop

# Create S3 Bucket to Manage Deployment or use existing deploy bucket
if [[ $CREATE_BUCKET == "true" ]]; then

  echo "Creating deployment bucket..."
  S3_BUCKET_NAME=$(create_s3_bucket $DEPLOY_BUCKET_SUFFIX) || die "Unable to create deployment bucket!"
  echo "$S3_BUCKET_NAME created."

else

  echo "Using deployment bucket $3"
  S3_BUCKET_NAME=$3

fi

# Package up Cloudformation Templates and Parameters for Deployment
PKG_TEMPLATE=$(package ./cloudformation/omt-all.yml $S3_BUCKET_NAME $STACK_NAME) || die "Unable to create deployment package!"

# Deploy Stack
create_or_update_stack $STACK_NAME $PKG_TEMPLATE $CONFIG_LOCATION
