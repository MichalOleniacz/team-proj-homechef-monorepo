export type LoginRequest = {
    email: string;
    password: string;
};

export type RegisterRequest = {
    email: string;
    password: string;
};

export type RegisterFormValues = {
    email: string;
    password: string;
    confirmPassword: string;
};

export type AuthResponse = {
    userId: string;
    email: string;
    accessToken: string;
    expiresIn: number; // int64
};
