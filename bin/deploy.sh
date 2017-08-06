#!/bin/bash
cd ..
rm -rf target
mvn package
cp target/*-1.0-SNAPSHOT.jar target/libs
mv target/libs target/connect-facebook