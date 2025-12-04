<template>
  <div class="editor-container">
    <label v-if="label" class="editor-label">{{ label }}</label>
    <div ref="editorRef" class="rich-text-editor"
         :contenteditable="!disabled"
         @input="onInput"
         @paste="onPaste"></div>
    <div class="toolbar" v-if="!disabled">
      <button type="button" @click="formatText('bold')" title="粗体">粗体</button>
      <button type="button" @click="formatText('italic')" title="斜体">斜体</button>
      <button type="button" @click="formatText('underline')" title="下划线">下划线</button>
      <button type="button" @click="triggerImageUpload" title="插入图片">插入图片</button>
      <button type="button" @click="showUploadConfig" title="配置上传路径">配置上传</button>
      <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="uploadImage">
    </div>
    
    <!-- 上传路径配置对话框 -->
    <div v-if="showConfigDialog" class="config-dialog-overlay" @click="closeConfigDialog">
      <div class="config-dialog" @click.stop>
        <h3>配置图片上传路径</h3>
        <div class="config-item">
          <label>自定义上传路径：</label>
          <input type="text" v-model="customUploadPath" placeholder="例如：/api/custom/upload">
          <button @click="addCustomPath">添加</button>
        </div>
        <div class="config-item">
          <label>当前使用的路径列表：</label>
          <div class="path-list">
            <div v-for="(path, index) in getCurrentApiPaths()" :key="index" class="path-item">
              {{ path }}
              <button v-if="isCustomPath(path)" @click="removeCustomPath(index)" class="remove-btn">删除</button>
            </div>
          </div>
        </div>
        <div class="dialog-buttons">
          <button @click="resetPaths">重置为默认路径</button>
          <button @click="closeConfigDialog" class="confirm-btn">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RichTextEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    label: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    // 允许父组件传入自定义的API路径列表
    customApiPaths: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      showConfigDialog: false,
      customUploadPath: ''
    }
  },
  mounted() {
    // 初始化编辑器内容，添加空值检查
    if (this.modelValue && this.$refs && this.$refs.editorRef) {
      this.$refs.editorRef.innerHTML = this.modelValue
    }
  },
  watch: {
    modelValue: {
      handler(newVal) {
        // 确保editorRef已初始化，添加更严格的空值检查
        if (this.$refs && this.$refs.editorRef && newVal !== this.$refs.editorRef.innerHTML) {
          // 只有当外部传入的值与编辑器当前内容不同时才更新
          // 避免编辑器内部修改导致的循环更新
          this.$refs.editorRef.innerHTML = newVal
        }
      },
      immediate: true
    }
  },
  methods: {
    // 获取当前使用的API路径列表
    getCurrentApiPaths() {
      const defaultPaths = [
        '/api/files/upload/images', 
        '/files/upload/images', 
        '/admin/upload/image',
        '/api/upload/image',
        '/upload/image',
        '/admin/files/upload',
        '/api/product/upload',
        '/product/upload',
        '/upload'
      ]
      const userCustomPaths = JSON.parse(localStorage.getItem('richTextEditorCustomPaths') || '[]')
      return [...new Set([...userCustomPaths, ...this.customApiPaths, ...defaultPaths])]
    },
    
    // 检查是否为自定义路径
    isCustomPath(path) {
      const defaultPaths = [
        '/api/files/upload/images', 
        '/files/upload/images', 
        '/admin/upload/image',
        '/api/upload/image',
        '/upload/image',
        '/admin/files/upload',
        '/api/product/upload',
        '/product/upload',
        '/upload'
      ]
      return !defaultPaths.includes(path) && !this.customApiPaths.includes(path)
    },
    
    // 显示上传配置对话框
    showUploadConfig() {
      this.showConfigDialog = true
    },
    
    // 关闭配置对话框
    closeConfigDialog() {
      this.showConfigDialog = false
      this.customUploadPath = ''
    },
    
    // 添加自定义路径
    addCustomPath() {
      const path = this.customUploadPath.trim()
      if (!path) {
        alert('请输入有效的路径')
        return
      }
      
      // 检查是否是本地文件系统路径
      if (path.includes('\\') || path.startsWith('C:') || path.startsWith('D:') || path.startsWith('E:')) {
        alert('不能使用本地文件系统路径！请输入API路径，例如：/api/upload/images 或 http://your-server/upload')
        return
      }
      
      let customPaths = JSON.parse(localStorage.getItem('richTextEditorCustomPaths') || '[]')
      if (!customPaths.includes(path)) {
        customPaths.push(path)
        localStorage.setItem('richTextEditorCustomPaths', JSON.stringify(customPaths))
        alert('路径添加成功！')
        this.customUploadPath = ''
      } else {
        alert('该路径已存在')
      }
    },
    
    // 删除自定义路径
    removeCustomPath(index) {
      const currentPaths = this.getCurrentApiPaths()
      const pathToRemove = currentPaths[index]
      
      let customPaths = JSON.parse(localStorage.getItem('richTextEditorCustomPaths') || '[]')
      customPaths = customPaths.filter(path => path !== pathToRemove)
      localStorage.setItem('richTextEditorCustomPaths', JSON.stringify(customPaths))
    },
    
    // 重置为默认路径
    resetPaths() {
      if (confirm('确定要重置为默认路径吗？这将清除所有自定义路径。')) {
        localStorage.removeItem('richTextEditorCustomPaths')
        alert('已重置为默认路径')
      }
    },
    
    // 处理输入事件，直接发送内容而不重新渲染
    onInput() {
      const content = this.$refs.editorRef.innerHTML
      this.$emit('update:modelValue', content)
    },
    
    // 处理粘贴事件
    onPaste(event) {
      // 阻止默认粘贴行为
      event.preventDefault()
      
      // 获取粘贴的文本
      const text = (event.clipboardData || window.clipboardData).getData('text/plain')
      
      // 使用document.execCommand插入文本，保持光标位置
      document.execCommand('insertText', false, text)
      
      // 触发输入事件更新值
      this.onInput()
    },
    
    // 文本格式化
    formatText(command) {
      try {
        document.execCommand(command, false, null)
        this.$refs.editorRef.focus()
        this.onInput()
      } catch (error) {
        console.error('格式化文本失败:', error)
      }
    },
    
    // 触发图片上传
    triggerImageUpload() {
      this.$refs.fileInput.click()
    },
    
    // 处理图片上传
    uploadImage(event) {
      // 添加事件目标检查
      if (!event || !event.target) {
        console.error('上传事件无效')
        return
      }
      
      const file = event.target.files[0]
      if (!file) return
      
      // 检查文件类型
      const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
      if (!validTypes.includes(file.type)) {
        alert('请上传有效的图片文件（JPG、PNG、GIF、WebP）')
        if (event.target) {
          event.target.value = ''
        }
        return
      }
      
      // 检查文件大小（限制为5MB）
      if (file.size > 5 * 1024 * 1024) {
        alert('图片大小不能超过5MB')
        if (event.target) {
          event.target.value = ''
        }
        return
      }
      
      console.log('开始上传图片:', file.name)
      
      const formData = new FormData()
      formData.append('files', file)
      
      // 尝试获取认证token，如果有的话
      const token = localStorage.getItem('token') || sessionStorage.getItem('token')
      
      // 配置请求头部
      const headers = {}
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
      
      // 获取所有可用的API路径，并过滤掉本地文件系统路径
      const apiPaths = this.getCurrentApiPaths().filter(path => 
        !path.includes('\\') && 
        !path.startsWith('C:') && 
        !path.startsWith('D:') && 
        !path.startsWith('E:')
      )
      
      console.log('可用的上传路径列表:', apiPaths)
      
      // 定义递归上传函数
      const tryUpload = (pathIndex) => {
        if (pathIndex >= apiPaths.length) {
          throw new Error('所有API路径尝试均失败，请检查服务器配置')
        }
        
        const currentPath = apiPaths[pathIndex]
        console.log(`尝试上传图片，目标URL: ${currentPath}, 尝试次数: ${pathIndex + 1}`)
        console.log('认证token是否存在:', !!token)
        
        return fetch(currentPath, {
          method: 'POST',
          body: formData,
          credentials: 'include', // 确保发送cookies
          headers: headers
        })
        .then(response => {
          console.log(`路径 ${currentPath} 响应状态:`, response.status)
          console.log('响应内容类型:', response.headers.get('content-type'))
          
          // 如果是404错误，尝试下一个路径
          if (response.status === 404) {
            console.log(`路径 ${currentPath} 返回404，尝试下一个路径`)
            return tryUpload(pathIndex + 1)
          }
          
          // 检查其他错误状态码
          if (!response.ok) {
            throw new Error(`请求失败，状态码: ${response.status}`)
          }
          
          // 获取原始响应文本
          return response.text().then(text => {
            console.log('原始响应内容:', text.substring(0, 100) + '...')
            
            // 检查是否包含HTML标签
            if (text.includes('<!DOCTYPE') || text.includes('<html') || text.includes('<head')) {
              console.error('检测到HTML响应，尝试下一个API路径')
              return tryUpload(pathIndex + 1)
            }
            
            // 尝试解析JSON
            try {
              return JSON.parse(text)
            } catch (e) {
              // 如果解析失败，尝试下一个路径
              console.error(`路径 ${currentPath} 返回非JSON响应，尝试下一个路径`)
              return tryUpload(pathIndex + 1)
            }
          })
        })
      }
      
      // 开始尝试上传，从第一个API路径开始
      tryUpload(0)
      .then(data => {
        console.log('上传响应数据格式:', Object.keys(data))
        // 检查各种可能的响应格式
        if (data && data.imageUrls && data.imageUrls.length > 0) {
          this.insertImageAtCursor(data.imageUrls[0])
        } else if (data && data.url) {
          this.insertImageAtCursor(data.url)
        } else if (data && data.data && data.data.imageUrls && data.data.imageUrls.length > 0) {
          this.insertImageAtCursor(data.data.imageUrls[0])
        } else {
          console.error('未预期的响应格式:', data)
          alert('上传失败: 服务器返回格式不正确')
        }
      })
      .catch(error => {
        console.error('图片上传错误:', error)
        // 更友好的错误提示
        if (error.message.includes('Failed to fetch')) {
          alert('上传失败: 无法连接到服务器。请确保API路径正确，并且服务器正在运行。本地文件系统路径不能用于上传！')
        } else if (error.message.includes('HTML')) {
          alert('上传失败: 服务器返回HTML内容而非JSON，可能API路径不正确或服务器未正确配置')
        } else if (error.message.includes('Unexpected token')) {
          alert('上传失败: 服务器返回的内容不是有效的JSON格式')
        } else if (error.message.includes('所有API路径尝试均失败')) {
          alert('上传失败: 无法连接到正确的图片上传API，请联系管理员或配置有效的API上传路径')
        } else {
          alert('上传失败: ' + error.message)
        }
      })
      .finally(() => {
        // 清空文件输入，添加安全检查
        if (event && event.target) {
          event.target.value = ''
        }
      })
    },
    
    // 在光标位置插入图片
    insertImageAtCursor(imageUrl) {
      try {
        // 添加更严格的空值检查
        if (!this.$refs || !this.$refs.editorRef) {
          console.error('编辑器引用未初始化')
          alert('编辑器未准备就绪，请稍后再试')
          return
        }
        
        const selection = window.getSelection()
        if (!selection || selection.rangeCount === 0) {
          // 如果没有选中区域，直接在末尾添加
          const img = document.createElement('img')
          img.src = imageUrl
          img.style.maxWidth = '100%'
          this.$refs.editorRef.appendChild(img)
        } else {
          // 获取当前选中区域
          const range = selection.getRangeAt(0)
          
          // 创建图片元素
          const img = document.createElement('img')
          img.src = imageUrl
          img.style.maxWidth = '100%'
          
          // 在光标位置插入图片
          range.deleteContents()
          range.insertNode(img)
          
          // 将光标移动到图片后面
          range.setStartAfter(img)
          range.setEndAfter(img)
          selection.removeAllRanges()
          selection.addRange(range)
        }
        
        // 更新内容并保持焦点
        this.onInput()
        if (this.$refs && this.$refs.editorRef) {
          this.$refs.editorRef.focus()
        }
      } catch (error) {
        console.error('插入图片失败:', error)
        alert('插入图片失败，请重试')
      }
    },
    
    // 获取编辑器内容
    getContent() {
      if (!this.$refs || !this.$refs.editorRef) {
        console.error('编辑器引用未初始化，无法获取内容')
        return ''
      }
      return this.$refs.editorRef.innerHTML
    },
    
    // 设置编辑器内容
    setContent(content) {
      if (!this.$refs || !this.$refs.editorRef) {
        console.error('编辑器引用未初始化，无法设置内容')
        return
      }
      this.$refs.editorRef.innerHTML = content
      this.$emit('update:modelValue', content)
    },
    
    // 清空编辑器
    clear() {
      if (!this.$refs || !this.$refs.editorRef) {
        console.error('编辑器引用未初始化，无法清空内容')
        return
      }
      this.$refs.editorRef.innerHTML = ''
      this.$emit('update:modelValue', '')
    }
  }
}
</script>

<style scoped>
.editor-container {
  width: 100%;
  margin-bottom: 20px;
}

.editor-label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.rich-text-editor {
  border: 1px solid #ddd;
  padding: 10px;
  min-height: 200px;
  outline: none;
  background-color: white;
}

.toolbar {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar button {
  padding: 5px 10px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  cursor: pointer;
  border-radius: 4px;
}

.toolbar button:hover {
  background-color: #e0e0e0;
}

/* 配置对话框样式 */
.config-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.config-dialog {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.config-dialog h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.config-item {
  margin-bottom: 15px;
}

.config-item label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.config-item input[type="text"] {
  width: calc(100% - 80px);
  padding: 8px;
  margin-right: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.config-item button {
  padding: 8px 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.config-item button:hover {
  background-color: #45a049;
}

.path-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
}

.path-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.path-item:last-child {
  border-bottom: none;
}

.remove-btn {
  background-color: #f44336 !important;
  color: white !important;
  padding: 3px 8px !important;
  font-size: 12px;
}

.remove-btn:hover {
  background-color: #d32f2f !important;
}

.dialog-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.confirm-btn {
  background-color: #2196F3 !important;
}

.confirm-btn:hover {
  background-color: #1976D2 !important;
}
</style>