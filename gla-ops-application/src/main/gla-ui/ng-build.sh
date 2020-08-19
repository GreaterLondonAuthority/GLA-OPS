npm install
npm install -g @angular/cli
NG_BUILD_MANGLE=false ./node_modules/\@angular/cli/bin/ng build --prod
cp -r dist/gla/ ../../../target/gla-ops/
