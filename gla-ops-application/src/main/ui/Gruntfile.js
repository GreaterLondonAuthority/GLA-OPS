/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

// Generated on 2016-07-28 using generator-angular 0.15.1

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

//Initialises env variables from .e nv file is file is present
require('dotenv').config({silent: true});
const escapeStringRegexp = require('escape-string-regexp');
const utils = require('./lib/grunt/utils.js');
const path = require('path');
const testConfig = require('./test/testConfig');
const glob = require('glob');
const fs = require('fs');


module.exports = (grunt) => {
  // grunt.loadNpmTasks('grunt-selenium-webdriver');
  grunt.loadNpmTasks('grunt-protractor-runner');
  grunt.loadNpmTasks('grunt-connect-proxy');
  grunt.loadNpmTasks('grunt-copyright');
  grunt.loadNpmTasks('grunt-zip');
  grunt.loadNpmTasks('grunt-browserify');
  grunt.loadNpmTasks('grunt-exec');


  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);
  let serveStatic = require('serve-static');

  // Automatically load required Grunt tasks
  require('jit-grunt')(grunt, {
    useminPrepare: 'grunt-usemin',
    ngtemplates: 'grunt-angular-templates',
    cdnify: 'grunt-google-cdn'
  });

  // Configurable paths for the application
  const appConfig = {
    app: 'app',
    dist: '../../../target/gla-ops'
  };

  // Define the configuration for all the tasks
  const config = {

    // Project settings
    yeoman: appConfig,

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      js: {
        //files: ['<%= yeoman.app %>/scripts/{,*/}*.js'],
        files: ['<%= yeoman.app %>/scripts/**/*.js'],
        tasks: [ //'newer:jshint:all',
          'newer:jscs:all'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      jsTest: {
        files: ['test/spec/{,*/}*.js'],
        tasks: [ //'newer:jshint:test',
          'newer:jscs:test', 'karma']
      },
      compass: {
        files: ['<%= yeoman.app %>/styles/{,*/}*.{scss,sass}',
          '<%= yeoman.app %>/scripts/**/*.{scss,sass}'],
        tasks: ['compass:server', 'postcss:server']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },

      html: {
        files: ['<%= yeoman.app %>/**/*.html'],
        tasks: ['ngtemplates:dev']
      },


      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '<%= yeoman.app %>/**/*.html',
          '.tmp/styles/{,*!/}*.css',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: '0.0.0.0',
        livereload: 35729
      },

      proxies: [
        {
          context: ['/api', '/sysops'],
          host: process.env.API_URL || 'ops-dev.london.gov.uk',
          port: process.env.API_PORT || 443,
          https: utils.isHttps(),
          xforward: false,
          changeOrigin: true,
          headers: {
            'host': process.env.API_URL || 'ops-dev.london.gov.uk'
          }
        }
      ],

      livereload: {
        options: {
          open: true,
          middleware: (connect) => {
            return [
              require('grunt-connect-proxy/lib/utils').proxyRequest,
              serveStatic('.tmp'),
              connect().use(
                '/app/styles',
                serveStatic('./app/styles')
              ),
              serveStatic(appConfig.app)
            ];
          }
        }
      },
      test: {
        options: {
          port: 9001,
          middleware: (connect) => {
            return [
              serveStatic('.tmp'),
              serveStatic('test'),
              serveStatic(appConfig.app)
            ];
          }
        }
      },
      dist: {
        options: {
          open: true,
          base: '<%= yeoman.dist %>'
        }
      }
    },

    // Make sure there are no obvious mistakes
    //    jshint: {
    //      options: {
    //        jshintrc: '.jshintrc',
    //        reporter: require('jshint-stylish')
    //      },
    //      all: {
    //        src: [
    //          'Gruntfile.js',
    //          '<%= yeoman.app %>/scripts/{,*/}*.js'
    //        ]
    //      },
    //      test: {
    //        options: {
    //          jshintrc: 'test/.jshintrc'
    //        },
    //        src: ['test/spec/{,*/}*.js']
    //      }
    //    },

    // Make sure code styles are up to par
    jscs: {
      options: {
        config: '.jscsrc'
      },
      all: {
        src: [
          //'Gruntfile.js',
          '<%= yeoman.app %>/scripts/**/*.js',
          '../../test/loadscripts/k6/**/*.js'
        ]
      },
      test: {
        src: ['test/spec/**/*.js']
      }
    },

    // Empties folders to start fresh
    clean: {
      options: {
        force: true
      },
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yeoman.dist %>/{,*/}*',
            '!<%= yeoman.dist %>/.git{,*/}*'
          ]
        }]
      },
      server: '.tmp'
    },

    // Add vendor prefixed styles
    postcss: {
      options: {
        processors: [
          require('autoprefixer-core')({
            browsers: ['last 1 version', 'safari >=4']
          })
        ]
      },
      server: {
        options: {
          map: true
        },
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      }
    },

    // Renames files for browser caching purposes
    filerev: {
      options: {
        // overriding process so that our files on the server use the Teamcity's build number instead
        process: (basename, name, extension) => {
          const timestamp = `t${new Date().getTime()}`;
          return `${basename}.${process.env.BUILD_NUMBER ?
            process.env.BUILD_NUMBER : timestamp}.${extension}`;
        },
      },
      dist: {
        src: [
          '<%= yeoman.dist %>/scripts/{,*/}*.js',
          '<%= yeoman.dist %>/styles/{,*/}*.css',
          '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
          '<%= yeoman.dist %>/styles/fonts/*'
        ]
      }
    },

    htmlmin: {
      dist: {
        options: {
          collapseWhitespace: true,
          conservativeCollapse: true,
          collapseBooleanAttributes: true,
          removeCommentsFromCDATA: true
        },
        files: [{
          expand: true,
          cwd: '<%= yeoman.dist %>',
          src: ['*.html'],
          dest: '<%= yeoman.dist %>'
        }]
      }
    },

    ngtemplates: {
      options: {
        module: 'GLA',
        htmlmin: '<%= htmlmin.dist.options %>',
      },

      dist: {
        cwd: '<%= yeoman.app %>',
        src: 'scripts/**/*.html',
        dest: '.tmp/scripts/templateCache.js'
      },

      dev: {
        cwd: '<%= ngtemplates.dist.cwd%>',
        src: '<%= ngtemplates.dist.src%>',
        dest: '<%= ngtemplates.dist.dest%>'
      }
    },

    // Test settings
    karma: {
      unit: {
        configFile: 'test/karma.conf.js',
        singleRun: true
      }
    },

    // Protractor settings
    protractor: {
      options: {
        //configFile: 'node_modules/protractor/referenceConf.js', // Default config file
        configFile: 'protractor-conf.js', // Default config file
        keepAlive: false, // If false, the grunt process stops when the test fails.
        noColor: false, // If true, protractor will not use colors in its output.
        args: {
          // Arguments passed to the command
        }
      },
      all: {},
      single: {
        options: {
          args: {
            capabilities: {
              shardTestFiles: false,
              maxInstances: 1,
              acceptInsecureCerts : true
            },
            cucumberOpts: {
              tags: testConfig.cucumberFilterTags(['@run'])
            }
          }
        }
      },
      filter: {
        options: {
          args: {
            cucumberOpts: {
              tags: testConfig.cucumberFilterTags()
            }
          }
        }
      }
    },

    copyright: {
      files: {
        src: [
          './app/scripts/**/*.js',
          './test/**/*.js',
          './lib/**/*.js',
          '!./lib/node_modules/**',
          './*.js'
        ]
      },
      options: {
        pattern: escapeStringRegexp(utils.copyrightText('../../../../LICENCE.txt'))
      }
    },

    zip: {
      'skip-files': {
        router: (filePath) => {
          const ignore = [
            'node',
            'node_modules',
            'bower_components',
            'chromedriver',
            'phantomjsdriver.log'
          ];
          const found = ignore.filter(item => {
            return filePath.split('/').indexOf(item) >= 0;
          }).length > 0;
          return found ? null : filePath;
        },
        src: ['./**'],
        dest: '../../../target/ui/gla-ui-src.zip'
      },
    },

    browserify: {
      options: {
        transform: [['babelify', {presets: ['es2015']}]],
      },

      dist: {
        src: ['app/scripts/app.js'],
        dest: '.tmp/scripts/app.js'
      },
      dev: {
        options: {
          watch: true,
          browserifyOptions: {
            debug: true
          }
        },
        files: [
          {
            expand: true,
            cwd: '<%= yeoman.app %>/scripts',
            src: ['**/*.js'],
            dest: '.tmp/scripts'
          }
        ]
      }
    },

    exec: {
      ngServe: {
        command: 'cd ../gla-ui && npm run serve'
      },

      ngBuild: {
        command: 'cd ../gla-ui && ./ng-build.sh'
      },

      ngBuildWin: {
        command: 'cd ..\\gla-ui && ng-build.sh'
      }
    }
  };
  grunt.initConfig(config);

  grunt.registerTask('serve', 'Compile then start a connect web server', (target) => {
    grunt.task.run([
      'exec:ngServe',
    ]);
  });

  grunt.registerTask('watchHtml', 'Build templateCache.js on html change', (target) => {
    grunt.task.run([
      'clean:server',
      'ngtemplates:dev',
      'watch:html'
    ]);
  });

  grunt.registerTask('test', [
    'clean:server',
    // 'wiredep',
    // 'concurrent:test',
    // 'postcss',
    'ngtemplates:dev',
    //'connect:test',
    'karma'
  ]);

  grunt.registerTask('e2e', 'Run integration tests', (target) => {
    let e2eConfig = grunt.option('single') ? 'single' : 'all';
    let filter = grunt.option('filter');
    if (filter) {
      e2eConfig = 'filter';
      const inclusions = [];
      const exclusions = [];
      filter.split(',').forEach(tag => {
        tag = tag.trim();
        if (tag.startsWith('~@')) {
          exclusions.push(tag)
        } else if (tag.startsWith('@')) {
          inclusions.push(tag)
        }
      });

      const args = config.protractor.filter.options.args;
      const cucumberOpts = config.protractor.filter.options.args.cucumberOpts;

      if (exclusions.length) {
        cucumberOpts.tags = cucumberOpts.tags.concat(exclusions);
      }
      if (inclusions.length) {
        cucumberOpts.tags.push(inclusions.join(','));
      }

      let files = glob.sync('test/features/**/*.feature');
      if (inclusions.length) {
        const annotationsRegex = new RegExp(inclusions.join('|'));
        files = files.filter(file => {
          let text = fs.readFileSync(file, 'utf8');
          return annotationsRegex.test(text);
        });
        args.specs = files;
      }

      console.log('cucumber tags:', cucumberOpts.tags);
    }
    const requiredVariables = [
      'ADMIN_USERNAME',
      'ADMIN_PASSWORD',
      'E2E_BASE_URL'
    ];

    const hasAllEnvVariablesSet = requiredVariables.some(envVariable => {
      return !!process.env[envVariable]
    });

    if (!hasAllEnvVariablesSet) {
      const errMessage = `Missing one of env variables: ${requiredVariables.join(', ')}.
      If you run locally you can override them inside .env file under the root ui directory `;
      throw new Error(errMessage);
    }

    grunt.task.run([
      `protractor:${e2eConfig}`,
    ]);
  });

  grunt.registerTask('build', [
    'clean:dist',
    'wiredep',
    'useminPrepare',
    'concurrent:dist',
    'postcss',
    'ngtemplates:dist',
    'browserify:dist',
    'concat',
    'copy:dist',
    //'cdnify',
    'cssmin',
    'uglify',
    'filerev',
    'usemin',
    'htmlmin',
    'zip'
  ]);

  //Deprecated: builds angular 1.7 application
  grunt.registerTask('default', [
    'jscs',
    'copyright',
    'test',
    'build'
  ]);

  //Builds angular 9 application which bootstraps angular 1.7 application and runs side by side.
  grunt.registerTask('ngBuild', [
    'jscs',
    'copyright',
    // 'test',
    'clean:dist',
    'ngtemplates:dist',
    'exec:ngBuild',
    'zip'
  ]);

  grunt.registerTask('ngBuildWin', [
    'jscs',
    'copyright',
    // 'test',
    'clean:dist',
    'ngtemplates:dist',
    'exec:ngBuildWin',
    'zip'
  ]);
};
