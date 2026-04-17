import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import Payment from './Payment';
import './Checkout.css';

function Checkout() {
    const [cart, setCart] = useState([]);
    const [loading, setLoading] = useState(true);
    const [totalAmount, setTotalAmount] = useState(0);
    const [orderId, setOrderId] = useState(null);
    const navigate = useNavigate();
    const { token } = useAuth();

    useEffect(() => {
        fetchCart();
    }, []);

    const fetchCart = async () => {
        try {
            const response = await fetch('http://localhost:8000/api/cart', {
                credentials: 'include',
                headers: {
                    'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch cart');
            }

            const data = await response.json();
            const cartItems = data.items || data;
            setCart(Array.isArray(cartItems) ? cartItems : []);
            calculateTotal(cartItems);
            setLoading(false);
        } catch (error) {
            console.error('Error fetching cart:', error);
            toast.error('Failed to load cart items');
            setLoading(false);
        }
    };

    const calculateTotal = (cartItems) => {
        if (!Array.isArray(cartItems)) {
            setTotalAmount(0);
            return;
        }
        const total = cartItems.reduce((sum, item) => {
            const price = item.price?.price || item.price || 0;
            const quantity = item.quantity || 0;
            return sum + (price * quantity);
        }, 0);
        setTotalAmount(total);
    };

    const handlePaymentSuccess = async (transactionId) => {
        try {
            // First, create the order through checkout
            const checkoutResponse = await fetch('http://localhost:8000/api/cart/checkout', {
                method: 'POST',
                headers: {
                    'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (!checkoutResponse.ok) {
                throw new Error('Failed to create order');
            }

            // Then process the payment
            const paymentResponse = await fetch('http://localhost:8000/api/payments/process', {
                method: 'POST',
                headers: {
                    'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    amount: totalAmount,
                    transactionId
                })
            });

            if (!paymentResponse.ok) {
                throw new Error('Payment failed');
            }

            toast.success('Payment successful!');
            navigate('/order-history');
        } catch (error) {
            console.error('Error during checkout:', error);
            toast.error(error.message || 'Checkout failed. Please try again.');

        }
    };

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    if (!Array.isArray(cart) || cart.length === 0) {
        return (
            <div className="empty-cart">
                <h2>Your cart is empty</h2>
                <button onClick={() => navigate('/products')} className="continue-shopping">
                    Continue Shopping
                </button>
            </div>
        );
    }

    return (
        <div className="checkout-container">
            <h2>Checkout</h2>
            <div className="checkout-content">
                <div className="order-summary">
                    <h3>Order Summary</h3>
                    {cart.map((item) => (
                        <div key={item.id} className="cart-item">
                            <span className="item-name">{item.product?.name || item.productName}</span>
                            <span className="item-quantity">x {item.quantity}</span>
                            <span className="item-price">
                                ${((item.product?.price || item.price) * item.quantity).toFixed(2)}
                            </span>
                        </div>
                    ))}
                    <div className="total-amount">
                        <span>Total:</span>
                        <span>${totalAmount.toFixed(2)}</span>
                    </div>
                </div>
                <Payment
                    orderId={orderId}
                    totalAmount={totalAmount}
                    onPaymentSuccess={handlePaymentSuccess}
                />
            </div>
        </div>
    );
}

export default Checkout;
//-------------------------------------------------------
