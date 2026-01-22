export type LoginRequest = {
    email: string;
    password: string;
};

export type RegisterRequest = {
    email: string;
    password: string;
    confirmPassword: string;
};

export type AuthUser = {
    email: string;
};

export type AuthResponse = {
    token?: string;
    user?: AuthUser;
};
