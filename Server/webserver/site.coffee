# require('zappajs') process.env.IP, 7373, ->
#     @use 'partials'
#     @use 'bodyParser', static: __dirname + '/public'
#     @use require('connect-assets')
#         src: './webserver/assets'

#     @io.set 'log level', 1

#     # console.log ('d 1')

#     fsLib = require("fs")
#     imageLib = require("imagemagick")
#     fsExtras = require('fs.extra')

#     # console.log ('d 2')

#     freezeTimeSessionId = undefined
#     sessionDirPath = undefined
#     appSocketBroker = new (require("../appsocketserver/SystemSocketBroker"))

#     # console.log ('d 3')
#     ###
#     # The status update website is sending the message so we can keep a reference to its socket
#     ###
#     @on 'idClientConnection': ->
#         appSocketBroker.websiteMessagingSocket = @socket

#     ###
#     # This is the main event that app instances use to communicate with this socket server. We pass
#     # off all message handling to the SystemSocketBroker instance.
#     ###
#     @on 'AppDataEmitEvent': ->
#         @socket.remoteAddress = @socket.handshake.address.address
#         appSocketBroker.processSystemMessages @data, @socket

#     ###
#     # The SystemSocketBroker sends a message when it's time to setup the filesystem for the session
#     ###
#     appSocketBroker.on 'onSetupSessionFileSystem', ->
#         freezeTimeSessionId = "FT3D-" + new Date().getTime()
#         sessionDirPath = "./sessions/" + freezeTimeSessionId
#         fsLib.mkdirSync sessionDirPath

#     ###
#     # Each PicTaker instance uploads its photo to the server, and this is where we handle that
#     ###
#     @post '/fileUpload': (req, res) ->
#         frameNumber = req.body.frameNumber
#         appSocketBroker.sendWebsiteClientMessage "picProcessingFC", frameNumber

#         fsLib.readFile @request.files.framePic.path, (err, data) ->
#             # console.log("test");
#             saveImagePath = sessionDirPath + "/frame" + frameNumber + ".jpg"
#             thumbImgName = "frame-thumb" + frameNumber + ".jpg"
#             thumbImagePath = sessionDirPath + "/" + thumbImgName

#             fsLib.writeFile saveImagePath, data, (err) ->
#                 console.log "Frame " + frameNumber + " successfully uploaded"
#                 res.send ":::Server::: Frame upload complete"
#                 # imageResizeOpts =
#                 #     srcPath: saveImagePath
#                 #     dstPath: thumbImagePath
#                 #     width: 177
#                 #     height: 100
#                 # #TODO: Need to figure out how to get these files to overwrite
#                 # #TODO: Also, after all thumbs have been uploaded, create demo strip
#                 # imageLib.resize imageResizeOpts, (err, stdout, stderr) ->
#                 #     fsExtras.copy thumbImagePath, "./webserver/public/thumbs_temp/" + thumbImgName, (err) ->
#                 #         appSocketBroker.sendWebsiteClientMessage "picProcessingCompleteFC", frameNumber
#                 #         res.send ":::Server::: Frame upload complete"

#     @view index: ->

#     @get '/': ->
#         @render 'index'

#     @view layout: ->
#         doctype 5
#         html ->
#             head ->
#                 title 'FreezeTime3D'
#                 link href: 'http://yui.yahooapis.com/3.8.0/build/cssreset/cssreset-min.css', rel: 'stylesheet'
#                 script src: 'http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js'
#                 link href: '/css/styles.css', rel: 'stylesheet'
#                 script src: '/socket.io/socket.io.js'
#                 js 'app'
#             body ->
#                 h1 'FreezeTime3D'
#                 p class: "status-field"
#                 p class: "pic-takers-label", -> "Pic Takers:"
#                 div class: "grid-container", ->


