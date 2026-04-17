import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './ProductManagement.css';

const ProductManagement = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    imageUrl: '',
    stockQuantity: ''
  });

  const { token, userRole } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!token || userRole !== 'ADMIN') {
      toast.error('Unauthorized access');
      navigate('/login');
      return;
    }
    fetchProducts();
  }, [token, userRole, navigate]);

  const fetchProducts = async () => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/products', {
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
        throw new Error('Failed to fetch products');
      }

      const data = await response.json();
      console.log('Products data:', data); // Debug log
      setProducts(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Products error:', error);
      toast.error('Error loading products');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const url = editingProduct
      ? `http://localhost:8000/api/admin/products/${editingProduct.id}`
      : 'http://localhost:8000/api/admin/products';
    const method = editingProduct ? 'PUT' : 'POST';

    try {
      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          ...formData,
          price: parseFloat(formData.price),
          stockQuantity: parseInt(formData.stockQuantity, 10)
        })
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error('Server response:', errorData);
        throw new Error(errorData || `Failed to ${editingProduct ? 'update' : 'create'} product`);
      }

      toast.success(`Product ${editingProduct ? 'updated' : 'created'} successfully`);
      fetchProducts();
      resetForm();
    } catch (error) {
      console.error('Product error:', error);
      toast.error(error.message || `Error ${editingProduct ? 'updating' : 'creating'} product`);
    }
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name,
      description: product.description,
      price: product.price.toString(),
      imageUrl: product.imageUrl || '',
      stockQuantity: product.stockQuantity.toString()
    });
  };

  const handleDelete = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8000/api/admin/products/${productId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error('Server response:', errorData);
        throw new Error(errorData || 'Failed to delete product');
      }

      toast.success('Product deleted successfully');
      setProducts(products.filter(p => p.id !== productId));
    } catch (error) {
      console.error('Delete error:', error);
      toast.error(error.message || 'Error deleting product');
    }
  };

  const resetForm = () => {
    setEditingProduct(null);
    setFormData({
      name: '',
      description: '',
      price: '',
      imageUrl: '',
      stockQuantity: ''
    });
  };

  if (loading) {
    return <div className="loading-spinner"></div>;
  }

  return (
    <div className="product-management">
      <h1>Product Management</h1>
      <div className="product-form-container">
        <h2>{editingProduct ? 'Edit Product' : 'Add New Product'}</h2>
        <form onSubmit={handleSubmit} className="product-form">
          <div className="form-group">
            <label htmlFor="name">Product Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              className="form-control"
            />
          </div>
          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              required
              className="form-control"
            />
          </div>
          <div className="form-group">
            <label htmlFor="price">Price</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              required
              min="0"
              step="0.01"
              className="form-control"
            />
          </div>
          <div className="form-group">
            <label htmlFor="imageUrl">Image URL</label>
            <input
              type="url"
              id="imageUrl"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              className="form-control"
            />
          </div>
          <div className="form-group">
            <label htmlFor="stockQuantity">Stock Quantity</label>
            <input
              type="number"
              id="stockQuantity"
              name="stockQuantity"
              value={formData.stockQuantity}
              onChange={handleChange}
              required
              min="0"
              className="form-control"
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editingProduct ? 'Update Product' : 'Add Product'}
            </button>
            {editingProduct && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={resetForm}
              >
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>
      <div className="products-list">
        <h2>Products</h2>
        <div className="products-grid">
          {products.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-image">
                <img src={product.imageUrl || 'https://placehold.co/200'} alt={product.name} />
              </div>
              <div className="product-info">
                <h3>{product.name}</h3>
                <p className="product-description">{product.description}</p>
                <p className="product-price">${product.price.toFixed(2)}</p>
                <p className="product-stock">Stock: {product.stockQuantity}</p>
                <div className="product-actions">
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleEdit(product)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => handleDelete(product.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProductManagement; 