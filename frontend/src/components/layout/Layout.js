import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Layout.css';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => { logout(); navigate('/login'); };

  const navItems = [
    { to: '/dashboard', icon: '⬡', label: 'Dashboard' },
    { to: '/review/new', icon: '⊕', label: 'New Review' },
    { to: '/history', icon: '◈', label: 'History' },
  ];

  return (
    <div className={`layout ${collapsed ? 'collapsed' : ''}`}>
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">◈</span>
            {!collapsed && <span className="logo-text">CodeScan<span className="logo-ai">AI</span></span>}
          </div>
          <button className="collapse-btn" onClick={() => setCollapsed(c => !c)} title="Toggle sidebar">
            {collapsed ? '→' : '←'}
          </button>
        </div>

        <nav className="sidebar-nav">
          {navItems.map(item => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">{item.icon}</span>
              {!collapsed && <span className="nav-label">{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-chip">
            <div className="avatar">{(user?.username || 'U')[0].toUpperCase()}</div>
            {!collapsed && (
              <div className="user-info">
                <div className="user-name">{user?.username}</div>
                <div className="user-email">{user?.email}</div>
              </div>
            )}
          </div>
          <button className="logout-btn" onClick={handleLogout} title="Logout">
            <span>⏻</span>
            {!collapsed && <span>Logout</span>}
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
