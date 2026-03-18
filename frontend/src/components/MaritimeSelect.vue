<template>
  <div class="maritime-select" :class="{ 'is-open': isOpen }">
    <div class="maritime-select-trigger" @click="toggleDropdown">
      <span class="maritime-select-value">{{ displayValue }}</span>
      <el-icon class="maritime-select-arrow" :class="{ 'is-reverse': isOpen }">
        <ArrowDown />
      </el-icon>
    </div>
    <transition name="maritime-dropdown">
      <div v-if="isOpen" class="maritime-select-dropdown">
        <div class="maritime-select-options">
          <div
            v-for="option in options"
            :key="option.value"
            class="maritime-select-option"
            :class="{ 'is-selected': modelValue === option.value }"
            @click="selectOption(option)"
          >
            <span class="option-label">{{ option.label }}</span>
            <el-icon v-if="modelValue === option.value" class="option-check">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowDown, Check } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  options: {
    type: Array,
    required: true
  },
  placeholder: {
    type: String,
    default: '请选择'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)

const displayValue = computed(() => {
  const selected = props.options.find(opt => opt.value === props.modelValue)
  return selected ? selected.label : props.placeholder
})

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const selectOption = (option) => {
  emit('update:modelValue', option.value)
  emit('change', option.value)
  isOpen.value = false
}

const handleClickOutside = (event) => {
  const selectEl = event.target.closest('.maritime-select')
  if (!selectEl) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.maritime-select {
  position: relative;
  width: 100%;
}

.maritime-select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border: 2px solid rgba(30, 144, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.maritime-select-trigger:hover {
  border-color: var(--maritime-blue);
  box-shadow: 0 0 20px rgba(30, 144, 255, 0.2);
}

.maritime-select.is-open .maritime-select-trigger {
  border-color: var(--maritime-blue);
  box-shadow: 0 0 20px rgba(30, 144, 255, 0.3);
}

.maritime-select-value {
  color: #333;
  font-size: 14px;
}

.maritime-select-arrow {
  transition: transform 0.3s ease;
  color: var(--maritime-blue);
}

.maritime-select-arrow.is-reverse {
  transform: rotate(180deg);
}

.maritime-select-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(30, 144, 255, 0.2);
  border: 1px solid rgba(30, 144, 255, 0.1);
  z-index: 10000;
  overflow: hidden;
}

.maritime-select-options {
  max-height: 300px;
  overflow-y: auto;
}

.maritime-select-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.maritime-select-option::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: var(--maritime-gradient-primary);
  transition: width 0.3s ease;
}

.maritime-select-option:hover {
  background: rgba(30, 144, 255, 0.05);
}

.maritime-select-option:hover::before {
  width: 4px;
}

.maritime-select-option.is-selected {
  background: rgba(30, 144, 255, 0.1);
  color: var(--maritime-blue);
  font-weight: 600;
}

.maritime-select-option.is-selected::before {
  width: 4px;
}

.option-label {
  position: relative;
  z-index: 1;
}

.option-check {
  color: var(--maritime-blue);
  font-weight: bold;
}

/* Dropdown Animation */
.maritime-dropdown-enter-active,
.maritime-dropdown-leave-active {
  transition: all 0.3s ease;
}

.maritime-dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.maritime-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Scrollbar Styling */
.maritime-select-options::-webkit-scrollbar {
  width: 6px;
}

.maritime-select-options::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.maritime-select-options::-webkit-scrollbar-thumb {
  background: var(--maritime-blue);
  border-radius: 3px;
}

.maritime-select-options::-webkit-scrollbar-thumb:hover {
  background: var(--maritime-blue-dark);
}
</style>
