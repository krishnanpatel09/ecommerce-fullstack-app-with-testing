
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import './ProductList.css';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantities, setQuantities] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [sortOrder, setSortOrder] = useState('');
  const [filteredProducts, setFilteredProducts] = useState([]);
  const navigate = useNavigate();
  const { isAuthenticated, token, logout } = useAuth();

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [products, searchTerm, priceRange, sortOrder]);

  const applyFilters = () => {
    let filtered = [...products];

    // Apply search filter
    if (searchTerm) {
      filtered = filtered.filter(product =>
          product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          product.description.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Apply price range filter
    if (priceRange.min !== '') {
      filtered = filtered.filter(product => product.price >= parseFloat(priceRange.min));
    }
    if (priceRange.max !== '') {
      filtered = filtered.filter(product => product.price <= parseFloat(priceRange.max));
    }

    // Apply sorting
    if (sortOrder === 'lowToHigh') {
      filtered.sort((a, b) => a.price - b.price);
    } else if (sortOrder === 'highToLow') {
      filtered.sort((a, b) => b.price - a.price);
    }

    setFilteredProducts(filtered);
  };

  const fetchProducts = async (search = '') => {
    try {
      setLoading(true);
      setError(null);

      const url = new URL('http://localhost:8000/api/products');
      if (search) {
        url.searchParams.append('search', search);
      }

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch products' }));
        throw new Error(errorData.message || 'Failed to fetch products');
      }

      const data = await response.json();
      if (!Array.isArray(data)) {
        console.error('Expected array of products but got:', data);
        setProducts([]);
        return;
      }
      setProducts(data);

      // Initialize quantities
      const initialQuantities = {};
      data.forEach(product => {
        initialQuantities[product.id] = 1;
      });
      setQuantities(initialQuantities);
    } catch (error) {
      console.error('Error fetching products:', error);
      setError(error.message);
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchProducts(searchTerm);
  };

  const handleQuantityChange = (productId, value) => {
    setQuantities(prev => ({
      ...prev,
      [productId]: Math.max(1, Math.min(value, products.find(p => p.id === productId)?.stockQuantity || 1))
    }));
  };

  const addToCart = async (productId) => {
    try {
      if (!isAuthenticated || !token) {
        sessionStorage.setItem('redirectUrl', window.location.pathname);
        toast.info('Please log in to add items to cart');
        navigate('/login');
        return;
      }

      const quantity = quantities[productId];
      const product = products.find(p => p.id === productId);

      if (!product) {
        toast.error('Product not found');
        return;
      }

      if (quantity > product.stockQuantity) {
        toast.error('Not enough stock available');
        return;
      }

      console.log('Sending request with token:', token);

      let retries = 3;
      let success = false;
      let lastError;

      while (retries > 0 && !success) {
        try {
          const response = await fetch(`http://localhost:8000/api/cart/items/${productId}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`
            },
            credentials: 'include',
            body: JSON.stringify({ quantity })
          });

          console.log('Response status:', response.status);
          const responseText = await response.text();
          console.log('Response body:', responseText);

          if (response.status === 401 || response.status === 403) {
            logout();
            sessionStorage.setItem('redirectUrl', window.location.pathname);
            toast.info('Session expired. Please log in again.');
            navigate('/login');
            return;
          }

          if (!response.ok) {
            const errorData = responseText ? JSON.parse(responseText) : {};
            if (errorData.message && errorData.message.includes('database is locked')) {
              lastError = new Error('Database is busy, retrying...');
              await new Promise(resolve => setTimeout(resolve, 1000)); // Wait 1 second before retry
              retries--;
              continue;
            }
            throw new Error(errorData.message || 'Failed to add item to cart');
          }

          const data = responseText ? JSON.parse(responseText) : {};
          toast.success(`Added ${quantity} item${quantity > 1 ? 's' : ''} to cart`);
          handleQuantityChange(productId, 1);
          success = true;
        } catch (error) {
          lastError = error;
          if (!error.message.includes('database is locked')) {
            break; // Don't retry if it's not a database lock error
          }
          retries--;
          await new Promise(resolve => setTimeout(resolve, 1000));
        }
      }

      if (!success) {
        throw lastError || new Error('Failed to add item to cart after retries');
      }
    } catch (error) {
      console.error('Error adding to cart:', error);
      toast.error(error.message || 'Failed to add item to cart');
    }
  };

  if (loading) {
    return (
        <div className="product-list">
          <h2>Our Products</h2>
          <div className="loading">Loading products...</div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="product-list">
          <h2>Our Products</h2>
          <div className="error">Error: {error}</div>
        </div>
    );
  }

  if (!Array.isArray(products)) {
    console.error('Products is not an array:', products);
    return (
        <div className="product-list">
          <h2>Our Products</h2>
          <div className="error">Error: Invalid product data</div>
        </div>
    );
  }

  return (
      <div className="product-list">
        <h2>Our Products</h2>
        <div className="filters-container">
          <form onSubmit={handleSearch} className="search-form">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search products..."
                className="search-input"
            />
            <button type="submit" className="search-button">Search</button>
          </form>

          <div className="price-filter">
            <input
                type="number"
                value={priceRange.min}
                onChange={(e) => setPriceRange({ ...priceRange, min: e.target.value })}
                placeholder="Min Price"
                className="price-input"
            />
            <span>to</span>
            <input
                type="number"
                value={priceRange.max}
                onChange={(e) => setPriceRange({ ...priceRange, max: e.target.value })}
                placeholder="Max Price"
                className="price-input"
            />
          </div>

          <select
              value={sortOrder}
              onChange={(e) => setSortOrder(e.target.value)}
              className="sort-select"
          >
            <option value="">Sort by Price</option>
            <option value="lowToHigh">Price: Low to High</option>
            <option value="highToLow">Price: High to Low</option>
          </select>
        </div>

        {loading ? (
            <div className="loading">Loading products...</div>
        ) : error ? (
            <div className="error">Error: {error}</div>
        ) : !Array.isArray(filteredProducts) ? (
            <div className="error">Error: Invalid product data</div>
        ) : filteredProducts.length === 0 ? (
            <div className="no-products">No products found matching your criteria.</div>
        ) : (
            <div className="products-grid">
              {filteredProducts.map(product => (
                  <div key={product.id} className="product-card">
                    <h3>{product.name}</h3>
                    <p className="description">{product.description}</p>
                    <p className="price">${product.price.toFixed(2)}</p>
                    <p className={`stock ${product.stockQuantity === 0 ? 'out-of-stock' : ''}`}>
                      {product.stockQuantity === 0 ? 'Out of Stock' : `In Stock: ${product.stockQuantity}`}
                    </p>
                    {product.stockQuantity > 0 && (
                        <div className="quantity-control">
                          <button
                              onClick={() => handleQuantityChange(product.id, quantities[product.id] - 1)}
                              disabled={quantities[product.id] <= 1}
                              className="quantity-btn"
                          >
                            -
                          </button>
                          <input
                              type="number"
                              min="1"
                              max={product.stockQuantity}
                              value={quantities[product.id]}
                              onChange={(e) => handleQuantityChange(product.id, parseInt(e.target.value) || 1)}
                              className="quantity-input"
                          />
                          <button
                              onClick={() => handleQuantityChange(product.id, quantities[product.id] + 1)}
                              disabled={quantities[product.id] >= product.stockQuantity}
                              className="quantity-btn"
                          >
                            +
                          </button>
                        </div>
                    )}
                    <button
                        onClick={() => addToCart(product.id)}
                        disabled={product.stockQuantity === 0}
                        className="btn-add-to-cart"
                    >
                      {product.stockQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
                    </button>
                  </div>
              ))}
            </div>
        )}
      </div>
  );
}

export default ProductList;