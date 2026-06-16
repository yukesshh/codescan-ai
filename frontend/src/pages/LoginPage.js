import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as loginApi } from '../services/api';
import './AuthPages.css';

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const { data } = await loginApi(form);
      login({ username: data.username, email: data.email, fullName: data.fullName, userId: data.userId }, data.token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Check your credentials.');
    } finally { setLoading(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg">
        <div className="auth-grid"></div>
        <div className="auth-orb orb-1"></div>
        <div className="auth-orb orb-2"></div>
      </div>
      <div className="auth-card fade-in">
        <div className="auth-logo">
          <span className="auth-logo-icon">◈</span>
          <span>CodeScan<span style={{ color: 'var(--accent)' }}>AI</span></span>
        </div>
        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-subtitle">Sign in to review your code with AI</p>

        {error && <div className="auth-error">⚠ {error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Username</label>
            <input
              type="text" placeholder="your_username" required
              value={form.username}
              onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password" placeholder="••••••••" required
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
            />
          </div>
          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? <><span className="spinner">⟳</span> Signing in...</> : 'Sign In →'}
          </button>
        </form>

        <p className="auth-switch">
          Don't have an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}
