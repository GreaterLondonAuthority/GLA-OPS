echo Java files
find src/main/java -name *.java -type f | wc -l

echo JavaScript files
find src/main/ui/app -name *.js -type f | wc -l

echo Html files
find src/main/ui/app -name *.html -type f | wc -l
