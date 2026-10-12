export function getLoginUser() {
    return JSON.parse(localStorage.getItem('system-user') || '{}')
}

export function getToken() {
    return localStorage.getItem('token') || ''
}

export function removeLoginUser() {
    localStorage.removeItem('system-user')
    localStorage.removeItem('token')
}

export function setLoginUser(user) {
    localStorage.setItem('system-user', JSON.stringify(user))
}

export function setToken(token) {
    localStorage.setItem('token', token)
}