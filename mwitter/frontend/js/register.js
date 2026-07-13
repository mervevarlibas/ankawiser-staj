$(document).ready(function () {

    $("#registerForm").submit(function (event) {

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
            url: "http://localhost:8080/users/register",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(registerData),

            success: function () {

                $("#message")
                    .removeClass("error-message")
                    .addClass("success-message")
                    .text("Hesabın oluşturuldu. Giriş sayfasına yönlendiriliyorsun.");

                setTimeout(function () {
                    window.location.href = "login.html";
                }, 1500);
            },

            error: function (xhr) {

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
});