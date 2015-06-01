(function() {
    require('zappajs')(process.env.IP, 7373, function() {

        // console.log("TEST");

        var appSocketBroker;
        var freezeTimeSessionId;
        var fsExtras;
        var fsLib;
        var imageLib;
        var sessionDirPath;

        this.use('partials');

        this.use('bodyParser', {
            "static": __dirname + '/public'
        });

        this.use(require('connect-assets')({
            src: './webserver/assets'
        }));

        this.io.set('log level', 1);
        fsLib = require("fs");
        imageLib = require("imagemagick");
        fsExtras = require('fs.extra');
        freezeTimeSessionId = void 0;
        sessionDirPath = void 0;
        appSocketBroker = new(require("../appsocketserver/SystemSocketBroker"));

        /**
         * The status update website is sending the message so we can keep a reference to its socket
         */
        this.on({'idClientConnection': function() {
            return appSocketBroker.websiteMessagingSocket = this.socket;
        }});
        
        /**
         * This is the main event that app instances use to communicate with this socket server. We pass
         * off all message handling to the SystemSocketBroker instance.
         */
        
        this.on({'AppDataEmitEvent': function() {

            console.log("\n [Emit Event Called From App] ->");

            this.socket.remoteAddress = this.socket.handshake.address.address;
            return appSocketBroker.processSystemMessages(this.data, this.socket);
        }});

        /**
         * The SystemSocketBroker sends a message when it's time to setup the filesystem for the session
         */
        
        appSocketBroker.on('onSetupSessionFileSystem', function() {
            freezeTimeSessionId = "MGBT-" + new Date().getTime();
            sessionDirPath = "./sessions/input-img/" + freezeTimeSessionId;

            console.log("\n [Server Log] ->");
            console.log(":::Server::: Set up a New File Folder For New Round: " + sessionDirPath);

            return fsLib.mkdirSync(sessionDirPath);
        });

        /**
         * Each PicTaker instance uploads its photo to the server, and this is where we handle that
         */
        this.post({'/fileUpload': function(req, res) {
            var frameNumber = req.body.frameNumber;
            frameNumber = Number(frameNumber);
            appSocketBroker.sendWebsiteClientMessage("picProcessingFC", frameNumber);

            return fsLib.readFile(this.request.files.framePic.path,
                function(err, data) {
                    
                    // File name formate: frame-%03d.jpg
                    var saveImagePath = sessionDirPath + "/frame-" + PadLeft(frameNumber, 3) + ".jpg";
                    // var thumbImgName = "frame-thumb" + frameNumber + ".jpg";
                    // var thumbImagePath = sessionDirPath + "/" + thumbImgName;
                    // console.log(frameNumber + " : " + saveImagePath);

                    return fsLib.writeFile(saveImagePath, data,
                        function(err) {

                            console.log("\n [Image File From App] ->");
                            console.log(":::Server::: Frame-" + PadLeft(frameNumber, 3) + " successfully uploaded");

                            appSocketBroker.countFrameImgNumFromApp(freezeTimeSessionId);

                            return res.send(":::Server::: Frame upload complete");
                        });
                });
        }});

        /**
         * Website page view
         */
        this.view({
            index: function() {}
        });

        this.get({'/': function() {
            return this.render('index');
        }});

        return this.view({ layout: function() {
            doctype(5);
            return html(function() {
                head(function() {
                    title('Miugo Bullet Time');
                    link({
                        href: 'http://yui.yahooapis.com/3.8.0/build/cssreset/cssreset-min.css',
                        rel: 'stylesheet'
                    });
                    script({
                        src: 'http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js'
                    });
                    link({
                        href: '/css/styles.css',
                        rel: 'stylesheet'
                    });
                    script({
                        src: '/socket.io/socket.io.js'
                    });
                    return js('app');
                });
                return body(function() {
                    h1('Miugo Bullet Time');
                    p({
                        "class": "status-field"
                    });
                    p({
                        "class": "pic-takers-label"
                    },
                    function() {
                        return "Pic Takers:";
                    });
                    return div({
                        "class": "grid-container"
                    },
                    function() {});
                });
            });
        }});
    });


    /**
     * Utility Function
     */  
    function PadLeft (num, n) {
        return (Math.pow(10,n) + num + '').substr(1);
    }

}).call(this);