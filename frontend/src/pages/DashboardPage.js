import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getDashboard } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { RadarChart, PolarGrid, PolarAngleAxis, Radar, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Cell } from 'recharts';
import './DashboardPage.css';

const ratingColor = (r) => r >= 8 ? 'var(--green)' : r >= 5 ? 'var(--yellow)' : 'var(--red)';
const complexityColor = (l) => ({ LOW:'var(--green)', MEDIUM:'var(--yellow)', HIGH:'var(--orange)', VERY_HIGH:'var(--red)' }[l] || 'var(--text-muted)');

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    getDashboard().then(r => { setStats(r.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="page-loading"><span className="spinner">⟳</span> Loading dashboard...</div>;

  const langData = stats?.languageBreakdown
    ? Object.entries(stats.languageBreakdown).map(([k, v]) => ({ name: k, reviews: v }))
    : [];

  return (
    <div className="dashboard-page fade-in">
      {/* Header */}
      <div className="dash-header">
        <div>
          <h1 className="dash-title">Welcome back, <span>{user?.username}</span> 👋</h1>
          <p className="dash-subtitle">Here's an overview of your code reviews</p>
        </div>
        <button className="btn-primary" onClick={() => navigate('/review/new')}>
          ⊕ New Review
        </button>
      </div>

      {/* Stat Cards */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#3b82f620', color: 'var(--accent)' }}>◈</div>
          <div className="stat-value">{stats?.totalReviews ?? 0}</div>
          <div className="stat-label">Total Reviews</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#10b98120', color: 'var(--green)' }}>★</div>
          <div className="stat-value" style={{ color: ratingColor(stats?.averageRating) }}>
            {stats?.averageRating ?? '—'}<span className="stat-unit">/10</span>
          </div>
          <div className="stat-label">Avg Rating</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#f59e0b20', color: 'var(--yellow)' }}>⚡</div>
          <div className="stat-value">{langData.length}</div>
          <div className="stat-label">Languages Used</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#ef444420', color: 'var(--red)' }}>⬡</div>
          <div className="stat-value">
            {stats?.recentReviews?.reduce((a, r) => a + (r.bugsFound || 0), 0) ?? 0}
          </div>
          <div className="stat-label">Bugs Found</div>
        </div>
      </div>

      {/* Charts + Recent */}
      <div className="dash-main">
        {/* Language Breakdown */}
        <div className="dash-card">
          <h2 className="card-title">Language Breakdown</h2>
          {langData.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={langData} margin={{ top: 10, right: 10, bottom: 0, left: -20 }}>
                <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip contentStyle={{ background: '#111d35', border: '1px solid #1e3a5f', borderRadius: 8, color: '#e2e8f0' }} />
                <Bar dataKey="reviews" radius={[6, 6, 0, 0]}>
                  {langData.map((_, i) => (
                    <Cell key={i} fill={['#3b82f6','#06b6d4','#10b981','#f59e0b','#f97316','#8b5cf6'][i % 6]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-chart">No reviews yet. <Link to="/review/new">Create one →</Link></div>
          )}
        </div>

        {/* Recent Reviews */}
        <div className="dash-card">
          <div className="card-header">
            <h2 className="card-title">Recent Reviews</h2>
            <Link to="/history" className="card-link">View all →</Link>
          </div>
          <div className="review-list">
            {stats?.recentReviews?.length > 0 ? stats.recentReviews.map(r => (
              <div key={r.id} className="review-row" onClick={() => navigate(`/review/${r.id}`)}>
                <div className="review-row-left">
                  <span className="lang-badge">{r.language}</span>
                  <span className="review-row-title">{r.title}</span>
                </div>
                <div className="review-row-right">
                  <span className="complexity-badge" style={{ color: complexityColor(r.complexityLevel) }}>
                    {r.complexityLevel}
                  </span>
                  <span className="rating-pill" style={{ color: ratingColor(r.overallRating) }}>
                    ★ {r.overallRating || '—'}
                  </span>
                </div>
              </div>
            )) : (
              <div className="empty-chart">No reviews yet. <Link to="/review/new">Get started →</Link></div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
