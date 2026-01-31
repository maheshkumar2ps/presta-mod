'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import Link from 'next/link';
import { Employee } from '@/types';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const employeeData = localStorage.getItem('employee');

    if (!token || !employeeData) {
      if (pathname !== '/admin/login') {
        router.push('/admin/login');
      }
      setLoading(false);
      return;
    }

    try {
      setEmployee(JSON.parse(employeeData));
    } catch {
      localStorage.removeItem('token');
      localStorage.removeItem('employee');
      router.push('/admin/login');
    }
    setLoading(false);
  }, [pathname, router]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('employee');
    router.push('/admin/login');
  };

  // Show login page without layout
  if (pathname === '/admin/login') {
    return children;
  }

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!employee) {
    return null;
  }

  const navigation = [
    { name: 'Products', href: '/admin/products', icon: '📦' },
    { name: 'Categories', href: '/admin/categories', icon: '📁' },
  ];

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="w-64 border-r bg-gray-50">
        <div className="flex h-16 items-center border-b px-6">
          <Link href="/admin/products" className="text-xl font-bold text-primary-600">
            Admin Panel
          </Link>
        </div>

        <nav className="p-4">
          <ul className="space-y-1">
            {navigation.map((item) => (
              <li key={item.name}>
                <Link
                  href={item.href}
                  className={`flex items-center gap-3 rounded-md px-4 py-2 text-sm font-medium transition-colors ${
                    pathname.startsWith(item.href)
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <span>{item.icon}</span>
                  {item.name}
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        <div className="absolute bottom-0 w-64 border-t p-4">
          <div className="flex items-center justify-between">
            <div className="text-sm">
              <p className="font-medium">{employee.fullName}</p>
              <p className="text-gray-500">{employee.profile}</p>
            </div>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              Logout
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <div className="h-16 border-b bg-white px-6 flex items-center justify-between">
          <h1 className="text-lg font-semibold">
            {navigation.find((n) => pathname.startsWith(n.href))?.name || 'Dashboard'}
          </h1>
          <Link href="/" className="text-sm text-primary-600 hover:text-primary-700">
            ← Back to Store
          </Link>
        </div>
        <div className="p-6">{children}</div>
      </main>
    </div>
  );
}
