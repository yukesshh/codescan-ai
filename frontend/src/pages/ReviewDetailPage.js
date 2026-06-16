import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getReview, deleteReview } from '../services/api';
import './ReviewDetailPage.css';

const severityConfig = {
  CRITICAL: { color: '#ef4444', bg: '#ef444415', icon: '🔴' },
  HIGH:     { color: '#f97316', bg: '#f9731615', icon: '🟠' },
  MEDIUM:   { color: '#f59e0b', bg: '#f59e0b15', icon: '🟡' },
  LOW:      { color: '#10b981', bg: '#10b98115', icon: '🟢' },
  INFO:     { color: '#3b82f6', bg: '#3b82f615', icon: '🔵' },
};

const issueTypeLabels = {
  BUG: '🐛 Bug', CODE_SMELL: '💨 Code Smell', SECURITY: '🔒 Security',
  PERFORMANCE: '⚡ Performance', STYLE: '✏️ Style',
  COMPLEXITY: '🌀 Complexity', BEST_PRACTICE: '✅ Best Practice'
};

const ratingColor = r => r >= 8 ? 'var(--green)' : r >= 5 ? 'var(--yellow)' : 'var(--red)';
const complexityColor = l => ({ LOW:'var(--green)', MEDIUM:'var(--yellow)', HIGH:'var(--orange)', VERY_HIGH:'var(--red)' }[l] || 'var(--text-muted)');

export default function ReviewDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [aiData, setAiData] = useState(null);
  const [activeTab, setActiveTab] = useState('summary');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    getReview(id).then(r => {
      setReview(r.data);
      try { setAiData(JSON.parse(r.data.aiFeedback)); } catch {}
      setLoading(false);
    }).catch(() => { navigate('/history'); });
  }, [id, navigate]);

  const handleDelete = async () => {
    if (!window.confirm('Delete this review?')) return;
    setDeleting(true);
    await deleteReview(id);
    navigate('/history');
  };

  if (loading) return <div className="page-loading"><span className="spinner">⟳</span> Loading review...</div>;
  if (!review) return null;

  const rating = review.overallRating || 0;
  const circumference = 2 * Math.PI * 40;
  const ratingOffset = circumference - (rating / 10) * circumference;

  return (
    <div className="review-detail fade-in">
      {/* Top Bar */}
      <div className="review-topbar">
        <button className="back-btn" onClick={() => navigate('/history')}>← Back</button>
        <div className="topbar-actions">
          <span className={`status-badge ${review.status.toLowerCase()}`}>{review.status}</span>
          <button className="delete-btn" onClick={handleDelete} disabled={deleting}>
            {deleting ? '...' : '🗑 Delete'}
          </button>
        </div>
      </div>

      {/* Hero */}
      <div className="review-hero">
        <div className="hero-info">
          <div className="hero-meta">
            <span className="lang-badge-lg">{review.language}</span>
            <span className="hero-date">{new Date(review.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })}</span>
          </div>
          <h1 className="hero-title">{review.title}</h1>
          {aiData?.summary && <p className="hero-summary">{aiData.summary}</p>}
        </div>

        {/* Rating Circle */}
        <div className="rating-circle-wrap">
          <svg width="110" height="110" viewBox="0 0 110 110">
            <circle cx="55" cy="55" r="40" fill="none" stroke="var(--bg-muted)" strokeWidth="8" />
            <circle cx="55" cy="55" r="40" fill="none"
              stroke={ratingColor(rating)} strokeWidth="8"
              strokeDasharray={circumference} strokeDashoffset={ratingOffset}
              strokeLinecap="round" transform="rotate(-90 55 55)"
              style={{ transition: 'stroke-dashoffset 0.8s ease' }}
            />
            <text x="55" y="50" textAnchor="middle" fill={ratingColor(rating)} fontSize="20" fontWeight="800" fontFamily="Syne">{rating}</text>
            <text x="55" y="66" textAnchor="middle" fill="#64748b" fontSize="10">/10</text>
          </svg>
          <div className="rating-label">Overall Score</div>
        </div>
      </div>

      {/* Stats Row */}
      <div className="stats-row">
        <div className="mini-stat">
          <span className="mini-val" style={{ color: complexityColor(review.complexityLevel) }}>{review.complexityLevel || 'N/A'}</span>
          <span className="mini-label">Complexity</span>
        </div>
        <div className="mini-stat">
          <span className="mini-val">{review.complexityScore ?? '—'}<span style={{fontSize:'0.9rem'}}>/100</span></span>
          <span className="mini-label">Score</span>
        </div>
        <div className="mini-stat">
          <span className="mini-val" style={{ color: review.bugsFound > 0 ? 'var(--red)' : 'var(--green)' }}>{review.bugsFound ?? 0}</span>
          <span className="mini-label">Bugs Found</span>
        </div>
        <div className="mini-stat">
          <span className="mini-val">{review.suggestionsCount ?? 0}</span>
          <span className="mini-label">Suggestions</span>
        </div>
        <div className="mini-stat">
          <span className="mini-val">{review.linesOfCode ?? 0}</span>
          <span className="mini-label">Lines of Code</span>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        {['summary', 'issues', 'code'].map(tab => (
          <button key={tab} className={`tab-btn ${activeTab === tab ? 'active' : ''}`} onClick={() => setActiveTab(tab)}>
            {tab === 'summary' ? '📊 Summary' : tab === 'issues' ? `🐛 Issues (${review.issues?.length || 0})` : '💻 Code'}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'summary' && (
          <div className="summary-tab fade-in">
            {aiData?.positives?.length > 0 && (
              <div className="feedback-section">
                <h3 className="section-title positives">✅ What's Good</h3>
                <ul className="feedback-list">
                  {aiData.positives.map((p, i) => <li key={i}>{p}</li>)}
                </ul>
              </div>
            )}
            {aiData?.improvements?.length > 0 && (
              <div className="feedback-section">
                <h3 className="section-title improvements">⚠️ Key Improvements</h3>
                <ul className="feedback-list improvements-list">
                  {aiData.improvements.map((p, i) => <li key={i}>{p}</li>)}
                </ul>
              </div>
            )}
          </div>
        )}

        {activeTab === 'issues' && (
          <div className="issues-tab fade-in">
            {review.issues?.length > 0 ? (
              <div className="issues-list">
                {review.issues.map(issue => {
                  const cfg = severityConfig[issue.severity] || severityConfig.INFO;
                  return (
                    <div key={issue.id} className="issue-card" style={{ borderLeftColor: cfg.color, background: cfg.bg }}>
                      <div className="issue-header">
                        <span className="issue-type">{issueTypeLabels[issue.issueType] || issue.issueType}</span>
                        <span className="issue-severity" style={{ color: cfg.color, background: cfg.bg, border: `1px solid ${cfg.color}40` }}>
                          {cfg.icon} {issue.severity}
                        </span>
                        {issue.lineNumber > 0 && <span className="issue-line">Line {issue.lineNumber}</span>}
                      </div>
                      <p className="issue-desc">{issue.description}</p>
                      {issue.suggestion && (
                        <div className="issue-suggestion">
                          <span className="suggestion-label">💡 Fix:</span> {issue.suggestion}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="no-issues">✅ No issues found! Your code looks great.</div>
            )}
          </div>
        )}

        {activeTab === 'code' && (
          <div className="code-tab fade-in">
            <div className="code-view-wrap">
              {review.originalCode.split('\n').map((line, i) => (
                <div key={i} className="code-line">
                  <span className="code-ln">{i + 1}</span>
                  <span className="code-content">{line || ' '}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
