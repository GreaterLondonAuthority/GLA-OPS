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
    app: require('./bower.json').appPath || 'app',
    dist: '../../../target/gla-ops'
  };

  // Define the configuration for all the tasks
  const config = {

    // Project settings
    yeoman: appConfig,

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
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
                '/bower_components',
                serveStatic('./bower_components')
              ),
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
              connect().use(
                '/bower_components',
                serveStatic('./bower_components')
              ),
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
          '<%= yeoman.app %>/scripts/**/*.js'
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

    // Automatically inject Bower components into the app
    wiredep: {
      app: {
        src: ['<%= yeoman.app %>/index.html'],
        ignorePath: /\.\.\//
      },
      test: {
        devDependencies: true,
        src: '<%= karma.unit.configFile %>',
        ignorePath: /\.\.\//,
        fileTypes: {
          js: {
            block: /(([\s\t]*)\/{2}\s*?bower:\s*?(\S*))(\n|\r|.)*?(\/{2}\s*endbower)/gi,
            detect: {
              js: /'(.*\.js)'/gi
            },
            replace: {
              js: '\'{{filePath}}\','
            }
          }
        }
      },
      sass: {
        src: ['<%= yeoman.app %>/styles/{,*/}*.{scss,sass}'],
        ignorePath: /(\.\.\/){1,2}bower_components\//
      }
    },

    // Compiles Sass to CSS and generates necessary files if requested
    compass: {
      options: {
        sassDir: '<%= yeoman.app %>/styles',
        cssDir: '.tmp/styles',
        generatedImagesDir: '.tmp/images/generated',
        imagesDir: '<%= yeoman.app %>/images',
        javascriptsDir: '<%= yeoman.app %>/scripts',
        fontsDir: '<%= yeoman.app %>/styles/fonts',
        importPath: './bower_components',
        httpImagesPath: '/images',
        httpGeneratedImagesPath: '/images/generated',
        httpFontsPath: '/styles/fonts',
        relativeAssets: false,
        assetCacheBuster: false,
        raw: 'Sass::Script::Number.precision = 10\n'
      },
      dist: {
        options: {
          generatedImagesDir: '<%= yeoman.dist %>/images/generated'
        }
      },
      server: {
        options: {
          sourcemap: false
        }
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

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= yeoman.app %>/index.html',
      options: {
        dest: '<%= yeoman.dist %>',
        flow: {
          html: {
            steps: {
              js: ['concat', 'uglify'],
              css: ['cssmin']
            },
            post: {
              js: [{
                name: 'concat',
                createConfig: function (context, block) {
                  var files = context.options.generated.files;
                  for (var i = 0; i < files.length; i++) {
                    if (files[i].dest === '.tmp/concat/scripts/scripts.js') {
                      files[i].src = [
                        // angular-ui-select is patched up and needs to be included manualy du to contact/brwserify issues
                        '<%= yeoman.app %>/scripts/directives/angular-ui-select/select.js',
                        '<%= yeoman.app %>/scripts/glaModule.js',
                        '.tmp/scripts/templateCache.js',
                        '.tmp/scripts/app.js'
                      ]
                    }
                  }
                }
                }]
            }
          }
        }
      }
    },

    // Performs rewrites based on filerev and the useminPrepare configuration
    usemin: {
      html: ['<%= yeoman.dist %>/{,*/}*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      js: ['<%= yeoman.dist %>/scripts/{,*/}*.js'],
      options: {
        assetsDirs: [
          '<%= yeoman.dist %>',
          '<%= yeoman.dist %>/images',
          '<%= yeoman.dist %>/styles'
        ],
        patterns: {
          js: [[/(images\/[^''""]*\.(png|jpg|jpeg|gif|webp|svg))/g, 'Replacing references to images']]
        }
      }
    },

    // The following *-min tasks will produce minified files in the dist folder
    // By default, your `index.html`'s <!-- Usemin block --> will take care of
    // minification. These next options are pre-configured if you do not wish
    // to use the Usemin blocks.
    // cssmin: {
    //   dist: {
    //     files: {
    //       '<%= yeoman.dist %>/styles/main.css': [
    //         '.tmp/styles/{,*/}*.css'
    //       ]
    //     }
    //   }
    // },
    uglify: {
      generated: {
        options: {
          mangle: false
        }
      }
    },
    // concat: {
    //   dist: {}
    // },

    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.{png,jpg,jpeg,gif}',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },

    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
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

    // ng-annotate tries to make the code safe for minification automatically
    // by using the Angular long form for dependency injection.
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/scripts',
          src: '*.js',
          dest: '.tmp/scripts'
        }]
      }
    },

    // Replace Google CDN references
    cdnify: {
      dist: {
        html: ['<%= yeoman.dist %>/*.html']
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>',
          dest: '<%= yeoman.dist %>',
          src: [
            '*.{ico,png,txt}',
            '*.html',
            'images/{,*/}*.{webp}',
            'styles/fonts/{,*/}*.*'
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/images',
          src: ['generated/*']
        }, {
          expand: true,
          cwd: '.',
          src: 'bower_components/bootstrap-sass-official/assets/fonts/bootstrap/*',
          dest: '<%= yeoman.dist %>'
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= yeoman.app %>/styles',
        dest: '.tmp/styles/',
        src: '{,*/}*.css'
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
        'compass:server'
      ],
      test: [
        'compass'
      ],
      dist: [
        'compass:dist',
        'imagemin',
        'svgmin'
      ]
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
          './*.js'
        ]
      },
      options: {
        pattern: escapeStringRegexp(utils.copyrightText('../../../LICENCE.txt'))
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
    }
  };
  grunt.initConfig(config);

  grunt.registerTask('serve', 'Compile then start a connect web server', (target) => {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      'wiredep',
      'concurrent:server',
      'postcss:server',
      'ngtemplates:dev',
      'browserify:dev',
      'configureProxies',
      'connect:livereload',
      'watch'
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
    'ngAnnotate',
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

  grunt.registerTask('default', [
    //'newer:jshint',
    'jscs',
    'copyright',
    'test',
    'build'
  ]);
};
