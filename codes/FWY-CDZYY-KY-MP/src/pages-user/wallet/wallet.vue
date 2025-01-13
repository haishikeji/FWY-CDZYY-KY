<template>
  <view class="page flex-column">
    <navigation-bar
        autoFixedStyle="background-color:#fff;"
        title="我的钱包"
        @ready="ready"
    ></navigation-bar>
    <view class="head flex-shrink">
      <view>
        <image class="bg" src="/pages-user/static/wallet-banner.png"></image>
        <view class="fg pt-48 pl-48 pr-48">
          <view class="flex-align-center">
            <uni-icons
                type="wallet-filled"
                size="24"
                color="#ffffff"
            ></uni-icons>
            <view class="fs-36 color-fff fw-500 ml-16">我的钱包</view>
            <view
                class="btn flex-center fs-28 ml-auto"
                @click="to(`/pages-user/wallet-refund/wallet-refund`)"
            >退款
            </view
            >
          </view>
          <view class="price mt-50" v-if="user">
            <text class="color-fff fw-600 fs-48" style="vertical-align: top"
            >¥
            </text
            >
            <text
                class="color-fff fw-600 ml-12"
                style="font-size: 46px; line-height: 46px"
            >{{ user.balance }}
            </text
            >
          </view>
        </view>
      </view>
    </view>
    <view class="body flex-grow flex-column">
      <view class="tabs flex-shrink flex-align-center">
        <view
            v-for="(item, index) in tabs"
            :key="index"
            class="flex-align-center fs-30 mr-60"
            :style="{
            color:
              tab === item.value ? 'rgba(0, 0, 0, 1);' : 'rgba(0, 0, 0, 0.6);',
          }"
            @click="changeTab(index)"
        >{{
            item.label
          }}
          <view
              :style="{ opacity: tab === item.value ? '1' : '0' }"
              class="active"
          ></view
          >
        </view>
      </view>
      <view class="list pl-30 pr-30">
        <view
            class="item flex-align-center"
            v-for="(item, index) in dataList"
            :key="index"
            @click="detail(index)"
        >
          <view>
            <view class="fs-30 fw-500" key="title" duration="300">{{
                typeMap[item.type - 1]
              }}
            </view>
            <view v-if="item.type!==2">
              <view class="fs-24" style="color: rgba(0, 0, 0, 0.4)">余额</view>
            </view>
            <view v-else>
              <text class="refund-status" :style="refundStyle(item.status)">{{ refundLabel(item.status) }}</text>
            </view>

          </view>
          <view class="ml-auto" style="text-align: right">
            <view class="fs-30 fw-500">
              <text>{{ item.type == 3 ? "- " : "" }}{{ item.amount }}</text>
              <text class="fs-24 ml-6">元</text>
            </view>
            <view class="fs-24" style="color: rgba(0, 0, 0, 0.4)">{{
                item.transactionTime
              }}
            </view>
          </view>
          <view class="ml-32" v-if="item.type === 3">
            <uni-icons
                type="right"
                size="12"
                color="rgba(0,0,0,0.4)"
            ></uni-icons>
          </view>
          <view class="ml-32" v-else>&nbsp;&nbsp;&nbsp;</view>
        </view>
      </view>
      <view style="height: 170rpx"></view>
    </view>
    <style-bottom-view background="#ffffff">
      <view class="pl-40 pr-40 pb-30 pt-30">
        <style-button
            type="primary"
            @click="to(`/pages-user/wallet-recharge/wallet-recharge`)"
        >充值
        </style-button
        >
      </view>
    </style-bottom-view>
  </view>
</template>

<script setup lang="ts">
import {useInfiniteScroll} from "../../utils/infinite-scroll";
import {onReachBottom, onPullDownRefresh, onShow} from "@dcloudio/uni-app";
import {fetchWallet, listRefund} from "../../api/user";
import {to} from "../../utils/navigate";
import {ref} from "vue";
import {rpxToPx} from "@/utils/device";

const user = ref();

const dataList = ref([])
const tab = ref(0);
const tabs = ref([
  {
    label: "全部",
    value: 0,
  },
  {
    label: "充值",
    value: 1,
  },
  {
    label: "消费",
    value: 3,
  },
  {
    label: "退款",
    value: 2,
  },
]);

const refundStatusMap = ref([
  {label: '退款成功 ', value: 'SUCCESS', color: '#4cd964'},
  {label: '退款关闭  ', value: 'CLOSED', color: '#000000'},
  {label: '退款处理中 ', value: 'PROCESSING', color: '#007aff'},
  {label: '退款异常  ', value: 'ABNORMAL', color: '#dd524d'},
  {label: '退款已申请  ', value: 'NEW', color: '#f0ad4e'},
])

const typeMap = ref(["充值", "退款", "消费"]);
/*const infiniteScroller = useInfiniteScroll(10, (page) => {
  return fetchWallet(tab.value, page, 10).then((res: any) => {
    if (res && res.length) {
      res.forEach((item: any) => {
        item.amount = (Number(item.amount) / 100).toFixed(2);
      });
    }
    return res;
  });
});*/
const scrollViewHeight = ref(0);

onShow(() => {
  // infiniteScroller.refresh();
  loadData();
  if (getApp<any>().globalData.user) {
    user.value = getApp<any>().globalData.user;
  }
})

onReachBottom(() => {
  // if(tab.value!==2){
  //   infiniteScroller.next();
  // }
  loadData();
});

onPullDownRefresh(() => {
  // if(tab.value!==2){
  //   infiniteScroller.refresh();
  // }
  loadData();
});

const ready = (e: any) => {
  scrollViewHeight.value =
      getApp<any>().globalData.device.windowHeight -
      (e.detail.navigationBarHeight + rpxToPx(380));
};

const refundLabel = (value: string) => {
  return refundStatusMap.value.find((item: any) => item.value === value)?.label
}

const refundStyle = (value: string) => {
  let rf = refundStatusMap.value.find((item: any) => item.value === value);
  return setupLabelColorStyle(rf?.color)
}

const setupLabelColorStyle = (hex: string = "#000000", opacity = 0.2) => {
  if (!hex) {
    hex = "#000000"
  }
  hex = hex.replace("#", "");
  // Convert the hex value to RGB values
  const r = parseInt(hex.substring(0, 2), 16);
  const g = parseInt(hex.substring(2, 4), 16);
  const b = parseInt(hex.substring(4, 6), 16);

  return {
    /*    'text-shadow': `2px 2px 3px rgba(${r},${g},${b},${opacity})`,
        'background-color': `rgba(${r},${g},${b},${opacity})`,*/
    'color': `rgb(${r},${g},${b})`
  }
}

const changeTab = (index: number) => {
  tab.value = tabs.value[index].value;
  loadData();
  /*  if (tab.value === 2) {
      let user = getApp<any>().globalData.user;
      if (user) {
        listRefund({userId: user.id,pageSize:1000}).then((res: any) => {
          console.log(res)
          let  dataList = (res.list || []).map(item => {
            item.amount = (Number(item.refund) / 100).toFixed(2);
            item.type = 2;
            if (item.successTime) {
              item.transactionTime = item.successTime.slice(0, 3).join("-") + " " + item.successTime.slice(3, 6).join(":")
            }
            return item;
          })
          infiniteScroller.setList(dataList);
        })
      } else {
        infiniteScroller.refresh();
      }

    }*/
}

const loadData = () => {
  if (!user) return;
  if (tab.value !== 2) {
    fetchWallet(tab.value, 1, 1000).then((res: any) => {
      if (res && res.length) {
        res.forEach((item: any) => {
          item.amount = (Number(item.amount) / 100).toFixed(2);
        });
      }
      dataList.value = res;
    });
  } else {
    listRefund({userId: user.value.id, pageSize: 1000}).then((res: any) => {
      console.log(res)
      let list = (res.list || []).map(item => {
        item.amount = (Number(item.refund) / 100).toFixed(2);
        item.type = 2;
        if (item.successTime) {
          item.transactionTime = item.successTime.slice(0, 3).join("-") + " " + item.successTime.slice(3, 6).join(":")
        }
        return item;
      })
      dataList.value = list;
    })
  }
}

const detail = (index: number) => {
  if (!dataList.value) {
    return;
  }
  if (dataList.value[index].type === 3) {
    uni.navigateTo({
      url: `/pages-charge/order/order?id=${dataList.value[index]?.orderNo}`,
    });
  }
};
</script>

<style lang="scss">
.page {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(
          180deg,
          #e0ebff 0%,
          rgba(255, 255, 255, 0) 60.13%
  );

  .head {
    padding: 30rpx 40rpx;

    & > view {
      position: relative;
      height: 320rpx;
      border-radius: 40rpx;
      overflow: hidden;

      .bg {
        height: 100%;
        width: 100%;
      }

      .fg {
        position: absolute;
        left: 0px;
        top: 0px;
        height: 100%;
        width: 100%;

        .btn {
          width: 120rpx;
          height: 64rpx;
          border-radius: 40rpx;
          background: rgba(255, 255, 255, 0.3);
          color: #076370;
        }
      }
    }
  }

  .body {
    .tabs {
      padding: 0 40rpx;

      & > view {
        position: relative;
        height: 72rpx;

        .active {
          position: absolute;
          left: 50%;
          bottom: 0px;
          transform: translateX(-50%);
          width: 40rpx;
          height: 4rpx;
          border-radius: 4rpx;
          background-color: var(--color-primary);
        }
      }
    }

    .list {
      .item {
        height: 130rpx;
        border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);

        &:last-child {
          border-bottom: none;
        }
      }
    }
  }
}

.refund-status {
  display: inline-block;
  //padding: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}
</style>
