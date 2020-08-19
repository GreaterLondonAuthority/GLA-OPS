
::check if driver exists
if exist chromedriver (
)
else (
    :: if not download and unzip it
    wget http://chromedriver.storage.googleapis.com/2.23/chromedriver_win32.zip
    unzip chromedriver_win32.zip
)

:: run selenium tests
mvn -Dtest=*Feature test

cd src\main\ui
grunt e2e
