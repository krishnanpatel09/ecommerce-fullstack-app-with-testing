import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navigation.css';

const Navigation = () => {
  const navigate = useNavigate();
  const { isAuthenticated, userRole, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navigation">
      <div className="nav-brand">
        <Link to="/" className="nav-logo">
          E-Commerce
        </Link>
      </div>
      <div className="nav-links">
        <Link to="/products" className="nav-link">Products</Link>
        {!isAuthenticated ? (
          <>
            <Link to="/login" className="nav-link">Login</Link>
            <Link to="/register" className="nav-link">Register</Link>
          </>
        ) : userRole === 'ADMIN' ? (
          <>
              <Link to="/admin/dashboard" className="nav-link">Dashboard</Link>
              <Link to="/admin/products" className="nav-link">Manage Products</Link>
              <Link to="/admin/orders" className="nav-link">Manage Orders</Link>
              <Link to="/admin/users" className="nav-link">Manage Users</Link>
              <button onClick={handleLogout} className="nav-link logout-btn">Logout</button>
          </>
        ) : (
          <>
            <Link to="/cart" className="nav-link">Cart</Link>
            <Link to="/orders" className="nav-link">My Orders</Link>
            <button onClick={handleLogout} className="nav-link logout-btn">Logout</button>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navigation; 