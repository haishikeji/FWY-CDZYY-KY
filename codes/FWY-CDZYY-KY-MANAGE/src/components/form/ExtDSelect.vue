<template>
  <el-select
      :size="size"
      transfer
      :disabled="disabled"
      :multiple="multiple"
      filterable
      clearable
      style="width: 100%"
      @change="handleChange"
      :placeholder="placeholder"
      v-model="state.dataVal">
    <template #empty>
      <el-empty :image-size="40" description=""></el-empty>
    </template>
    <el-option
        v-for="(item,idx) in state.dicts"
        :key="idx"
        :label="item.name||item.label"
        :value="item.value">
      <span class="option-item" style="float: left"><i :style="setupColorStyle(state.colorList[item.value%8])">{{ item.name || item.label }}</i></span>
      <!--        :style="setupColorStyle(item.color)"-->
    </el-option>
  </el-select>
</template>
<script setup lang="ts" name="ExtDSelect">
import {onMounted, reactive, watch,nextTick} from 'vue';
import {Session} from "/@/utils/storage";
import u from "/@/utils/u";

const props = defineProps({
  modelValue: {
    type: [Number, String]
  },
  //可选的值(字典二次过滤)
  values: {
    type: Array,
    default: () => {
      return []
    }
  },
  type: {
    type: String,
    required: true
  },
  dataRange: {
    type: Array<IFieldRange>
  },
  disabled: {
    type: Boolean,
    default: false
  },
  multiple: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  size: {
    type: String,
    default: 'default'
  }
})

const state = reactive({
  dicts: [] as Array<any>,
  dataVal: null as any,
  colorList: ["#FFB800", "#009688", "#1E9FFF", "#00C7D2", "#599CDE", "#FF5722", "#eb2f96", "#4a5055"],
  dictList: {
    'User.status': [
      {label: '有效', value: 1},
      {label: '无效', value: 0},
    ]
  }
})

const emit = defineEmits(['update:modelValue', 'on-change']);

const typeOf = (obj: any) => {
  return Object.prototype.toString.call(obj).slice(8, -1).toLowerCase();
}


watch(() => props.modelValue, (val, oldVal) => {
  console.log('ExtDSelect', val, oldVal,typeOf(val) )
  nextTick(()=>{
    if (typeOf(val) === "number") {
      state.dataVal = val + "";
    }else{
      state.dataVal = val;
    }
  })

},{immediate:true})


const handleChange = (val: number | Array<number>) => {
  console.log("handleChange", val)
  emit("update:modelValue", val)
  emit("on-change", val)

}

const setupColorStyle = (hex: string = "#000000") => {
  let hexToRgb = u.hexToRgb(hex);
  let {r, g, b} = hexToRgb;
  let v = {
    'text-shadow': `2px 2px 3px rgba(${r},${g},${b},0.2)`,
    // 'background-color': `rgba(${r},${g},${b},0.2)`,
    'color': `rgb(${r},${g},${b})`
  }
  return v;
}

const setupDicts = () => {
  let dictList: Array<any> = [];
  if (!u.isEmptyOrNull(props.dataRange)) {
    dictList = props.dataRange;
  } else {
    let dicts = Session.get("dicts");
    if (!u.isEmptyOrNull(dicts)) {
      dictList = dicts[props.type];
    } else {
      dictList = state.dictList[props.type]
    }
  }
  state.dicts = dictList;
  console.table(dictList)
}


onMounted(() => {
  setupDicts();
});
</script>

<style scoped lang="scss">
.option-item {
  display: inline-block;
}
</style>
