'use strict';

(function () {
    function init() {
        let router = new Router([
            new Route('signin', 'signin.html'),
            new Route('signup', 'signup.html'),
            new Route('camera', 'camera.html')
        ]);
    }

    init();
    updateNavUser();
}());