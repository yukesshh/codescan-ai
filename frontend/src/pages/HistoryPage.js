import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getReviews, deleteReview } from '../services/api';
import './HistoryPage.css';

const ratingColor = r => r >= 8 ? 'var(--green)' : r >= 5 ? 'var(--yellow)' : 'var(--red)';
const complexityColor = l => ({ LOW:'var(--green)', MEDIUM:'var(--yellow)', HIGH:'var(--orange)', VERY_HIGH:'var(--red)' }[l] || 'var(--text-muted)');

export default function HistoryPage() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filterLang, setFilterLang] = useState('all');
  const navigate = useNavigate();

  useEffect(() => {
    getReviews().then(r => { setReviews(r.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm('Delete this review?')) return;
    await deleteReview(id);
    setReviews(r => r.filter(x => x.id !== id));
  };

  const languages = ['all', ...new Set(reviews.map(r => r.language))];

  const filtered = reviews.filter(r => {
    const matchSearch = r.title.toLowerCase().includes(search.toLowerCase()) || r.language.toLowerCase().includes(search.toLowerCase());
    const matchLang = filterLang === 'all' || r.language === filterLang;
    return matchSearch && matchLang;
  });

  if (loading) return <div className="page-loading"><span className="spinner">⟳</span> Loading history...</div>;

  return (
    <div className="history-page fade-in">
      <div className="history-header">
        <div>
          <h1 className="page-title">◈ Review History</h1>
          <p className="page-subtitle">{reviews.length} total review{reviews.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn-primary" onClick={() => navigate('/review/new')}>⊕ New Review</button>
      </div>

      {/* Filters */}
      <div className="history-filters">
        <input
          className="search-input" type="text"
          placeholder="🔍  Search by title or language..."
          value={search} onChange={e => setSearch(e.target.value)}
        />
        <div className="lang-filters">
          {languages.map(l => (
            <button key={l} className={`lang-filter-btn ${filterLang === l ? 'active' : ''}`} onClick={() => setFilterLang(l)}>
              {l === 'all' ? 'All' : l}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      {filtered.length > 0 ? (
        <div className="reviews-table-wrap">
          <table className="reviews-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Language</th>
                <th>Rating</th>
                <th>Complexity</th>
                <th>Bugs</th>
                <th>Lines</th>
                <th>Date</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(r => (
                <tr key={r.id} className="review-tr" onClick={() => navigate(`/review/${r.id}`)}>
                  <td className="td-title">{r.title}</td>
                  <td><span className="lang-badge">{r.language}</span></td>
                  <td>
                    <span className="td-rating" style={{ color: ratingColor(r.overallRating) }}>
                      ★ {r.overallRating || '—'}
                    </span>
                  </td>
                  <td>
                    <span style={{ color: complexityColor(r.complexityLevel), fontSize: '0.8rem', fontWeight: 600 }}>
                      {r.complexityLevel || 'N/A'}
                    </span>
                  </td>
                  <td>
                    <span style={{ color: r.bugsFound > 0 ? 'var(--red)' : 'var(--green)', fontWeight: 600 }}>
                      {r.bugsFound ?? 0}
                    </span>
                  </td>
                  <td className="td-muted">{r.linesOfCode ?? 0}</td>
                  <td className="td-muted">{new Date(r.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'2-digit' })}</td>
                  <td>
                    <button className="row-delete-btn" onClick={e => handleDelete(e, r.id)} title="Delete">✕</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-icon">◈</div>
          <h3>{reviews.length === 0 ? 'No reviews yet' : 'No results found'}</h3>
          <p>{reviews.length === 0 ? 'Submit your first code review to get started.' : 'Try a different search or filter.'}</p>
          {reviews.length === 0 && (
            <button className="btn-primary" onClick={() => navigate('/review/new')}>⊕ Create First Review</button>
          )}
        </div>
      )}
    </div>
  );
}
