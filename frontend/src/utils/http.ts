import axios from 'axios'

const BASE_API = import.meta.env.VITE_BASE_API || '/api'

// 是否启用 Mock（默认 true，可通过环境变量控制）
const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false'

const http = axios.create({
  baseURL: BASE_API,
  timeout: 15000,
  withCredentials: false,
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
})

// 请求拦截：可在此注入 Token
http.interceptors.request.use((config) => {
  // const token = localStorage.getItem('token')
  // if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：统一处理后端的 {code,message,data,timestamp}
http.interceptors.response.use(
  (response) => {
    // 二进制下载等直接放行
    const rt = response.request?.responseType
    const ct = response.headers?.['content-type'] || ''
    if (rt === 'blob' || rt === 'arraybuffer' || ct.includes('octet-stream')) {
      return response
    }

    const payload = response.data
    // 按后端统一结构解包
    if (payload && typeof payload === 'object' && 'code' in payload && 'data' in payload) {
      // 兼容 code === 0 或 code === 200 都表示成功
      if (payload.code === 0 || payload.code === 200) return payload.data
      const err = new Error(payload.message || '业务失败') as any
      err.code = payload.code
      err.response = response
      throw err
    }
    // 非统一结构（极少数兼容场景）
    return payload
  },
  (error) => {
    if (error.response) {
      // 后端返回了响应，但可能有错误
      const p = error.response.data
      const msg = p?.message || `HTTP ${error.response.status}`
      const err = new Error(msg) as any
      err.code = p?.code ?? error.response.status
      err.response = error.response
      throw err
    } else if (error.request) {
      // 网络错误
      throw new Error('网络不可用或服务器无响应')
    } else {
      throw new Error(error.message || '请求发生错误')
    }
  }
)

export { http }
export { USE_MOCK }
