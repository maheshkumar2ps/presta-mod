/** @type {import('next').NextConfig} */
// S3 hostname for product images (e.g. my-bucket.s3.us-east-1.amazonaws.com)
// Set NEXT_PUBLIC_S3_IMAGE_HOSTNAME so Next.js Image can load S3 URLs.
const s3ImageHost = process.env.NEXT_PUBLIC_S3_IMAGE_HOSTNAME;

const nextConfig = {
  output: 'standalone',
  images: {
    // Disable optimization to avoid sharp dependency and Docker fetch issues
    unoptimized: true,
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '8080',
        pathname: '/images/**',
      },
      {
        protocol: 'http',
        hostname: 'backend',
        port: '8080',
        pathname: '/images/**',
      },
      // Allow S3 product image URLs (required when using S3 for uploads)
      ...(s3ImageHost
        ? [
            {
              protocol: 'https',
              hostname: s3ImageHost,
              pathname: '/**',
            },
          ]
        : []),
    ],
  },
  async rewrites() {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
    const baseUrl = apiUrl.replace('/api/v1', '');

    return [
      {
        source: '/api/:path*',
        destination: `${baseUrl}/api/:path*`,
      },
      {
        source: '/images/:path*',
        destination: `${baseUrl}/images/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
