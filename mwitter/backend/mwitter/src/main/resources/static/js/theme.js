(function () {
    "use strict";

    var storageKey = "mwitter-theme";

    function getSavedTheme() {
        try {
            return localStorage.getItem(storageKey);
        } catch (error) {
            return null;
        }
    }

    function saveTheme(theme) {
        try {
            localStorage.setItem(storageKey, theme);
        } catch (error) {
            // Depolama kapalıysa tema mevcut oturumda çalışmayı sürdürür.
        }
    }

    function updateButton(button, isDark) {
        button.textContent = isDark ? "☀ Açık Mod" : "☾ Karanlık Mod";
        button.setAttribute("aria-pressed", String(isDark));
        button.setAttribute("aria-label", isDark ? "Açık temaya geç" : "Karanlık temaya geç");
    }

    document.body.classList.toggle("dark-mode", getSavedTheme() === "dark");

    document.addEventListener("DOMContentLoaded", function () {
        var button = document.getElementById("themeToggle");
        if (!button) return;

        updateButton(button, document.body.classList.contains("dark-mode"));
        button.addEventListener("click", function () {
            var isDark = document.body.classList.toggle("dark-mode");
            saveTheme(isDark ? "dark" : "light");
            updateButton(button, isDark);
        });
    });
})();
