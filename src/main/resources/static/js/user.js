async function handleSignUp(form) {
    let user = {
        'email': form.email.value,
        'password': form.password.value,
        'displayName': form.displayName.value
    };
    let response = await fetch('/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(user)
    });

    if (response.status !== 200) {
        let data = await response.json();
        console.log(data.message)
    } else {
        location.href = '#signin';
    }
}

async function handleSignIn(form) {
    let user = {
        'email': form.email.value,
        'password': form.password.value
    };

    let response = await fetch('/users/authorize', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(user)
    });

    if (response.status !== 200) {
        let data = await response.json();
        console.log(data.message)
    } else {
        let token = await response.headers.get('Authorization');
        token = token.replace('Bearer ', '');
        localStorage.setItem('token', token);

        await updateNavUser();
        await showCameraList();
        location.href = '#camera';
    }
}

async function handleSignOut() {
    if (isTokenCached()) {
        let response = await fetch_secure('api/auth/logout', {method: 'GET'});
        if (response.status !== 200) {
            console.log('user was not logged out');
        } else {
            localStorage.removeItem('token');
            await updateNavUser();
            location.href = '#items';
        }
    }
}

function isTokenCached() {
    return localStorage.getItem('token') !== null;
}

async function getCurrentUser() {
    if (isTokenCached()) {
        let response = await fetch_secure('users/current', {method: 'GET'});
        if (response.status !== 200) {
            localStorage.removeItem('token');
            return null;
        } else {
            return await response.json();
        }
    } else {
        return null;
    }
}

async function updateNavUser() {
    let currentUser = await getCurrentUser();
    if (currentUser != null) {
        document.getElementById('navUser').innerHTML = `${currentUser.displayName} | <a href="#" onclick="handleSignOut()">Sign out</a>`;
    } else {
        document.getElementById('navUser').innerHTML = '<a href="#signin">Sign in</a> or <a href="#signup">Sign up</a>'
    }
}