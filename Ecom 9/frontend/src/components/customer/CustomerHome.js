import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import './CustomerHome.css';

const CustomerHome = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch('/api/products');
      if (!response.ok) {
        throw new Error('Failed to fetch products');
      }
      const data = await response.json();
      setProducts(data);
    } catch (error) {
      console.error('Products error:', error);
      setError('Unable to load products. Please make sure the backend server is running.');
      toast.error('Failed to load products. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        toast.error('Please login to add items to cart');
        return;
      }

      const response = await fetch('/api/cart/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ productId })
      });

      if (!response.ok) {
        throw new Error('Failed to add to cart');
      }

      toast.success('Product added to cart successfully!');
    } catch (error) {
      console.error('Add to cart error:', error);
      toast.error('Failed to add product to cart');
    }
  };

  if (loading) {
    return (
      <div className="customer-home">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading products...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="customer-home">
        <div className="error-message">
          <h2>Oops! Something went wrong</h2>
          <p>{error}</p>
          <p>Please make sure:</p>
          <ul>
            <li>The backend server is running on port 8080</li>
            <li>You're connected to the internet</li>
            <li>Try refreshing the page</li>
          </ul>
          <button onClick={fetchProducts} className="btn btn-primary">
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="customer-home">
      <h1>Welcome to Our Store</h1>
      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <img src={product.imageUrl} alt={product.name} className="product-image" />
            <h3>{product.name}</h3>
            <p>{product.description}</p>
            <p className="product-price">${product.price.toFixed(2)}</p>
            <button
              onClick={() => addToCart(product.id)}
              className="btn btn-primary"
            >
              Add to Cart
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CustomerHome; 