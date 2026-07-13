$(document).ready(function () {//HTML tamamen yüklendikten sonra JavaScript’in çalışmasını sağlar

    $("#loginForm").submit(function (event) {//id="loginForm" olan form gönderildiğinde bu fonksiyon çalışır

        event.preventDefault();//Formun normal davranışını durdurur. Bunu yazmazsak sayfa yenilenir

        const email = $("#email").val();//val metodu kutu içindeki yazıyı getirir
        const password = $("#password").val();//HTML’deki inputların değerini alır.

        const loginData = {//Backend’e göndereceğimiz JavaScript nesnesini oluşturur
            email: email,
            password: password
        };

        $.ajax({//ajax burda çalışmaya başlıyor
            url: "http://localhost:8080/users/login",
            method: "POST",
            contentType: "application/json",//Backend’e gönderdiğimiz verinin JSON olduğunu belirtir.
            data: JSON.stringify(loginData),//JavaScript nesnesini JSON metnine çevirir

            success: function (response) {//Backend 200 OK döndürürse çalışır.

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
});
//JSON.stringify JavaScript nesnesini JSON metnine çeviriyor