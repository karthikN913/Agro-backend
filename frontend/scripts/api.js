const API_BASE_URL = "https://agro-backend-hscn.onrender.com/api";

function getAuthHeaders(token = null) {
    const headers = { 'Content-Type': 'application/json' };
    const authToken = token || localStorage.getItem('token');
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    return headers;
}

const api = {
    // ── Users ─────────────────────────────────────────────────────────────────

    async register(userData, token) {
        const res = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: getAuthHeaders(token),
            body: JSON.stringify(userData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async login(token) {
        const res = await fetch(`${API_BASE_URL}/users/login`, {
            method: 'POST',
            headers: getAuthHeaders(token)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async getAllUsers() {
        const res = await fetch(`${API_BASE_URL}/users`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    // ── Products ──────────────────────────────────────────────────────────────

    async getAllProducts() {
        const res = await fetch(`${API_BASE_URL}/products`, { headers: getAuthHeaders() });
        return res.json();
    },

    async addProduct(productData) {
        const res = await fetch(`${API_BASE_URL}/products`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(productData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    // ── Orders ────────────────────────────────────────────────────────────────

    async placeOrder(orderData) {
        const res = await fetch(`${API_BASE_URL}/orders`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(orderData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async getOrders(userId, role) {
        const endpoint = role === 'FARMER' ? `/orders/farmer/${userId}` : `/orders/buyer/${userId}`;
        const res = await fetch(`${API_BASE_URL}${endpoint}`, { headers: getAuthHeaders() });
        return res.json();
    },

    async updateOrderStatus(orderId, status) {
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ status })
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    // ── Community Posts ───────────────────────────────────────────────────────

    async getPosts() {
        const res = await fetch(`${API_BASE_URL}/community`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async createPost(postData) {
        const res = await fetch(`${API_BASE_URL}/community`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(postData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async likePost(postId) {
        const res = await fetch(`${API_BASE_URL}/community/${postId}/like`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    // ── Government Schemes ────────────────────────────────────────────────────

    async getSchemes() {
        const res = await fetch(`${API_BASE_URL}/schemes`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    // ── Reviews ───────────────────────────────────────────────────────────────

    async getProductReviews(productId) {
        const res = await fetch(`${API_BASE_URL}/reviews/product/${productId}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async getReviewSummary(productId) {
        const res = await fetch(`${API_BASE_URL}/reviews/product/${productId}/summary`, { headers: getAuthHeaders() });
        if (!res.ok) return { count: 0, average: 0 };
        return res.json();
    },

    async submitReview(reviewData) {
        const res = await fetch(`${API_BASE_URL}/reviews`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(reviewData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    // ── Messages ──────────────────────────────────────────────────────────────

    async getMessages(userId1, userId2) {
        const res = await fetch(`${API_BASE_URL}/messages/${userId1}/${userId2}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    // ── Search & Filters ──────────────────────────────────────────────────────
    async searchProducts(params) {
        const queryParams = new URLSearchParams();
        if (params.query) queryParams.append('query', params.query);
        if (params.category) queryParams.append('category', params.category);
        if (params.minPrice) queryParams.append('minPrice', params.minPrice);
        if (params.maxPrice) queryParams.append('maxPrice', params.maxPrice);
        if (params.location) queryParams.append('location', params.location);

        const res = await fetch(`${API_BASE_URL}/products/search?${queryParams.toString()}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    // ── Credit Ledger (Udhar) ──────────────────────────────────────────────────
    async getCreditLedger(userId) {
        const res = await fetch(`${API_BASE_URL}/credit?userId=${userId}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async addCreditRecord(recordData) {
        const res = await fetch(`${API_BASE_URL}/credit`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(recordData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async getCreditSummary(userId) {
        const res = await fetch(`${API_BASE_URL}/credit/summary?userId=${userId}`, { headers: getAuthHeaders() });
        if (!res.ok) return { totalCreditExtended: 0, totalRepayments: 0, netOutstanding: 0, customers: [] };
        return res.json();
    },

    // ── Crop Category Subscriptions ───────────────────────────────────────────
    async getSubscriptions(userId) {
        const res = await fetch(`${API_BASE_URL}/subscriptions?userId=${userId}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async subscribeToCrop(userId, category) {
        const res = await fetch(`${API_BASE_URL}/subscriptions`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ user: { id: userId }, category })
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async unsubscribeFromCrop(userId, category) {
        const res = await fetch(`${API_BASE_URL}/subscriptions?userId=${userId}&category=${encodeURIComponent(category)}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error(await res.text());
        return true;
    }
};

// ─── Auth Utilities ───────────────────────────────────────────────────────────

function getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

function requireAuth(allowedRoles = []) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = 'index.html';
        return null;
    }
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        alert('Unauthorized access');
        window.location.href = 'index.html';
        return null;
    }
    return user;
}

function logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    if (window.firebase) {
        firebase.auth().signOut().catch(() => { });
    }
    window.location.href = 'index.html';
}
