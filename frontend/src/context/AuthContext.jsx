import { createContext, useContext, useState, useCallback } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('accessToken'))
  const [email, setEmail] = useState(localStorage.getItem('userEmail'))

  const login = useCallback(async (loginEmail, password) => {
    const res = await client.post('/auth/login', { email: loginEmail, password })
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('userEmail', loginEmail)
    setToken(res.data.accessToken)
    setEmail(loginEmail)
  }, [])

  const register = useCallback(async (fullName, regEmail, password) => {
    await client.post('/auth/register', { fullName, email: regEmail, password })
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('userEmail')
    setToken(null)
    setEmail(null)
  }, [])

  return (
    <AuthContext.Provider value={{ token, email, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
