(() => {
    "use strict";

    const token = sessionStorage.getItem("adminToken");
    if (!token) {
        window.location.replace("/admin-login.html");
        return;
    }

    const history = [];
    const maximumPoints = 36;
    let refreshTimer;

    const byId = id => document.getElementById(id);
    const number = value => new Intl.NumberFormat("tr-TR").format(value);
    const percent = value => value < 0 ? "Kullanılamıyor" : `${value.toFixed(1)}%`;
    const bytes = value => {
        if (value == null || value < 0) return "Kullanılamıyor";
        const units = ["B", "KB", "MB", "GB", "TB"];
        let size = value;
        let index = 0;
        while (size >= 1024 && index < units.length - 1) {
            size /= 1024;
            index++;
        }
        return `${size >= 10 || index === 0 ? size.toFixed(0) : size.toFixed(1)} ${units[index]}`;
    };
    const duration = seconds => {
        const days = Math.floor(seconds / 86400);
        const hours = Math.floor((seconds % 86400) / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return [days && `${days} gün`, hours && `${hours} sa`, `${minutes} dk`].filter(Boolean).join(" ");
    };
    const ratio = (used, total) => used < 0 || total <= 0 ? -1 : used * 100 / total;

    function setProgress(id, value) {
        byId(id).style.width = `${Math.max(0, Math.min(100, value < 0 ? 0 : value))}%`;
    }

    function updateChart(cpu, ram) {
        history.push({ cpu: Math.max(0, cpu), ram: Math.max(0, ram) });
        if (history.length > maximumPoints) history.shift();
        const width = 720;
        const height = 210;
        const top = 15;
        const points = key => history.map((entry, index) => {
            const x = history.length === 1 ? 0 : index * width / (history.length - 1);
            const y = top + height - Math.min(100, entry[key]) * height / 100;
            return `${x.toFixed(1)},${y.toFixed(1)}`;
        }).join(" ");
        byId("cpuLine").setAttribute("points", points("cpu"));
        byId("ramLine").setAttribute("points", points("ram"));
        byId("chartDescription").textContent =
            `Güncel CPU yüzde ${Math.max(0, cpu).toFixed(1)}, RAM yüzde ${Math.max(0, ram).toFixed(1)}.`;
    }

    function render(metrics) {
        const serverRamPercent = ratio(metrics.serverMemoryUsedBytes, metrics.serverMemoryTotalBytes);
        const jvmPercent = ratio(metrics.jvmHeapUsedBytes, metrics.jvmHeapMaxBytes);
        const diskPercent = ratio(metrics.diskUsedBytes, metrics.diskTotalBytes);

        byId("systemCpu").textContent = percent(metrics.systemCpuPercent);
        byId("processCpu").textContent = percent(metrics.processCpuPercent);
        byId("serverMemory").textContent = percent(serverRamPercent);
        byId("serverMemoryDetail").textContent =
            `${bytes(metrics.serverMemoryUsedBytes)} / ${bytes(metrics.serverMemoryTotalBytes)}`;
        byId("jvmMemory").textContent = percent(jvmPercent);
        byId("jvmMemoryDetail").textContent =
            `${bytes(metrics.jvmHeapUsedBytes)} / ${bytes(metrics.jvmHeapMaxBytes)}`;
        setProgress("systemCpuBar", metrics.systemCpuPercent);
        setProgress("processCpuBar", metrics.processCpuPercent);
        setProgress("serverMemoryBar", serverRamPercent);
        setProgress("jvmMemoryBar", jvmPercent);

        byId("serverName").textContent = metrics.serverName;
        byId("processId").textContent = metrics.processId;
        byId("processors").textContent = metrics.availableProcessors;
        byId("diskUsage").textContent = diskPercent < 0
            ? "Kullanılamıyor"
            : `${bytes(metrics.diskUsedBytes)} / ${bytes(metrics.diskTotalBytes)} (${diskPercent.toFixed(1)}%)`;
        byId("uptime").textContent = duration(metrics.uptimeSeconds);
        byId("databaseStatus").textContent = metrics.databaseStatus === "UP" ? "Bağlı" : "Bağlantı sorunu";
        byId("totalRequests").textContent = number(metrics.totalRequests);
        byId("averageResponse").textContent = `${metrics.averageResponseMs.toFixed(1)} ms`;
        byId("errorRate").textContent = `${metrics.errorRatePercent.toFixed(1)}%`;
        byId("failedRequests").textContent = `${number(metrics.failedRequests)} hatalı istek`;
        byId("liveThreads").textContent = number(metrics.liveThreads);
        byId("peakThreads").textContent = `${number(metrics.peakThreads)} tepe değer`;

        const status = byId("systemStatus");
        status.textContent = metrics.status === "UP" ? "Sistem çalışıyor" : "Kontrol gerekli";
        status.className = `admin-status ${metrics.status === "UP" ? "admin-status-up" : "admin-status-warning"}`;
        byId("lastUpdated").textContent =
            `Son güncelleme ${new Date(metrics.measuredAt).toLocaleTimeString("tr-TR")}`;
        byId("dashboardError").hidden = true;
        updateChart(metrics.systemCpuPercent, serverRamPercent < 0 ? jvmPercent : serverRamPercent);
    }

    async function loadMetrics() {
        try {
            const response = await fetch("/admin/metrics", {
                headers: { Authorization: `Bearer ${token}` },
                cache: "no-store"
            });
            if (response.status === 401 || response.status === 403) {
                sessionStorage.removeItem("adminToken");
                window.location.replace("/admin-login.html");
                return;
            }
            if (!response.ok) throw new Error("Sunucu metrikleri şu anda alınamadı.");
            render(await response.json());
        } catch (error) {
            const alert = byId("dashboardError");
            alert.textContent = error.message;
            alert.hidden = false;
            byId("systemStatus").textContent = "Bağlantı kesildi";
            byId("systemStatus").className = "admin-status admin-status-warning";
        }
    }

    byId("logoutButton").addEventListener("click", () => {
        sessionStorage.removeItem("adminToken");
        sessionStorage.removeItem("adminUsername");
        window.location.replace("/admin-login.html");
    });

    document.addEventListener("visibilitychange", () => {
        clearInterval(refreshTimer);
        if (!document.hidden) {
            loadMetrics();
            refreshTimer = setInterval(loadMetrics, 5000);
        }
    });

    loadMetrics();
    refreshTimer = setInterval(loadMetrics, 5000);
})();
