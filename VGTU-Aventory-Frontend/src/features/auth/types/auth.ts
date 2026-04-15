export interface RegisterRequest {
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  email: string;
  role: string;
}
