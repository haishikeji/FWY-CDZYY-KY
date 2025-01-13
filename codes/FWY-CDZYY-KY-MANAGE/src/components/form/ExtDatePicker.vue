<style scoped lang="scss">
</style>
<template>
  <el-date-picker
      ref="extDatePickerRef"
      @change="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
      :type="type"
      clearable
      :placeholder="placeholder"
      :format="format"
      :value-format="valueFormat"
      :readonly="readonly"
      v-model="state.dateValue"></el-date-picker>
</template>
<script setup lang="ts" name="ExtDatePicker">
import {reactive, watch, ref,onMounted,nextTick} from 'vue';
import u from "/@/utils/u";

const extDatePickerRef = ref(null);
//https://element-plus.gitee.io/zh-CN/component/date-picker.html#attributes
const props = defineProps({
  modelValue: {
    type: [String,Number]
  },
  /**
   * year/month/date/dates/datetime/ week/datetimerange/daterange/ monthrange
   */
  type: {
    type: String,
    default:'datetime'
  },
  /**
   * 时间显示的格式
   */
  format: {
    type: String,
    default: 'YYYY-MM-DD HH:mm:ss'
  },
  valueFormat: {
    type: String,
    default:'YYYY-MM-DD HH:mm:ss'
  },
  readonly: {
    type: Boolean,
    default: false
  },
  isEnd: {
    type: Boolean,
    default: false
  },
  fields: {
    type: String,
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  placement: {
    type: String,
  },
  disabledValid: {
    type: Function
  },
  shortcuts: {
    type: Array<shortcut>
  }
})

type shortcut = {
  text: string,
  value: Date | Function
}

// 定义子组件向父组件传值/事件
const emit = defineEmits(['update:modelValue', 'on-change', 'on-range-change']);

const state = reactive({
  isRangeMode: props.type?.endsWith("range"),
  dateValue: null,
})

onMounted(()=>{
  console.log(props.modelValue)
  nextTick(()=>{
    console.log(props.modelValue)
  })
  setupDate(props.modelValue)
})

watch(() => props.modelValue, (val:any, oldVal) => {
  console.log('props.modelValue', val, oldVal)
  state.dateValue = val;
 // setupDate(val);
/*
  if (!state.isRangeMode) {
    if (props.type === 'year') {
      state.dateValue
    }
    state.dateValue = val == null ? null : new Date(val)
  } else {
    if (props.modelValue) {
      if (!props.modelValue[`${props.fields}Start`] && !props.modelValue[`${props.fields}End`]) {
        state.dateValue = null;
      }
    }
  }*/
})

watch(() => state.dateValue, (val, oldVal) => {
  console.log('props.modelValue', val, oldVal)
  /*  if (!state.isRangeMode) {
      let changedValue = null;
      if (val) {
        changedValue = val.getTime();
      }
      if (props.type === 'date' && props.isEnd && changedValue) {
        emit('update:modelValue', changedValue + 24 * 3600 * 1000 - 1);
      } else {
        emit('update:modelValue', changedValue);
      }*/
  // }
})

const handleChange = (e: Date | Array<Date>) => {
  console.log('handleChange', e)
  reactiveEmit(e);
  /*if (state.isRangeMode) {
    props.modelValue[`${props.fields}Start`] = e[0] ? new Date(e[0]) : null;
    props.modelValue[`${props.fields}End`] = e[1] ? new Date(e[1]) : null;
    if (e[0] == "") {
      emit("on-range-change", props.modelValue);
    }*/
  // }
}

const setupDate = (val:any)=>{
  if (!val) {
    state.dateValue = null;
    return;
  }
  let today = new Date();
  let year = today.getFullYear();
  let fmtDate :Date|Array<Date>;
  switch (props.type) {
    case 'year':
      fmtDate = new Date(val, 0, 1);
      break;
    case 'month':
      let monthValue = Number.parseInt(val)-1;
      fmtDate = new Date(year,monthValue,1);
      break;
    case 'date':
      fmtDate = new Date(val)
      break;
    case 'dates':
      if (Array.isArray(val)) {
        fmtDate = val.map(k => new Date(k))
      }
      break;
    case 'datetime':
      fmtDate = new Date(val)
      break;
    case 'week':
      break;
    case 'datetimerange':
      if (Array.isArray(val)) {
        fmtDate = val.map(k => new Date(k))
      }
      break;
    case 'daterange':
      if (Array.isArray(val)) {
        fmtDate = val.map(k => new Date(k))
      }
      break;
    case 'monthrange':
      if (Array.isArray(val)) {
        fmtDate = val.map(k => new Date(k))
      }
      break;
    default:
      break;
  }

  state.dateValue = fmtDate;
  console.log(state.dateValue)
}

/**
 * 响应数据，根据不同的日期类型返回不同的值（默认为date类型，需要进行转义）
 * @param date
 */
const reactiveEmit = (date: any) => {
  console.log(date)
  let reactiveDate = null;
  if (date) {
    reactiveDate = date;
    switch (props.type) {
      case 'year':
        reactiveDate = new Date(date).getFullYear();
        break;
      case 'month':
        reactiveDate = new Date(date).getMonth() + 1;
        break;
      case 'date':
        if (props.isEnd) {
          reactiveDate = u.date.end(new Date(date))
        }
        break;
      case 'dates':
        if (props.isEnd) {
          if (Array.isArray(reactiveDate)) {
            reactiveDate = reactiveDate.map(k => u.date.end(new Date(k)))
          }
        }
        break;
      case 'datetime':
        if (props.isEnd) {
          reactiveDate = u.date.end(new Date(date))
        }
        break;
      case 'week':
        break;
      case 'datetimerange':
        break;
      case 'daterange':
        break;
      case 'monthrange':
        break;
      default:
        break;
    }
    // reactiveDate = val.getTime();
  }
  emit('update:modelValue', reactiveDate);
  emit('on-change', reactiveDate);
  /*  if (!state.isRangeMode) {


  /!*    if (props.type === 'date' && props.isEnd && reactiveDate) {
        emit('update:modelValue', reactiveDate + 24 * 3600 * 1000 - 1);
      } else {
        emit('update:modelValue', reactiveDate);
      }*!/


    }*/
}


const handleBlur = (e: FocusEvent) => {
  console.log(e)
}

const handleFocus = (e: FocusEvent) => {
  console.log(e)
}

</script>
