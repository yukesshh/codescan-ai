import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createReview } from '../services/api';
import './NewReviewPage.css';

const LANGUAGES = ['java', 'python', 'javascript', 'typescript', 'c', 'cpp', 'csharp', 'go', 'rust', 'kotlin', 'swift', 'php', 'ruby', 'sql', 'other'];

const SAMPLE_CODE = {
  java: `public class Calculator {
    public int divide(int a, int b) {
        // TODO: handle edge cases
        return a / b;
    }
    
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println(calc.divide(10, 0));
    }
}`,
  python: `def find_user(users, name):
    for i in range(len(users)):
        if users[i]['name'] == name:
            return users[i]
    return None

data = None
result = find_user(data, "Alice")
print(result['email'])`,
  javascript: `async function fetchData(url) {
  const response = await fetch(url)
  const data = response.json()
  return data
}

var x = 10
var x = 20
console.log(x)`
};

export default function NewReviewPage() {
  const [form, setForm] = useState({ title: '', language: 'java', code: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.code.trim()) { setError('Please paste your code before submitting.'); return; }
    setLoading(true); setError('');
    try {
      const { data } = await createReview(form);
      navigate(`/review/${data.id}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create review. Please try again.');
      setLoading(false);
    }
  };

  const loadSample = () => {
    const sample = SAMPLE_CODE[form.language] || SAMPLE_CODE.java;
    setForm(f => ({ ...f, code: sample, title: f.title || `Sample ${form.language} review` }));
  };

  const lineCount = form.code ? form.code.split('\n').length : 0;
  const charCount = form.code.length;

  return (
    <div className="new-review-page fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">⊕ New Code Review</h1>
          <p className="page-subtitle">Paste your code and let AI analyze it instantly</p>
        </div>
      </div>

      {error && <div className="review-error">⚠ {error}</div>}

      <form onSubmit={handleSubmit} className="review-form">
        <div className="form-top-row">
          <div className="form-group flex-grow">
            <label>Review Title *</label>
            <input
              type="text" placeholder="e.g. User authentication module"
              required value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
            />
          </div>
          <div className="form-group">
            <label>Language *</label>
            <select value={form.language} onChange={e => setForm(f => ({ ...f, language: e.target.value }))}>
              {LANGUAGES.map(l => <option key={l} value={l}>{l.charAt(0).toUpperCase() + l.slice(1)}</option>)}
            </select>
          </div>
        </div>

        <div className="form-group">
          <div className="code-label-row">
            <label>Code *</label>
            <div className="code-meta">
              {lineCount > 0 && <span>{lineCount} lines · {charCount} chars</span>}
              <button type="button" className="sample-btn" onClick={loadSample}>Load sample →</button>
            </div>
          </div>
          <div className="code-editor-wrap">
            <div className="line-numbers">
              {form.code.split('\n').map((_, i) => <div key={i}>{i + 1}</div>)}
              {form.code === '' && <div>1</div>}
            </div>
            <textarea
              className="code-textarea"
              placeholder={`// Paste your ${form.language} code here...\n// The AI will analyze bugs, complexity, style, performance & more.`}
              value={form.code}
              onChange={e => setForm(f => ({ ...f, code: e.target.value }))}
              rows={22}
              spellCheck={false}
            />
          </div>
        </div>

        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={() => navigate('/dashboard')}>
            Cancel
          </button>
          <button type="submit" className="btn-analyze" disabled={loading}>
            {loading ? (
              <>
                <span className="spinner">⟳</span>
                <span>Analyzing with AI...</span>
                <span className="analyzing-dots"><span>.</span><span>.</span><span>.</span></span>
              </>
            ) : (
              <>⚡ Analyze Code</>
            )}
          </button>
        </div>

        {loading && (
          <div className="loading-status">
            <div className="loading-bar"><div className="loading-progress"></div></div>
            <p>AI is reviewing your code for bugs, complexity, style issues, and best practices...</p>
          </div>
        )}
      </form>
    </div>
  );
}
