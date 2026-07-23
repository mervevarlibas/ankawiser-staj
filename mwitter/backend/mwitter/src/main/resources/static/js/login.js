$(document).ready(function () {//HTML tamamen yüklendikten sonra JavaScript’in çalışmasını sağlar
    let loginType = "user";

    $(".login-type-option").click(function () {
        loginType = $(this).data("login-type");
        $(".login-type-option")
            .removeClass("active")
            .attr("aria-pressed", "false");
        $(this)
            .addClass("active")
            .attr("aria-pressed", "true");

        const isAdmin = loginType === "admin";
        $("#loginSubmitButton").text(isAdmin ? "Yönetici Paneline Gir" : "Giriş Yap");
        $("#loginTypeDescription").text(
            isAdmin
                ? "Yalnızca yetkili yönetici hesabı kabul edilir."
                : "Mwitter hesabınla giriş yap."
        );
        $("#message").text("");
    });

    $("#loginForm").submit(function (event) {//id="loginForm" olan form gönderildiğinde bu fonksiyon çalışır

        event.preventDefault();//Formun normal davranışını durdurur. Bunu yazmazsak sayfa yenilenir

        const email = $("#email").val();//val metodu kutu içindeki yazıyı getirir
        const password = $("#password").val();//HTML’deki inputların değerini alır.

        const loginData = {//Backend’e göndereceğimiz JavaScript nesnesini oluşturur
            email: email,
            password: password
        };

        $.ajax({//ajax burda çalışmaya başlıyor
            url: "/users/login",
            method: "POST",
            contentType: "application/json",//Backend’e gönderdiğimiz verinin JSON olduğunu belirtir.
            data: JSON.stringify(loginData),//JavaScript nesnesini JSON metnine çevirir

            success: function (response) {//Backend 200 OK döndürürse çalışır.
                if (loginType === "admin") {
                    if (response.role !== "ADMIN") {
                        $("#message").text("Bu hesap yönetici paneline erişemez.");
                        return;
                    }
                    sessionStorage.setItem("adminToken", response.token);
                    sessionStorage.setItem("adminUsername", response.username);
                    window.location.href = "admin-dashboard.html";
                    return;
                }

                localStorage.setItem("token", response.token);//JWT’yi tarayıcıda saklar.
                localStorage.setItem("userId", response.id);//localstorage tarayıcının kücük vtabanıdır
                localStorage.setItem("username", response.username);//Kullanıcının id ve kullanıcı adını da saklar.

                window.location.href = "index.html";//Başarılı girişten sonra ana sayfaya yönlendirir
            },

            error: function (xhr) {//Backend 400, 401 gibi hata döndürürse çalışır.

                let message = "Giriş yapılamadı.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;//Backend’den gelen hata mesajını alır.
                }

                $("#message").text(message);//hata mesajını sayfada gösterir.
            }
        });
    });

    function closeForgotPasswordModal() {
        $("#forgotPasswordModal").hide();
    }

    $("#forgotPasswordLink").click(function (event) {
        event.preventDefault();
        $("#forgotPasswordMessage").removeClass("success-message error-message").text("");
        $("#forgotPasswordEmail").val($("#email").val().trim());
        $("#forgotPasswordModal").css("display", "flex");
        $("#forgotPasswordEmail").trigger("focus");
    });

    $("#closeForgotPasswordModal").click(closeForgotPasswordModal);
    $("#forgotPasswordModal").click(function (event) {
        if (event.target === this) closeForgotPasswordModal();
    });
    $(document).keydown(function (event) {
        if (event.key === "Escape") closeForgotPasswordModal();
    });

    $("#forgotPasswordEmail").keydown(function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            $("#sendResetLinkButton").trigger("click");
        }
    });

    $("#sendResetLinkButton").click(function () {
        const emailInput = document.getElementById("forgotPasswordEmail");
        const email = emailInput.value.trim();
        const button = $(this);
        const message = $("#forgotPasswordMessage");

        if (!email || !emailInput.checkValidity()) {
            message.removeClass("success-message").addClass("error-message")
                .text("Lütfen geçerli bir e-posta adresi gir.");
            return;
        }

        button.prop("disabled", true).text("Gönderiliyor...");
        message.removeClass("success-message error-message").text("");
        $.ajax({
            url: "/users/forgot-password",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ email: email }),
            success: function () {
                message.removeClass("error-message").addClass("success-message")
                    .text("E-posta kayıtlıysa sıfırlama bağlantısı gönderildi. Gelen kutunu kontrol et.");
            },
            error: function () {
                message.removeClass("success-message").addClass("error-message")
                    .text("Şu anda bağlantı gönderilemedi. Lütfen daha sonra tekrar dene.");
            },
            complete: function () {
                button.prop("disabled", false).text("Sıfırlama Linki Gönder");
            }
        });
    });
});
//JSON.stringify JavaScript nesnesini JSON metnine çeviriyor
