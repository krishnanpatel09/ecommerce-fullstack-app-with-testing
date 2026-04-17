import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import './Payment.css';

function Payment({ orderId, totalAmount, onPaymentSuccess }) {
    const [formData, setFormData] = useState({
        cardNumber: '',
        cardHolderName: '',
        expiryDate: '',
        cvv: ''
    });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { token } = useAuth();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await fetch('http://localhost:8000/api/payments/process', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`
                },
                credentials: 'include',
                body: JSON.stringify({
                    orderId,
                    ...formData,
                    amount: totalAmount
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Payment failed');
            }

            if (data.success) {
                toast.success('Payment successful!');
                // Clear the cart after successful payment
                try {
                    await fetch('http://localhost:8000/api/cart/clear', {
                        method: 'POST',
                        headers: {
                            'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        },
                        credentials: 'include'
                    });
                } catch (error) {
                    console.error('Error clearing cart:', error);
                }

                if (onPaymentSuccess) {
                    onPaymentSuccess(data.transactionId);
                }
                // Navigate to order history after successful payment
                navigate('/order-history');
            } else {
                throw new Error(data.message || 'Payment failed');
            }
        } catch (error) {
            console.error('Payment error:', error);
            toast.error(error.message || 'Payment failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="payment-container">
            <h2>Payment Details</h2>
            <form onSubmit={handleSubmit} className="payment-form">
                <div className="form-group">
                    <label htmlFor="cardNumber">Card Number</label>
                    <input
                        type="text"
                        id="cardNumber"
                        name="cardNumber"
                        value={formData.cardNumber}
                        onChange={handleChange}
                        placeholder="1234 5678 9012 3456"
                        required
                        maxLength="16"
                        pattern="[0-9]{16}"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="cardHolderName">Card Holder Name</label>
                    <input
                        type="text"
                        id="cardHolderName"
                        name="cardHolderName"
                        value={formData.cardHolderName}
                        onChange={handleChange}
                        placeholder="Far Kashani"
                        required
                    />
                </div>
                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="expiryDate">Expiry Date</label>
                        <input
                            type="text"
                            id="expiryDate"
                            name="expiryDate"
                            value={formData.expiryDate}
                            onChange={handleChange}
                            placeholder="MM/YY"
                            required
                            maxLength="5"
                            pattern="(0[1-9]|1[0-2])\/([0-9]{2})"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="cvv">CVV</label>
                        <input
                            type="text"
                            id="cvv"
                            name="cvv"
                            value={formData.cvv}
                            onChange={handleChange}
                            placeholder="123"
                            required
                            maxLength="4"
                            pattern="[0-9]{3,4}"
                        />
                    </div>
                </div>
                <div className="amount-display">
                    <p>Total Amount: ${totalAmount.toFixed(2)}</p>
                </div>
                <button type="submit" className="pay-button" disabled={loading}>
                    {loading ? 'Processing...' : 'Pay Now'}
                </button>
            </form>
        </div>
    );
}

export default Payment;