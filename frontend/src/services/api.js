import axios from "axios";

const API = axios.create({
  baseURL: process.env.REACT_APP_API_URL || "http://localhost:8080/api",
});

// Attach JWT token to every request
API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auth
export const register = (data) => API.post("/auth/register", data);
export const login = (data) => API.post("/auth/login", data);

// Reviews
export const createReview = (data) => API.post("/reviews", data);
export const getReviews = () => API.get("/reviews");
export const getReview = (id) => API.get(`/reviews/${id}`);
export const deleteReview = (id) => API.delete(`/reviews/${id}`);
export const getDashboard = () => API.get("/reviews/dashboard");

export default API;
