// Vue CLI 配置文件
module.exports = {
  // 开发服务器配置
  devServer: {
    // 监听所有网络接口
    host: '0.0.0.0',
    // 使用8080端口
    port: 8080,
    // 自动打开浏览器
    open: true,
    // 配置API代理，将/api请求转发到后端服务器
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  }
}