const API_BASE_URL = (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
    ? "http://localhost:8080/api"
    : "https://agro-backend-hscn.onrender.com/api";

function getAuthHeaders(token = null) {
    const headers = { 'Content-Type': 'application/json' };
    const authToken = token || localStorage.getItem('token');
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    return headers;
}

async function fetchWithRetry(url, options = {}, retries = 5, delay = 2000) {
    for (let i = 0; i < retries; i++) {
        try {
            const res = await fetch(url, options);
            // 502 Bad Gateway, 503 Service Unavailable, and 504 Gateway Timeout are common on Render cold starts.
            // 500 Internal Server Error can also happen transiently if database pool is warming up.
            if ((res.status === 500 || res.status === 502 || res.status === 503 || res.status === 504) && i < retries - 1) {
                console.warn(`Transient server error (${res.status}) on ${url}. Retrying in ${delay}ms... (Attempt ${i + 1}/${retries})`);
                await new Promise(resolve => setTimeout(resolve, delay));
                delay *= 1.5;
                continue;
            }
            return res;
        } catch (err) {
            if (i < retries - 1) {
                console.warn(`Network connection error on ${url}: ${err.message}. Retrying in ${delay}ms... (Attempt ${i + 1}/${retries})`);
                await new Promise(resolve => setTimeout(resolve, delay));
                delay *= 1.5;
                continue;
            }
            throw err;
        }
    }
}

const api = {
    // ── Users ─────────────────────────────────────────────────────────────────

    async register(userData, token) {
        const res = await fetchWithRetry(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: getAuthHeaders(token),
            body: JSON.stringify(userData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async login(token) {
        const res = await fetchWithRetry(`${API_BASE_URL}/users/login`, {
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

    async getProductsByFarmer(farmerId) {
        const res = await fetch(`${API_BASE_URL}/products/farmer/${farmerId}`, { headers: getAuthHeaders() });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async deleteProduct(productId, farmerId) {
        const res = await fetch(`${API_BASE_URL}/products/${productId}?farmerId=${farmerId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error(await res.text());
        return res.text();
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

    // ── Transporter Endpoints ───────────────────────────────────────────────────────

    async getAvailableDeliveries() {
        const res = await fetch(`${API_BASE_URL}/orders/transporter/available`, { headers: getAuthHeaders() });
        return res.json();
    },

    async getTransporterOrders(transporterId) {
        const res = await fetch(`${API_BASE_URL}/orders/transporter/${transporterId}`, { headers: getAuthHeaders() });
        return res.json();
    },

    async assignTransporter(orderId, transporterId) {
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/assign/${transporterId}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
        });
        if (!res.ok) throw new Error(await res.text());
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

    async updateTransporterLocation(orderId, location) {
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/location`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ location })
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

    async getBatchReviewSummaries() {
        const res = await fetch(`${API_BASE_URL}/reviews/summaries`, { headers: getAuthHeaders() });
        if (!res.ok) return {};
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
    },

    // ── Transporter Logistics Bidding ──────────────────────────────────────────
    async updateVehicleProfile(userId, vehicleData) {
        const res = await fetch(`${API_BASE_URL}/users/${userId}/vehicle`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(vehicleData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async getBidsForOrder(orderId) {
        const res = await fetch(`${API_BASE_URL}/bids/order/${orderId}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async getBidsByTransporter(transporterId) {
        const res = await fetch(`${API_BASE_URL}/bids/transporter/${transporterId}`, { headers: getAuthHeaders() });
        if (!res.ok) return [];
        return res.json();
    },

    async submitDeliveryBid(bidData) {
        const res = await fetch(`${API_BASE_URL}/bids`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(bidData)
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    },

    async acceptDeliveryBid(bidId) {
        const res = await fetch(`${API_BASE_URL}/bids/${bidId}/accept`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
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

// ─── Comprehensive Crop Image Library ──────────────────────────────────────────
// Uses verified Unsplash photo IDs — these are permanent and never expire.

const STATIC_CROP_IMAGES = {
    // ── Vegetables ─────────────────────────────────────────────────────────────
    tomato:       "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&q=80&fit=crop",
    tomatoes:     "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&q=80&fit=crop",
    potato:       "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600&q=80&fit=crop",
    potatoes:     "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600&q=80&fit=crop",
    onion:        "https://images.unsplash.com/photo-1508747703725-719777637510?w=600&q=80&fit=crop",
    onions:       "https://images.unsplash.com/photo-1508747703725-719777637510?w=600&q=80&fit=crop",
    brinjal:      "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=600&q=80&fit=crop",
    eggplant:     "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=600&q=80&fit=crop",
    aubergine:    "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=600&q=80&fit=crop",
    okra:         "https://images.unsplash.com/photo-1634733988138-bf2c3a2a13fa?w=600&q=80&fit=crop",
    bhindi:       "https://images.unsplash.com/photo-1634733988138-bf2c3a2a13fa?w=600&q=80&fit=crop",
    ladyfinger:   "https://images.unsplash.com/photo-1634733988138-bf2c3a2a13fa?w=600&q=80&fit=crop",
    carrot:       "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600&q=80&fit=crop",
    carrots:      "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600&q=80&fit=crop",
    cabbage:      "https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?w=600&q=80&fit=crop",
    cauliflower:  "https://images.unsplash.com/photo-1568584711075-3d021a7c3ca3?w=600&q=80&fit=crop",
    spinach:      "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&q=80&fit=crop",
    palak:        "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&q=80&fit=crop",
    pumpkin:      "https://images.unsplash.com/photo-1570586437263-ab629fccc818?w=600&q=80&fit=crop",
    cucumber:     "https://images.unsplash.com/photo-1604977042946-1eecc30f269e?w=600&q=80&fit=crop",
    bitter:       "https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&q=80&fit=crop",
    gourd:        "https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&q=80&fit=crop",
    karela:       "https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&q=80&fit=crop",
    capsicum:     "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=600&q=80&fit=crop",
    bellpepper:   "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=600&q=80&fit=crop",
    pepper:       "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=600&q=80&fit=crop",
    peas:         "https://images.unsplash.com/photo-1587049633312-d628ae50a8ae?w=600&q=80&fit=crop",
    greenpeas:    "https://images.unsplash.com/photo-1587049633312-d628ae50a8ae?w=600&q=80&fit=crop",
    coconut:      "https://images.unsplash.com/photo-1580984969071-a8da8e2eb3bc?w=600&q=80&fit=crop",
    coconuts:     "https://images.unsplash.com/photo-1580984969071-a8da8e2eb3bc?w=600&q=80&fit=crop",
    corn:         "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&q=80&fit=crop",
    maize:        "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&q=80&fit=crop",
    sweetcorn:    "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&q=80&fit=crop",
    radish:       "https://images.unsplash.com/photo-1587765024070-4f65a7da3ea9?w=600&q=80&fit=crop",
    mooli:        "https://images.unsplash.com/photo-1587765024070-4f65a7da3ea9?w=600&q=80&fit=crop",
    beetroot:     "https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?w=600&q=80&fit=crop",
    turnip:       "https://images.unsplash.com/photo-1566486189376-d5f21e25aae4?w=600&q=80&fit=crop",
    mushroom:     "https://images.unsplash.com/photo-1504545102780-26774c1bb073?w=600&q=80&fit=crop",
    mushrooms:    "https://images.unsplash.com/photo-1504545102780-26774c1bb073?w=600&q=80&fit=crop",

    // ── Fruits ─────────────────────────────────────────────────────────────────
    mango:        "https://images.unsplash.com/photo-1553279768-865429fa0078?w=600&q=80&fit=crop",
    mangoes:      "https://images.unsplash.com/photo-1553279768-865429fa0078?w=600&q=80&fit=crop",
    banana:       "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&q=80&fit=crop",
    bananas:      "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&q=80&fit=crop",
    watermelon:   "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&q=80&fit=crop",
    apple:        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&q=80&fit=crop",
    apples:       "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&q=80&fit=crop",
    orange:       "https://images.unsplash.com/photo-1547514701-42782101795e?w=600&q=80&fit=crop",
    oranges:      "https://images.unsplash.com/photo-1547514701-42782101795e?w=600&q=80&fit=crop",
    grapes:       "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=600&q=80&fit=crop",
    grape:        "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=600&q=80&fit=crop",
    papaya:       "https://images.unsplash.com/photo-1526318896980-cf78c088247c?w=600&q=80&fit=crop",
    guava:        "https://images.unsplash.com/photo-1536511132770-e5058c7e8c46?w=600&q=80&fit=crop",
    lemon:        "https://images.unsplash.com/photo-1582287014914-1db0b4a3a471?w=600&q=80&fit=crop",
    lemons:       "https://images.unsplash.com/photo-1582287014914-1db0b4a3a471?w=600&q=80&fit=crop",
    lime:         "https://images.unsplash.com/photo-1582287014914-1db0b4a3a471?w=600&q=80&fit=crop",
    pomegranate:  "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=600&q=80&fit=crop",
    pineapple:    "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=600&q=80&fit=crop",
    strawberry:   "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&q=80&fit=crop",
    strawberries: "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&q=80&fit=crop",
    jackfruit:    "https://images.unsplash.com/photo-1603048588665-791ca4e97b19?w=600&q=80&fit=crop",
    kiwi:         "https://images.unsplash.com/photo-1618897996318-5a901fa696ca?w=600&q=80&fit=crop",
    sapota:       "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600&q=80&fit=crop",
    chikoo:       "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600&q=80&fit=crop",

    // ── Grains & Cereals ───────────────────────────────────────────────────────
    rice:         "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&q=80&fit=crop",
    basmati:      "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&q=80&fit=crop",
    wheat:        "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600&q=80&fit=crop",
    oats:         "https://images.unsplash.com/photo-1614961233913-a5113a4a34ed?w=600&q=80&fit=crop",
    barley:       "https://images.unsplash.com/photo-1511348835736-84b53e8a4e55?w=600&q=80&fit=crop",
    sorghum:      "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600&q=80&fit=crop",
    jowar:        "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600&q=80&fit=crop",
    millet:       "https://images.unsplash.com/photo-1544816565-cd3f6cc34ee8?w=600&q=80&fit=crop",
    bajra:        "https://images.unsplash.com/photo-1544816565-cd3f6cc34ee8?w=600&q=80&fit=crop",
    ragi:         "https://images.unsplash.com/photo-1544816565-cd3f6cc34ee8?w=600&q=80&fit=crop",
    maize:        "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&q=80&fit=crop",
    flaxseed:     "https://images.unsplash.com/photo-1508136519-4da4dfdb0bc6?w=600&q=80&fit=crop",
    sesame:       "https://images.unsplash.com/photo-1508136519-4da4dfdb0bc6?w=600&q=80&fit=crop",
    sunflower:    "https://images.unsplash.com/photo-1508136519-4da4dfdb0bc6?w=600&q=80&fit=crop",

    // ── Pulses ─────────────────────────────────────────────────────────────────
    lentil:       "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    lentils:      "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    dal:          "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    dhal:         "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    chickpea:     "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    chickpeas:    "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    chana:        "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    rajma:        "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600&q=80&fit=crop",
    blackeyed:    "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    moong:        "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    urad:         "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    toor:         "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",
    soybean:      "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop",

    // ── Dairy ──────────────────────────────────────────────────────────────────
    milk:         "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&q=80&fit=crop",
    ghee:         "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&q=80&fit=crop",
    butter:       "https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=600&q=80&fit=crop",
    paneer:       "https://images.unsplash.com/photo-1603048588665-791ca4e97b19?w=600&q=80&fit=crop",
    curd:         "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&q=80&fit=crop",
    yogurt:       "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&q=80&fit=crop",
    cheese:       "https://images.unsplash.com/photo-1552767059-ce182ead6c1b?w=600&q=80&fit=crop",

    // ── Spices ─────────────────────────────────────────────────────────────────
    chilli:       "https://images.unsplash.com/photo-1588252317543-e380f2d93e14?w=600&q=80&fit=crop",
    chillies:     "https://images.unsplash.com/photo-1588252317543-e380f2d93e14?w=600&q=80&fit=crop",
    chili:        "https://images.unsplash.com/photo-1588252317543-e380f2d93e14?w=600&q=80&fit=crop",
    garlic:       "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=600&q=80&fit=crop",
    ginger:       "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&q=80&fit=crop",
    turmeric:     "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&q=80&fit=crop",
    haldi:        "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&q=80&fit=crop",
    coriander:    "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    cumin:        "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    jeera:        "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    cardamom:     "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    clove:        "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    mustard:      "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    fenugreek:    "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    methi:        "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    curry:        "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    saffron:      "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    nandini:      "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&q=80&fit=crop",
};

const STATIC_CATEGORY_IMAGES = {
    Vegetables: "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600&q=80&fit=crop",
    Fruits:     "https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600&q=80&fit=crop",
    Grains:     "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600&q=80&fit=crop",
    Dairy:      "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&q=80&fit=crop",
    Spices:     "https://images.unsplash.com/photo-1596797038530-2c107229654b?w=600&q=80&fit=crop",
    Pulses:     "https://images.unsplash.com/photo-1585996388907-fbba2c244c4e?w=600&q=80&fit=crop"
};

// The ultimate fallback if nothing else matches
const ULTIMATE_FALLBACK = "https://images.unsplash.com/photo-1566385101042-1a010c129fa6?w=600&q=80&fit=crop";

function getProductImage(product) {
    if (!product) return ULTIMATE_FALLBACK;
    if (product.imageUrl && product.imageUrl.startsWith('http')) return product.imageUrl;
    const nameLower = (product.name || '').toLowerCase().replace(/\s+/g, '');
    // Full word match first
    for (const key in STATIC_CROP_IMAGES) {
        if (nameLower === key) return STATIC_CROP_IMAGES[key];
    }
    // Partial match second
    for (const key in STATIC_CROP_IMAGES) {
        if (nameLower.includes(key) || key.includes(nameLower)) {
            return STATIC_CROP_IMAGES[key];
        }
    }
    // Category fallback
    return STATIC_CATEGORY_IMAGES[product.category] || ULTIMATE_FALLBACK;
}

// Called when an img fails to load — cascades through fallbacks
function onProductImageError(img, product) {
    const category = img.getAttribute('data-category') || 'Vegetables';
    const fallback = STATIC_CATEGORY_IMAGES[category] || ULTIMATE_FALLBACK;
    if (img.src !== fallback) {
        img.src = fallback;
    } else {
        img.src = ULTIMATE_FALLBACK;
    }
    img.onerror = null; // prevent infinite loop
}




// --- Global Language Persistence (Google Translate) ---
function changeLanguage(langCode) {
    if (langCode === 'en') {
        document.cookie = 'googtrans=/en/en; expires=Thu, 01-Jan-1970 00:00:01 GMT; path=/';
        document.cookie = 'googtrans=/en/en; expires=Thu, 01-Jan-1970 00:00:01 GMT; domain=' + window.location.hostname + '; path=/';
    } else {
        document.cookie = 'googtrans=/en/' + langCode + '; path=/';
        document.cookie = 'googtrans=/en/' + langCode + '; domain=' + window.location.hostname + '; path=/';
    }
    localStorage.setItem('preferredLang', langCode);
    window.location.reload();
}

(function initLanguage() {
    const savedLang = localStorage.getItem('preferredLang');
    if (savedLang && savedLang !== 'en') {
        document.cookie = 'googtrans=/en/' + savedLang + '; path=/';
        document.cookie = 'googtrans=/en/' + savedLang + '; domain=' + window.location.hostname + '; path=/';
    } else if (savedLang === 'en') {
        document.cookie = 'googtrans=/en/en; expires=Thu, 01-Jan-1970 00:00:01 GMT; path=/';
        document.cookie = 'googtrans=/en/en; expires=Thu, 01-Jan-1970 00:00:01 GMT; domain=' + window.location.hostname + '; path=/';
    }
    
    // Set the select element to the correct value on load
    window.addEventListener('DOMContentLoaded', () => {
        const selector = document.querySelector('.lang-selector');
        if (selector) {
            selector.value = savedLang || 'en';
        }
    });
})();
