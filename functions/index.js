const express = require("express");
const cors = require("cors");

const app = express();

const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors({ origin: "*" }));
app.use(express.json());

/* =========================
   TEST ROUTE
========================= */
app.get("/", (req, res) => {
  res.send("Agro Backend Running 🚀");
});

/* =========================
   AUTH ROUTES (FAKE for now)
========================= */
app.post("/api/users/register", (req, res) => {
  const user = req.body;

  res.json({
    success: true,
    message: "User registered successfully",
    user
  });
});

app.post("/api/users/login", (req, res) => {
  res.json({
    success: true,
    message: "Login successful",
    token: "dummy-token-123"
  });
});

/* =========================
   PRODUCTS ROUTES
========================= */
let products = [];

app.get("/api/products", (req, res) => {
  res.json(products);
});

app.post("/api/products", (req, res) => {
  const product = req.body;
  products.push(product);

  res.json({
    success: true,
    message: "Product added",
    product
  });
});

/* =========================
   ORDERS ROUTES
========================= */
let orders = [];

app.post("/api/orders", (req, res) => {
  const order = req.body;
  orders.push(order);

  res.json({
    success: true,
    message: "Order placed",
    order
  });
});

app.get("/api/orders", (req, res) => {
  res.json(orders);
});

/* =========================
   START SERVER
========================= */
app.listen(PORT, () => {
  console.log("Server running on port " + PORT);
});
