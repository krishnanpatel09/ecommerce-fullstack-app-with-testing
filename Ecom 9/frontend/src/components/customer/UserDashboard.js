import React from 'react';
import { Link } from 'react-router-dom';
import './UserDashboard.css';

const UserDashboard = () => {
  return (
    <div className="user-dashboard">
      <h1>Welcome to Your Dashboard</h1>
      <div className="dashboard-cards">
        <div className="dashboard-card">
          <h3>My Orders</h3>
          <p>View your order history</p>
          <Link to="/orders" className="dashboard-link">
            View Orders
          </Link>
        </div>
        <div className="dashboard-card">
          <h3>Shopping Cart</h3>
          <p>View items in your cart</p>
          <Link to="/cart" className="dashboard-link">
            Go to Cart
          </Link>
        </div>
        <div className="dashboard-card">
          <h3>Browse Products</h3>
          <p>Explore our product catalog</p>
          <Link to="/products" className="dashboard-link">
            Shop Now
          </Link>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard; 