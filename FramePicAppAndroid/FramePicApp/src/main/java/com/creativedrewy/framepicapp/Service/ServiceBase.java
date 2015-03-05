package com.creativedrewy.framepicapp.service;

import android.app.Activity;
import android.util.Pair;
import android.widget.Toast;

import com.creativedrewy.framepicapp.R;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.SocketIORequest;

import org.json.JSONArray;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;

/**
 * Functionality related to all models in the application; provides uniform server interaction
 */
public class ServiceBase {
    protected SocketIOClient _globalSocketIOClient;
    protected String _serverIP;
    protected String _roleString;   //Derived classes will need to specify a unique role string
    protected String _registerMessage;

    public String getServerIP() { return _serverIP; }
    public void setServerIP(String _serverIP) { this._serverIP = _serverIP; }

    /**
     * Constructor
     * @param ipAddress IP address to FT3D socket server
     */
    public ServiceBase(String ipAddress) {
        _serverIP = ipAddress;
    }

    /**
     * Startup the connection to the FT3D socket server, providing the observable for subscription
     */
    public Observable<Pair<String, String>> subscribeConnection() {
        SocketIORequest req = new SocketIORequest("http://" + _serverIP + ":7373");
        req.setTimeout(2500);

        return Observable.create((Subscriber<? super Pair<String, String>> subscriber) -> {
            SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), req, (ex, socketIOClient) -> {
                if (ex != null) {
                    subscriber.onError(new Throwable());
                } else {
                    _globalSocketIOClient = socketIOClient;

                    socketIOClient.on("ServerDataEmitEvent", (jsonArray, acknowledge) -> {
                        JSONObject returnJSON = jsonArray.optJSONObject(0);
                        if (returnJSON != null) {
                            subscriber.onNext(new Pair<>(returnJSON.optString("msg"), returnJSON.optString("payload")));
                        }
                    });

                    sendAppDataEmit(_registerMessage);
                }
            });
        });
    }

    /**
     * Send data to the socket server via a socket.io "emit" call
     * @param message Message data to send to the server
     * @param payload Any relevant payload data
     */
    public void sendAppDataEmit(String message, String payload) {
        JSONObject messageData = new JSONObject();

        try {
            messageData.put("role", _roleString);
            messageData.put("message", message);
            messageData.put("payload", payload);
        } catch (Exception e) { }

        _globalSocketIOClient.emit("AppDataEmitEvent", new JSONArray().put(messageData));
    }

    /**
     * Overloaded method to just specify message data
     * @param message
     */
    public void sendAppDataEmit(String message) {
        sendAppDataEmit(message, "");
    }
}
