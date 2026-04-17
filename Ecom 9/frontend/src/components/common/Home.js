import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css';

function Home() {
  const navigate = useNavigate();

  const handleShopNow = () => {
    navigate('/products');
  };

  return (
    <div className="home-container">
      <div className="hero-section">
        <h1>Welcome to Our E-Commerce Store</h1>
        <p>Discover amazing products at great prices</p>
        <button className="shop-now-btn" onClick={handleShopNow}>
          Shop Now
        </button>
      </div>
    </div>
  );
}

export default Home; 