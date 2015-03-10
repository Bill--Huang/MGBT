/*
 * Client side code to be run in a compiled js file via connect-assets
 */

(function() {
    $(function() {
        
        var addPicTakerStatusBox;
        var gridContainer;
        var init;
        var resetSystem;
        var setupUpdateSocket;
        var statusField;
        var statusPrefix;
        var updatePicTakerStatus;
        var updateStatusField;

        statusPrefix = "Status: ";
        statusField = $(".status-field");
        gridContainer = $(".grid-container");
        init = function() {
          this.siteSocket = io.connect('http://localhost:7373');
          setupUpdateSocket();
          return updateStatusField("System ready, waiting to get started.");
        };

        /*
         * Setup the websocket connection with the running website
         */
        setupUpdateSocket = function() {
          siteSocket.on('connect', function() {
            return siteSocket.emit('idClientConnection');
          });
          return siteSocket.on('update', function(data) {
            switch (data.msg) {
              case "registerMasterFC":
                return updateStatusField("Master registered. Next up: Pic Taker ordering.");
              case "initPicTakerOrderFC":
                return updateStatusField("Pic Taker ordering started. Master will tell you when to submit.");
              case "systemResetFC":
                return resetSystem();
              case "freezeTimeInitiatedFC":
                return updateStatusField("Boom! Time frozen in 3D!");
              case "picTakerHasRegisteredFC":
                return addPicTakerStatusBox(data.payload);
              case "picTakerHasOrderedFC":
                return updatePicTakerStatus(data.payload, "Ordered: " + data.payload);
              case "picTakerIsReadyFC":
                return updatePicTakerStatus(data.payload, data.payload + ": Ready");
              case "picProcessingFC":
                return updatePicTakerStatus(data.payload, data.payload + ": Processing");
              case "picProcessingCompleteFC":
                return $(gridContainer.children("div").get(data.payload)).html("<img src='/thumbs_temp/frame-thumb" + data.payload + ".jpg'>");
              case "picTakerUnRegister":
                return $("div[pic-taker-ip='" + data.payload + "']").remove();
            }
          });
        };

        /*
         * The reset message has been sent, update the website UI to show correct status
         */
        resetSystem = function() {
          updateStatusField("System Reset. Next up: Pic Taker ordering.");
          $(gridContainer).children("div").html("<p></p>");
          return $(gridContainer).children("div").find("p").html("Registered");
        };

        /*
         * Update the status message for a particular Pic Taker box here on the site
         */
        updatePicTakerStatus = function(picTakerNumber, updateString) {
          return $(gridContainer.children("div").get(picTakerNumber)).find("p").html(updateString);
        };

        /*
         * Update the status field with master-related messages
         */
        updateStatusField = function(msg) {
          return statusField.html(statusPrefix + msg);
        };

        /*
         * Add a status box for a connected Pic Taker to the UI
         */
        addPicTakerStatusBox = function(picTakerIP) {
          var labelP, picTakerDiv;
          picTakerDiv = document.createElement('div');
          picTakerDiv.className = "pic-taker-cell";
          picTakerDiv.setAttribute("pic-taker-ip", picTakerIP);
          labelP = document.createElement('p');
          labelP.innerHTML = 'Registered';
          picTakerDiv.appendChild(labelP);
          return gridContainer.append(picTakerDiv);
        };
        return init();
    });

}).call(this);
