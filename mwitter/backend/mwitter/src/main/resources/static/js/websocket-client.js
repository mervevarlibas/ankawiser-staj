(function () {
    const apiBase = "";
    let client = null;
    const subscriptions = new Map();

    function createClient() {
        const token = localStorage.getItem("token");
        if (!token || typeof SockJS === "undefined" || typeof StompJs === "undefined") return null;
        client = new StompJs.Client({
            webSocketFactory: () => new SockJS(apiBase + "/ws"),
            connectHeaders: { Authorization: "Bearer " + token },
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: function () {}
        });
        client.onConnect = function () {
            subscriptions.forEach(function (handlers, destination) {
                client.subscribe(destination, function (frame) {
                    let payload;
                    try { payload = JSON.parse(frame.body); } catch (_) { return; }
                    handlers.forEach(handler => handler(payload));
                });
            });
            window.dispatchEvent(new CustomEvent("mwitter:websocket-connected"));
        };
        client.onWebSocketClose = () => window.dispatchEvent(new CustomEvent("mwitter:websocket-disconnected"));
        client.onStompError = () => window.dispatchEvent(new CustomEvent("mwitter:websocket-error"));
        client.activate();
        return client;
    }

    window.MwitterSocket = {
        get connected() { return !!client && client.connected; },
        connect: function () { return client || createClient(); },
        subscribe: function (destination, handler) {
            if (!subscriptions.has(destination)) subscriptions.set(destination, new Set());
            subscriptions.get(destination).add(handler);
            this.connect();
            return () => subscriptions.get(destination)?.delete(handler);
        },
        publish: function (message) {
            if (!client || !client.connected) throw new Error("WebSocket bağlantısı hazır değil.");
            client.publish(message);
        },
        deactivate: function () { if (client) client.deactivate(); }
    };
})();
