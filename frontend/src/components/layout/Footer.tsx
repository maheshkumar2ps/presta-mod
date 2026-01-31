import Link from 'next/link';

export function Footer() {
  return (
    <footer className="border-t bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-4">
          {/* Brand */}
          <div>
            <h3 className="mb-4 text-lg font-bold text-primary-600">PrestaShop</h3>
            <p className="text-sm text-gray-600">
              Your one-stop shop for amazing products at great prices.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="mb-4 font-medium">Quick Links</h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>
                <Link href="/" className="hover:text-primary-600">
                  Home
                </Link>
              </li>
              <li>
                <Link href="/category/home" className="hover:text-primary-600">
                  Products
                </Link>
              </li>
              <li>
                <Link href="/category/electronics" className="hover:text-primary-600">
                  Electronics
                </Link>
              </li>
              <li>
                <Link href="/category/clothing" className="hover:text-primary-600">
                  Clothing
                </Link>
              </li>
            </ul>
          </div>

          {/* Customer Service */}
          <div>
            <h4 className="mb-4 font-medium">Customer Service</h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>Contact Us</li>
              <li>Shipping & Returns</li>
              <li>FAQ</li>
              <li>Size Guide</li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="mb-4 font-medium">Contact</h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>support@prestashop.com</li>
              <li>1-800-PRESTASHOP</li>
            </ul>
          </div>
        </div>

        <div className="mt-8 border-t pt-8 text-center text-sm text-gray-600">
          <p>&copy; 2024 PrestaShop. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
