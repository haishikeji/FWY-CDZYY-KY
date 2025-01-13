<template>
  <el-radio-group v-model="modelVal" @change="handleChange">
    <el-radio v-for="item in state.dicts" :key="item.value" :disabled="readonly" :label="item.value">{{ item.label }}</el-radio>
  </el-radio-group>
</template>
<script setup lang="ts" name="ExtDRadio">
import {reactive, onMounted, computed} from 'vue';
import u from "/@/utils/u";
import {Session} from "/@/utils/storage";

const props = defineProps({
  modelValue: {
    type: Number
  },
  type: {
    type: String,
    require:true
  },
  readonly: {
    type: Boolean,
    default: false
  },

});

const state = reactive({
  dicts: [] as Array<Dict>
})

const emit = defineEmits(['update:modelValue']);

const modelVal = computed(() => props.modelValue);

const handleChange = (val: number | Array<number>) => {
  console.log("handleChange", val)
  emit("update:modelValue", val)
}

onMounted(() => {
  console.log(state.dicts)
  const dicts = Session.get("dicts");
  if (u.isEmptyOrNull(dicts)) {
    return '--'
  }
  console.log(dicts)
  let k = props.type;
  console.log(k)
  state.dicts = dicts[`${k}`]
  console.log(dicts)
  console.log(state.dicts)
});

</script>
