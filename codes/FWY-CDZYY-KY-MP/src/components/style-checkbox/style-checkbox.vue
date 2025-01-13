<template>
  <view
    class="lh-24 br-round flex-center"
    @click.stop="handleClick"
    :style="{
      width: `${size}rpx`,
      height: `${size}rpx`,
      border: checked
        ? '1rpx solid var(--color-primary)'
        : '1rpx solid rgba(0,0,0,0.15)',
      backgroundColor: checked ? 'var(--color-primary)' : 'rgba(0,0,0,0)',
    }"
  >
    <uni-icons
      v-if="checked"
      type="checkmarkempty"
      :size="iconsize"
      color="#ffffff"
    ></uni-icons>
  </view>
</template>

<script lang="ts">
export default {
  props: {
    checked: Boolean,
    size: {
      type: Number,
      default: 32,
    },
    iconsize: {
      type: Number,
      default: 10,
    },
  },
  methods:{
    handleClick(){
      console.log(!this.checked)
      this.$emit('on-checkChange',!this.checked);
    }
  }
};
</script>

<style lang="scss"></style>
