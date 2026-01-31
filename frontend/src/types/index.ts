// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

// Pagination
export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      direction: string;
    };
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  number: number;
  size: number;
}

// Product
export interface Product {
  id: number;
  name: string;
  description?: string;
  descriptionShort?: string;
  linkRewrite: string;
  price: number;
  salePrice?: number;
  wholesalePrice?: number;
  reference?: string;
  ean13?: string;
  quantity: number;
  inStock: boolean;
  active: boolean;
  visibility: 'BOTH' | 'CATALOG' | 'SEARCH' | 'NONE';
  condition: 'NEW' | 'USED' | 'REFURBISHED';
  productType: 'STANDARD' | 'PACK' | 'VIRTUAL' | 'COMBINATIONS';
  onSale: boolean;
  weight?: number;
  metaTitle?: string;
  metaDescription?: string;
  defaultCategory?: Category;
  categories?: Category[];
  images?: ProductImage[];
  coverImage?: string;
  variants?: ProductVariant[];
  dateAdd?: string;
  dateUpd?: string;
}

// Product for listing (PLP)
export interface ProductListing {
  id: number;
  name: string;
  descriptionShort?: string;
  linkRewrite: string;
  price: number;
  salePrice?: number;
  reference?: string;
  quantity: number;
  inStock: boolean;
  onSale: boolean;
  coverImage?: string;
  defaultCategory?: {
    id: number;
    name: string;
    linkRewrite: string;
  };
}

// Category
export interface Category {
  id: number;
  name: string;
  description?: string;
  linkRewrite: string;
  parentId?: number;
  levelDepth: number;
  position: number;
  active: boolean;
  metaTitle?: string;
  metaDescription?: string;
  children?: Category[];
  breadcrumb?: Breadcrumb[];
}

export interface Breadcrumb {
  id: number;
  name: string;
  linkRewrite: string;
}

// Product Image
export interface ProductImage {
  id: number;
  productId: number;
  url: string;
  position: number;
  cover: boolean;
  legend?: string;
}

// Product Variant
export interface ProductVariant {
  id: number;
  productId: number;
  name: string;
  reference?: string;
  ean13?: string;
  price: number;
  priceImpact: number;
  quantity: number;
  inStock: boolean;
  defaultOn: boolean;
}

// Auth
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  employee: Employee;
}

export interface Employee {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  profile: string;
}

// Product Create/Update DTOs
export interface ProductCreateDto {
  name: string;
  description?: string;
  descriptionShort?: string;
  linkRewrite?: string;
  price: number;
  wholesalePrice?: number;
  quantity?: number;
  minimalQuantity?: number;
  reference?: string;
  ean13?: string;
  isbn?: string;
  weight?: number;
  width?: number;
  height?: number;
  depth?: number;
  active?: boolean;
  visibility?: string;
  condition?: string;
  productType?: string;
  onSale?: boolean;
  onlineOnly?: boolean;
  metaTitle?: string;
  metaDescription?: string;
  defaultCategoryId?: number;
  categoryIds?: number[];
}

export interface ProductUpdateDto extends Partial<ProductCreateDto> {}

// Category Create DTO
export interface CategoryCreateDto {
  name: string;
  description?: string;
  linkRewrite?: string;
  parentId?: number;
  position?: number;
  active?: boolean;
  metaTitle?: string;
  metaDescription?: string;
}
