import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './OrderList.css';

function OrderList() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const { token, userRole } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!token || userRole !== 'ADMIN') {
      toast.error('Unauthorized access');
      navigate('/login');
      return;
    }
    fetchOrders();
  }, [token, userRole, navigate]);

  const fetchOrders = async () => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/orders', {
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          toast.error('Unauthorized access');
          navigate('/login');
          return;
        }
        throw new Error('Failed to load orders');
      }

      const data = await response.json();
      console.log('Orders data:', data); // Debug log
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Orders error:', error);
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      const response = await fetch(`http://localhost:8000/api/admin/orders/${orderId}/status`, {
        method: 'PUT',
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ status: newStatus })
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error('Server response:', errorData);
        throw new Error(errorData || 'Failed to update order status');
      }

      toast.success('Order status updated successfully');
      // Update the order in the local state
      setOrders(orders.map(order => 
        order.id === orderId 
          ? { ...order, status: newStatus }
          : order
      ));
    } catch (error) {
      console.error('Update error:', error);
      toast.error(error.message || 'Failed to update order status');
    }
  };

  if (loading) {
    return <div className="loading-spinner"></div>;
  }

  return (
    <div className="order-list">
      <h2>Order Management</h2>
      {orders.length === 0 ? (
        <div className="no-orders">
          <p>No orders found</p>
        </div>
      ) : (
        <div className="orders-grid">
          {orders.map(order => (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <h3>Order #{order.id}</h3>
                <span className={`status ${(order.status || '').toLowerCase()}`}>
                  {order.status}
                </span>
              </div>
              <div className="order-details">
                <p><strong>Customer:</strong> {order.customerName || order.userId}</p>
                <p><strong>Email:</strong> {order.customerEmail || 'N/A'}</p>
                <p><strong>Total Amount:</strong> ${(order.totalAmount || 0).toFixed(2)}</p>
                <p><strong>Order Date:</strong> {order.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'N/A'}</p>
              </div>
              <div className="order-items">
                <h4>Items:</h4>
                {Array.isArray(order.orderItems) ? (
                  <ul>
                    {order.orderItems.map(item => (
                      <li key={item.id || Math.random()}>
                        <span className="item-name">{item.productName}</span>
                        <span className="item-quantity">x {item.quantity}</span>
                        <span className="item-price">${((item.price || 0) * (item.quantity || 0)).toFixed(2)}</span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No items available</p>
                )}
              </div>
              <div className="order-actions">
                <select
                  value={order.status || ''}
                  onChange={(e) => updateOrderStatus(order.id, e.target.value)}
                  className={`status-select status-${(order.status || '').toLowerCase()}`}
                >
                  <option value="PENDING">Pending</option>
                  <option value="PROCESSING">Processing</option>
                  <option value="SHIPPED">Shipped</option>
                  <option value="DELIVERED">Delivered</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default OrderList; 