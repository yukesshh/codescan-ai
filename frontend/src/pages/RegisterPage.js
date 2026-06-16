import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { register as registerApi } from '../services/api';
import './AuthPages.css';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const { data } = await registerApi(form);
      login({ username: data.username, email: data.email, fullName: data.fullName, userId: data.userId }, data.token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed. Please try again.');
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
        <h1 className="auth-title">Create account</h1>
        <p className="auth-subtitle">Start reviewing code with AI intelligence</p>

        {error && <div className="auth-error">⚠ {error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Full Name</label>
            <input type="text" placeholder="John Doe"
              value={form.fullName}
              onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} />
          </div>
          <div className="form-group">
            <label>Username *</label>
            <input type="text" placeholder="john_dev" required
              value={form.username}
              onChange={e => setForm(f => ({ ...f, username: e.target.value }))} />
          </div>
          <div className="form-group">
            <label>Email *</label>
            <input type="email" placeholder="john@example.com" required
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
          </div>
          <div className="form-group">
            <label>Password * (min 6 chars)</label>
            <input type="password" placeholder="••••••••" required minLength={6}
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
          </div>
          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? <><span className="spinner">⟳</span> Creating account...</> : 'Create Account →'}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
