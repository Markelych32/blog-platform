import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig, AxiosError } from 'axios';

// Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
}

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  avatar?: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
}

export interface Category {
  id: string;
  name: string;
  postCount?: number;
}

export interface Tag {
  id: string;
  name: string;
  postCount?: number;
}

export interface Post {
  id: string;
  title: string;
  content: string;
  author?: {
    id: string;
    name: string;
  };
  category: Category;
  tags: Tag[];
  readingTime?: number;
  createdAt: string;
  updatedAt: string;
  status?: PostStatus;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  categoryId: string;
  tagIds: string[];
  status: PostStatus;
}

export interface UpdatePostRequest extends CreatePostRequest {
  id: string;
}

export interface ApiError {
  status: number;
  message: string;
  errors?: Array<{
    field: string;
    message: string;
  }>;
}

export enum PostStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED'
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

class ApiService {
  private api: AxiosInstance;
  private static instance: ApiService;

  private constructor() {
    this.api = axios.create({
      baseURL: '/api/v1',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Add request interceptor for authentication
    this.api.interceptors.request.use(
        (config: InternalAxiosRequestConfig) => {
          const token = localStorage.getItem('token');
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
          return config;
        },
        (error: AxiosError) => {
          return Promise.reject(error);
        }
    );

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
        (response: AxiosResponse) => response,
        (error: AxiosError) => {
          if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
          }
          return Promise.reject(this.handleError(error));
        }
    );
  }

  public static getInstance(): ApiService {
    if (!ApiService.instance) {
      ApiService.instance = new ApiService();
    }
    return ApiService.instance;
  }

  private handleError(error: AxiosError): ApiError {
    if (error.response?.data) {
      return error.response.data as ApiError;
    }
    return {
      status: 500,
      message: 'An unexpected error occurred'
    };
  }

  // Auth endpoints
  public async login(credentials: LoginRequest): Promise<AuthResponse> {
    console.log('Login credentials:', JSON.stringify(credentials));
    const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/login', credentials);
    localStorage.setItem('token', response.data.token);
    return response.data;
  }

  public async signup(userData: SignupRequest): Promise<string> {
    const response: AxiosResponse<string> = await this.api.post('/auth/signup', userData);
    return response.data;
  }

  public async getUserProfile(): Promise<UserProfile> {
    const response: AxiosResponse<UserProfile> = await this.api.get('/auth/profile');
    return response.data;
  }

  public logout(): void {
    localStorage.removeItem('token');
  }

  // Posts endpoints
  public async getPosts(params: {
    categoryId?: string;
    tagId?: string;
  }): Promise<Post[]> {
    const response: AxiosResponse<Post[]> = await this.api.get('/posts', { params });
    return response.data;
  }

  public async getPost(id: string): Promise<Post> {
    const response: AxiosResponse<Post> = await this.api.get(`/posts/${id}`);
    return response.data;
  }

  public async createPost(post: CreatePostRequest): Promise<Post> {
    const response: AxiosResponse<Post> = await this.api.post('/posts', post);
    return response.data;
  }

  public async updatePost(id: string, post: UpdatePostRequest): Promise<Post> {
    const response: AxiosResponse<Post> = await this.api.put(`/posts/${id}`, post);
    return response.data;
  }

  public async deletePost(id: string): Promise<void> {
    await this.api.delete(`/posts/${id}`);
  }

  public async getDrafts(params: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Post[]> {
    const response: AxiosResponse<Post[]> = await this.api.get('/posts/drafts', { params });
    return response.data;
  }

  // Categories endpoints
  public async getCategories(): Promise<Category[]> {
    const response: AxiosResponse<Category[]> = await this.api.get('/categories');
    return response.data;
  }

  public async createCategory(name: string): Promise<Category> {
    const response: AxiosResponse<Category> = await this.api.post('/categories', { name });
    return response.data;
  }

  public async updateCategory(id: string, name: string): Promise<Category> {
    const response: AxiosResponse<Category> = await this.api.put(`/categories/${id}`, { id, name });
    return response.data;
  }

  public async deleteCategory(id: string): Promise<void> {
    await this.api.delete(`/categories/${id}`);
  }

  // Tags endpoints
  public async getTags(params?: {
    page?: number;
    size?: number;
  }): Promise<Tag[] | PaginatedResponse<Tag>> {
    try {
      const response: AxiosResponse = await this.api.get('/tags', { params });

      // Check if the response is in the paginated format
      if (response.data && typeof response.data === 'object' && 'content' in response.data) {
        // Return the paginated response
        return response.data as PaginatedResponse<Tag>;
      }

      // If not paginated, return the array directly
      return response.data as Tag[];
    } catch (error) {
      console.error('Error fetching tags:', error);
      throw error;
    }
  }

  public async createTags(names: string[]): Promise<Tag[]> {
    const response: AxiosResponse<Tag[]> = await this.api.post('/tags', { names });
    return response.data;
  }

  public async deleteTag(id: string): Promise<void> {
    await this.api.delete(`/tags/${id}`);
  }
}

// Export a singleton instance
export const apiService = ApiService.getInstance();