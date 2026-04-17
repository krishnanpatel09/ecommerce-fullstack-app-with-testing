import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './OrderHistory.css';

const OrderHistory = ({ refreshTrigger }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const { token } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, [token, refreshTrigger]); // Add refreshTrigger to dependencies

  const fetchOrders = async () => {
    try {
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await fetch('http://localhost:8000/api/customer/orders', {
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          toast.error('Please log in to view your orders');
          navigate('/login');
          return;
        }
        throw new Error('Failed to fetch orders');
      }

      const data = await response.json();
      console.log('Orders data:', data); // Debug log
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Orders error:', error);
      toast.error('Error loading orders');
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading-spinner"></div>;
  }

  return (
      <div className="order-history">
        <h1>Order History</h1>
        {!Array.isArray(orders) || orders.length === 0 ? (
            <div className="empty-orders">
              <p>No orders found</p>
            </div>
        ) : (
            <div className="orders-list">
              {orders.map(order => (
                  <div key={order?.id || Math.random()} className="order-card">
                    <div className="order-header">
                      <div className="order-info">
                        <h3>Order #{order?.id || 'N/A'}</h3>
                        <p className="order-date">
                          {order?.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'Date not available'}
                        </p>
                      </div>
                      <div className="order-status">
                  <span className={`status-badge status-${(order?.status || '').toLowerCase()}`}>
                    {order?.status || 'Unknown'}
                  </span>
                      </div>
                    </div>
                    <div className="order-items">
                      {Array.isArray(order?.orderItems) ? order.orderItems.map(item => (
                          <div key={item?.id || Math.random()} className="order-item">
                            <div className="item-details">
                              <h4>{item?.productName || 'Unknown Product'}</h4>
                              <p className="item-price">
                                ${(item?.price || 0).toFixed(2)} x {item?.quantity || 0}
                              </p>
                            </div>
                            <div className="item-total">
                              ${((item?.price || 0) * (item?.quantity || 0)).toFixed(2)}
                            </div>
                          </div>
                      )) : (
                          <div className="order-item">
                            <p>No items available</p>
                          </div>
                      )}
                    </div>
                    <div className="order-footer">
                      <div className="order-total">
                        <span>Total:</span>
                        <span>${(order?.totalAmount || 0).toFixed(2)}</span>
                      </div>
                    </div>
                  </div>
              ))}
            </div>
        )}
      </div>
  );
};

export default OrderHistory; 