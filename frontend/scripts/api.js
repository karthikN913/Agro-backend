const API_BASE_URL = `http://${window.location.hostname}:8080/api`;

function getAuthHeaders(token = null) {
    const headers = { 'Content-Type': 'application/json' };
    const authToken = token || localStorage.getItem('token');
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    return headers;
}

const api = {
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

    async getAllProducts() {
        const res = await fetch(`${API_BASE_URL}/products`, {
            headers: getAuthHeaders()
        });
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
        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: getAuthHeaders()
        });
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
    }
};

// Auth Utilities
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
