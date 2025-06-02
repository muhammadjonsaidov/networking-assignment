
// API Types matching your backend DTOs
export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id?: number;
  name: string;
  price: number;
  stock: number;
  status?: string;
  category?: string;
  description?: string;
}

export interface Order {
  id: number;
  product: Product;
  customer: Customer;
  createdBy: User;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';
  orderDate: string;
  createdAt: string;
  updatedAt?: string;
}

export interface Activity {
  id: number;
  actor: string;
  action: string;
  details: string;
  timestamp: string;
}

export interface DashboardStats {
  totalProduct: number;
  productRevenue: number;
  productSold: number;
  avgMonthlySales: number;
  revenueChange: number;
  soldChange: number;
  avgSalesChange: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  role?: 'ROLE_USER' | 'ROLE_ADMIN';
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
