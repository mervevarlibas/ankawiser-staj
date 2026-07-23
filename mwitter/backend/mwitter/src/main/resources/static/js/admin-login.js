(() => {
    "use strict";

    const existingToken = sessionStorage.getItem("adminToken");
    if (existingToken) {
        window.location.replace("/admin-dashboard.html");
        return;
    }

    const form = document.getElementById("adminLoginForm");
    const button = document.getElementById("adminLoginButton");
    const message = document.getElementById("message");

    form.addEventListener("submit", async event => {
        event.preventDefault();
        button.disabled = true;
        button.textContent = "Kontrol ediliyor…";
        message.textContent = "";

        try {
            const response = await fetch("/users/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: document.getElementById("email").value.trim(),
                    password: document.getElementById("password").value
                })
            });
            const payload = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(payload.message || "Giriş bilgileri doğrulanamadı.");
            }
            if (payload.role !== "ADMIN") {
                throw new Error("Bu hesap yönetici paneline erişemez.");
            }

            sessionStorage.setItem("adminToken", payload.token);
            sessionStorage.setItem("adminUsername", payload.username);
            window.location.replace("/admin-dashboard.html");
        } catch (error) {
            message.textContent = error.message;
        } finally {
            button.disabled = false;
            button.textContent = "Panele gir";
        }
    });
})();
