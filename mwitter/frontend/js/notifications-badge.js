(function () {
    const token = localStorage.getItem("token");
    if (!token) return;
    const badge = document.getElementById("notificationBadge");
    if (!badge) return;
    let unreadCount = 0;

    function renderBadge() {
        badge.textContent = unreadCount > 99 ? "99+" : String(unreadCount);
        badge.hidden = unreadCount === 0;
    }
    window.setNotificationBadge = function (count) {
        unreadCount = Math.max(0, Number(count) || 0);
        renderBadge();
    };

    fetch("http://localhost:8080/notifications/unread-count", {
        headers: { Authorization: "Bearer " + token }
    }).then(response => {
        if (!response.ok) throw new Error();
        return response.json();
    }).then(window.setNotificationBadge).catch(function () {});

    window.MwitterSocket.subscribe("/user/queue/notifications", function (notification) {
        window.setNotificationBadge(unreadCount + 1);
        window.dispatchEvent(new CustomEvent("mwitter:notification", { detail: notification }));
    });
})();
