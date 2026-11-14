(function () {
    const ink = "#f4efe4";
    const muted = "#b7c0b4";
    const grid = "rgba(244, 239, 228, 0.08)";
    const solar = "#e6a23c";
    const wind = "#6fcec6";

    function read(id) {
        const node = document.getElementById(id);
        if (!node || !node.textContent.trim()) {
            return null;
        }
        return JSON.parse(node.textContent);
    }

    function scales() {
        return {
            x: { ticks: { color: muted, maxRotation: 0 }, grid: { color: grid } },
            y: {
                ticks: { color: muted },
                grid: { color: grid },
                title: { display: true, text: "kWh", color: muted }
            }
        };
    }

    function legend() {
        return { labels: { color: ink } };
    }

    function lines(canvasId, payload) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || !payload) {
            return;
        }
        new Chart(canvas, {
            type: "line",
            data: {
                labels: payload.labels,
                datasets: [
                    {
                        label: "East Array",
                        data: payload.solarKwh,
                        borderColor: solar,
                        backgroundColor: "rgba(230, 162, 60, 0.16)",
                        fill: true,
                        tension: 0.35,
                        spanGaps: false,
                        pointRadius: 0
                    },
                    {
                        label: "Ridge Turbine",
                        data: payload.windKwh,
                        borderColor: wind,
                        backgroundColor: "rgba(111, 206, 198, 0.12)",
                        fill: true,
                        tension: 0.35,
                        spanGaps: false,
                        pointRadius: 0
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: legend() },
                scales: scales()
            }
        });
    }

    function bars(canvasId, payload, datasets) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || !payload) {
            return;
        }
        new Chart(canvas, {
            type: "bar",
            data: { labels: payload.labels, datasets: datasets },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: legend() },
                scales: scales()
            }
        });
    }

    lines("dailyChart", read("daily-data"));
    bars("weekChart", read("weekly-data"), [
        { label: "East Array", data: (read("weekly-data") || {}).solarKwh, backgroundColor: solar },
        { label: "Ridge Turbine", data: (read("weekly-data") || {}).windKwh, backgroundColor: wind }
    ]);
    lines("forecastChart", read("forecast-data"));
    const comparison = read("comparison-data");
    bars("compareChart", comparison, [
        { label: "Predicted", data: comparison && comparison.predictedKwh, backgroundColor: "#8d7350" },
        { label: "Actual", data: comparison && comparison.actualKwh, backgroundColor: "#9cbf86" }
    ]);
})();
