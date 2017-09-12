#!/usr/bin/env bash

set -ev

readonly versions=$(curl https://api.bintray.com/packages/dkorotych/lazybones-templates/maven-quickstart-template/ | jq -r .versions[])
readonly templates="$HOME/.lazybones/templates/"

mkdir -p $templates
cd $templates

for templateVersion in $versions
do
	wget -c https://dl.bintray.com/dkorotych/lazybones-templates/maven-quickstart-template-${templateVersion}.zip
done
