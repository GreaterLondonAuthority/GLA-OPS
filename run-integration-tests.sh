
# check if driver exists
if ! [ -f chromedriver ] ; then
    # if not download and unzip it
    if [[ "$OSTYPE" == "darwin"* ]]; then
        curl -O http://chromedriver.storage.googleapis.com/2.23/chromedriver_mac32.zip
        unzip chromedriver_mac32.zip
    else
        wget http://chromedriver.storage.googleapis.com/2.23/chromedriver_linux32.zip
        unzip chromedriver_linux32.zip
    fi
fi

chmod u+x chromedriver

# run selenium tests
#mvn -Dtest=*Feature test

cd src/main/ui
ADMIN_USERNAME=$1 ADMIN_PASSWORD=$2 grunt e2e
cd ../../..

# create JUnit format test report for TeamCity reporting

mkdir ./target/ui
if [ -f target/ui/cucumber_report.json ] ; then
    cat ./target/ui/cucumber_report.json | src/main/ui/node_modules/.bin/cucumber-junit > ./target/ui/cucumber_report.xml;
fi
cp ./src/test/resources/empty-junit-report.xml ./target/ui/cucumber_report1.xml
cp ./src/test/resources/cucumber-junit-sample.xml ./target/ui/cucumber_report2.xml
