#!/usr/bin/env bash

cd ../

echo "Building BurkeyShop2..."
rm -r target
mvn clean package

echo "Copying to run/"
mkdir run/plugins/
cp target/*.jar run/plugins/burkeyshop2-1.12.2_VERSION.jar