(function (global) {
    "use strict";

    const apiBase = "http://localhost:8080";
    let activeInput = null;
    let mentionStart = -1;
    let mentionEnd = -1;
    let selectedIndex = 0;
    let requestNumber = 0;
    let debounceTimer = null;

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function renderMentionedContent(content, mentions) {
        const usersByName = new Map(
            (mentions || []).map(mention => [mention.username.toLocaleLowerCase(), mention])
        );
        const mentionPattern = /(^|[^\p{L}\p{N}._])@([\p{L}\p{N}._]+)/gu;
        let html = "";
        let lastIndex = 0;

        for (const match of String(content ?? "").matchAll(mentionPattern)) {
            const mention = usersByName.get(match[2].toLocaleLowerCase());
            const usernameStart = match.index + match[1].length;
            html += escapeHtml(String(content).slice(lastIndex, usernameStart));
            html += mention
                ? `<a class="mention-link" href="profile.html?userId=${encodeURIComponent(mention.userId)}">@${escapeHtml(match[2])}</a>`
                : escapeHtml("@" + match[2]);
            lastIndex = usernameStart + match[2].length + 1;
        }
        return html + escapeHtml(String(content ?? "").slice(lastIndex));
    }

    function getDropdown() {
        let dropdown = document.getElementById("mentionSuggestions");
        if (!dropdown) {
            dropdown = document.createElement("div");
            dropdown.id = "mentionSuggestions";
            dropdown.className = "mention-suggestions";
            dropdown.hidden = true;
            document.body.appendChild(dropdown);
        }
        return dropdown;
    }

    function isMentionInput(element) {
        return element && (element.id === "content" || element.classList.contains("comment-input"));
    }

    function currentMention(input) {
        const caret = input.selectionStart;
        const beforeCaret = input.value.slice(0, caret);
        const match = beforeCaret.match(/(^|[^\p{L}\p{N}._])@([\p{L}\p{N}._]*)$/u);
        if (!match) return null;
        return { query: match[2], start: caret - match[2].length - 1, end: caret };
    }

    function positionDropdown(input) {
        const rect = input.getBoundingClientRect();
        const dropdown = getDropdown();
        dropdown.style.left = Math.max(8, rect.left + window.scrollX) + "px";
        dropdown.style.top = rect.bottom + window.scrollY + 6 + "px";
        dropdown.style.width = Math.max(220, Math.min(rect.width, 360)) + "px";
    }

    function hideSuggestions() {
        const dropdown = getDropdown();
        dropdown.hidden = true;
        dropdown.innerHTML = "";
        activeInput = null;
    }

    function selectUser(user) {
        if (!activeInput || mentionStart < 0) return;
        const before = activeInput.value.slice(0, mentionStart);
        const after = activeInput.value.slice(mentionEnd);
        const replacement = "@" + user.username + " ";
        activeInput.value = before + replacement + after;
        const caret = before.length + replacement.length;
        activeInput.focus();
        activeInput.setSelectionRange(caret, caret);
        activeInput.dispatchEvent(new Event("input", { bubbles: true }));
        hideSuggestions();
    }

    function renderSuggestions(users) {
        const dropdown = getDropdown();
        dropdown.innerHTML = "";
        const currentUserId = localStorage.getItem("userId");
        const visibleUsers = users.filter(user => user.id !== currentUserId).slice(0, 8);
        if (!visibleUsers.length) { hideSuggestions(); return; }
        visibleUsers.forEach(function (user, index) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "mention-suggestion" + (index === selectedIndex ? " active" : "");
            button.innerHTML = `<span class="mention-avatar">${escapeHtml(user.username.charAt(0).toUpperCase())}</span><span>@${escapeHtml(user.username)}</span>`;
            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
                selectUser(user);
            });
            dropdown.appendChild(button);
        });
        dropdown.hidden = false;
        positionDropdown(activeInput);
    }

    function searchUsers(input, mention) {
        activeInput = input;
        mentionStart = mention.start;
        mentionEnd = mention.end;
        selectedIndex = 0;
        const thisRequest = ++requestNumber;
        const token = localStorage.getItem("token");
        fetch(apiBase + "/users/search?username=" + encodeURIComponent(mention.query), {
            headers: token ? { Authorization: "Bearer " + token } : {}
        }).then(response => response.ok ? response.json() : Promise.reject())
            .then(users => { if (thisRequest === requestNumber) renderSuggestions(users); })
            .catch(() => { if (thisRequest === requestNumber) hideSuggestions(); });
    }

    document.addEventListener("input", function (event) {
        if (!isMentionInput(event.target)) return;
        clearTimeout(debounceTimer);
        const mention = currentMention(event.target);
        if (!mention) { hideSuggestions(); return; }
        debounceTimer = setTimeout(() => searchUsers(event.target, mention), 180);
    });

    document.addEventListener("keydown", function (event) {
        const dropdown = getDropdown();
        if (dropdown.hidden || event.target !== activeInput) return;
        const buttons = Array.from(dropdown.querySelectorAll(".mention-suggestion"));
        if (event.key === "ArrowDown" || event.key === "ArrowUp") {
            event.preventDefault();
            selectedIndex = (selectedIndex + (event.key === "ArrowDown" ? 1 : -1) + buttons.length) % buttons.length;
            buttons.forEach((button, index) => button.classList.toggle("active", index === selectedIndex));
        } else if (event.key === "Enter" || event.key === "Tab") {
            event.preventDefault();
            buttons[selectedIndex]?.dispatchEvent(new MouseEvent("mousedown", { bubbles: true }));
        } else if (event.key === "Escape") {
            hideSuggestions();
        }
    }, true);

    document.addEventListener("mousedown", event => {
        if (!getDropdown().contains(event.target) && event.target !== activeInput) hideSuggestions();
    });
    window.addEventListener("resize", () => activeInput && positionDropdown(activeInput));
    window.addEventListener("scroll", () => activeInput && positionDropdown(activeInput), true);

    global.renderMentionedContent = renderMentionedContent;
})(window);
