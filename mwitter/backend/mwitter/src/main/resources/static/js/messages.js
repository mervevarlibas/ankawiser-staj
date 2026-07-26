$(document).ready(function () {
    const apiBase = "";
    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");
    const currentUsername = localStorage.getItem("username") || "Kullanıcı";
    let selectedUserId = new URLSearchParams(window.location.search).get("userId");
    let selectedUsername = "";
    let stompClient = null;

    if (!token || !currentUserId) {
        window.location.href = "login.html";
        return;
    }

    $("#sidebarUsername").text(currentUsername);
    $("#sidebarAvatarLetter").text(currentUsername.charAt(0).toUpperCase());

    $("#profileLink").click(function (event) {
        event.preventDefault();
        window.location.href = "profile.html?userId=" + encodeURIComponent(currentUserId);
    });

    $("#logoutButton").click(function () {
        if (stompClient) stompClient.deactivate();
        localStorage.removeItem("token");
        localStorage.removeItem("userId");
        localStorage.removeItem("username");
        window.location.href = "login.html";
    });

    function authHeaders() {
        return { Authorization: "Bearer " + token };
    }

    function escapeHtml(value) {
        return $("<div>").text(value == null ? "" : String(value)).html();
    }

    function formatDate(value) {
        return value ? new Date(value).toLocaleString("tr-TR") : "";
    }

    function handleUnauthorized(xhr) {
        if (xhr.status === 401 || xhr.status === 403) {
            localStorage.removeItem("token");
            window.location.href = "login.html";
            return true;
        }
        return false;
    }

    function loadConversationList() {
        $.ajax({
            url: apiBase + "/messages/conversations",
            method: "GET",
            headers: authHeaders(),
            success: function (conversations) {
                const list = $("#conversationList").empty();
                let selectedExists = false;

                for (const conversation of conversations) {
                    if (conversation.userId === selectedUserId) selectedExists = true;
                    const prefix = conversation.lastMessageSentByCurrentUser ? "Sen: " : "";
                    list.append(`
                        <button type="button"
                            class="conversation-item ${conversation.userId === selectedUserId ? "active" : ""}"
                            data-user-id="${escapeHtml(conversation.userId)}"
                            data-username="${escapeHtml(conversation.username)}">
                            <span class="avatar conversation-avatar">${escapeHtml(conversation.username.charAt(0).toUpperCase())}</span>
                            <span class="conversation-copy">
                                <strong>${escapeHtml(conversation.username)}</strong>
                                <small>${escapeHtml(prefix + conversation.lastMessage)}</small>
                            </span>
                            ${conversation.unreadCount > 0 ? `<span class="unread-badge">${conversation.unreadCount}</span>` : ""}
                        </button>
                    `);
                }

                if (selectedUserId && !selectedExists && selectedUsername) {
                    list.prepend(createNewConversationItem(selectedUserId, selectedUsername));
                }

                if (!conversations.length && !selectedUserId) {
                    list.html('<div class="empty-card">Henüz bir sohbetin yok.</div>');
                }
            },
            error: function (xhr) {
                if (!handleUnauthorized(xhr)) {
                    $("#conversationList").html('<div class="empty-card">Sohbetler yüklenemedi.</div>');
                }
            }
        });
    }

    function createNewConversationItem(userId, username) {
        return `
            <button type="button" class="conversation-item active"
                data-user-id="${escapeHtml(userId)}" data-username="${escapeHtml(username)}">
                <span class="avatar conversation-avatar">${escapeHtml(username.charAt(0).toUpperCase())}</span>
                <span class="conversation-copy"><strong>${escapeHtml(username)}</strong><small>Yeni sohbet</small></span>
            </button>
        `;
    }

    function selectConversation(userId, username) {
        selectedUserId = userId;
        selectedUsername = username;
        const nextUrl = "messages.html?userId=" + encodeURIComponent(userId);
        window.history.replaceState({}, "", nextUrl);
        $("#chatUsername").text(username);
        $("#messageForm").show();
        $(".conversation-item").removeClass("active");
        $(".conversation-item[data-user-id='" + CSS.escape(userId) + "']").addClass("active");
        loadMessages();
    }

    function loadMessages() {
        if (!selectedUserId) return;

        $.ajax({
            url: apiBase + "/messages/" + encodeURIComponent(selectedUserId),
            method: "GET",
            headers: authHeaders(),
            success: function (messages) {
                const list = $("#messageList").empty();
                if (!messages.length) {
                    list.append('<div class="chat-placeholder">Henüz mesaj yok. İlk mesajı sen gönder.</div>');
                } else {
                    for (const message of messages) appendMessage(message);
                }
                scrollToLatest();
                loadConversationList();
            },
            error: function (xhr) {
                if (!handleUnauthorized(xhr)) {
                    $("#messageList").html('<div class="chat-placeholder">Mesajlar yüklenemedi.</div>');
                }
            }
        });
    }

    function appendMessage(message) {
        if ($("#messageList [data-message-id='" + CSS.escape(message.id) + "']").length) return;
        $("#messageList .chat-placeholder").remove();
        const mine = message.senderId === currentUserId;
        $("#messageList").append(`
            <div class="message-row ${mine ? "mine" : "theirs"}" data-message-id="${escapeHtml(message.id)}">
                <div class="message-bubble">
                    <p>${escapeHtml(message.content)}</p>
                    <small>${escapeHtml(formatDate(message.sentAt))}</small>
                </div>
            </div>
        `);
    }

    function scrollToLatest() {
        const element = document.getElementById("messageList");
        element.scrollTop = element.scrollHeight;
    }

    function connectWebSocket() {
        stompClient = window.MwitterSocket;
        const subscribeToMessages = function () {
            $("#connectionStatus").text("Bağlı").addClass("connected");
        };
        stompClient.subscribe("/user/queue/messages", function (message) {
                const belongsToOpenConversation = selectedUserId
                    && (message.senderId === selectedUserId || message.receiverId === selectedUserId);

                if (belongsToOpenConversation) {
                    appendMessage(message);
                    scrollToLatest();
                }
                loadConversationList();
        });
        if (stompClient.connected) subscribeToMessages();
        window.addEventListener("mwitter:websocket-connected", subscribeToMessages);
        window.addEventListener("mwitter:websocket-disconnected", function () {
            $("#connectionStatus").text("Yeniden bağlanıyor...").removeClass("connected");
        });
        window.addEventListener("mwitter:websocket-error", function () {
            $("#connectionStatus").text("Bağlantı hatası").removeClass("connected");
        });
        stompClient.connect();
    }

    $("#conversationList").on("click", ".conversation-item", function () {
        selectConversation(String($(this).data("user-id")), String($(this).data("username")));
    });

    $("#messageForm").submit(function (event) {
        event.preventDefault();
        const content = $("#messageInput").val().trim();
        if (!content || !selectedUserId) return;
        if (!stompClient || !stompClient.connected) {
            alert("Mesaj bağlantısı henüz hazır değil.");
            return;
        }

        stompClient.publish({
            destination: "/app/chat.send",
            body: JSON.stringify({ receiverId: selectedUserId, content: content })
        });
        $("#messageInput").val("").focus();
    });

    $("#messageInput").keydown(function (event) {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            $("#messageForm").trigger("submit");
        }
    });

    if (selectedUserId === currentUserId) selectedUserId = null;
    if (selectedUserId) {
        $.ajax({
            url: apiBase + "/users/" + encodeURIComponent(selectedUserId),
            method: "GET",
            headers: authHeaders(),
            success: function (profile) {
                selectedUsername = profile.username;
                selectConversation(selectedUserId, selectedUsername);
                loadConversationList();
            },
            error: function () {
                selectedUserId = null;
                loadConversationList();
            }
        });
    } else {
        loadConversationList();
    }

    connectWebSocket();
});
