import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { AuthProvider } from './context/AuthContext';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

// Import components
import Navigation from './components/common/Navigation';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import AdminDashboard from './components/admin/AdminDashboard';
import UserDashboard from './components/customer/UserDashboard';
import Cart from './components/customer/Cart';
import OrderHistory from './components/customer/OrderHistory';
import ProductManagement from './components/admin/ProductManagement';
import OrderList from './components/admin/OrderList';
import ProtectedRoute from './components/auth/ProtectedRoute';
import ProductList from './components/customer/ProductList';
import Home from './components/common/Home';
import Checkout from './components/customer/Checkout';
import UserManagement from "./components/admin/UserManagement";

function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="App">
                    <Navigation />
                    <ToastContainer position="top-right" autoClose={3000} />
                    <main className="main-content">
                        <Routes>
                            {/* Public Routes */}
                            <Route path="/" element={<Home />} />
                            <Route path="/products" element={<ProductList />} />
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />

                            {/* Admin Routes */}
                            <Route
                                path="/admin/dashboard"
                                element={
                                    <ProtectedRoute allowedRoles={['ADMIN']}>
                                        <AdminDashboard />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/admin/products"
                                element={
                                    <ProtectedRoute allowedRoles={['ADMIN']}>
                                        <ProductManagement />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/admin/orders"
                                element={
                                    <ProtectedRoute allowedRoles={['ADMIN']}>
                                        <OrderList />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/admin/users"
                                element={
                                <ProtectedRoute allowedRoles={['ADMIN']}>
                                    <UserManagement />
                                </ProtectedRoute>
                                }
                            />

                            {/* User Routes */}
                            <Route
                                path="/dashboard"
                                element={
                                    <ProtectedRoute allowedRoles={['USER']}>
                                        <UserDashboard />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/cart"
                                element={
                                    <ProtectedRoute allowedRoles={['USER']}>
                                        <Cart />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/checkout"
                                element={
                                    <ProtectedRoute allowedRoles={['USER']}>
                                        <Checkout />
                                    </ProtectedRoute>
                                }
                            />
                            <Route
                                path="/orders"
                                element={
                                    <ProtectedRoute allowedRoles={['USER']}>
                                        <OrderHistory />
                                    </ProtectedRoute>
                                }
                            />

                            {/* Home Route */}
                            <Route path="*" element={<Navigate to="/" />} />
                        </Routes>
                    </main>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App; 