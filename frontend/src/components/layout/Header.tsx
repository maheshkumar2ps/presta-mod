import Link from 'next/link';
import { SearchBar } from './SearchBar';

export function Header() {
  return (
    <header className="sticky top-0 z-50 border-b bg-white">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between gap-4">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2">
            <span className="text-2xl font-bold text-primary-600">PrestaShop</span>
          </Link>

          {/* Search */}
          <div className="hidden flex-1 md:flex md:max-w-md">
            <SearchBar />
          </div>

          {/* Navigation */}
          <nav className="flex items-center gap-4">
            <Link
              href="/category/home"
              className="text-sm font-medium text-gray-700 hover:text-primary-600"
            >
              Products
            </Link>
            <Link
              href="/admin/login"
              className="btn-outline btn-sm"
            >
              Admin
            </Link>
          </nav>
        </div>

        {/* Mobile Search */}
        <div className="pb-4 md:hidden">
          <SearchBar />
        </div>
      </div>
    </header>
  );
}
