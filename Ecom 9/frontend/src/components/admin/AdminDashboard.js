import React from 'react';
import { Link } from 'react-router-dom';
import './AdminDashboard.css';

const AdminDashboard = () => {
  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>
      <div className="dashboard-cards">
        <div className="dashboard-card">
          <h3>Product Management</h3>
          <p>Manage your product inventory</p>
          <Link to="/admin/products" className="dashboard-link">
            Manage Products
          </Link>
        </div>
        <div className="dashboard-card">
          <h3>Order Management</h3>
          <p>View and manage customer orders</p>
          <Link to="/admin/orders" className="dashboard-link">
            Manage Orders
          </Link>
        </div>
        <div className="dashboard-card">
          <h3>User Management</h3>
          <p>Manage user accounts</p>
          <Link to="/admin/users" className="dashboard-link">
            Manage Users
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard; 