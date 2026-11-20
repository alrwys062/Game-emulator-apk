/* coi-serviceworker.js - Cross-Origin Isolation (COOP + COEP) & Offline Engine Cache SW for RetroVerse */
(() => {
  const SHELL_CACHE = 'rv-shell-v1';
  const EJS_CACHE = 'ejs-v1';
  const DEFAULT_COEP = 'credentialless';
  const SHELL_URLS = ['./', './index.html', './coi-serviceworker.js', './icon-192.png', './icon-512.png'];

  function withIsolationHeaders(response) {
    if (!response || response.status === 0 || response.type === 'opaque') return response;
    const h = new Headers(response.headers);
    h.set('Cross-Origin-Opener-Policy', 'same-origin');
    h.set('Cross-Origin-Embedder-Policy', DEFAULT_COEP);
    if (!h.has('Cross-Origin-Resource-Policy')) {
      h.set('Cross-Origin-Resource-Policy', 'cross-origin');
    }
    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: h
    });
  }

  function isEjsCdnRequest(urlStr) {
    try {
      const u = new URL(urlStr);
      return u.hostname === 'cdn.emulatorjs.org' || u.hostname === 'cdn.jsdelivr.net';
    } catch (_) {
      return false;
    }
  }

  if (typeof window === 'undefined') {
    self.addEventListener('install', (event) => {
      event.waitUntil(
        caches.open(SHELL_CACHE).then(async (cache) => {
          for (const path of SHELL_URLS) {
            try {
              const res = await fetch(new Request(path, { cache: 'reload' }));
              if (res && res.ok) await cache.put(path, res);
            } catch (_) {}
          }
        }).finally(() => self.skipWaiting())
      );
    });

    self.addEventListener('activate', (event) => {
      event.waitUntil(
        caches.keys().then((keys) =>
          Promise.all(
            keys
              .filter((k) => k !== SHELL_CACHE && k !== EJS_CACHE)
              .map((k) => caches.delete(k))
          )
        ).then(() => self.clients.claim())
      );
    });

    self.addEventListener('message', (event) => {
      const data = event.data || {};
      if (data.type === 'deregister') {
        self.registration
          .unregister()
          .then(() => self.clients.matchAll())
          .then((clients) => clients.forEach((c) => c.navigate(c.url)));
      } else if (data.type === 'SKIP_WAITING') {
        self.skipWaiting();
      } else if (data.type === 'CLEAR_EJS_CACHE') {
        caches.delete(EJS_CACHE);
      }
    });

    self.addEventListener('fetch', (event) => {
      const request = event.request;
      if (request.method !== 'GET' && request.method !== 'HEAD') return;
      if (request.cache === 'only-if-cached' && request.mode !== 'same-origin') return;

      // 1) Cache-first for EmulatorJS CDN assets (cors + omit credentials to avoid opaque quota bloat)
      if (isEjsCdnRequest(request.url)) {
        event.respondWith(
          caches.open(EJS_CACHE).then(async (cache) => {
            const cached = await cache.match(request, { ignoreVary: true });
            if (cached) {
              return withIsolationHeaders(cached);
            }
            const corsReq = new Request(request.url, {
              method: request.method,
              mode: 'cors',
              credentials: 'omit'
            });
            const netRes = await fetch(corsReq);
            if (netRes && netRes.ok && netRes.type !== 'opaque' && request.method === 'GET') {
              try {
                await cache.put(request, netRes.clone());
              } catch (_) {}
            }
            return withIsolationHeaders(netRes);
          }).catch(() => caches.match(request, { ignoreVary: true }))
        );
        return;
      }

      // 2) Navigation requests: network-first + offline navigate fallback to cached index.html
      if (request.mode === 'navigate') {
        event.respondWith(
          fetch(request)
            .then((res) => {
              if (res && res.ok) {
                const clone = res.clone();
                caches.open(SHELL_CACHE).then((c) => c.put('./index.html', clone)).catch(() => {});
              }
              return withIsolationHeaders(res);
            })
            .catch(async () => {
              const cache = await caches.open(SHELL_CACHE);
              const fallback = (await cache.match('./index.html')) || (await cache.match('./'));
              return fallback ? withIsolationHeaders(fallback) : Response.error();
            })
        );
        return;
      }

      // 3) Same-origin / app shell & local ./ejs/data/: cache fallback + COOP/COEP headers
      const modifiedRequest =
        DEFAULT_COEP === 'credentialless' && request.mode === 'no-cors'
          ? new Request(request, { credentials: 'omit' })
          : request;

      event.respondWith(
        fetch(modifiedRequest)
          .then((res) => {
            if (res && res.ok && request.method === 'GET' && new URL(request.url).origin === self.location.origin) {
              const clone = res.clone();
              caches.open(SHELL_CACHE).then((c) => c.put(request, clone)).catch(() => {});
            }
            return withIsolationHeaders(res);
          })
          .catch(async () => {
            const cached = await caches.match(request, { ignoreSearch: false });
            return cached ? withIsolationHeaders(cached) : fetch(request);
          })
      );
    });
  } else {
    // Window registration helper
    if ('storage' in navigator && typeof navigator.storage.persist === 'function') {
      navigator.storage.persist().catch(() => {});
    }
    if (window.crossOriginIsolated || !window.isSecureContext || !('serviceWorker' in navigator)) return;

    const swSrc = (document.currentScript && document.currentScript.src) || 'coi-serviceworker.js';
    navigator.serviceWorker
      .register(swSrc)
      .then((reg) => {
        reg.addEventListener('updatefound', () => {
          const nw = reg.installing;
          if (!nw) return;
          nw.addEventListener('statechange', () => {
            if (nw.state === 'activated' && !navigator.serviceWorker.controller) {
              if (!sessionStorage.getItem('rv_coi_reloaded')) {
                sessionStorage.setItem('rv_coi_reloaded', '1');
                window.location.reload();
              }
            }
          });
        });
        if (reg.active && !navigator.serviceWorker.controller) {
          if (!sessionStorage.getItem('rv_coi_reloaded')) {
            sessionStorage.setItem('rv_coi_reloaded', '1');
            window.location.reload();
          }
        }
      })
      .catch(() => {});
  }
})();
