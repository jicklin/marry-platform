const TOKEN_KEY = 'marry:token'
const TOKEN_TYPE_KEY = 'marry:token-type'
const REFRESH_KEY = 'marry:refresh-token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string, tokenType: string = 'Bearer') {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_TYPE_KEY, tokenType)
}

export function getTokenType(): string {
  return localStorage.getItem(TOKEN_TYPE_KEY) || 'Bearer'
}

export function getRefreshToken(): string {
  return localStorage.getItem(REFRESH_KEY) || ''
}

export function setRefreshToken(token: string) {
  localStorage.setItem(REFRESH_KEY, token)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_TYPE_KEY)
  localStorage.removeItem(REFRESH_KEY)
}