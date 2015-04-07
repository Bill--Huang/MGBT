
// console.log(PadLeft(1, 3));
// console.log(PadLeft(2, 3));
// console.log(PadLeft(10, 3));
// console.log(PadLeft(99, 3));
// console.log(PadLeft(100, 3));
// function PadLeft (num, n) {
// 	var txt = Math.pow(10,n) + num + '';
// 	console.log(txt);
//     return (Math.pow(10,n) + num + '').substr(1);
// }

var ffmpeg = require('fluent-ffmpeg');

var imgFileName = '1';
var imgPathName = "../sessions/input-img/1/frame-%03d.jpg";
var videoPathName = "../sessions/output-video/video-" + imgFileName + ".mp4";

//
var ffmpegCommand = ffmpeg(imgPathName)
// withInputFps
	.withInputFPS(2)
	.withOutputFPS(2)
	// .frames()
	.noAudio()
	// .toFormat('avi')
	.on('end', function() {
		console.log("\n [Server Log] ->");
		console.log(":::Server::: Conversion Finished");
		console.log(":::Server::: Path: " + videoPathName);
	})
	.on('error', function(err, stdout, stderr) {
		console.log("\n [Server Log] ->");
		console.log(":::Server::: Cannot process: " + err.message);
	});

ffmpegCommand.save(videoPathName);