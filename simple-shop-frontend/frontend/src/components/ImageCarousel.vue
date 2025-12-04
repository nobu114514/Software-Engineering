<template>
  <div v-if="show" class="image-carousel-overlay" @click.self="close">
    <div class="image-carousel-container">
      <button class="close-btn" @click="close">&times;</button>
      
      <button class="nav-btn prev-btn" @click="prevImage" :disabled="currentIndex === 0">
        &lt;
      </button>
      
      <div class="carousel-image-wrapper">
        <img 
          :src="currentImage" 
          :alt="`图片 ${currentIndex + 1}/${images.length}`"
          class="carousel-image"
          @error="handleImageError"
        >
      </div>
      
      <button class="nav-btn next-btn" @click="nextImage" :disabled="currentIndex === images.length - 1">
        &gt;
      </button>
      
      <div class="carousel-indicators">
        <span 
          v-for="(img, index) in images" 
          :key="index"
          class="indicator"
          :class="{ active: index === currentIndex }"
          @click="goToImage(index)"
        ></span>
      </div>
      
      <div class="carousel-counter">
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImageCarousel',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    images: {
      type: Array,
      default: () => []
    },
    initialIndex: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      currentIndex: 0,
      defaultImage: 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'
    }
  },
  computed: {
    currentImage() {
      if (this.images.length === 0) return this.defaultImage;
      return this.images[this.currentIndex] || this.defaultImage;
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.currentIndex = Math.min(this.initialIndex, this.images.length - 1);
        this.addKeydownListener();
      } else {
        this.removeKeydownListener();
      }
    }
  },
  beforeUnmount() {
    this.removeKeydownListener();
  },
  methods: {
    nextImage() {
      if (this.currentIndex < this.images.length - 1) {
        this.currentIndex++;
      }
    },
    prevImage() {
      if (this.currentIndex > 0) {
        this.currentIndex--;
      }
    },
    goToImage(index) {
      if (index >= 0 && index < this.images.length) {
        this.currentIndex = index;
      }
    },
    close() {
      this.$emit('close');
    },
    handleImageError(e) {
      e.target.src = this.defaultImage;
    },
    addKeydownListener() {
      document.addEventListener('keydown', this.handleKeydown);
    },
    removeKeydownListener() {
      document.removeEventListener('keydown', this.handleKeydown);
    },
    handleKeydown(e) {
      switch(e.key) {
        case 'ArrowRight':
          this.nextImage();
          break;
        case 'ArrowLeft':
          this.prevImage();
          break;
        case 'Escape':
          this.close();
          break;
      }
    }
  }
}
</script>

<style scoped>
.image-carousel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.image-carousel-container {
  position: relative;
  width: 90%;
  height: 80%;
  max-width: 1200px;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2.5rem;
  cursor: pointer;
  z-index: 10;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  font-size: 2rem;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
  z-index: 5;
}

.nav-btn:hover:not(:disabled) {
  background-color: rgba(0, 0, 0, 0.8);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.prev-btn {
  left: 20px;
}

.next-btn {
  right: 20px;
}

.carousel-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.carousel-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.indicator.active {
  background-color: white;
  width: 20px;
  border-radius: 6px;
}

.carousel-counter {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .image-carousel-container {
    width: 95%;
    height: 70%;
  }
  
  .nav-btn {
    font-size: 1.5rem;
    width: 40px;
    height: 40px;
  }
  
  .prev-btn {
    left: 10px;
  }
  
  .next-btn {
    right: 10px;
  }
}
</style>