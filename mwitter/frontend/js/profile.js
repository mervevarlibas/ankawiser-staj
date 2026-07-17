$(document).ready(function() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    // URL'den userId al
    const params = new URLSearchParams(window.location.search);
    let userId = params.get("userId");

    //eğer yoksa kendi profiline git
    if (!userId) {
        userId = localStorage.getItem("userId");
    }

    const currentUserId = localStorage.getItem("userId");

    // Kullanıcı adı, "Benim Profilim" yazısı vb. artık loadProfileInfo()
    // içinde ProfileResponse'tan doldurulacak.

    loadUserPosts(userId); //sadece postları getirir
    loadProfileInfo(userId); //profil bilgisi+follow durumu getirir

    // Profil içinden profile gitme
    $("#profilePosts").on("click", ".post-username", function() {
        const clickedUserId = $(this).data("user-id");
        window.location.href = "profile.html?userId=" + clickedUserId;
    });
    $("#profilePosts").on("click", ".like-button", function() {

        const button = $(this);
        const postId = button.data("post-id");
        const isLiked = String(button.attr("data-liked")) === "true";

        const endpoint = isLiked ? "/unlike" : "/like";

        $.ajax({
            url: "http://localhost:8080/posts/" + postId + endpoint,
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },

            success: function() {
                loadUserPosts(userId);
            },

            error: function(xhr) {
                let message = "Beğeni işlemi yapılamadı.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });
    $("#profilePosts").on("click", ".repost-button", function() {
        const button = $(this);
        const postId = button.data("post-id");
        const isReposted = String(button.attr("data-reposted")) === "true";
        const endpoint = isReposted ? "/unrepost" : "/repost";

        $.ajax({
            url: "http://localhost:8080/posts/" + postId + endpoint,
            method: "POST",
            headers: { Authorization: "Bearer " + token },
            success: function() {
                loadUserPosts(userId);
            },
            error: function(xhr) {
                let message = "Repost işlemi yapılamadı.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                alert(message);
            }
        });
    });
    $("#profilePosts").on("click", ".delete-post-button", function() {
        const postId = $(this).data("post-id");
        if (!confirm("Bu gönderiyi silmek istediğine emin misin?")) return;

        $.ajax({
            url: "http://localhost:8080/posts/" + postId,
            method: "DELETE",
            headers: { Authorization: "Bearer " + token },
            success: function() { loadUserPosts(userId); },
            error: function(xhr) {
                alert(xhr.responseJSON?.message || "Gönderi silinemedi.");
            }
        });
    });

    $("#profilePosts").on("click", ".delete-comment-button", function() {
        const postId = $(this).data("post-id");
        const commentId = $(this).data("comment-id");
        if (!confirm("Bu yorumu silmek istediğine emin misin?")) return;

        $.ajax({
            url: "http://localhost:8080/posts/" + postId + "/comments/" + commentId,
            method: "DELETE",
            headers: { Authorization: "Bearer " + token },
            success: function() { loadComments(postId); },
            error: function(xhr) {
                alert(xhr.responseJSON?.message || "Yorum silinemedi.");
            }
        });
    });
    // Yorumlar butonuna tıklayınca aç/kapat
    $("#profilePosts").on("click", ".comment-button", function() {

        const postId = $(this).data("post-id");
        const commentsArea = $("#comments-" + postId);

        if (commentsArea.is(":visible") && commentsArea.data("loaded")) {
            commentsArea.slideUp();
            return;
        }

        loadComments(postId);
        commentsArea.slideDown();
    });

    // Yorum gönder butonu
    $("#profilePosts").on("click", ".submit-comment-btn", function() {

        const postId = $(this).data("post-id");
        const input = $("#comment-input-" + postId);
        const content = input.val().trim();

        if (content === "") {
            return;
        }

        $.ajax({
            url: "http://localhost:8080/posts/" + postId + "/comments",
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            contentType: "application/json",
            data: JSON.stringify({ content: content }),

            success: function() {
                input.val("");
                loadComments(postId);
            },

            error: function(xhr) {
                let message = "Yorum gönderilemedi.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });

    // Enter'a basınca yorum gönder
    $("#profilePosts").on("keypress", ".comment-input", function(e) {
        if (e.which === 13) {
            $(this).siblings(".submit-comment-btn").click();
        }
    });
    // Sol menü profil butonu
    $("#profileLink").click(function(e) {
        e.preventDefault();

        const myUserId = localStorage.getItem("userId");

        if (!myUserId) {
            alert("Kullanıcı bilgisi bulunamadı.");
            return;
        }

        window.location.href = "profile.html?userId=" + myUserId; //kendi profiline yönlendirir
    });

    // Takip Et / Takipten Çık butonu
    $("#followButton").click(function() {

        const token = localStorage.getItem("token");
        const targetUserId = userId;
        const button = $(this);

        const isFollowing = button.text().trim() === "Takipten Çık"; //takip ediyoruz yani

        const endpoint = isFollowing ?
            "/unfollow/" + targetUserId :
            "/follow/" + targetUserId; //backend usercontrollera gönderir duruma göre

        $.ajax({
            url: "http://localhost:8080/users" + endpoint,
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },

            success: function() {
                loadProfileInfo(targetUserId); //işlemden sonra backendden yeniden çekiyoruz
            },

            error: function(xhr) {
                console.log(xhr);
                alert("Follow işlemi başarısız.");
            }
        });
    });

    // Takipçi sayısına tıklayınca takipçi listesini göster
    $("#followerCount").parent().css("cursor", "pointer").click(function() {
        openFollowListModal(userId, "followers");
    });

    $("#followingCount").parent().css("cursor", "pointer").click(function() {
        openFollowListModal(userId, "following");
    });
    // Modal kapatma
    $("#closeFollowListModal").click(function() {
        $("#followListModal").hide();
    });

    //boş alana tıklayınca da kapansın
    $("#followListModal").click(function(e) {
        if (e.target.id === "followListModal") {
            $("#followListModal").hide();
        }
    });

    // Listedeki bir kullanıcıya tıklayınca o kullanıcının profiline git
    $("#followListBody").on("click", ".follow-list-item", function() {
        const clickedUserId = $(this).data("user-id");
        window.location.href = "profile.html?userId=" + clickedUserId;
    });
    if (userId === currentUserId) {
        $("#changePasswordButton").show();
        $("#messageButton").hide();
    } else {
        $("#messageButton").show();
    }

    $("#messageButton").click(function() {
        window.location.href = "messages.html?userId=" + encodeURIComponent(userId);
    });

    $("#changePasswordButton").click(function() {
        $("#changePasswordMessage").text("");
        $("#oldPassword").val("");
        $("#newPassword").val("");
        $("#confirmNewPassword").val("");
        $("#changePasswordModal").show();
    });

    $("#closeChangePasswordModal").click(function() {
        $("#changePasswordModal").hide();
    });

    $("#changePasswordModal").click(function(e) {
        if (e.target.id === "changePasswordModal") {
            $("#changePasswordModal").hide();
        }
    });

    $("#submitChangePassword").click(function() {

        const oldPassword = $("#oldPassword").val();
        const newPassword = $("#newPassword").val();
        const confirmNewPassword = $("#confirmNewPassword").val();

        if (!oldPassword || !newPassword || !confirmNewPassword) {
            $("#changePasswordMessage").text("Tüm alanları doldur.");
            return;
        }

        if (newPassword !== confirmNewPassword) {
            $("#changePasswordMessage").text("Yeni şifreler eşleşmiyor.");
            return;
        }

        $.ajax({
            url: "http://localhost:8080/users/change-password",
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            contentType: "application/json",
            data: JSON.stringify({
                oldPassword: oldPassword,
                newPassword: newPassword
            }),

            success: function() {
                $("#changePasswordMessage")
                    .css("color", "green")
                    .text("Şifren değiştirildi.");

                setTimeout(function() {
                    $("#changePasswordModal").hide();
                }, 1200);
            },

            error: function(xhr) {
                let message = "Şifre değiştirilemedi.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                $("#changePasswordMessage")
                    .css("color", "red")
                    .text(message);
            }
        });
    });
    $("#profilePosts").on("click", ".post", function(e) {

        // butonlara basıldıysa yönlendirme yapma
        if ($(e.target).closest("button").length) {
            return;
        }

        const postId = $(this).data("post-id");

        window.location.href = "post.html?postId=" + postId;
    });
    $("#profilePosts").on("click", ".like-button, .repost-button, .comment-button", function(e) {
        e.stopPropagation();
    });
});

function openFollowListModal(userId, type) {
    // type: "followers" ya da "following"

    const token = localStorage.getItem("token");
    const title = type === "followers" ? "Takipçiler" : "Takip Edilenler";

    $("#followListTitle").text(title);
    $("#followListBody").html("<p>Yükleniyor...</p>");
    $("#followListModal").show();

    $.ajax({
        url: "http://localhost:8080/users/" + userId + "/" + type,
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(users) {

            $("#followListBody").empty();

            if (users.length === 0) {
                $("#followListBody").append("<p>Kimse yok.</p>");
                return;
            }

            for (const user of users) {
                $("#followListBody").append(`
                    <div class="follow-list-item" data-user-id="${user.id}">
                        ${user.username}
                    </div>
                `);
            }
        },

        error: function() {
            $("#followListBody").html("<p>Liste yüklenemedi.</p>");
        }
    });
}

function loadUserPosts(userId) { //postları getirme

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    $.ajax({
        url: "http://localhost:8080/posts/user/" + userId, //PostController>getPostsByUserId
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(posts) {

            $("#profilePosts").empty();

            if (posts.length === 0) {
                $("#profilePosts").append("<p>Henüz gönderi yok.</p>");
                return;
            }

            for (const post of posts) { //her postu ekrana basar

                const formattedDate =
                    new Date(post.createdAt)
                    .toLocaleString("tr-TR");
                $("#profilePosts").append(`
    <article class="post" data-post-id="${post.id}">

        ${post.repost ? `
            <div class="repost-label">↻ ${post.repostedByUsername} repostladı</div>
        ` : ""}

        <div class="post-header">

            <div class="avatar post-avatar">
                ${post.username.charAt(0).toUpperCase()}
            </div>

            <div class="post-user-info">

                <button
                    type="button"
                    class="post-username"
                    data-user-id="${post.userId}"
                >
                    ${post.username}
                </button>

                <small>${formattedDate}</small>

            </div>

        </div>

        <p class="post-content">
            ${post.content}
        </p>

        <div class="post-actions">

            <button
                type="button"
                class="post-action like-button ${post.likedByCurrentUser ? "liked" : ""}"
                data-post-id="${post.id}"
                data-liked="${post.likedByCurrentUser}"
            >
                <span class="like-icon">
                    ${post.likedByCurrentUser ? "♥" : "♡"}
                </span>
                <span>${post.likeCount}</span>
            </button>

            <button
                type="button"
                class="post-action repost-button ${post.repostedByCurrentUser ? "reposted" : ""}"
                data-post-id="${post.id}"
                data-reposted="${post.repostedByCurrentUser}"
                aria-label="${post.repostedByCurrentUser ? "Repostu geri al" : "Repostla"}"
            >
                <span class="repost-icon">↻</span>
                <span>${post.repostCount}</span>
            </button>

            <button
                type="button"
                class="post-action comment-button"
                data-post-id="${post.id}"
            >
                💬 <span>Yorumlar</span>
            </button>

            ${post.userId === currentUserId ? `
                <button type="button"
                    class="post-action delete-post-button"
                    data-post-id="${post.id}">Sil</button>
            ` : ""}

        </div>

        <div class="comments-area" id="comments-${post.id}"></div>

    </article>
`);

            }
        },

        error: function() {
            alert("Profil yüklenemedi.");
        }
    });
}


function loadProfileInfo(userId) {

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    // 1) Profil sayılarını (followersCount, followingCount) çek
    $.ajax({
        url: "http://localhost:8080/users/" + userId, //UserController>getProfile
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(profile) {

            $("#followerCount").text(profile.followersCount); //sayıları günceller
            $("#followingCount").text(profile.followingCount);

            const firstLetter = profile.username.charAt(0).toUpperCase();
            $("#profileAvatarLetter").text(firstLetter);
            $("#profileName").text(profile.username); //isim
            $("#profileUsernameTag").text("@" + profile.username); //profil harfi

            if (userId === currentUserId) { //kendi profilimse
                $("#profileUsername").text("Benim Profilim");
            } else {
                $("#profileUsername").text(profile.username);
            }
        },

        error: function() {
            alert("Profil bilgisi alınamadı.");
        }
    });

    // Kendi profilinse takip butonunu göstermeye gerek yok,
    if (userId === currentUserId) {
        $("#followButton").hide();
        return;
    }

    $("#followButton").show();

    // 2) ProfileResponse takip durumunu döndürmüyor, o yüzden
    // takipçi listesinden kendi id'mizin olup olmadığına bakıyoruz.
    $.ajax({
        url: "http://localhost:8080/users/" + userId + "/followers",
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(followers) {

            const isFollowing = followers.some(function(follower) {
                return follower.id === currentUserId;
            });

            $("#followButton").text(isFollowing ? "Takipten Çık" : "Takip Et");
        },

        error: function() {
            // Takip durumu belirlenemedi, güvenli varsayılan
            $("#followButton").text("Takip Et");
        }
    });
}

function loadComments(postId) {

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");
    const commentsArea = $("#comments-" + postId);

    $.ajax({
        url: "http://localhost:8080/posts/" + postId + "/comments",
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(comments) {

            commentsArea.empty();
            commentsArea.data("loaded", true);

            if (comments.length === 0) {
                commentsArea.append("<p class=\"no-comments\">Henüz yorum yok.</p>");
            } else {
                for (const comment of comments) {

                    const formattedDate =
                        new Date(comment.createdAt).toLocaleString("tr-TR");

                    commentsArea.append(` 
                        <div class="comment-item">
    <strong> ${comment.username}</strong>
    <span class="comment-date">${formattedDate }</span>
    ${comment.userId === currentUserId ? `
        <button type="button"
            class="delete-comment-button"
            data-post-id="${postId}"
            data-comment-id="${comment.id}">Sil</button>
    ` : ""}
    <p>${comment.content}</p> 
</div>
    `);
                }
            }

            if (commentsArea.find(".comment-input").length === 0) {
                commentsArea.append(` 
                    <div class="comment-form">
    <input
    type="text"
class="comment-input"
id="comment-input-${postId}"
maxlength="200"
placeholder="Yorum yaz..." 
/>
    <button
     type = "button"
class ="submit-comment-btn"
data-post-id ="${postId}"
 >
    Gönder 
    </button> 
    
    </div>
    `);
            }
        },

        error: function() {
            commentsArea.html("<p>Yorumlar yüklenemedi.</p>");
        }
    });
}
