/** @type {import('next').NextConfig} */
const nextConfig = {
  // i18n: {
  //   locales: ['en', 'ko', 'zh', 'ja'],
  //   defaultLocale: 'ko',
  // },
  webpack: (config, { buildId, dev, isServer, defaultLoaders, webpack }) => {
    config.cache = false;
    config.module.rules.push({
      test: /\.svg$/,
      use: ['@svgr/webpack'],
    });
    return config;
  },
};

export default nextConfig;
