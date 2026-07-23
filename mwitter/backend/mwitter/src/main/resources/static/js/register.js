$(document).ready(function() {

    let registeredEmail = ""; //hangi email için kod doğrulaması yapacağımızı burda tutuyoruz
    let codeTimerInterval = null;

    function startCodeTimer(seconds) {

        // Önceki timer çalışıyorsa durdur (resend sonrası tekrar başlatınca çakışmasın)
        if (codeTimerInterval) {
            clearInterval(codeTimerInterval);
        }

        let remaining = seconds;
        updateTimerDisplay(remaining);

        $("#verifyCodeButton").prop("disabled", false);

        codeTimerInterval = setInterval(function() {
            remaining--;

            if (remaining <= 0) {
                clearInterval(codeTimerInterval);
                $("#codeTimer").text("Kodun süresi doldu. Yeni kod iste.");
                $("#verifyCodeButton").prop("disabled", true);
                return;
            }

            updateTimerDisplay(remaining);
        }, 1000);
    }

    function updateTimerDisplay(remaining) {
        const minutes = Math.floor(remaining / 60);
        const seconds = remaining % 60;
        const secondsPadded = seconds < 10 ? "0" + seconds : seconds;
        $("#codeTimer").text("Kalan süre: " + minutes + ":" + secondsPadded);
    }
    $("#registerForm").submit(function(event) {

        event.preventDefault();

        const username = $("#username").val().trim();
        const email = $("#email").val().trim();
        const phoneNumber = $("#phoneNumber").val().trim();
        const password = $("#password").val();

        const registerData = {
            username: username,
            email: email,
            password: password,
            phoneNumber: phoneNumber
        };

        $.ajax({
            url: "/users/register",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(registerData),

            success: function() {

                registeredEmail = email;
                $("#message").text("");
                $("#verifyMessage")
                    .removeClass("error-message")
                    .addClass("success-message")
                    .text("Hesabın oluşturuldu. Mailine gelen kodu gir."); //logine atmak yerine doğrulama bölümüne

                // Kayıt formunu gizle, doğrulama bölümünü göster
                $("#registerForm").hide();
                $("#verificationSection").show();
                startCodeTimer(120);
            },

            error: function(xhr) {

                let message = "Kayıt işlemi başarısız oldu.";

                if (xhr.responseJSON) {

                    if (
                        xhr.responseJSON.errors &&
                        xhr.responseJSON.errors.length > 0
                    ) {
                        message = xhr.responseJSON.errors.join(" ");
                    } else if (xhr.responseJSON.message) {
                        message = xhr.responseJSON.message;
                    }
                }

                $("#message")
                    .removeClass("success-message")
                    .addClass("error-message")
                    .text(message);
            }
        });
    });
    // Doğrulama kodu gönder butonu
    $("#verifyCodeButton").click(function() {

        const code = $("#verificationCode").val().trim();

        if (code === "") {
            $("#verifyMessage")
                .removeClass("success-message")
                .addClass("error-message")
                .text("Lütfen kodu gir.");
            return;
        }

        const verifyData = {
            email: registeredEmail,
            code: code
        };

        $.ajax({
            url: "/users/verify-email",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(verifyData),

            success: function() {

                $("#verifyMessage")
                    .removeClass("error-message")
                    .addClass("success-message")
                    .text("Hesabın doğrulandı! Giriş sayfasına yönlendiriliyorsun.");

                setTimeout(function() {
                    window.location.href = "login.html";
                }, 1500);
            },

            error: function(xhr) {

                let message = "Doğrulama başarısız oldu.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                $("#verifyMessage")
                    .removeClass("success-message")
                    .addClass("error-message")
                    .text(message);
            }
        });
    });
    $("#resendCodeButton").click(function() {

        const button = $(this);
        button.prop("disabled", true); // spam tıklamayı önlemek için

        $.ajax({
            url: "/users/resend-verification-code",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ email: registeredEmail }),

            success: function() {

                $("#verifyMessage")
                    .removeClass("error-message")
                    .addClass("success-message")
                    .text("Yeni kod gönderildi, mailini kontrol et.");

                startCodeTimer(120);
                // 30 saniye boyunca butonu tekrar tıklanamaz yapıyoruz,
                // kullanıcı art arda mail bombardımanı yapamasın diye.
                let secondsLeft = 30;
                button.text("Tekrar gönder (" + secondsLeft + "s)");

                const countdown = setInterval(function() {
                    secondsLeft--;
                    if (secondsLeft <= 0) {
                        clearInterval(countdown);
                        button.prop("disabled", false).text("Kodu tekrar gönder");
                    } else {
                        button.text("Tekrar gönder (" + secondsLeft + "s)");
                    }
                }, 1000);
            },

            error: function(xhr) {

                let message = "Kod tekrar gönderilemedi.";

                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }

                $("#verifyMessage")
                    .removeClass("success-message")
                    .addClass("error-message")
                    .text(message);

                button.prop("disabled", false);
            }
        });
    });
});
