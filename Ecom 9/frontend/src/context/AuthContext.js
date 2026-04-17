import React, { createContext, useState, useContext, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [userRole, setUserRole] = useState(null);
  const [loading, setLoading] = useState(true);

  const parseToken = (token) => {
    if (!token) return null;
    try {
      const decoded = jwtDecode(token.replace('Bearer ', ''));
      console.log('Decoded token:', decoded); // Debug log
      return {
        email: decoded.sub,
        role: decoded.role,
        userId: decoded.userId,
        username: decoded.username
      };
    } catch (error) {
      console.error('Error parsing token:', error);
      return null;
    }
  };

  useEffect(() => {
    // Check if user is logged in on component mount
    const storedToken = localStorage.getItem('token');
    console.log('Stored token:', storedToken); // Debug log
    
    if (storedToken) {
      try {
        const userData = parseToken(storedToken);
        console.log('Parsed user data:', userData); // Debug log
        if (userData) {
          setUser(userData);
          setUserRole(userData.role);
          setToken(storedToken);
          console.log('User role set to:', userData.role); // Debug log
        } else {
          throw new Error('Invalid token');
        }
      } catch (error) {
        console.error('Error parsing token:', error);
        // Clear invalid data
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
        setUserRole(null);
      }
    } else {
      setToken(null);
      setUser(null);
      setUserRole(null);
    }
    setLoading(false);
  }, []);

  const login = (newToken) => {
    console.log('Login called with token:', newToken); // Debug log
    // Ensure token has Bearer prefix
    const tokenWithBearer = newToken.startsWith('Bearer ') ? newToken : `Bearer ${newToken}`;
    const userData = parseToken(tokenWithBearer);
    
    if (userData) {
      localStorage.setItem('token', tokenWithBearer);
      setToken(tokenWithBearer);
      setUser(userData);
      setUserRole(userData.role);
      console.log('Login successful, user role:', userData.role); // Debug log
    } else {
      throw new Error('Invalid token format');
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    setUserRole(null);
    console.log('Logout completed, all states cleared'); // Debug log
  };

  const value = {
    user,
    login,
    logout,
    isAuthenticated: !!user && !!token,
    loading,
    token,
    userRole
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 