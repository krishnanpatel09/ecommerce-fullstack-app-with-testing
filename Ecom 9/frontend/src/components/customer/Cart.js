
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import './Cart.css';

const Cart = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const response = await fetch('http://localhost:8000/api/cart', {
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          toast.error('Please log in to view your cart');
          navigate('/login');
          return;
        }
        throw new Error('Failed to fetch cart');
      }

      const data = await response.json();
      console.log('Cart data:', data); // Debug log
      setCartItems(data.items || []);
    } catch (error) {
      console.error('Cart error:', error);
      toast.error('Error loading cart');
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (productId, quantity) => {
    if (quantity < 1) return;

    try {
      const response = await fetch(`http://localhost:8000/api/cart/items/${productId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token
        },
        credentials: 'include',
        body: JSON.stringify({ quantity })
      });

      if (!response.ok) {
        throw new Error('Failed to update cart');
      }

      fetchCart(); // Refresh cart after update
    } catch (error) {
      console.error('Cart update error:', error);
      toast.error('Error updating cart');
    }
  };

  const removeItem = async (productId) => {
    try {
      const response = await fetch(`http://localhost:8000/api/cart/items/${productId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': token
        },
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to remove item');
      }

      fetchCart(); // Refresh cart after removal
    } catch (error) {
      console.error('Cart remove error:', error);
      toast.error('Error removing item');
    }
  };

  const handleCheckout = () => {
    if (cartItems.length === 0) {
      toast.error('Your cart is empty');
      return;
    }
    navigate('/checkout');
  };

  if (loading) {
    return <div className="loading-spinner"></div>;
  }

  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  return (
      <div className="cart-container">
        <h1>Shopping Cart</h1>
        {cartItems.length === 0 ? (
            <div className="empty-cart">
              <p>Your cart is empty</p>
            </div>
        ) : (
            <>
              <div className="cart-items">
                {cartItems.map(item => (
                    <div key={item.id} className="cart-item">
                      <div className="item-details">
                        <h3>{item.productName}</h3>
                        <p className="item-price">${item.price.toFixed(2)}</p>
                      </div>
                      <div className="item-quantity">
                        <button
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                        >
                          -
                        </button>
                        <span>{item.quantity}</span>
                        <button
                            className="quantity-btn"
                            onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                        >
                          +
                        </button>
                      </div>
                      <div className="item-total">
                        ${(item.price * item.quantity).toFixed(2)}
                      </div>
                      <button
                          className="remove-btn"
                          onClick={() => removeItem(item.productId)}
                      >
                        Remove
                      </button>
                    </div>
                ))}
              </div>
              <div className="cart-summary">
                <div className="total">
                  <span>Total: ${total.toFixed(2)}</span>
                </div>
                <button
                    className="btn btn-primary checkout-btn"
                    onClick={handleCheckout}
                    disabled={processing}
                >
                  {processing ? 'Processing...' : 'Checkout'}
                </button>
              </div>
            </>
        )}
      </div>
  );
};
export default Cart;