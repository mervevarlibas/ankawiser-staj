$(document).ready(function() { //sayfa tamamen yüklendiğinde bu kod çalışsın

            const token = localStorage.getItem("token"); // çünkü login olduğumuzda backend bana token verdi tarayıcıya kaydettik, burda onu geri alıyorum
            const username = localStorage.getItem("username");

            if (!token) { // token yoksa login ekranına dön
                window.location.href = "login.html";
                return;
            }

            $("#welcome").text("Hoş geldin, " + username);
            $("#sidebarUsername").text(username);

            const firstLetter = username //kullanıcı adı merve
                ?
                username.charAt(0).toUpperCase() //ilk harfini alıyor
                :
                "M"; //username gelmezse m koy

            $("#sidebarAvatarLetter").text(firstLetter); //profil ikonuna harfi koyuyor
            $("#composerAvatarLetter").text(firstLetter); //post yazdığımız yerdeki avatar harfi

            function loadUnreadConversationCount() {
                $.ajax({
                    url: "/messages/conversations",
                    method: "GET",
                    headers: {
                        Authorization: "Bearer " + token
                    },
                    success: function(conversations) {
                        const unreadConversationCount = conversations.filter(function(conversation) {
                            return conversation.unreadCount > 0;
                        }).length;

                        const badge = $("#unreadConversationBadge");
                        if (unreadConversationCount > 0) {
                            badge.text(unreadConversationCount).prop("hidden", false);
                        } else {
                            badge.text("").prop("hidden", true);
                        }
                    }
                });
            }

            loadUnreadConversationCount();
            setInterval(loadUnreadConversationCount, 10000);

            $("#content").on("input", function() {
                const length = $(this).val().length;
                $("#characterCount").text(length + " / 200");
            });

            $("#logoutButton").click(function() {
                localStorage.removeItem("token"); // çıkış yapınca siliyor
                localStorage.removeItem("userId");
                localStorage.removeItem("username");

                window.location.href = "login.html"; // token olmadığı için tekrar giriş yapman lazım
            });
            // Timeline'daki bir posta tıklayınca detay sayfasına git
            $("#timeline").on("click", ".post", function(e) {

                // butonlara (beğeni, yorum) basıldıysa yönlendirme yapma
                if ($(e.target).closest("button, a").length) {
                    return;
                }

                const postId = $(this).data("post-id");
                window.location.href = "post.html?postId=" + postId;
            });

            // Beğeni/yorum butonlarına basınca üstteki yönlendirmeyi tetikleme
            $("#timeline").on("click", ".like-button, .repost-button, .comment-button", function(e) {
                e.stopPropagation();
            });
            $("#createPostButton").click(function() { //butona basınca çalışır

                const content = $("#content").val().trim(); //textarea’daki yazıyı alır

                if (content === "") {
                    alert("Gönderi boş olamaz.");
                    return;
                }

                $.ajax({
                    url: "/posts",
                    method: "POST", //backende gidiyor postcontroller>createpost
                    headers: {
                        Authorization: "Bearer " + token //jwt gönderiyoruz
                    },
                    contentType: "application/json",
                    data: JSON.stringify({ content: content }), //backende giden veri json formatında

                    success: function() {
                        $("#content").val("");
                        $("#characterCount").text("0 / 200");
                        loadTimeline(); //timeline yenilenir
                    },

                    error: function(xhr) { //xhr AJAX error objesi
                        let message = "Gönderi paylaşılamadı.";

                        if (xhr.responseJSON && xhr.responseJSON.message) { //sadece mesaj kısmını alıyoruz
                            message = xhr.responseJSON.message;
                        }

                        alert(message);
                    }
                });
            });

            // Arama sonucundaki kullanıcı adına tıklayınca profile git
            $("#searchResults").on("click", ".search-username", function() { //id si searchresults olanı seç, kullanıcı adına tıklanınca çalıştır
                const userId = $(this).data("user-id"); //this tıklanan element,htmlden veri alır idyi atar
                window.location.href = "profile.html?userId=" + userId; //aldığı idyi kullanır
            });

            // Arama kutusu
            $("#userSearch").on("input", function() { //kullanıcı inputa her harf yazdığında çalışır

                        const query = $(this).val().trim(); //input içindeki yazıyı alır.this=input,val=içindeki yazı,trim=boşlukarı sil.

                        if (query.length < 2) { //tek harfle arama, gereksiz api çağırımını engellemek
                            $("#searchResults").empty(); //sonucu temizle işlemi durdur
                            return;
                        }

                        $.ajax({ //burda backende istek atıyoruz
                                    url: "/users/search?username=" + query, //usercontroller>search e gider backendde,query=merve mesela
                                    method: "GET",
                                    headers: {
                                        Authorization: "Bearer " + token
                                    },

                                    success: function(users) {

                                            $("#searchResults").empty(); //eski arama silinir

                                            if (users.length === 0) { //liste boşsa
                                                $("#searchResults").append("<p>Kullanıcı bulunamadı</p>");
                                                return;
                                            }

                                            const currentUserId = localStorage.getItem("userId"); //kendi user id m anlamadım bunu

                                            for (const user of users) { //gelen kullanıcıları tek tek gezer
                                                const isFollowed = user.followedByCurrentUser; //bunu takip ediyor musun
                                                //ekrana yeni kullanıcı ekler,data user id tıklayınca id alırız
                                                $("#searchResults").append(`
                        <div class="search-item">

                            <span class="search-username"
                                  data-user-id="${user.id}">
                                  ${user.username}
                            </span>

                            ${
                                user.id === currentUserId//eğer bu bensem buton gösterme
                                ? ""
                                : `<button class="follow-user-btn"
                                        data-user-id="${user.id}">
                                    ${isFollowed ? "Takip Ediliyor" : "Takip Et"}//buna göre buton değişir
                                   </button>`
                            }

                        </div>
                    `);
                }
            },

            error: function () {//backendde sıkıntı olursa
                $("#searchResults").html("<p>Hata oluştu</p>");
            }
        });
    });

    // Arama sonucundaki takip butonu
    $("#searchResults").on("click", ".follow-user-btn", function () {//searchresults içindeki follow butonuna basılırsa çalış

        const targetUserId = $(this).data("user-id");//tıklanan kullanıcının id si
        const button = $(this);//text değiştirmek için butonu aldık

        const isFollowing = button.text().trim() === "Takip Ediliyor";//butona bakarak anlıyoruz

        const url = isFollowing//hangi apiye gidecek
            ? "/users/unfollow/" + targetUserId
            : "/users/follow/" + targetUserId;

        $.ajax({//backende istek
            url: url,
            method: "POST",//veri değiştiriyoruz
            headers: {
                Authorization: "Bearer " + token
            },

            success: function () {
                button.text(isFollowing ? "Takip Et" : "Takip Ediliyor");//arayüzü günceller.öncesi>sonrası
            },

            error: function (xhr) {
                console.log(xhr);//hata olursa hatayı console a bas
                alert("İşlem başarısız");
            }
        });
    });

    $("#timeline").on("click", ".like-button", function () {//tıklanınca çalış

        const button = $(this);//tıklanan butonu al
        const postId = button.data("post-id");//post id al
        const isLiked = String(button.attr("data-liked")) === "true";//like durumu htmlden beğenildi mi diye geliyor

        const endpoint = isLiked ? "/unlike" : "/like";//hangi endpoint olduğuna karar verir

        $.ajax({//backende git
            url: "/posts/" + postId + endpoint,
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },

            success: function () {
                loadTimeline();//başarılı olursa timelinei tekrar çeker çünkü kalp rengi ve like sayısı değişti
            },

            error: function (xhr) {
                let message = "Beğeni işlemi yapılamadı.";//normal hata mesajı

                if (xhr.responseJSON && xhr.responseJSON.message) {//backend hata mesajı varsaa onu göster
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });

    $("#timeline").on("click", ".repost-button", function () {
        const button = $(this);
        const postId = button.data("post-id");
        const isReposted = String(button.attr("data-reposted")) === "true";
        const endpoint = isReposted ? "/unrepost" : "/repost";

        $.ajax({
            url: "/posts/" + postId + endpoint,
            method: "POST",
            headers: { Authorization: "Bearer " + token },
            success: function () {
                loadTimeline();
            },
            error: function (xhr) {
                let message = "Repost işlemi yapılamadı.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                alert(message);
            }
        });
    });

    $("#timeline").on("click", ".delete-post-button", function () {
        const postId = $(this).data("post-id");
        if (!confirm("Bu gönderiyi silmek istediğine emin misin?")) return;

        $.ajax({
            url: "/posts/" + postId,
            method: "DELETE",
            headers: { Authorization: "Bearer " + token },
            success: function () { loadTimeline(); },
            error: function (xhr) {
                alert(xhr.responseJSON?.message || "Gönderi silinemedi.");
            }
        });
    });

    $("#timeline").on("click", ".delete-comment-button", function () {
        const postId = $(this).data("post-id");
        const commentId = $(this).data("comment-id");
        if (!confirm("Bu yorumu silmek istediğine emin misin?")) return;

        $.ajax({
            url: "/posts/" + postId + "/comments/" + commentId,
            method: "DELETE",
            headers: { Authorization: "Bearer " + token },
            success: function () { loadComments(postId); },
            error: function (xhr) {
                alert(xhr.responseJSON?.message || "Yorum silinemedi.");
            }
        });
    });

    $("#timeline").on("click", ".post-username", function () {//timeline içindeki post username tıklanınca çalış

        const userId = $(this).data("user-id");//tıklanan kullanıcının idsini al

        if (!userId) {//id yoksa işlemi durdur
            alert("UserId bulunamadı!");
            return;
        }

        window.location.href = "profile.html?userId=" + userId;//profil sayfasına yönlendir
    });

    // Yorumlar butonuna tıklayınca aç/kapat
    $("#timeline").on("click", ".comment-button", function () {

        const postId = $(this).data("post-id");//tıklanan postun idsini al
        const commentsArea = $("#comments-" + postId);// o posta ait yorum alanını seç

        // Zaten açıksa kapat
        if (commentsArea.is(":visible") && commentsArea.data("loaded")) {//visible ekranda açık mı, loaded daha önce yüklenmiş mi
            commentsArea.slideUp();//kapat
            return;
        }

        loadComments(postId);//ilk kez açılıyorsa backende gider GET /posts/{postId}/comments getirir
        commentsArea.slideDown();//yorum alanını aç
    });

    // Yorum gönder butonuna tıklayınca
    $("#timeline").on("click", ".submit-comment-btn", function () {

        const postId = $(this).data("post-id");
        const input = $("#comment-input-" + postId);//her postun kendi input alanı var doğru olanı buluyor
        const content = input.val().trim();//yazılan yorumu al input icindeki yazı boşlukları sil

        if (content === "") {
            return;
        }

        $.ajax({
            url: "/posts/" + postId + "/comments",
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            contentType: "application/json",//json gönderme
            data: JSON.stringify({ content: content }),

            success: function () {
                input.val("");//başarılı olursa input temizlenir
                loadComments(postId);//yorumları tekrar yükler
            },

            error: function (xhr) {
                let message = "Yorum gönderilemedi.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                alert(message);
            }
        });
    });

    // Enter'a basınca da yorum gönderilsin
    $("#timeline").on("keypress", ".comment-input", function (e) {//keypress klavyeden tuşa basınca tetiklenir
        if (e.which === 13) {//13 enter tuşu
            $(this).siblings(".submit-comment-btn").click();
        }
    });

    $("#profileLink").click(function (e) {//sol menüdeki profil linkine tıklanınca
        e.preventDefault();//linkin normal davranışını iptal eder

        const userId = localStorage.getItem("userId");

        if (!userId) {
            alert("Kullanıcı bilgisi bulunamadı.");
            return;
        }

        window.location.href = "profile.html?userId=" + userId;
    });

    loadTimeline();
});


function loadTimeline() {//timeline i yükleyen fonksiyon

    const token = localStorage.getItem("token");//backende kim olduğunu söylemek için
    const currentUserId = localStorage.getItem("userId");

    $.ajax({//GET /posts/timeline backend postcontroller>gettimeline
        url: "/posts/timeline",
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function (posts) {

            $("#timeline").empty();//eskileri sil

            if (posts.length === 0) {
                $("#timeline").append(`
                    <div class="empty-card">
                        Timeline'ında henüz gönderi yok.
                    </div>
                `);
                return;
            }

            for (const post of posts) {//her post için çalış

                const formattedDate =
                    new Date(post.createdAt).toLocaleString("tr-TR");//backendden gelen tarih okunabilir hale gelir
//postu ekrana basar
                $("#timeline").append(`
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

                        <p class="post-content">${renderMentionedContent(post.content, post.mentions)}</p>

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
                                💬
                                <span>Yorumlar</span>
                            </button>

                            ${post.userId === currentUserId ? `
                                <button
                                    type="button"
                                    class="post-action delete-post-button"
                                    data-post-id="${post.id}"
                                >Sil</button>
                            ` : ""}

                        </div>

                        <div class="comments-area" id="comments-${post.id}"></div>

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


function loadComments(postId) {//seçilen postun yorumlarını getirir

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");
    const commentsArea = $("#comments-" + postId);//doğru postun yorum alanını bulur

    $.ajax({
        url: "/posts/" + postId + "/comments",
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function (comments) {

            commentsArea.empty();//eski yorumları sil
            commentsArea.data("loaded", true);

            if (comments.length === 0) {
                commentsArea.append("<p class=\"no-comments\">Henüz yorum yok.</p>");
            } else {
                for (const comment of comments) {//yorumları bas

                    const formattedDate =
                        new Date(comment.createdAt).toLocaleString("tr-TR");

                    commentsArea.append(`
                        <div class="comment-item">
                            <strong>${comment.username}</strong>
                            <span class="comment-date">${formattedDate}</span>
                            ${comment.userId === currentUserId ? `
                                <button type="button"
                                    class="delete-comment-button"
                                    data-post-id="${postId}"
                                    data-comment-id="${comment.id}">Sil</button>
                            ` : ""}
                            <p>${renderMentionedContent(comment.content, comment.mentions)}</p>
                        </div>
                    `);
                }
            }

            // Yorum ekleme kutusu (her açılışta tekrar eklenmesin diye kontrol et)
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
                            type="button"
                            class="submit-comment-btn"
                            data-post-id="${postId}"
                        >
                            Gönder
                        </button>
                    </div>
                `);
            }
        },

        error: function () {
            commentsArea.html("<p>Yorumlar yüklenemedi.</p>");
        }
    });
}
