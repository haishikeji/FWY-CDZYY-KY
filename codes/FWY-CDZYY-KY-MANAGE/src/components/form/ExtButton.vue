<style lang="scss" scoped>
</style>
<template>
  <div style="display: inline-block">
    <el-tooltip v-if="tips" :content="tips" placement="left-start">
      <el-button
          :loading="loading"
          :size="size"
          :plain="plain"
          v-auth="auth" :disabled="disabled" :type="type" :text="isText"  @click="handleClick">
        <SvgIcon v-if="icon" :name="icon"/>
        {{ name }}
      </el-button>
    </el-tooltip>
    <el-button v-else
               :loading="loading"
               :size="size"
               :plain="plain"
               v-auth="auth" :disabled="disabled" :type="type" :text="isText"  @click="handleClick">
      <SvgIcon v-if="icon" :name="icon"/>{{ name }}
    </el-button>
  </div>
</template>
<script setup lang="ts" name="ExtButton">

// 定义子组件向父组件传值/事件
const emit = defineEmits(['on-click']);

// eslint-disable-next-line @typescript-eslint/no-unused-vars,no-unused-vars
const props = defineProps({
  icon:{
    type:String
  },
  name: {
    type: String,
    require:true,
  },
  auth: {
    type: String
  },
  plain: {
    type: Boolean,
    default:true
  },
  size: {
    type: String,
    default: "default"
  },
  tips: {
    type: String,
  },
  disabled: {
    type: Boolean,
    default: false
  },
  isText: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'primary'
  },
  loading:{
    type:Boolean,
    default:false
  }
})

const handleClick = () => {
  emit('on-click')
}

</script>
