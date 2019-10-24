async function handleSignUp(form) {
    let user = {
        'email': form.email.value,
        'password': form.password.value
    };
    let response = await fetch('api/auth/create', {
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
    let email = form.email.value;
    let password = form.password.value;

    let response = await fetch('api/auth/login?email=' + email + '&password=' + password, {
        method: 'GET'
    });

    if (response.status !== 200) {
        let data = await response.json();
        console.log(data.message)
    } else {
        let data = await response.json();
        localStorage.setItem('bearer', data.token);
        await updateNavUser();
        location.href = '#items'
    }
}

async function handleSignOut() {
    if (isBearerCached()) {
        let response = await fetch_secure('api/auth/logout', {method: 'GET'});
        if (response.status !== 200) {
            console.log('user was not logged out');
        } else {
            localStorage.removeItem('bearer');
            await updateNavUser();
            location.href = '#items';
        }
    }
}

function isBearerCached() {
    return localStorage.getItem('bearer') !== null;
}

async function getCurrentUser() {
    if (isBearerCached()) {
        let response = await fetch_secure('api/auth/currentuser', {method: 'GET'});
        if (response.status !== 200) {
            localStorage.removeItem('bearer');
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
        document.getElementById('navUser').innerHTML = `${currentUser.firstName} ${currentUser.lastName} | <a href="#" onclick="handleSignOut()">Sign out</a>`;
    } else {
        document.getElementById('navUser').innerHTML = '<a href="#signin">Sign in</a> or <a href="#signup">Sign up</a>'
    }
}