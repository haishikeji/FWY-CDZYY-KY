<template>
  <view>
    <map
      :style="{
        width: '100%',
        height: '100vh',
      }"
      :latitude="34.617999"
      :longitude="112.453"
      min-scale="1"
      :scale="12"
    ></map>
  </view>
</template>

<script setup lang="ts">
import { isDebug } from "@/utils/constant";
import { onLoad } from "@dcloudio/uni-app";
onLoad(() => {
  if (isDebug) {
    return;
  }
  uni.switchTab({
    url: "/pages/map/map",
  });
});
</script>

<style></style>
