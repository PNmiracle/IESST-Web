import http from "node:http";

const listenPort = Number(process.env.PREVIEW_PORT || 8090);
const upstreamHost = process.env.UPSTREAM_HOST || "127.0.0.1";
const upstreamPort = Number(process.env.UPSTREAM_PORT || 8080);

function blocked(request) {
  const path = new URL(request.url, "http://preview.local").pathname;
  if (!["GET", "HEAD"].includes(request.method || "")) return true;
  return path === "/admin"
    || path.startsWith("/admin/")
    || path.startsWith("/api/admin/")
    || path.startsWith("/api/auth/")
    || path.startsWith("/api/student/")
    || path.startsWith("/actuator/");
}

const server = http.createServer((request, response) => {
  if (blocked(request)) {
    response.writeHead(404, {
      "Cache-Control": "no-store",
      "Content-Type": "text/plain; charset=utf-8",
      "X-Content-Type-Options": "nosniff",
    });
    response.end("Preview route not available");
    return;
  }

  const upstream = http.request({
    host: upstreamHost,
    port: upstreamPort,
    method: request.method,
    path: request.url,
    headers: {
      ...request.headers,
      host: `${upstreamHost}:${upstreamPort}`,
      "x-forwarded-proto": "https",
    },
  }, (upstreamResponse) => {
    const headers = {
      ...upstreamResponse.headers,
      "cache-control": upstreamResponse.headers["cache-control"] || "no-cache",
      "referrer-policy": "strict-origin-when-cross-origin",
      "x-content-type-options": "nosniff",
      "x-frame-options": "SAMEORIGIN",
    };
    response.writeHead(upstreamResponse.statusCode || 502, headers);
    upstreamResponse.pipe(response);
  });

  upstream.on("error", () => {
    if (!response.headersSent) response.writeHead(502, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Preview service unavailable");
  });
  request.pipe(upstream);
});

server.listen(listenPort, "127.0.0.1", () => {
  console.log(`Read-only preview proxy: http://127.0.0.1:${listenPort}`);
});
