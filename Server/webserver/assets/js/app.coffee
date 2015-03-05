###
Client side code to be run in a compiled js file via connect-assets
###

$ ->
    statusPrefix = "Status: "
    statusField = $(".status-field")
    gridContainer = $(".grid-container")

    init = ->
        @siteSocket = io.connect 'http://localhost:7373'
        do setupUpdateSocket
        updateStatusField "System ready, waiting to get started."

    ###
    Setup the websocket connection with the running website
    ###
    setupUpdateSocket = ->
        siteSocket.on 'connect', ->
            siteSocket.emit 'idClientConnection'

        siteSocket.on 'update', (data) ->
            switch data.msg
                when "registerMasterFC" then updateStatusField "Master registered. Next up: Pic Taker ordering."
                when "initPicTakerOrderFC" then updateStatusField "Pic Taker ordering started. Master will tell you when to submit."
                when "systemResetFC" then do resetSystem
                when "freezeTimeInitiatedFC" then updateStatusField "Boom! Time frozen in 3D!"
                when "picTakerHasRegisteredFC" then addPicTakerStatusBox data.payload
                when "picTakerHasOrderedFC" then updatePicTakerStatus data.payload, "Ordered: " + data.payload
                when "picTakerIsReadyFC" then updatePicTakerStatus data.payload, data.payload + ": Ready"
                when "picProcessingFC" then updatePicTakerStatus data.payload, data.payload + ": Processing"
                when "picProcessingCompleteFC" then $(gridContainer.children("div").get(data.payload)).html("<img src='/thumbs_temp/frame-thumb" + data.payload + ".jpg'>")
                when "picTakerUnRegister" then $("div[pic-taker-ip='" + data.payload + "']").remove()

    ###
    The reset message has been sent, update the website UI to show correct status
    ###
    resetSystem = ->
        updateStatusField "System Reset. Next up: Pic Taker ordering."
        $(gridContainer).children("div").html("<p></p>")
        $(gridContainer).children("div").find("p").html("Registered")

    ###
    Update the status message for a particular Pic Taker box here on the site
    ###
    updatePicTakerStatus = (picTakerNumber, updateString) ->
        $(gridContainer.children("div").get(picTakerNumber)).find("p").html(updateString)

    ###
    Update the status field with master-related messages
    ###
    updateStatusField = (msg) ->
        statusField.html statusPrefix + msg

    ###
    Add a status box for a connected Pic Taker to the UI
    ###
    addPicTakerStatusBox = (picTakerIP) ->
        picTakerDiv = document.createElement 'div'
        picTakerDiv.className = "pic-taker-cell"
        picTakerDiv.setAttribute("pic-taker-ip", picTakerIP)
        labelP = document.createElement 'p'
        labelP.innerHTML = 'Registered'

        picTakerDiv.appendChild(labelP)
        gridContainer.append(picTakerDiv)

    do init