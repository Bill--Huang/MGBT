/**
 * Class that convert images into mp4 video.
 * use fluent-ffmpeg module
 */
module.exports = new Class({
	ffmpeg: null,
	ffmpegCommand: null,

	initialize: function() {
        this.ffmpeg = require('fluent-ffmpeg');
        // this.ffmpegCommand = ffmpeg('Raw/frame%02d.jpg');
    },

    convertImageToMP4: function(imgFileName) {

    	var imgPathName = "./sessions/raw-img/" + imgFileName + "/frame-%03d.jpg";
    	var videoPathName = "./sessions/output-video/video-" + imgFileName + ".mp4";

    	this.ffmpegCommand = this.ffmpeg(imgPathName)
    		.on('end', function() {
	    		console.log("\n [Server Log] ->");
	    		console.log(":::Server::: Conversion Finished");
	    		console.log(":::Server::: Path: " + videoPathName);
	  		})
	    	.on('error', function(err, stdout, stderr) {
	    		console.log("\n [Server Log] ->");
	    		console.log(":::Server::: Cannot process: " + err.message);
	  		});

		this.ffmpegCommand.save(videoPathName);
    },
});


