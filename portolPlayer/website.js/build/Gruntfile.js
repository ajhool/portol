var portolFiles = [
    		"../src/portol/gruntUtils/banner.js",
		"../src/portol/endpointServiceModules/**/**/*.js",
		"../src/portol/endpointServiceModules/**/*.js",
    		"../src/portol/endpointServiceModules/*.js",
    		"../src/portol/frameModules/playerModules/*.js",
    		"../src/portol/frameModules/splashModules/**/*.js",
    		"../src/portol/frameModules/splashModules/*.js",
    		"../src/portol/frameModules/*.js",
    		"../src/portol/Portol.js",
    		"../src/portol/gruntUtils/footer.js",
	];
	
var pathToLocalDemo = "../../website/js/";

module.exports = function(grunt) {
  grunt.initConfig({
    
    connect: {
      default_options: {},
      dev: {
        options: {
          port: 9999,
          keepalive: true
        }
      }
    },
    watch: {},
    jshint: {
      all: portolFiles,
    },
    concat: {    
    		".tmp/portol.all.concat.js": [].concat(portolFiles),

		    ".tmp/portol.debug.concat.js": [].concat(portolFiles),
    },
    
    uglify: {
      all: {
      	options: {
      		compress: false,
      		mangle: false,
      	},
        
        files: {
        	'../dist/portolwebsite.all.js': ['.tmp/portol.all.concat.js'],
        	}
      },
      debug: {
        options: {
          beautify: true,
          compress: false,
          mangle: false,
          //banner: "window.portol = (function () {\n'use strict'\n",
          //footer: "\nreturn api;\n" +
			//		"}());"
        },
        files: {
        	'../dist/portol.debug.js': ['.tmp/portol.debug.concat.js'],
        	}
    	}
    },
    
    copy: {
        all: {
            src: "../dist/portolwebsite.all.js",
            dest: pathToLocalDemo + "portolwebsite.all.js"
        },
        debug: {
        	src: "../dist/portolwebsite.debug.js",
            dest: pathToLocalDemo + "portolwebsite.debug.js"
        }
    },
    
    jasmine: {
      tests: {        
        src: [
        	"../../src/main/webapp/dash.all.js",
        	"../src/portol/LoginButton.js",
        	"../src/portol/PreviewButton.js",
        	"../src/portol/ServerReply.js",
        	"../src/portol/SVGDDMenu.js",
        	"../src/portol/Canvas.js",
        	"../src/portol/Portol.js"
            // "../src/streaming/MediaPlayer.js",
            //"../src/streaming/Context.js",
            //"../src/dash/Dash.js",
            //"../src/dash/DashContext.js",
            //"../src/dash/**/*.js",
            //"../src/streaming/**/*.js",
			//"../src/lib/**/*.js"
        ],
        options: {
          host: 'http://127.0.0.1:8000',		  
          keepRunner: true,
          helpers: [
            //"./test/js/utils/Helpers.js",
            //"./test/js/utils/SpecHelper.js",
            //"./test/js/utils/ObjectsHelper.js",
            //"./test/js/utils/MPDHelper.js",
            //"./test/js/utils/VOHelper.js"
          ],
          specs: [
            //'./test/js/dash/TimelineConverterSpec.js',
            //'./test/js/dash/DashHandlerSpec.js',
            //'./test/js/dash/RepresentationControllerSpec.js',
            //'./test/js/streaming/MediaPlayerSpec.js',
            //'./test/js/streaming/FragmentControllerSpec.js',
            //'./test/js/streaming/FragmentModelSpec.js',
            //'./test/js/streaming/AbrControllerSpec.js'
          ],
          vendor: [
            //".grunt/src/lib/xml2json.js",
            //".grunt/src/lib/objectiron.js",
            //".grunt/src/lib/Math.js",
            //".grunt/src/lib/long.js",
            //".grunt/src/lib/dijon.js",	
            //".grunt/src/lib/base64.js"
          ],
          template: require('grunt-template-jasmine-istanbul'),
          templateOptions: {
            coverage: './reports/coverage.json',
            report: './reports/coverage',
            files: '../**/*'
          },
          junit: {
            path: grunt.option('jsunit-path'),
            consolidate: true
          }
        }
      }
    },
    jsdoc: {
        dist: {
            options: {
                destination: '../docs/jsdocs',
                configure: "jsdoc/jsdoc_conf.json"
            }
        }
    }
  });

  // Require needed grunt-modules
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-jasmine');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-jsdoc');

  // Define tasks
  grunt.registerTask('default', ['jshint', 'concat', 'uglify', 'copy']);
};
