$(document).ready(function () {

    const token = localStorage.getItem("token");//çünkü login olduk
    const username = localStorage.getItem("username");

    if (!token) {
        window.location.href = "login.html";//token yoksa login ekranına dön
        return;
    }

    $("#welcome").text("Hoş geldin, " + username);

    $("#sidebarUsername").text(username);

    const firstLetter = username
        ? username.charAt(0).toUpperCase()
        : "M";

    $("#sidebarAvatarLetter").text(firstLetter);
    $("#composerAvatarLetter").text(firstLetter);

    $("#content").on("input", function () {

        const length = $(this).val().length;

        $("#characterCount").text(length + " / 200");
    });

    $("#logoutButton").click(function () {

        localStorage.removeItem("token");//çıkış yapınca siliyor
        localStorage.removeItem("userId");
        localStorage.removeItem("username");

        window.location.href = "login.html";//token olmadığı için tekrar giriş yapman lazım
    });

    $("#createPostButton").click(function () {

        const content = $("#content").val().trim();

        if (content === "") {
            alert("Gönderi boş olamaz.");
            return;
        }

        $.ajax({
            url: "http://localhost:8080/posts",
            method: "POST",

            headers: {
                Authorization: "Bearer " + token
            },

            contentType: "application/json",

            data: JSON.stringify({
                content: content
            }),

            success: function () {

                $("#content").val("");
                $("#characterCount").text("0 / 200");

                loadTimeline();
            },

            error: function (xhr) {

                let message = "Gönderi paylaşılamadı.";

                if (
                    xhr.responseJSON &&
                    xhr.responseJSON.message
                ) {
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });

    /*
     * Postlar JavaScript ile sonradan oluşturulduğu için
     * doğrudan .like-button üzerine değil, #timeline üzerine
     * click dinleyicisi ekliyoruz.
     */
    $("#timeline").on("click", ".like-button", function () { //frontendde kalp butonuna basınca çalışan kısım

        const button = $(this); //this, tıklanan like butonudur.jquery nesnesine çevirdik
        const postId = button.data("post-id"); //post idsini alma

        const isLiked = //beğenildi mi kontrolü
            String(button.attr("data-liked")) === "true";

        const endpoint = isLiked //Bu ternary operator’dür if elsein kısa hali
            ? "/unlike"
            : "/like";

        $.ajax({ //ajax isteği url jwt header içinde gider
            url:
                "http://localhost:8080/posts/" +
                postId +
                endpoint,

            method: "POST",

            headers: {
                Authorization: "Bearer " + token
            },

            success: function () { //başarılı olursa timeline yeniden çekilir çünkü like sayısı değişti
                loadTimeline();
            },

            error: function (xhr) {

                let message = "Beğeni işlemi yapılamadı.";

                if (
                    xhr.responseJSON &&
                    xhr.responseJSON.message
                ) {
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });

    loadTimeline();
});


function loadTimeline() {

    const token = localStorage.getItem("token");

    $.ajax({
        url: "http://localhost:8080/posts/timeline",
        method: "GET",

        headers: {
            Authorization: "Bearer " + token
        },

        success: function (posts) {

            $("#timeline").empty();

            if (posts.length === 0) {

                $("#timeline").append(`
                    <div class="empty-card">
                        Timeline'ında henüz gönderi yok.
                    </div>
                `);

                return;
            }

            for (const post of posts) {

                const formattedDate =
                    new Date(post.createdAt)
                        .toLocaleString("tr-TR");

                $("#timeline").append(`
                    <article
                        class="post"
                        data-post-id="${post.id}"
                    >

                        <div class="post-header">

                            <div class="avatar post-avatar">
                                ${post.username
                                    .charAt(0)
                                    .toUpperCase()}
                            </div>

                            <div class="post-user-info">

                                <button
                                    type="button"
                                    class="post-username"
                                    data-user-id="${post.userId}"
                                >
                                    ${post.username}
                                </button>

                                <small>
                                    ${formattedDate}
                                </small>

                            </div>

                        </div>

                        <p class="post-content">
                            ${post.content}
                        </p>

                        <div class="post-actions">

                            <button
                                type="button"
                                class="
                                    post-action
                                    like-button
                                    ${post.likedByCurrentUser
                                        ? "liked"
                                        : ""}
                                "
                                data-post-id="${post.id}"
                                data-liked="${post.likedByCurrentUser}"
                            >

                                <span class="like-icon">
                                    ${post.likedByCurrentUser
                                        ? "♥"//beğenilmişse
                                        : "♡"}
                                </span>

                                <span> 
                                    ${post.likeCount} 
                                </span>

                            </button>

                            <button
                                type="button"
                                class="post-action comment-button"
                                data-post-id="${post.id}"
                            >
                                💬
                                <span>Yorumlar</span>
                            </button>

                        </div>

                        <div
                            class="comments-area"
                            id="comments-${post.id}"
                        ></div>

                    </article>
                `);
            }
        },

        error: function (xhr) {

            if (xhr.status === 401 || xhr.status === 403) {

                localStorage.removeItem("token");
                localStorage.removeItem("userId");
                localStorage.removeItem("username");

                window.location.href = "login.html";
                return;
            }

            alert("Timeline yüklenemedi.");
        }
    });
}