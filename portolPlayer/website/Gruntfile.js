var allFiles = ['js/userMgmt.js'];

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
      all: allFiles,
    },
    /*
    concat: {    
    		".tmp/portol.all.concat.js": [pathToDash + 'dash.all.js'].concat(portolFiles),

		    ".tmp/portol.debug.concat.js": [pathToDash + 'dash.debug.js'].concat(portolFiles),
    },
    uglify: {
      all: {
      	options: {
      		compress: false,
      		mangle: false,
      	},
        
        files: {
        	'../dist/portol.all.js': ['.tmp/portol.all.concat.js'],
        	}
      },
      */
      debug: {
        options: {
          beautify: true,
          compress: false,
          mangle: false,
        },
        files: {
        	'js/userMgmt.js': allFiles,
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
  //grunt.loadNpmTasks('grunt-contrib-jasmine');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  //grunt.loadNpmTasks('grunt-contrib-uglify');
 // grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-jsdoc');

  // Define tasks
  grunt.registerTask('default', ['jshint']);
};
