$(document).ready(function () {

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

    loadUserPosts(userId);//sadece postları getirir
    loadProfileInfo(userId);//profil bilgisi+follow durumu getirir

    // Profil içinden profile gitme
    $("#profilePosts").on("click", ".post-username", function () {
        const clickedUserId = $(this).data("user-id");
        window.location.href = "profile.html?userId=" + clickedUserId;
    });

    // Sol menü profil butonu
    $("#profileLink").click(function (e) {
        e.preventDefault();

        const myUserId = localStorage.getItem("userId");

        if (!myUserId) {
            alert("Kullanıcı bilgisi bulunamadı.");
            return;
        }

        window.location.href = "profile.html?userId=" + myUserId;//kendi profiline yönlendirir
    });

    // Takip Et / Takipten Çık butonu
    $("#followButton").click(function () {

        const token = localStorage.getItem("token");
        const targetUserId = userId;
        const button = $(this);

        const isFollowing = button.text().trim() === "Takipten Çık";//takip ediyoruz yani

        const endpoint = isFollowing
            ? "/unfollow/" + targetUserId
            : "/follow/" + targetUserId;//backend usercontrollera gönderir duruma göre

        $.ajax({
            url: "http://localhost:8080/users" + endpoint,
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },

            success: function () {
                loadProfileInfo(targetUserId);//işlemden sonra backendden yeniden çekiyoruz
            },

            error: function (xhr) {
                console.log(xhr);
                alert("Follow işlemi başarısız.");
            }
        });
    });

    // Takipçi sayısına tıklayınca takipçi listesini göster
    $("#followerCount").parent().css("cursor", "pointer").click(function () {
        openFollowListModal(userId, "followers");
    });

    // Takip edilen sayısına tıklayınca takip edilenler listesini göster
    $("#followingCount").parent().css("cursor", "pointer").click(function () {
        openFollowListModal(userId, "following");
    });

    // Modal kapatma
    $("#closeFollowListModal").click(function () {
        $("#followListModal").hide();
    });

    //boş alana tıklayınca da kapansın
    $("#followListModal").click(function (e) {
        if (e.target.id === "followListModal") {
            $("#followListModal").hide();
        }
    });

    // Listedeki bir kullanıcıya tıklayınca o kullanıcının profiline git
    $("#followListBody").on("click", ".follow-list-item", function () {
        const clickedUserId = $(this).data("user-id");
        window.location.href = "profile.html?userId=" + clickedUserId;
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
        url: "http://localhost:8080/users/" + userId + "/" + type,//GET /users/{userId}/followers gibi
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function (users) {

            $("#followListBody").empty();

            if (users.length === 0) {
                $("#followListBody").append("<p>Kimse yok.</p>");
                return;
            }

            for (const user of users) {//kullanıcıları listeler tıklanılabilir hale getirir
                $("#followListBody").append(`
                    <div class="follow-list-item" data-user-id="${user.id}">
                        ${user.username}
                    </div>
                `);
            }
        },

        error: function () {
            $("#followListBody").html("<p>Liste yüklenemedi.</p>");
        }
    });
}


function loadUserPosts(userId) {//postları getirme

    const token = localStorage.getItem("token");

    $.ajax({
        url: "http://localhost:8080/posts/user/" + userId,//PostController>getPostsByUserId
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function (posts) {

            $("#profilePosts").empty();

            if (posts.length === 0) {
                $("#profilePosts").append("<p>Henüz gönderi yok.</p>");
                return;
            }

            for (const post of posts) {//her postu ekrana basar

                const formattedDate =
                    new Date(post.createdAt)
                        .toLocaleString("tr-TR");

                $("#profilePosts").append(`
                    <article class="post">

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

                                <small>
                                    ${formattedDate}
                                </small>

                            </div>

                        </div>

                        <p class="post-content">
                            ${post.content}
                        </p>

                    </article>
                `);
            }
        },

        error: function () {
            alert("Profil yüklenemedi.");
        }
    });
}


function loadProfileInfo(userId) {

    const token = localStorage.getItem("token");
    const currentUserId = localStorage.getItem("userId");

    // 1) Profil sayılarını (followersCount, followingCount) çek
    $.ajax({
        url: "http://localhost:8080/users/" + userId,//UserController>getProfile
        method: "GET",
        headers: {
            Authorization: "Bearer " + token
        },

        success: function (profile) {

            $("#followerCount").text(profile.followersCount);//sayıları günceller
            $("#followingCount").text(profile.followingCount);

            const firstLetter = profile.username.charAt(0).toUpperCase();
            $("#profileAvatarLetter").text(firstLetter);
            $("#profileName").text(profile.username);//isim
            $("#profileUsernameTag").text("@" + profile.username);//profil harfi

            if (userId === currentUserId) {//kendi profilimse
                $("#profileUsername").text("Benim Profilim");
            } else {
                $("#profileUsername").text(profile.username);
            }
        },

        error: function () {
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

        success: function (followers) {

            const isFollowing = followers.some(function (follower) {
                return follower.id === currentUserId;
            });

            $("#followButton").text(isFollowing ? "Takipten Çık" : "Takip Et");
        },

        error: function () {
            // Takip durumu belirlenemedi, güvenli varsayılan
            $("#followButton").text("Takip Et");
        }
    });
}