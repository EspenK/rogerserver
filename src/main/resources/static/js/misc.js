function waitForElementFirst(element, time, func, arg) {
    let container;
    if (document.getElementById(element) != null) {
        container = document.getElementById(element);
        setTimeout(function () {
            func(container, arg);
        }, time);
    } else {
        setTimeout(function () {
            waitForElementFirst(element, time, func, arg);
        }, time);
    }
}

async function fetch_secure(input, init) {
    if (!isTokenCached()) {
        return null;
    }

    init = init || {};
    init.headers = init.headers || {};

    init.withCredentials = true;
    init.credentials = 'include';
    init.headers.Authorization = 'Bearer ' + localStorage.token;

    return await fetch(input, init);
}