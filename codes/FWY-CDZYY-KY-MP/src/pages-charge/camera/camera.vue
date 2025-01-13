<template>
  <view class="page flex-column">
    <cover-image
      class="page_back"
      :style="backStyle"
      src="/pages-charge/static/camera-back.png"
      @click="back"
    ></cover-image>
    <view class="page_camera flex-grow">
      <camera
        v-if="!isDevTool"
        device-position="back"
        mode="scanCode"
        :flash="cameraFlash"
        @scancode="handleScancode"
        @ready="handleReady"
        @error="handleError"
        :style="{
          width: '100%',
          height: `${cameraHeight}px`,
        }"
      ></camera>
      <view
        v-else
        class="mock-camera"
        :style="{
          width: '100%',
          height: `${cameraHeight}px`,
        }"
      ></view>
    </view>
    <view class="page_action flex-shrink" id="page_action">
      <cover-view
        @click="toggleFlash"
        style="
          text-align: center;
          position: absolute;
          width: 100%;
          left: 0;
          top: -96px;
        "
      >
        <cover-image
          style="width: 28px; height: 28px; margin: 0 auto"
          src="/pages-charge/static/scan-code-light.png"
        ></cover-image>
        <cover-view
          style="
            color: rgba(255, 255, 255, 0.7);
            font-size: 13px;
            margin-top: 10px;
          "
          >{{ cameraFlash === "off" ? "轻触照亮" : "轻触关闭" }}</cover-view
        >
      </cover-view>
      <view class="flex">
        <view
          class="flex-center flex-column"
          @click="redirect('/pages-charge/codeing/codeing')"
        >
          <image
            class="width-96"
            mode="widthFix"
            src="/pages-charge/static/scan-code-input.png"
          ></image>
          <view class="fs-24 color-fff mt-20">输入充电桩编码</view>
        </view>
        <view class="flex-center flex-column" @click="chooseImage">
          <image
            class="width-96"
            mode="widthFix"
            src="/pages-charge/static/scan-code-album.png"
          ></image>
          <view class="fs-24 color-fff mt-20">打开相册</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { isDevTool } from "../../utils/device";
import { deCode } from "../../utils/code";
import { back, redirect } from "../../utils/navigate";
import { onLoad, onReady } from "@dcloudio/uni-app";
import { fetchChargeStatus, searchQRCode } from "../../api/charge";
import { upload } from "@/utils/uploader";

const cameraHeight = ref(0);
const cameraFlash = ref("off");
const backStyle = ref("");

onLoad(() => {
  const menuButtonRect = uni.getMenuButtonBoundingClientRect();
  backStyle.value = `left:12px;top:${menuButtonRect.top + 6}px;`;
  uni.showLoading({
    title: "加载中",
  });
  fetchChargeStatus(true, true)
    .then(() => {
      uni.hideLoading();
    })
    .catch(() => {
      uni.hideLoading();
    });
});

onReady(() => {
  // #ifdef MP-WEIXIN
  const query = wx.createSelectorQuery();
  query.select("#page_action").boundingClientRect();
  query.exec(function (res: any) {
    if (res && res.length) {
      cameraHeight.value =
        getApp<any>().globalData.device.windowHeight - res[0].height;
      uni.hideLoading();
    }
  });
  // #endif
});

const handleReady = () => {
  uni.hideLoading();
};

const handleScancode = (e: any) => {
  uni.vibrateShort({});
  if (e.detail && e.detail.result) {
    deCode(e.detail.result);
  }
};

const handleError = (e: any) => {
  uni.hideLoading();
  if (e.detail && e.detail.errMsg && /auth/.test(e.detail.errMsg)) {
    uni.showModal({
      content: "请打开摄像头权限开关",
      success: () => {
        uni.openSetting();
      },
    });
    return;
  }
  uni.showModal({
    content: e.detail.errMsg,
  });
};

const toggleFlash = () => {
  if (cameraFlash.value === "off") {
    cameraFlash.value = "on";
    return;
  }
  cameraFlash.value = "off";
};

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    sourceType: ["album"],
    success: (res) => {
      uni.showLoading({
        title: "正在识别",
      });
      upload(res.tempFilePaths[0], {
        onSuccess: (res) => {
          searchQRCode(res.url)
            .then((res) => {
              if (
                res.errmsg === "ok" &&
                res.errcode === 0 &&
                res.code_results &&
                res.code_results.length
              ) {
                const fi = res.code_results.findIndex(
                  (item: any) => item.type_name === "QR_CODE"
                );
                if (fi >= 0) {
                  deCode(res.code_results[fi].data);
                  return;
                }
              }
              console.log(res);
              return Promise.reject({
                errMsg: "微信识别出错，请重试",
              });
            })
            .catch((err) => {
              uni.hideLoading();
              uni.showModal({
                content: err.errMsg || "出现错误，请重试",
              });
            });
        },
        onFail: (err) => {
          uni.hideLoading();
          uni.showModal({
            content: err.errMsg || "出现错误，请重试",
          });
        },
      });
    },
    fail: (err) => {
      console.log(err);
      if (/cancel/.test(err.errMsg)) {
        return;
      }
      uni.showModal({
        content: err.errMsg || "出现错误，请重试",
      });
    },
  });
};
</script>

<style lang="scss">
.page {
  position: relative;
  height: 100vh;
  background-color: #000;
  &_back {
    position: absolute;
    width: 24px;
    height: auto;
  }
  &_camera {
    height: 100%;
    .mock-camera {
      background-color: #999;
    }
  }
  &_action {
    position: relative;
    box-sizing: content-box;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    & > view {
      height: 222rpx;
      & > view {
        width: 50%;
        height: 222rpx;
      }
    }
  }
}
</style>
