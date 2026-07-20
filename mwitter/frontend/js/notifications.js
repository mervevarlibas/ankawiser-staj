$(document).ready(function () {
    const apiBase = "http://localhost:8080";
    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");
    const username = localStorage.getItem("username") || "Kullanıcı";
    if (!token || !currentUserId) { window.location.href = "login.html"; return; }

    $("#sidebarUsername").text(username);
    $("#sidebarAvatarLetter").text(username.charAt(0).toUpperCase());
    $("#profileLink").attr("href", "profile.html?userId=" + encodeURIComponent(currentUserId));
    $("#logoutButton").click(function () {
        window.MwitterSocket.deactivate();
        localStorage.clear();
        window.location.href = "login.html";
    });

    function escapeHtml(value) { return $("<div>").text(value == null ? "" : String(value)).html(); }
    function icon(type) { return { FOLLOW: "👤", LIKE: "♥", COMMENT: "💬", REPOST: "↻", MENTION: "@" }[type] || "🔔"; }
    function notificationHtml(item) {
        return `<button type="button" class="notification-item ${item.read ? "" : "unread"}"
            data-type="${escapeHtml(item.type)}" data-post-id="${escapeHtml(item.postId)}"
            data-actor-id="${escapeHtml(item.actorUserId)}">
            <span class="notification-icon">${icon(item.type)}</span>
            <span class="notification-copy"><strong>${escapeHtml(item.message)}</strong>
            <small>${new Date(item.createdAt).toLocaleString("tr-TR")}</small></span>
        </button>`;
    }
    function show(items) {
        const list = $("#notificationList").empty();
        if (!items.length) list.html('<div class="empty-card">Henüz bildirimin yok.</div>');
        else items.forEach(item => list.append(notificationHtml(item)));
    }

    $.ajax({ url: apiBase + "/notifications", headers: { Authorization: "Bearer " + token } })
        .done(show).fail(() => $("#notificationList").html('<div class="empty-card">Bildirimler yüklenemedi.</div>'));
    $.ajax({ url: apiBase + "/notifications/mark-read", method: "POST", headers: { Authorization: "Bearer " + token } })
        .done(() => window.setNotificationBadge?.(0));

    window.addEventListener("mwitter:notification", event => {
        $("#notificationList .empty-card").remove();
        $("#notificationList").prepend(notificationHtml(event.detail));
        $.ajax({ url: apiBase + "/notifications/mark-read", method: "POST", headers: { Authorization: "Bearer " + token } })
            .done(() => window.setNotificationBadge?.(0));
    });
    $("#notificationList").on("click", ".notification-item", function () {
        const type = String($(this).data("type"));
        if (type === "FOLLOW") window.location.href = "profile.html?userId=" + encodeURIComponent($(this).data("actor-id"));
        else window.location.href = "post.html?postId=" + encodeURIComponent($(this).data("post-id"));
    });
});
