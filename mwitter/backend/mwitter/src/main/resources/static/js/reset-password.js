$(document).ready(function () {
    const token = new URLSearchParams(window.location.search).get("token");
    const form = $("#resetPasswordForm");
    const message = $("#message");

    if (!token) {
        message.addClass("error-message").text("Geçersiz şifre sıfırlama bağlantısı.");
        form.hide();
        return;
    }

    form.submit(function (event) {
        event.preventDefault();
        const newPassword = $("#newPassword").val();
        const confirmPassword = $("#confirmNewPassword").val();
        const button = $("#resetPasswordButton");

        if (newPassword.length < 6) {
            message.removeClass("success-message").addClass("error-message")
                .text("Yeni şifre en az 6 karakter olmalı.");
            return;
        }
        if (newPassword !== confirmPassword) {
            message.removeClass("success-message").addClass("error-message")
                .text("Şifreler eşleşmiyor.");
            return;
        }

        button.prop("disabled", true).text("Sıfırlanıyor...");
        $.ajax({
            url: "/users/reset-password",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ token: token, newPassword: newPassword }),
            success: function () {
                form.hide();
                message.removeClass("error-message").addClass("success-message")
                    .text("Şifren sıfırlandı. Giriş sayfasına yönlendiriliyorsun...");
                window.history.replaceState({}, document.title, "reset-password.html");
                setTimeout(() => window.location.href = "login.html", 1800);
            },
            error: function (xhr) {
                const backendMessage = xhr.responseJSON?.message || "";
                const expired = backendMessage.toLowerCase().includes("expired");
                message.removeClass("success-message").addClass("error-message")
                    .text(expired
                        ? "Bu sıfırlama bağlantısının süresi dolmuş. Lütfen yeni bağlantı iste."
                        : "Bağlantı geçersiz, kullanılmış veya süresi dolmuş.");
                button.prop("disabled", false).text("Şifreyi Sıfırla");
            }
        });
    });
});
