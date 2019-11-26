'use strict';

(async function () {
    function init() {
        let router = new Router([
            new Route('signin', 'signin.html'),
            new Route('signup', 'signup.html'),
            new Route('camera', 'camera.html', true),
            new Route('newcamera', 'newcamera.html'),
            new Route('capture', 'capture.html'),
            new Route('webhook', 'webhook.html'),
            new Route('newwebhook', 'newwebhook.html')
        ]);
    }

    init();
    await updateNavUser();
}());