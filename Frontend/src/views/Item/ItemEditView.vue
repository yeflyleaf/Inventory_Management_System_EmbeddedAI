<!--
  模块名称: 商品编辑/新增页面
  功能描述: 用于新增或编辑商品信息，支持条形码识别和多图片上传。
-->
<template>
  <div class="page-container">
    <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
    <form @submit.prevent="saveItem" class="edit-form">
      <div class="form-group">
        <label>商品编码</label>
        <div class="barcode-input-group">
          <input v-model="form.sku" required placeholder="输入商品编码或通过图片识别" />
          <label class="barcode-upload-btn" :class="{ 'is-loading': isRecognizing }">
            <input 
              type="file" 
              accept="image/*" 
              @change="handleBarcodeImage" 
              :disabled="isRecognizing"
              class="hidden-input"
            />
            <span v-if="isRecognizing" class="loading-spinner"></span>
            <span v-else>📷 识别条形码</span>
          </label>
        </div>
        <p v-if="recognizeMessage" :class="['recognize-message', recognizeStatus]">
          {{ recognizeMessage }}
        </p>
      </div>
      <div class="form-group">
        <label>商品名称</label>
        <input v-model="form.name" required />
      </div>
      <div class="form-group">
        <label>分类</label>
        <input v-model="form.category" />
      </div>
      <div class="form-group">
        <label>单位</label>
        <input v-model="form.unit" placeholder="例如: 千克, 件" required />
      </div>
      <div class="form-group">
        <label>条形码</label>
        <div class="barcode-input-group">
          <input v-model="form.barcode" placeholder="条形码（可选）" />
          <label class="barcode-upload-btn secondary" :class="{ 'is-loading': isRecognizingBarcode }">
            <input 
              type="file" 
              accept="image/*" 
              @change="handleBarcodeBarcodeImage" 
              :disabled="isRecognizingBarcode"
              class="hidden-input"
            />
            <span v-if="isRecognizingBarcode" class="loading-spinner"></span>
            <span v-else>📷 识别</span>
          </label>
        </div>
        <p v-if="barcodeRecognizeMessage" :class="['recognize-message', barcodeRecognizeStatus]">
          {{ barcodeRecognizeMessage }}
        </p>
      </div>
      <div class="form-group">
        <label>销售价</label>
        <input v-model.number="form.salePrice" type="number" step="0.01" required />
      </div>

      <!-- 商品图片上传 (支持最多5张) -->
      <div class="form-group">
        <label>商品图片 <span class="image-count">({{ imageCount }}/5张)</span></label>
        <div class="product-images-section">
          <!-- 已上传的图片列表 -->
          <div class="images-grid">
            <div 
              v-for="(img, index) in allImages" 
              :key="index" 
              class="image-item"
            >
              <img 
                :src="img.preview || getImageUrl(img.url)" 
                alt="商品图片" 
                class="product-image"
                @click="showImageInModal(index)"
              />
              <button 
                type="button" 
                class="remove-image-btn" 
                @click="removeImage(index)" 
                title="删除图片"
              >×</button>
              <div v-if="img.uploading" class="upload-overlay">
                <span class="loading-spinner"></span>
              </div>
            </div>
            
            <!-- 添加图片按钮 (当图片数量少于5张时显示) -->
            <label 
              v-if="imageCount < 5" 
              class="add-image-btn" 
              :class="{ 'is-uploading': isUploadingImage }"
            >
              <input 
                type="file" 
                accept="image/*" 
                @change="handleProductImageUpload" 
                :disabled="isUploadingImage"
                class="hidden-input"
              />
              <div class="upload-placeholder">
                <span v-if="isUploadingImage" class="loading-spinner large"></span>
                <template v-else>
                  <span class="upload-icon">📷</span>
                  <span class="upload-text">添加照片</span>
                </template>
              </div>
            </label>
          </div>
        </div>
        <p v-if="imageUploadMessage" :class="['recognize-message', imageUploadStatus]">
          {{ imageUploadMessage }}
        </p>
      </div>

      <!-- 条形码图片预览 -->
      <div v-if="previewImage" class="preview-section">
        <label>条形码图片预览</label>
        <div class="preview-container">
          <img :src="previewImage" alt="条形码预览" class="preview-image" />
          <button type="button" class="remove-preview-btn" @click="removePreview">×</button>
        </div>
      </div>

      <div class="actions">
        <button type="submit" class="btn-primary" v-throttle>保存</button>
        <button type="button" class="btn-secondary" @click="cancelEdit" v-throttle>取消</button>
      </div>
    </form>

    <!-- 图片查看弹窗 -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <div class="modal-content" @click.stop>
        <button class="nav-btn prev-btn" @click="prevImage" v-if="allImages.length > 1">‹</button>
        <img 
          :src="currentModalImage" 
          alt="商品图片大图" 
          class="modal-image"
        />
        <button class="nav-btn next-btn" @click="nextImage" v-if="allImages.length > 1">›</button>
        <button class="close-modal-btn" @click="closeImageModal">×</button>
        <div class="modal-indicator" v-if="allImages.length > 1">
          {{ currentImageIndex + 1 }} / {{ allImages.length }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from '@/api/request'
import { showError, showSuccess } from '@/utils/toast'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref({
  sku: '',
  name: '',
  category: '',
  unit: '',
  barcode: '',
  salePrice: 0,
  imageUrls: [],  // 改为数组，支持多张图片
})

// 条形码识别相关状态（商品编码）
const isRecognizing = ref(false)
const recognizeMessage = ref('')
const recognizeStatus = ref('') // 'success' 或 'error'
const previewImage = ref(null)

// 条形码识别相关状态（条形码字段）
const isRecognizingBarcode = ref(false)
const barcodeRecognizeMessage = ref('')
const barcodeRecognizeStatus = ref('')

// 商品图片上传相关状态
const isUploadingImage = ref(false)
const imageUploadMessage = ref('')
const imageUploadStatus = ref('')
const showImageModal = ref(false)
const currentImageIndex = ref(0)

// 用于存储正在上传的图片预览和临时文件名
const uploadingImages = ref([])  // { preview, filename, url }

// 记录临时上传的文件名列表，用于取消或替换时清理
const uploadedTempFilenames = ref([])

/**
 * 所有图片的统一数据结构
 */
const allImages = computed(() => {
  const images = []
  
  // 添加已保存的图片URL
  if (form.value.imageUrls && form.value.imageUrls.length > 0) {
    for (const url of form.value.imageUrls) {
      const tempImage = uploadingImages.value.find(img => img.url === url)
      if (tempImage) {
        images.push({
          url: url,
          preview: tempImage.preview,
          filename: tempImage.filename,
          isTemp: true,
          uploading: false
        })
      } else {
        images.push({
          url: url,
          preview: null,
          filename: null,
          isTemp: false,
          uploading: false
        })
      }
    }
  }
  
  return images
})

/**
 * 当前图片数量
 */
const imageCount = computed(() => {
  return form.value.imageUrls ? form.value.imageUrls.length : 0
})

/**
 * 当前弹窗显示的图片
 */
const currentModalImage = computed(() => {
  if (allImages.value.length === 0) return ''
  const img = allImages.value[currentImageIndex.value]
  return img.preview || getImageUrl(img.url)
})

/**
 * 获取完整的图片URL
 */
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  let baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  
  // 修复：移除 /api 后缀，确保指向服务器根目录的 /uploads
  if (baseUrl.endsWith('/api')) {
    baseUrl = baseUrl.slice(0, -4)
  }
  
  return `${baseUrl}${path}`
}

/**
 * 显示图片弹窗
 */
const showImageInModal = (index) => {
  currentImageIndex.value = index
  showImageModal.value = true
}

/**
 * 关闭图片弹窗
 */
const closeImageModal = () => {
  showImageModal.value = false
}

/**
 * 上一张图片
 */
const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else {
    currentImageIndex.value = allImages.value.length - 1
  }
}

/**
 * 下一张图片
 */
const nextImage = () => {
  if (currentImageIndex.value < allImages.value.length - 1) {
    currentImageIndex.value++
  } else {
    currentImageIndex.value = 0
  }
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const response = await axios.get(`/products/${route.params.id}`)
      form.value = {
        ...response,
        imageUrls: response.imageUrls || []
      }
    } catch (error) {
      console.error('Failed to load item', error)
      showError('加载商品信息失败')
    }
  }
})

/**
 * 组件卸载时（如导航离开），如果还有临时图片未保存，则清理
 */
onUnmounted(async () => {
  for (const filename of uploadedTempFilenames.value) {
    await deleteTempImage(filename)
  }
  // 清理预览 URL
  for (const img of uploadingImages.value) {
    if (img.preview) {
      URL.revokeObjectURL(img.preview)
    }
  }
})

/**
 * 处理商品编码的条形码图片识别
 */
const handleBarcodeImage = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 显示预览
  previewImage.value = URL.createObjectURL(file)
  
  // 开始识别
  isRecognizing.value = true
  recognizeMessage.value = '正在识别条形码...'
  recognizeStatus.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    // 使用原始 axios 发送 multipart 请求
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await fetch(`${baseUrl}/barcode/recognize`, {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })

    const result = await response.json()

    if (result.success && result.data && result.data.success) {
      form.value.sku = result.data.barcode
      recognizeMessage.value = `✓ 识别成功: ${result.data.barcode}`
      recognizeStatus.value = 'success'
    } else {
      recognizeMessage.value = result.message || '未能识别条形码，请确保图片清晰'
      recognizeStatus.value = 'error'
    }
  } catch (error) {
    console.error('Barcode recognition failed:', error)
    recognizeMessage.value = '识别失败，请重试'
    recognizeStatus.value = 'error'
  } finally {
    isRecognizing.value = false
    // 重置 input 以便可以再次选择同一文件
    event.target.value = ''
  }
}

/**
 * 处理条形码字段的图片识别
 */
const handleBarcodeBarcodeImage = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 显示预览
  previewImage.value = URL.createObjectURL(file)
  
  // 开始识别
  isRecognizingBarcode.value = true
  barcodeRecognizeMessage.value = '正在识别条形码...'
  barcodeRecognizeStatus.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await fetch(`${baseUrl}/barcode/recognize`, {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })

    const result = await response.json()

    if (result.success && result.data && result.data.success) {
      form.value.barcode = result.data.barcode
      barcodeRecognizeMessage.value = `✓ 识别成功: ${result.data.barcode}`
      barcodeRecognizeStatus.value = 'success'
    } else {
      barcodeRecognizeMessage.value = result.message || '未能识别条形码，请确保图片清晰'
      barcodeRecognizeStatus.value = 'error'
    }
  } catch (error) {
    console.error('Barcode recognition failed:', error)
    barcodeRecognizeMessage.value = '识别失败，请重试'
    barcodeRecognizeStatus.value = 'error'
  } finally {
    isRecognizingBarcode.value = false
    event.target.value = ''
  }
}

/**
 * 删除临时图片
 */
const deleteTempImage = async (filename) => {
  if (!filename) return
  try {
    await axios.delete(`/upload/product-image?filename=${filename}`)
    console.log('Temporary image deleted:', filename)
  } catch (error) {
    console.error('Failed to delete temporary image:', error)
  }
}

/**
 * 处理商品图片上传（支持多张）
 */
const handleProductImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查是否已达到最大图片数量
  if (imageCount.value >= 5) {
    imageUploadMessage.value = '最多只能上传5张图片'
    imageUploadStatus.value = 'error'
    return
  }

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    imageUploadMessage.value = '请选择图片文件'
    imageUploadStatus.value = 'error'
    return
  }

  // 检查文件大小 (20MB)
  if (file.size > 20 * 1024 * 1024) {
    imageUploadMessage.value = '图片大小不能超过20MB'
    imageUploadStatus.value = 'error'
    return
  }

  // 显示预览
  const preview = URL.createObjectURL(file)
  
  // 开始上传
  isUploadingImage.value = true
  imageUploadMessage.value = '正在上传图片...'
  imageUploadStatus.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await fetch(`${baseUrl}/upload/product-image`, {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })

    const result = await response.json()

    if (result.success && result.data) {
      // 添加到图片列表
      if (!form.value.imageUrls) {
        form.value.imageUrls = []
      }
      form.value.imageUrls.push(result.data.url)
      
      // 记录临时文件名和预览
      uploadedTempFilenames.value.push(result.data.filename)
      uploadingImages.value.push({
        url: result.data.url,
        filename: result.data.filename,
        preview: preview
      })
      
      imageUploadMessage.value = `✓ 图片上传成功 (${imageCount.value}/5)`
      imageUploadStatus.value = 'success'
    } else {
      imageUploadMessage.value = result.message || '图片上传失败'
      imageUploadStatus.value = 'error'
      URL.revokeObjectURL(preview)
    }
  } catch (error) {
    console.error('Image upload failed:', error)
    imageUploadMessage.value = '图片上传失败，请重试'
    imageUploadStatus.value = 'error'
    URL.revokeObjectURL(preview)
  } finally {
    isUploadingImage.value = false
    event.target.value = ''
  }
}

/**
 * 移除指定索引的商品图片
 */
const removeImage = async (index) => {
  const img = allImages.value[index]
  
  // 如果是刚刚上传的临时图片，需要从服务器删除
  if (img.isTemp && img.filename) {
    await deleteTempImage(img.filename)
    // 从临时文件名列表中移除
    const tempIndex = uploadedTempFilenames.value.indexOf(img.filename)
    if (tempIndex > -1) {
      uploadedTempFilenames.value.splice(tempIndex, 1)
    }
    // 从预览数组中移除
    const previewIndex = uploadingImages.value.findIndex(i => i.url === img.url)
    if (previewIndex > -1) {
      if (uploadingImages.value[previewIndex].preview) {
        URL.revokeObjectURL(uploadingImages.value[previewIndex].preview)
      }
      uploadingImages.value.splice(previewIndex, 1)
    }
  }
  
  // 释放 blob URL
  if (img.preview) {
    URL.revokeObjectURL(img.preview)
  }
  
  // 从 form 的 imageUrls 中移除
  form.value.imageUrls.splice(index, 1)
  imageUploadMessage.value = ''
  
  // 如果当前索引超出范围，调整弹窗索引
  if (currentImageIndex.value >= form.value.imageUrls.length) {
    currentImageIndex.value = Math.max(0, form.value.imageUrls.length - 1)
  }
}

/**
 * 移除预览图片
 */
const removePreview = () => {
  if (previewImage.value) {
    URL.revokeObjectURL(previewImage.value)
  }
  previewImage.value = null
}

const saveItem = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`/products/${route.params.id}`, form.value)
    } else {
      await axios.post('/products', form.value)
    }
    showSuccess('保存成功')
    // 保存成功后，临时文件已被移动或确认，清除标记以避免 onUnmounted 删除
    uploadedTempFilenames.value = []
    uploadingImages.value = []
    router.push('/items')
  } catch (error) {
    console.error('Failed to save item', error)
    showError(error.message || '保存失败')
  }
}

/**
 * 取消编辑，清理临时文件
 */
const cancelEdit = async () => {
  // onUnmounted 会处理清理，这里直接返回即可，或者显式调用
  // 为了用户体验（快速响应），直接返回，让 onUnmounted 处理
  router.back()
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
  max-width: 600px;
  margin: 0 auto;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 5px;
  font-weight: 500;
  color: #34495e;
}

input {
  padding: 8px;
  border: 1px solid #bdc3c7;
  border-radius: 4px;
}

/* 条形码输入组 */
.barcode-input-group {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.barcode-input-group input {
  flex: 1;
}

.barcode-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  white-space: nowrap;
  min-width: 110px;
}

.barcode-upload-btn.secondary {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  min-width: 80px;
}

.barcode-upload-btn:hover:not(.is-loading) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.barcode-upload-btn.is-loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.hidden-input {
  display: none;
}

/* 加载动画 */
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-spinner.large {
  width: 32px;
  height: 32px;
  border-width: 3px;
  border-color: rgba(52, 73, 94, 0.3);
  border-top-color: #3498db;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 识别消息 */
.recognize-message {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
}

.recognize-message.success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  color: #155724;
  border: 1px solid #c3e6cb;
}

.recognize-message.error {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 商品图片上传区域 - 多图片网格 */
.product-images-section {
  margin-top: 8px;
}

.image-count {
  font-size: 13px;
  color: #7f8c8d;
  font-weight: normal;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-width: 560px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.image-item:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-item .product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  border: none;
  border-radius: 0;
  max-width: none;
  max-height: none;
}

.image-item .remove-image-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  font-size: 16px;
  z-index: 10;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-image-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  min-height: 100px;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.add-image-btn:hover:not(.is-uploading) {
  border-color: #3498db;
  background: linear-gradient(135deg, #e8f4fc 0%, #d6eaf8 100%);
  transform: translateY(-2px);
}

.add-image-btn.is-uploading {
  opacity: 0.7;
  cursor: not-allowed;
}

.add-image-btn .upload-icon {
  font-size: 28px;
}

.add-image-btn .upload-text {
  font-size: 12px;
}

/* 保留旧样式作为备用 */
.product-image-section {
  margin-top: 8px;
}

.product-image-preview {
  position: relative;
  display: inline-block;
  max-width: 200px;
}

.product-image {
  width: 100%;
  max-width: 200px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.remove-image-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: 2px solid white;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(231, 76, 60, 0.4);
}

.remove-image-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 10px rgba(231, 76, 60, 0.5);
}

.upload-image-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 150px;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.upload-image-btn:hover:not(.is-uploading) {
  border-color: #3498db;
  background: linear-gradient(135deg, #e8f4fc 0%, #d6eaf8 100%);
  transform: translateY(-2px);
}

.upload-image-btn.is-uploading {
  opacity: 0.7;
  cursor: not-allowed;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 36px;
}

.upload-text {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

/* 预览区域 */
.preview-section {
  margin-top: 10px;
}

.preview-container {
  position: relative;
  display: inline-block;
  margin-top: 8px;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.remove-preview-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e74c3c;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.remove-preview-btn:hover {
  background: #c0392b;
  transform: scale(1.1);
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.btn-primary {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

/* 图片查看弹窗 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-image {
  max-width: 100%;
  max-height: 85vh;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.close-modal-btn {
  position: absolute;
  top: -15px;
  right: -15px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: 3px solid white;
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.close-modal-btn:hover {
  transform: scale(1.1);
}

/* 图片导航按钮 */
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: none;
  cursor: pointer;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.nav-btn:hover {
  background: white;
  transform: translateY(-50%) scale(1.1);
}

.prev-btn {
  left: -70px;
}

.next-btn {
  right: -70px;
}

.modal-indicator {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.5);
  padding: 6px 16px;
  border-radius: 20px;
}
</style>
