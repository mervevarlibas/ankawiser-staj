(() => {
    "use strict";

    const setupMobileKeyboard = () => {
        if (!window.visualViewport || !window.matchMedia("(max-width: 760px)").matches) {
            return;
        }

        const root = document.documentElement;
        let baselineHeight = Math.max(window.innerHeight, window.visualViewport.height);

        const updateKeyboardState = () => {
            const viewport = window.visualViewport;
            const editableFocused = document.activeElement?.matches(
                "input, textarea, select, [contenteditable='true']"
            );
            if (!editableFocused) {
                baselineHeight = Math.max(window.innerHeight, viewport.height + viewport.offsetTop);
            }
            const keyboardHeight = Math.max(
                0,
                baselineHeight - viewport.height - viewport.offsetTop
            );
            const keyboardOpen = editableFocused && keyboardHeight > 120;

            root.classList.toggle("mobile-keyboard-open", keyboardOpen);
            root.style.setProperty(
                "--mobile-keyboard-height",
                keyboardOpen ? `${keyboardHeight}px` : "0px"
            );
        };

        window.visualViewport.addEventListener("resize", updateKeyboardState);
        window.visualViewport.addEventListener("scroll", updateKeyboardState);
        document.addEventListener("focusin", updateKeyboardState);
        document.addEventListener("focusout", () => {
            window.setTimeout(updateKeyboardState, 80);
        });
    };

    setupMobileKeyboard();

    if ("serviceWorker" in navigator) {
        window.addEventListener("load", () => {
            navigator.serviceWorker.register("/sw.js").catch(error => {
                console.warn("Mwitter çevrimdışı desteği başlatılamadı:", error);
            });
        });
    }

    const standalone = window.matchMedia("(display-mode: standalone)").matches
        || window.navigator.standalone === true;
    if (standalone) {
        document.documentElement.classList.add("is-standalone");
        return;
    }

    const isLoginPage = window.location.pathname === "/login.html";
    if (!isLoginPage) {
        return;
    }

    const isIos = /iphone|ipad|ipod/i.test(navigator.userAgent);
    let installPrompt;

    const createInstallButton = () => {
        if (document.querySelector(".pwa-install-button")) return;

        const button = document.createElement("button");
        button.type = "button";
        button.className = "pwa-install-button";
        button.textContent = "Mwitter’ı yükle";
        button.addEventListener("click", async () => {
            if (installPrompt) {
                installPrompt.prompt();
                await installPrompt.userChoice;
                installPrompt = null;
                button.remove();
                return;
            }
            showIosInstructions();
        });
        document.body.appendChild(button);
    };

    const showIosInstructions = () => {
        const overlay = document.createElement("div");
        overlay.className = "pwa-install-overlay";
        overlay.innerHTML = `
            <section class="pwa-install-sheet" role="dialog" aria-modal="true" aria-labelledby="pwaInstallTitle">
                <button type="button" class="pwa-install-close" aria-label="Kapat">×</button>
                <img src="/icons/apple-touch-icon.png" alt="" width="64" height="64">
                <h2 id="pwaInstallTitle">Mwitter’ı ana ekrana ekle</h2>
                <ol>
                    <li>Safari araç çubuğundaki <strong>Paylaş</strong> simgesine dokun.</li>
                    <li><strong>Ana Ekrana Ekle</strong> seçeneğini seç.</li>
                    <li>Sağ üstteki <strong>Ekle</strong> düğmesine dokun.</li>
                </ol>
            </section>`;

        const close = () => overlay.remove();
        overlay.querySelector(".pwa-install-close").addEventListener("click", close);
        overlay.addEventListener("click", event => {
            if (event.target === overlay) close();
        });
        document.body.appendChild(overlay);
    };

    window.addEventListener("beforeinstallprompt", event => {
        event.preventDefault();
        installPrompt = event;
        createInstallButton();
    });

    window.addEventListener("appinstalled", () => {
        document.querySelector(".pwa-install-button")?.remove();
    });

    if (isIos) {
        window.addEventListener("DOMContentLoaded", createInstallButton, { once: true });
    }
})();
