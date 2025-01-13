<template>
  <style-dialog
      v-if="visible"
      @close="handleClose"
      title="优惠领取">

    <view class="page pt-20 pr-40 pl-40">
      <view class="tab-list flex-align-end">
        <view class="fs-28 color-666 mt-20 w50" :class="mode===0?'active':''" @click="changeTab(0)">权益卡</view>
        <view class="fs-28 color-666 mt-20 w50" :class="mode===1?'active':''" @click="changeTab(1)">优惠券</view>
      </view>

      <view v-if="mode===0">
        <view
            class="item mt-20 "
            v-for="(item, index) in rightsList"
            :key="index">

          <image class="full-percent" src="/pages-charge/static/discount-bg.png"></image>
          <view class="absolute-full flex pt-8 pb-8">
            <view class="left  flex-justify-center pl-48">

              <view class="item-content_left">
                <view class="flex-align-end">
                  <view class="item-header__left">
                    <image class="width-84" src="/static/images/discount-rights-card.png" mode="widthFix"></image>
                    <view class="fs-48 lh-58 color-000 fw-500 ml-16">
                      {{ (item.discount / 10).toFixed(1) }}折
                    </view>
                    <view class="fs-26 color-999 ml-16" v-if="mode === 0"
                    >抵扣充电服务费
                    </view>
                  </view>
                </view>


                <block>
                  <view class="fs-26 color-999 mt-8"
                        @click.stop="to(`/pages-common/activity/activity?id=${item.activityId}`)">
                    <view class="fs-26 color-999">到期时间：{{ item.endTime }}</view>
                    <view class="flex-align-center" v-if="mode === 0">
                      <view class="fs-24 color-999">充值{{ ((item.amountMin || 0) / 100).toFixed(2) }}元，享受{{ (item.discount / 10).toFixed(1) }}折服务费权益，有效期{{item.validity}}天
<!--                        <text class="color-primary">详细规则</text>
                        <text class="fs-24 ml-4 color-primary">>></text>-->
                      </view>
                    </view>
                  </view>
                </block>
              </view>
              <view class="item-content_right">
                <text class="item-content_right-charge item-status ml-10" @tap.stop="handleCharge(item)">去充值</text>
              </view>

            </view>
          </view>
        </view>


        <view style="width: 100%;text-align: center;display: flex;flex-direction: column;align-content: center;align-items: center;">
          <image v-if="!rightsList||rightsList.length==0"
                 style="width: 240rpx;" mode="widthFix"
                 src="/static/images/search-empty.png"></image>

<!--          <text style="font-size: 22rpx;">暂无权益</text>-->
        </view>


<!--        <view style="width: 100%;text-align: center;">
          <image v-if="!rightsList||rightsList.length==0" style="width: 240rpx;" mode="widthFix" src="/static/images/search-empty.png"></image>
        </view>-->

      </view>

      <view v-if="mode===1">
        <view
            class="item mt-20 "
            v-for="(item, index) in couponList"
            :class="item.collected?'inactive':''"
            :key="index">
          <image class="full-percent" src="/static/images/discount-bg.png"></image>
          <view class="absolute-full flex pt-8 pb-8">
            <view class="left  flex-justify-center pl-48">
              <view class="item-content_left">
                <view class="flex-align-end">
                  <view class="item-header__left">

                    <image
                        class="width-84"
                        src="/static/images/discount-coupon.png"
                        mode="widthFix"
                    ></image>
                    <view></view>
                    <view class="fs-48 lh-58 color-000 fw-500 ml-16" v-if="item.couponType=='FullDiscount'">
                      {{ ((item.discount || 0) / 100).toFixed(2) }}元
                    </view>

                    <view class="fs-48 lh-58 color-000 fw-500 ml-16" v-else>
                      {{ (item.discount / 10).toFixed(1) }}折
                    </view>

                    <view class="fs-26 color-999 ml-16">
                      充电服务费：{{ item.couponType == 'FullDiscount' ? '满减券' : '折扣券' }}
                    </view>
                  </view>

                </view>

                <block>
                  <view class="fs-26 color-999 mt-8"
                        @click.stop="to(`/pages-common/activity/activity?id=${item.activityId}`)">
                    <view class="fs-26 color-999">到期时间：{{ item.endTime }}</view>
                    <view class="flex-align-center">
                      <view class="fs-26 color-999">服务费门槛：{{ ((item.minServiceMoney || 0) / 100).toFixed(2) }}元
                        <text class="color-primary">详细规则</text>
                        <text class="fs-24 ml-4 color-primary">>></text>
                      </view>
                    </view>
                  </view>
                </block>
              </view>
              <view class="item-content_right">
                <view class="item-status ml-10" @tap.stop="handleCollectCoupon(item,index)">{{ item.collected ? '已领取' : '领取' }}</view>
              </view>
            </view>
          </view>
        </view>

        <view style="width: 100%;text-align: center;display: flex;flex-direction: column;align-content: center;align-items: center;">
          <image v-if="!couponList||couponList.length==0"
                 style="width: 240rpx;" mode="widthFix"
                 src="/static/images/search-empty.png"></image>

<!--          <text style="font-size: 22rpx;">暂无优惠券</text>-->
        </view>
      </view>


    </view>
    >
  </style-dialog>

</template>

<script setup lang="ts">
import {onPullDownRefresh} from "@dcloudio/uni-app";
import {ref} from "vue";
import {collectCoupon, listAvailableCoupons, listAvailableRights} from "@/api/user";
import {to} from "@/utils/navigate";

const mode = ref(0);
const rightsList = ref<any[]>([]);
const couponList = ref<any[]>([]);
const stationId = ref(null);
const visible = ref(false);

const handleClose = () => {
  visible.value = false;
}

onPullDownRefresh(() => {
  loadData();
});

const changeTab = (tab: number) => {
  mode.value = tab;
}

const handleCharge = (rights:any) => {
  to(`/pages-user/wallet-recharge/wallet-recharge?discount=${rights.amountMin}`)
}

const handleCollectCoupon = (coupon: any, index: number) => {
  if (coupon.collected) return;
  collectCoupon(coupon.id).then((res) => {
    console.log(res)
    uni.showToast({
      title: "领券成功",
      icon: "success"
    });
    couponList.value[index].collected = true;
  }).catch(e => {
    console.error(e)
    if (e.errMsg) {
      uni.showToast({
        title: e.errMsg,
        icon: "none"
      });
    }
  });
}


const loadData = () => {
  uni.showLoading({
    title: "加载中",
  });
  Promise.all([listAvailableCoupons(stationId.value), listAvailableRights(stationId.value)]).then((values) => {
    console.log(values)
    uni.hideLoading();
    couponList.value = values[0].list||[];
    rightsList.value = values[1].list||[];
  })
 /* listAvailableCoupons(stationId.value).then((res) => {
    console.log(res)
    uni.hideLoading();
    couponList.value = res.list;
  });*/
}
const open = (sid: any) => {
  visible.value = true;
  stationId.value = sid;
  loadData()
}


defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  width: 100%;
  background-color: #f6f7fa;

  .item {
    position: relative;
    min-height: 220rpx;
    border-radius: 20rpx;
    overflow: hidden;
    background: #fff;

    .left {
      flex-grow: 1;
    }

    .right {
      flex-shrink: 0;

      .btn {
        border-radius: 8rpx;
        border: 1px solid var(--color-primary);
        color: var(--color-primary);
        width: 144rpx;
        height: 56rpx;
      }
    }
  }

  .tab-list {
    width: 100%;

    .w50 {
      width: 50%;
      text-align: center;
    }

    .active {
      color: var(--color-primary);
    }
  }
}

.item-status {
  font-size: 24rpx;
  padding: 0rpx 6rpx;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  border-radius: 6rpx;

}

.item-header__left {
  display: inline-flex;
  align-items: flex-end;
}

.item-header__right {
  padding: 5rpx;
  display: flex;
  //justify-content: flex-end;
}

.inactive {
  cursor: not-allowed;
  pointer-events: none;

  .item-status {
    font-size: 24rpx;
    padding: 0rpx 6rpx;
    color: #ccc !important;
    border: 2rpx solid #ccc;
    border-radius: 6rpx;

  }

  .color-primary {
    color: #ccc !important;
  }
}

.item-content_left {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;

}

.item-content_right {
  width: 120rpx;
  display: flex;
  align-items: center;
  align-content: center;
}
</style>
