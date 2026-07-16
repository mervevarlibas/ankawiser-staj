$(document).ready(function() {

    const token = localStorage.getItem("token");

    const params = new URLSearchParams(window.location.search);
    const postId = params.get("postId");

    console.log("POST ID:", postId); // kontrol

    $.ajax({
        url: "http://localhost:8080/posts/" + postId,
        method: "GET",

        headers: {
            Authorization: "Bearer " + token
        },

        success: function(post) {

            console.log("POST GELDİ:", post);

            const formattedDate =
                new Date(post.createdAt).toLocaleString("tr-TR");

            $("#postDetail").html(`
                <article class="post">

                    <div class="post-header">
                        <div class="avatar post-avatar">
                            ${post.username.charAt(0).toUpperCase()}
                        </div>

                        <div class="post-user-info">
                            <strong>${post.username}</strong>
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
                    </div>

                    <div class="comments-area" id="comments-${post.id}"></div>

                </article>
            `);

            // Post detay sayfasında yorumlar hep açık dursun
            loadComments(post.id);
        },

        error: function(xhr) {
            console.log(xhr);
            alert("POST GELMEDİ");
        }
    });

    // Beğeni butonu (post gelince DOM'a eklendiği için delegation kullanıyoruz)
    $("#postDetail").on("click", ".like-button", function() {

        const button = $(this);
        const clickedPostId = button.data("post-id");
        const isLiked = String(button.attr("data-liked")) === "true";

        const endpoint = isLiked ? "/unlike" : "/like";

        $.ajax({
            url: "http://localhost:8080/posts/" + clickedPostId + endpoint,
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },

            success: function() {
                location.reload(); // basit yöntem: sayfayı yenileyip güncel hali göster
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

    // Yorum gönder butonu
    $("#postDetail").on("click", ".submit-comment-btn", function() {

        const clickedPostId = $(this).data("post-id");
        const input = $("#comment-input-" + clickedPostId);
        const content = input.val().trim();

        if (content === "") {
            return;
        }

        $.ajax({
            url: "http://localhost:8080/posts/" + clickedPostId + "/comments",
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            contentType: "application/json",
            data: JSON.stringify({ content: content }),

            success: function() {
                input.val("");
                loadComments(clickedPostId);
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
    $("#postDetail").on("keypress", ".comment-input", function(e) {
        if (e.which === 13) {
            $(this).siblings(".submit-comment-btn").click();
        }
    });
});


function loadComments(postId) {

    const token = localStorage.getItem("token");
    const commentsArea = $("#comments-" + postId);

    // CSS'te .comments-area varsayılan olarak gizli (timeline'daki
    // aç/kapa özelliği için), post detay sayfasında hep açık kalsın.
    commentsArea.show();

    $.ajax({
        url: "http://localhost:8080/posts/" + postId + "/comments",
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function(comments) {

            commentsArea.empty();

            if (comments.length === 0) {
                commentsArea.append("<p class=\"no-comments\">Henüz yorum yok.</p>");
            } else {
                for (const comment of comments) {

                    const formattedDate =
                        new Date(comment.createdAt).toLocaleString("tr-TR");

                    commentsArea.append(`
                        <div class="comment-item">
                            <strong>${comment.username}</strong>
                            <span class="comment-date">${formattedDate}</span>
                            <p>${comment.content}</p>
                        </div>
                    `);
                }
            }

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
                        type="button"
                        class="submit-comment-btn"
                        data-post-id="${postId}"
                    >
                        Gönder
                    </button>
                </div>
            `);
        },

        error: function() {
            commentsArea.html("<p>Yorumlar yüklenemedi.</p>");
        }
    });
}