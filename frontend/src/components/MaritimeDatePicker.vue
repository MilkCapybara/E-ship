<template>
  <div class="maritime-date-picker">
    <el-date-picker
      v-model="dateValue"
      :type="type"
      :placeholder="placeholder"
      :format="format"
      :value-format="valueFormat"
      :disabled-date="disabledDate"
      class="maritime-date-input"
      popper-class="maritime-date-popper"
      :teleported="true"
      @change="handleChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Date, Array],
    default: ''
  },
  type: {
    type: String,
    default: 'date'
  },
  placeholder: {
    type: String,
    default: '选择日期'
  },
  format: {
    type: String,
    default: 'YYYY-MM-DD'
  },
  valueFormat: {
    type: String,
    default: 'YYYY-MM-DD'
  },
  disabledDate: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const dateValue = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  dateValue.value = newVal
})

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style>
/* Override Element Plus Date Picker Styles */
.maritime-date-input.el-date-editor {
  width: 100%;
}

.maritime-date-input .el-input__wrapper {
  border: 2px solid rgba(30, 144, 255, 0.2);
  border-radius: 8px;
  padding: 8px 12px;
  transition: all 0.3s ease;
  box-shadow: none;
}

.maritime-date-input .el-input__wrapper:hover {
  border-color: var(--maritime-blue);
  box-shadow: 0 0 20px rgba(30, 144, 255, 0.2);
}

.maritime-date-input.is-focus .el-input__wrapper {
  border-color: var(--maritime-blue);
  box-shadow: 0 0 20px rgba(30, 144, 255, 0.3);
}

.maritime-date-input .el-input__inner {
  color: #333;
  font-size: 14px;
}

.maritime-date-input .el-input__prefix {
  color: var(--maritime-blue);
}

/* Date Picker Dropdown - 确保在对话框之上 */
.maritime-date-popper.el-picker__popper,
.maritime-date-popper.el-popper {
  z-index: 3100 !important;
  border: 1px solid rgba(30, 144, 255, 0.2);
  box-shadow: 0 8px 32px rgba(30, 144, 255, 0.2);
  border-radius: 12px;
}

.maritime-date-popper .el-date-picker__header {
  background: var(--maritime-gradient-primary);
  color: white;
  border-radius: 12px 12px 0 0;
  padding: 16px;
}

.maritime-date-popper .el-date-picker__header-label {
  color: white;
  font-weight: 600;
}

.maritime-date-popper .el-date-picker__header-label:hover {
  color: var(--maritime-gold);
}

.maritime-date-popper .el-picker-panel__icon-btn {
  color: white;
}

.maritime-date-popper .el-picker-panel__icon-btn:hover {
  color: var(--maritime-gold);
}

/* Calendar Cells */
.maritime-date-popper .el-date-table td {
  padding: 4px;
}

.maritime-date-popper .el-date-table td.available:hover {
  background: rgba(30, 144, 255, 0.1);
}

.maritime-date-popper .el-date-table td.current:not(.disabled) {
  background: var(--maritime-gradient-primary);
  color: white;
  font-weight: 600;
  border-radius: 8px;
}

.maritime-date-popper .el-date-table td.today .el-date-table-cell__text {
  color: var(--maritime-blue);
  font-weight: 700;
  border: 2px solid var(--maritime-blue);
  border-radius: 8px;
}

.maritime-date-popper .el-date-table td.in-range {
  background: rgba(30, 144, 255, 0.1);
}

.maritime-date-popper .el-date-table td.start-date,
.maritime-date-popper .el-date-table td.end-date {
  background: var(--maritime-gradient-primary);
  color: white;
  border-radius: 8px;
}

/* Week Header */
.maritime-date-popper .el-date-table th {
  color: var(--maritime-blue);
  font-weight: 600;
  border-bottom: 2px solid rgba(30, 144, 255, 0.2);
}

/* Month/Year Picker */
.maritime-date-popper .el-month-table td .cell,
.maritime-date-popper .el-year-table td .cell {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.maritime-date-popper .el-month-table td .cell:hover,
.maritime-date-popper .el-year-table td .cell:hover {
  background: rgba(30, 144, 255, 0.1);
  color: var(--maritime-blue);
}

.maritime-date-popper .el-month-table td.current .cell,
.maritime-date-popper .el-year-table td.current .cell {
  background: var(--maritime-gradient-primary);
  color: white;
  font-weight: 600;
}

/* Time Picker */
.maritime-date-popper .el-time-panel {
  border: 1px solid rgba(30, 144, 255, 0.2);
  box-shadow: 0 8px 32px rgba(30, 144, 255, 0.2);
}

.maritime-date-popper .el-time-spinner__item.active:not(.disabled) {
  color: var(--maritime-blue);
  font-weight: 700;
}

.maritime-date-popper .el-time-spinner__item:hover:not(.disabled):not(.active) {
  background: rgba(30, 144, 255, 0.1);
}
</style>

<style scoped>
.maritime-date-picker {
  width: 100%;
}
</style>
