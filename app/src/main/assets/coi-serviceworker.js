/* coi-serviceworker.js - Cross-Origin Isolation (COOP + COEP) Service Worker for RetroVerse */
(() => {
  const DEFAULT_COEP = 'credentialless';

  if (typeof window === 'undefined') {
    self.addEventListener('install', () => {
      self.skipWaiting();
    });

    self.addEventListener('activate', (event) => {
      event.waitUntil(self.clients.claim());
    });

    self.addEventListener('message', (event) => {
      if (event.data && event.data.type === 'deregister') {
        self.registration
          .unregister()
          .then(() => self.clients.matchAll())
          .then((clients) => {
            clients.forEach((client) => client.navigate(client.url));
          });
      }
    });

    self.addEventListener('fetch', (event) => {
      const request = event.request;
      if (request.cache === 'only-if-cached' && request.mode !== 'same-origin') {
        return;
      }

      const coepMode = DEFAULT_COEP;
      const modifiedRequest =
        coepMode === 'credentialless' && request.mode === 'no-cors'
          ? new Request(request, { credentials: 'omit' })
          : request;

      event.respondWith(
        fetch(modifiedRequest)
          .then((response) => {
            if (!response || response.status === 0) {
              return response;
            }
            const headers = new Headers(response.headers);
            headers.set('Cross-Origin-Opener-Policy', 'same-origin');
            headers.set('Cross-Origin-Embedder-Policy', coepMode);
            if (!headers.has('Cross-Origin-Resource-Policy')) {
              headers.set('Cross-Origin-Resource-Policy', 'cross-origin');
            }
            return new Response(response.body, {
              status: response.status,
              statusText: response.statusText,
              headers: headers
            });
          })
          .catch((err) => {
            console.error('[COI SW] Fetch failed:', err);
            return fetch(request);
          })
      );
    });
  } else {
    if (window.crossOriginIsolated) return;
    if (!window.isSecureContext || !('serviceWorker' in navigator)) return;

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
      .catch((err) => {
        console.warn('[COI] Registration skipped or failed:', err);
      });
  }
})();
