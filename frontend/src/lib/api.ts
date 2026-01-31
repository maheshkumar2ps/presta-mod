import {
  ApiResponse,
  Page,
  Product,
  ProductListing,
  Category,
  ProductImage,
  ProductVariant,
  LoginRequest,
  LoginResponse,
  ProductCreateDto,
  ProductUpdateDto,
  CategoryCreateDto,
} from '@/types';

// Use different API URLs for server-side (Docker internal) vs client-side (browser)
const API_BASE = typeof window === 'undefined'
  ? (process.env.API_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1')
  : (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1');

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`;

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Add auth token if available (client-side only)
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) {
      (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new ApiError(response.status, error.message || 'An error occurred');
  }

  return response.json();
}

// Public API - Products
export const productsApi = {
  list: (params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<ApiResponse<Page<ProductListing>>> => {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    return fetchApi(`/products${query ? `?${query}` : ''}`);
  },

  search: (query: string, params?: {
    page?: number;
    size?: number;
  }): Promise<ApiResponse<Page<ProductListing>>> => {
    const searchParams = new URLSearchParams({ q: query });
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size) searchParams.set('size', String(params.size));

    return fetchApi(`/products/search?${searchParams}`);
  },

  getBySlug: (slug: string): Promise<ApiResponse<Product>> => {
    return fetchApi(`/products/${slug}`);
  },

  getVariants: (slug: string): Promise<ApiResponse<ProductVariant[]>> => {
    return fetchApi(`/products/${slug}/variants`);
  },

  getImages: (slug: string): Promise<ApiResponse<ProductImage[]>> => {
    return fetchApi(`/products/${slug}/images`);
  },
};

// Public API - Categories
export const categoriesApi = {
  getTree: (): Promise<ApiResponse<Category[]>> => {
    return fetchApi('/categories');
  },

  getAll: (): Promise<ApiResponse<Category[]>> => {
    return fetchApi('/categories/flat');
  },

  getBySlug: (slug: string): Promise<ApiResponse<Category>> => {
    return fetchApi(`/categories/${slug}`);
  },

  getProducts: (slug: string, params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<ApiResponse<Page<ProductListing>>> => {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    return fetchApi(`/categories/${slug}/products${query ? `?${query}` : ''}`);
  },
};

// Auth API
export const authApi = {
  login: (credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    return fetchApi('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  },
};

// Admin API - Products
export const adminProductsApi = {
  list: (params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<ApiResponse<Page<Product>>> => {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    return fetchApi(`/admin/products${query ? `?${query}` : ''}`);
  },

  getById: (id: number): Promise<ApiResponse<Product>> => {
    return fetchApi(`/admin/products/${id}`);
  },

  create: (data: ProductCreateDto): Promise<ApiResponse<Product>> => {
    return fetchApi('/admin/products', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  update: (id: number, data: ProductUpdateDto): Promise<ApiResponse<Product>> => {
    return fetchApi(`/admin/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  delete: (id: number): Promise<ApiResponse<void>> => {
    return fetchApi(`/admin/products/${id}`, {
      method: 'DELETE',
    });
  },

  bulkUpdateStatus: (ids: number[], active: boolean): Promise<ApiResponse<void>> => {
    const params = new URLSearchParams();
    ids.forEach(id => params.append('ids', String(id)));
    params.set('active', String(active));

    return fetchApi(`/admin/products/bulk/status?${params}`, {
      method: 'PATCH',
    });
  },

  bulkDelete: (ids: number[]): Promise<ApiResponse<void>> => {
    const params = new URLSearchParams();
    ids.forEach(id => params.append('ids', String(id)));

    return fetchApi(`/admin/products/bulk?${params}`, {
      method: 'DELETE',
    });
  },

  uploadImage: async (productId: number, file: File, legend?: string, cover?: boolean): Promise<ApiResponse<ProductImage>> => {
    const formData = new FormData();
    formData.append('file', file);
    if (legend) formData.append('legend', legend);
    if (cover) formData.append('cover', 'true');

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    const response = await fetch(`${API_BASE}/admin/products/${productId}/images`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Upload failed' }));
      throw new ApiError(response.status, error.message);
    }

    return response.json();
  },

  deleteImage: (imageId: number): Promise<ApiResponse<void>> => {
    return fetchApi(`/admin/products/images/${imageId}`, {
      method: 'DELETE',
    });
  },
};

// Admin API - Categories
export const adminCategoriesApi = {
  list: (): Promise<ApiResponse<Category[]>> => {
    return fetchApi('/admin/categories');
  },

  getTree: (): Promise<ApiResponse<Category[]>> => {
    return fetchApi('/admin/categories/tree');
  },

  getById: (id: number): Promise<ApiResponse<Category>> => {
    return fetchApi(`/admin/categories/${id}`);
  },

  create: (data: CategoryCreateDto): Promise<ApiResponse<Category>> => {
    return fetchApi('/admin/categories', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  update: (id: number, data: CategoryCreateDto): Promise<ApiResponse<Category>> => {
    return fetchApi(`/admin/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  delete: (id: number): Promise<ApiResponse<void>> => {
    return fetchApi(`/admin/categories/${id}`, {
      method: 'DELETE',
    });
  },
};

export { ApiError };
