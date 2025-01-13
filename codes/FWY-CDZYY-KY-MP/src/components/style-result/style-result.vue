<template>
  <view class="style-result">
    <image class="style-result_icon" :src="icon" v-if="icon" mode="widthFix"></image>
    <view class="style-result_title">{{ title }}</view>
    <view class="style-result_tip" v-if="tip">{{ tip }}</view>
    <slot></slot>
  </view>
</template>

<script lang="ts">
export default {
  props: {
    icon: String,
    title: String,
    tip: String,
    btn: String,
  },
};
</script>

<style lang="scss">
.style-result {
  position: fixed;
  width: 100vw;
  height: 100vh;
  left: 0;
  top: 0;
  z-index: 99999;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  &_icon {
    width: 324rpx;
  }
  &_title {
    color: #000;
    text-align: center;
    font-size: 30rpx;
    font-weight: 500;
    line-height: normal;
  }
  &_tip {
    width: 370rpx;
    margin-top: 16rpx;
    color: rgba(0, 0, 0, 0.6);
    text-align: center;
    font-size: 26rpx;
    font-weight: 400;
  }
}
</style>
