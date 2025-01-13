<template>
  <view :class="['page']">
    <view v-if="data && priceInfo">
      <view class="block">
        <view class="station">
          <ChargeMachine
              :title="'NO.' + data.equipment.shortId"
              :list="data.equipment.connectorInfos"
              :time="priceInfo.useTime"
              :parkingNo="data.equipment.parkingNo"
          ></ChargeMachine>
        </view>
        <view class="pt-20 pb-20 pl-30 pr-30 flex-align-center">
          <image
              src="/pages-charge/static/machines-banner-address.png"
              mode="widthFix"
              class="flex-shrink mr-12 width-40"
          />
          <view class="fs-26 color-666">{{ data.station.address }}</view>
        </view>
      </view>
      <!--      <view class="mt-40 ml-30 color-999 fs-32 fw-500">费用说明</view>-->
      <view
          class="mt-20 block height-96 flex-align-center pl-30 pr-30"
          @click="openPriceDesc"
      >
        <view class="fs-28 color-000">充电费用</view>
        <view class="ml-64 fs-26 color-333"
        >{{ priceInfo.minPrice }}~{{ priceInfo.maxPrice }}元/度
        </view
        >
        <view class="ml-auto">
          <uni-icons type="right" size="16" color="rgba(0,0,0,0.4)"></uni-icons>
        </view>
      </view>
      <template v-if="appointmentData">
        <view class="mt-40 color-999 fs-32 fw-500">已预约充电</view>
        <view class="mt-20 block flex-align-center" style="height: 180rpx">
          <image
              class="width-56 ml-30"
              src="/pages-charge/static/icon-alarm.png"
              mode="widthFix"
          ></image>
          <view class="ml-24">
            <view class="flex" v-if="appointmentCountDown">
              <view class="fs-28 color-000">将在</view>
              <view class="fs-44 lh-36 color-primary ml-8 mr-8">{{
                  appointmentCountDown
                }}
              </view>
              <view class="fs-28 color-000">后开始充电</view>
            </view>
            <view class="fs-24 color-999 mt-8"
            >请在{{ appointmentData.startTime }}前到达充电桩并开始充电
            </view
            >
          </view>
        </view>
        <view class="mt-40 flex-center">
          <view
              class="flex-center height-68 br-68 fs-26 color-666"
              style="width: 184rpx; border: 1px solid rgba(0, 0, 0, 0.3)"
              @click="cancelAppointment"
          >取消预约
          </view
          >
        </view>
      </template>
      <template v-else>
        <!--        <view class="mt-40 ml-30 color-999 fs-32 fw-500">选择充电方式</view>-->
        <view class="mt-20 block pl-30 pr-30">
          <view
              v-for="(type, index) in chargeTypes"
              :key="index"
              :class="['pt-40', 'pb-40', 'flex-align-center']"
              :style="{
              borderTop: index === 0 ? '' : '1rpx solid rgba(0, 0, 0, 0.10)',
            }"
              @click="changeType(index)"
          >
            <view>
              <view class="fs-28 lh-28 color-000 fw-500">{{ type.title }}</view>
              <view
                  class="fs-24 lh-24 mt-16"
                  style="color: #f8386a"
                  v-if="chargeType === index"
              >{{ type.checkTip }}
              </view
              >
              <view class="fs-24 color-999 lh-24 mt-16" v-else>{{
                  type.tip
                }}
              </view>
            </view>
            <view class="ml-auto">
              <style-checkbox :checked="chargeType === index"></style-checkbox>
            </view>
          </view>
        </view>
        <view class="mt-20 block pl-30 pr-30" v-if="chargeType === 0">
          <view
              class="pt-40 pb-40 flex-between"
              style="border-bottom: 1rpx solid rgba(0, 0, 0, 0.1)"
              @click="selectTime"
          >
            <view class="fs-28 color-000 fw-500">充电时间</view>
            <view class="flex-align-center lh-28">
              <block v-if="chargeTime.isPowerSaving">
                <view class="fs-28 color-333 fw-500 mr-16">00:00 - 08:00</view>
              </block>
              <block v-else>
                <view
                    v-if="chargeTime.format"
                    class="fs-28 color-333 fw-500 mr-16"
                >{{ chargeTime.format }}
                </view
                >
                <view v-else class="fs-26 mr-16" style="color: #cacaca"
                >请选择
                </view
                >
                <uni-icons type="right" size="16" color="#CACACA"></uni-icons>
              </block>
            </view>
          </view>
          <view class="pt-40 pb-40 flex-between">
            <view class="fs-28 color-000 fw-500">预计费用</view>
            <view class="flex-align-center lh-28">
              <block v-if="chargeTime.isPowerSaving">
                <view class="fs-28 color-333 fw-500"
                >低至{{ priceInfo.minPrice }}元/kwh
                </view
                >
              </block>
              <block v-else>
                <view
                    v-if="chargeTime.formatPrice"
                    class="fs-28 color-333 fw-500"
                >{{ chargeTime.formatPrice }}
                </view
                >
                <view v-else class="fs-26 mr-16" style="color: #cacaca"
                >选择时间后显示
                </view
                >
              </block>
            </view>
          </view>
        </view>

        <view
            class="mt-20 block flex-align-center pl-30 pr-30 pt-30 pb-30"
            @click="
            to(
              `/pages-charge/discount/discountV2?rightsId=${userRechargeRightsId}&couponId=${userCouponId}&stationId=${stationId}`
            )
          "
        >
          <view class="fs-28 lh-28 color-000 fw-500">优惠</view>
          <block v-if="discountList.length > 0">
            <view class="fs-28 lh-28 ml-auto" style="color: #f43636"
            >{{ discountList[discountIndex].discountFormat.toFixed(1) }}折
            </view
            >
            <uni-icons type="right" size="16" color="#CACACA"></uni-icons>
          </block>
          <block v-else-if="activity">
            <view class="fs-28 lh-28 ml-auto" style="color: #f43636"
            >{{ activity.minDiscount }}折
            </view
            >
            <view class="fs-28 lh-28 color-666 ml-8">权益待领取</view>
            <uni-icons type="right" size="16" color="#CACACA"></uni-icons>
          </block>
          <view v-else class="fs-28 lh-28 color-666 ml-auto">无</view>
        </view>

        <view
            class="mt-20 block flex-align-center pl-30 pr-30 pt-30 pb-30"
            v-if="chargeType === 0"
        >
          <view class="flex-column">
            <view class="fs-28 lh-28 color-000 fw-500">省钱模式</view>
            <view class="fs-24 lh-30 color-999 mt-8"
            >处于00:00-08:00时，电费低至{{ priceInfo.minPrice }}元/kwh
            </view
            >
          </view>
          <view class="ml-auto">
            <switch
                :checked="chargeTime.isPowerSaving"
                color="#2d9e95"
                @change="changePowerSaving"
            />
          </view>
        </view>
      </template>
      <view class="pt-60 pb-80"></view>
      <style-bottom-view>
        <view class="pt-20 pl-40 pr-40 pb-20 bg-fff flex">
          <template v-if="appointmentData">
            <view class="mr-10" style="width: 50%">
              <style-button size="small" @click="selectTime"
              >修改时间
              </style-button
              >
            </view
            >
            <view class="ml-10" style="width: 50%">
              <style-button type="primary" size="small" @click="submitNow"
              >立即开始
              </style-button
              >
            </view>
          </template>
          <style-button v-else type="primary" size="small" @click="submit">{{
              chargeType === 0 ? "提交预约" : "马上充电"
            }}
          </style-button>
        </view>
      </style-bottom-view>
    </view>
    <style-dialog
        v-if="chargeTimeDialog"
        @close="closeTime"
        :title="
        '请选择' + (chargeTime.isPowerSaving ? '省电模式' : '') + '充电时间'
      "
    >
      <view class="flex" style="border-top: 1rpx solid rgba(0, 0, 0, 0.1)">
        <view
            class="flex-shrink"
            style="height: 840rpx; width: 256rpx; background-color: #f0f0f0"
        >
          <view
              class="flex-align-center pl-32 height-90"
              v-for="(day, index) in timesInfo.day"
              :key="index"
              :style="{
              backgroundColor:
                day.format === chargeTime.day ? '#fff' : 'transparent',
            }"
              @click="changeDay(index)"
          >
            <view
                :class="[
                'fs-32',
                'fw-500',
                day.format === chargeTime.day ? 'color-primary' : 'color-000',
              ]"
            >{{ day.format }}
            </view
            >
            <view
                class="fs-24 color-666 ml-12 br-8 width-60 height-40 flex-center"
                style="background-color: rgba(0, 0, 0, 0.07)"
                v-if="day.year && day.format !== chargeTime.day"
            >{{ day.year }}年
            </view
            >
          </view>
        </view>
        <scroll-view
            scroll-y
            class="flex-grow"
            style="height: 840rpx"
            :scroll-into-view="chargeTime.scrollId"
        >
          <view
              :id="`hour-${hour.format}`"
              class="flex-align-center pl-40 height-90 fs-32"
              v-for="(hour, index) in timesInfo.hour"
              :key="index"
              :style="{
              color: hour.disabled
                ? '#999'
                : hour.format === chargeTime.hour
                ? 'var(--color-primary)'
                : '#000',
            }"
              @click="changeHour(index)"
          >
            {{ hour.format }}
          </view>
          <view class="hour-placeholder"></view>
        </scroll-view>
      </view>
    </style-dialog>
    <PriceDesc
        v-if="priceDescVisible"
        :desc="priceInfo.policyInfos || []"
        @close="closePriceDesc"
    ></PriceDesc>
  </view>
</template>

<script setup lang="ts">
import {cancelAppointmentCharge, changeAppointmentTime, fetchChargeStatus, fetchStationByConnectorIdOrShortId, fetchStationPriceDesc, startAppointmentCharge, startCharge,} from "@/api/charge";
import {fetchProfile} from "@/api/user";
import {onLoad, onShow} from "@dcloudio/uni-app";
import {ref} from "vue";
import ChargeMachine from "../machines/charge-machine/charge-machine.vue";
import PriceDesc from "../machines/price-desc/price-desc.vue";
import {format} from "@/utils/date";
import {redirect, to} from "../../utils/navigate";

const DAY = 24 * 60 * 60 * 1000;
const options = ref<any>();
const data = ref<any>();
const priceInfo = ref();
const priceDescVisible = ref(false);
const timesInfo = ref<any>({
  day: [],
  time: [],
});

const chargeType = ref(1);
const chargeTypes = ref([
  {
    title: "预约充电",
    tip: "预约特定时间点开始充电",
    checkTip: "部分车辆可能进入休眠状态无法唤醒，将导致充电失败",
  },
  {
    title: "立即充电",
  },
]);
const chargeTimeDialog = ref(false);
const chargeTime = ref({
  time: 0, // 时间戳
  day: "",
  hour: "",
  format: "",
  formatPrice: "",
  scrollId: "",
  isPowerSaving: false, // 省电模式
});
const appointmentData = ref();
const appointmentCountDown = ref();

const discountIndex = ref(-1);
const discountList = ref<any[]>([]);
const activity = ref();

const userRechargeRightsId = ref();
const userCouponId = ref();
const stationId = ref();


const changeType = (index: number) => {
  chargeType.value = index;
};

const isPassTime = (hour: string, day: number) => {
  const now = new Date();
  const time = new Date(format("y/M/d", day) + " " + hour + ":00");
  return now > time;
};

const getHourPrice = (hour: string) => {
  let price = "";
  priceInfo.value.policyInfos.forEach((item: any) => {
    if (Number(hour) >= Number(item.startTime.substring(0, 2))) {
      price = item.totalPrice;
    }
  });
  return price;
};

const rebuildHours = (day: Date) => {
  timesInfo.value.hour = [];
  const nowHour = format("h");
  const isTaday = format("d") === format("d", day.getTime());
  // 当前时间的24小时内
  let startHour = isTaday ? Number(nowHour) : 0;
  let endHour = isTaday ? 24 : Number(nowHour) + 1;
  if (chargeTime.value.isPowerSaving) {
    startHour = 0;
    endHour = 8;
  }
  for (let index = startHour; index < endHour; index++) {
    timesInfo.value.hour = [...timesInfo.value.hour, ...buildHours(index, day)];
  }
};

const buildHours = (hour: number, day: Date) => {
  let res: any[] = [];
  let hourTemp = hour >= 10 ? `${hour}` : `0${hour}`;
  let priceTemp = getHourPrice(hourTemp);
  for (let index = 0; index < 12; index++) {
    res.push({
      format: `${hourTemp}:${index * 5 >= 10 ? index * 5 : `0${index * 5}`}`,
      formatPrice: priceTemp,
      disabled: isPassTime(
          `${hourTemp}:${index * 5 >= 10 ? index * 5 : `0${index * 5}`}`,
          chargeTime.value.time ? chargeTime.value.time : day.getTime()
      ),
    });
  }
  return res;
};

const selectTime = () => {
  if (chargeTime.value.isPowerSaving) {
    uni.showToast({
      title: "省电模式将在00:00-08:00进行充电",
      icon: "none",
    });
    return;
  }
  if (!appointmentData.value && chargeType.value === 1) {
    uni.showToast({
      title: "请选择预约模式",
      icon: "none",
    });
    return;
  }
  const now = appointmentData.value
      ? new Date(appointmentData.value.startTime.replace(/-/g, "/"))
      : new Date();
  const year = format("y", now.getTime());
  const nextyear = format("y", now.getTime() + DAY);

  // 天
  timesInfo.value.day = [
    {
      time: now.getTime(),
      format: format("M月d日", now.getTime()),
      year: "",
    },
    {
      time: now.getTime() + DAY,
      format: format("M月d日", now.getTime() + DAY),
      year: year === nextyear ? "" : nextyear.slice(2),
    },
  ];
  rebuildHours(now);

  // 预约时间
  if (!appointmentData.value) {
    if (!chargeTime.value.day) {
      chargeTime.value.time = timesInfo.value.day[0].time;
      chargeTime.value.day = timesInfo.value.day[0].format;
    }
    // 滚动到特定位置
    if (chargeTime.value.hour) {
      chargeTime.value.scrollId = `hour-${chargeTime.value.hour}`;
    } else {
      let hourNow = now.getHours();
      chargeTime.value.scrollId = `hour-${hourNow}:00`;
    }
  }
  // 修改时间
  if (appointmentData.value) {
    chargeTime.value.time = timesInfo.value.day[0].time;
    chargeTime.value.day = timesInfo.value.day[0].format;
    const findIndex = timesInfo.value.hour.findIndex(
        (item: any) => item.format === format("h:m:s", now.getTime())
    );
    if (findIndex >= 0) {
      chargeTime.value.hour = timesInfo.value.hour[findIndex].format;
      chargeTime.value.formatPrice =
          timesInfo.value.hour[findIndex].formatPrice;
    }
  }
  chargeTimeDialog.value = true;
};

const changeDay = (index: number) => {
  chargeTime.value.time = timesInfo.value.day[index].time;
  chargeTime.value.day = timesInfo.value.day[index].format;
  if (chargeTime.value.hour) {
    chargeTime.value.hour = "";
    chargeTime.value.formatPrice = "";
  }
  const now = new Date(chargeTime.value.time);
  rebuildHours(now);
  setTimeout(() => {
    chargeTime.value.scrollId = `hour-${timesInfo.value.hour[0].format}`;
  }, 0);
};

const changeHour = (index: number) => {
  if (timesInfo.value.hour[index].disabled) {
    uni.showToast({
      title: "无法选择过去的时间",
      icon: "none",
    });
    return;
  }
  if (appointmentData.value) {
    // 修改时间
    uni.showModal({
      title: "温馨提示",
      content: `确认修改为${timesInfo.value.hour[index].format}吗？`,
      confirmText: "确定",
      confirmColor: "#2d9e95",
      success(modal) {
        if (modal.confirm) {
          uni.showLoading({
            title: "提交中",
            mask: true,
          });
          changeAppointmentTime(
              appointmentData.value.startChargeSeq,
              `${format("y-M-d", chargeTime.value.time)} ${
                  timesInfo.value.hour[index].format
              }:00`
          )
              .then(() => {
                return fetchChargeStatus();
              })
              .then((res) => {
                uni.hideLoading();
                if (res && res.isAppointment) {
                  uni.showToast({
                    icon: "success",
                    title: "修改成功",
                  });
                  closeTime();
                  appointmentData.value = res;
                  startAppointmentCountDown();
                }
              });
        }
      },
    });
    return;
  }
  chargeTime.value.hour = timesInfo.value.hour[index].format;
  chargeTime.value.format = `${format("y年M月d日", chargeTime.value.time)} ${
      chargeTime.value.hour
  }`;
  chargeTime.value.formatPrice = timesInfo.value.hour[index].formatPrice;
  closeTime();
};

const changePowerSaving = (e: any) => {
  chargeTime.value = {
    time: 0, // 时间戳
    day: "",
    hour: "",
    isPowerSaving: e.detail.value,
    format: "",
    formatPrice: "",
    scrollId: "",
  };
};

const closeTime = () => {
  chargeTimeDialog.value = false;
  if (appointmentData.value) {
    return;
  }
  if (!chargeTime.value.hour) {
    chargeTime.value = {
      time: 0, // 时间戳
      day: "",
      hour: "",
      isPowerSaving: chargeTime.value.isPowerSaving, // 省电模式
      format: "",
      formatPrice: "",
      scrollId: "",
    };
  }
};

const openPriceDesc = () => {
  priceDescVisible.value = true;
};

const closePriceDesc = () => {
  priceDescVisible.value = false;
};

const cancelAppointment = () => {
  uni.showModal({
    title: "取消预约",
    content: "确定取消预约吗？",
    cancelText: "取消预约",
    cancelColor: "#999999",
    confirmText: "点错了",
    confirmColor: "#2d9e95",
    success: (res) => {
      if (res.cancel) {
        uni.showLoading({
          title: "提交中",
          mask: true,
        });
        cancelAppointmentCharge()
            .then(() => {
              uni.hideLoading();
              uni.showToast({
                icon: "success",
                title: "已取消",
              });
              appointmentData.value = undefined;
              startAppointmentCountDown();
            })
            .catch((err) => {
              uni.hideLoading();
            });
      }
    },
  });
};

let appointmentCountDownTimer = 0;

const startAppointmentCountDown = () => {
  appointmentCountDownTimer && clearTimeout(appointmentCountDownTimer);
  if (!appointmentData.value) {
    appointmentCountDown.value = "";
    return;
  }
  appointmentCountDown.value = formatAppointmentCountDown(
      appointmentData.value.startTime
  );
  if (appointmentCountDown.value) {
    appointmentCountDownTimer = setTimeout(() => {
      startAppointmentCountDown();
    }, 1000);
  } else {
    uni.showModal({
      content: "当前预约时间已到，将开始进行充电",
      showCancel: false,
      success() {
        uni.switchTab({
          url: "/pages/map/map",
        });
      },
    });
  }
};

const formatAppointmentCountDown = (time: string) => {
  const now = new Date();
  const date = new Date(time.replace(/-/g, "/"));
  let secondTime = parseInt(`${(date.getTime() - now.getTime()) / 1000}`);
  if (secondTime <= 0) {
    return "";
  }
  let hourTime = 0;
  let minuteTime = 0; // 分
  if (secondTime >= 60) {
    minuteTime = parseInt(`${secondTime / 60}`);
    secondTime = parseInt(`${secondTime % 60}`);
    if (minuteTime >= 60) {
      hourTime = parseInt(`${minuteTime / 60}`);
      minuteTime = parseInt(`${minuteTime % 60}`);
    }
  }
  return `${hourTime < 10 ? "0" + hourTime : hourTime}:${
      minuteTime < 10 ? "0" + minuteTime : minuteTime
  }:${secondTime < 10 ? "0" + secondTime : secondTime}`;
};

const submit = () => {
  if (
      chargeType.value === 0 &&
      !chargeTime.value.isPowerSaving &&
      !chargeTime.value.hour
  ) {
    uni.showToast({
      title: "请选择充电时间",
      icon: "none",
    });
    return;
  }
  const now = new Date();
  let startTime = chargeTime.value.time;
  if (chargeTime.value.isPowerSaving) {
    startTime = now.getTime();
    // 省电模式
    if (
        now.getHours() >= 8 &&
        now.getDate() === Number(format("d", chargeTime.value.time))
    ) {
      startTime += DAY;
    }
  }
  let query = "";
  if (chargeType.value === 0) {
    query += "isBooking=true";
    query += `&startTime=${format("y-M-d", startTime)} ${
        chargeTime.value.isPowerSaving
            ? "00:00:00"
            : `${chargeTime.value.hour}:00`
    }`;
    if (chargeTime.value.isPowerSaving) {
      query += `&endTime=${format("y-M-d", startTime)} 08:00:00`;
    }
  }
  if (chargeType.value === 1) {
    query += "isBooking=false";
  }
  if (discountIndex.value >= 0) {
    query += `&rechargeRightsId=${discountList.value[discountIndex.value].id}`;
  }
  console.log(query)
  uni.showLoading({
    title: "提交中",
    mask: true,
  });
  startCharge(options.value.sn, query)
      .then(() => {
        if (chargeType.value === 0) {
          fetchChargeStatus()
              .then((res) => {
                if (res && res.isAppointment) {
                  uni.hideLoading();
                  uni.showToast({
                    icon: "success",
                    title: "预约成功",
                  });
                  appointmentData.value = res;
                  startAppointmentCountDown();
                }
              })
              .catch(() => {
                uni.navigateBack();
              });
        }
        if (chargeType.value === 1) {
          redirect(`/pages-charge/ordering/ordering?sn=${options.value.sn}&start=1`)
        }
      })
      .catch((err) => {
        uni.hideLoading();
        uni.showModal({
          content: `${err.errMsg}`,
          showCancel: false,
        });
      });
};

const submitNow = () => {
  uni.showLoading({
    title: "提交中",
    mask: true,
  });
  startAppointmentCharge(options.value.sn)
      .then(() => {
        _checkStartCharge();
      })
      .catch((err) => {
        uni.hideLoading();
        uni.showModal({
          content: `${err.errMsg}`,
          showCancel: false,
        });
      });
};

const _checkStartCharge = () => {
  fetchChargeStatus().then((res) => {
    if (res && [2].includes(res.chargeStatus)) {
      uni.hideLoading();
      redirect(`/pages-charge/ordering/ordering?sn=${options.value.sn}&start=1`)
    } else {
      _checkStartCharge();
    }
  });
};

onLoad((_options: any) => {
  // sn=SN100523042860091 测试环境
  console.log("options", _options);
  let sn = _options.sn;
  uni.showLoading({
    title: "加载中",
  });
  fetchStationByConnectorIdOrShortId(sn)
      .then((res) => {
        console.log(res);
        let {station, equipment} = res;
        if (station) {
          stationId.value = station.StationID;
        }
        if (equipment && equipment.connectorInfos && equipment.connectorInfos.length) {
          sn = equipment.connectorInfos[0].connectorId;
          _options.sn = equipment.connectorInfos[0].connectorId;
        }
        options.value = _options;
        data.value = res;
        return fetchStationPriceDesc(sn);
      })
      .then((res) => {
        // console.log(res);
        uni.hideLoading();
        priceInfo.value = res;
        return fetchChargeStatus(false, true);
      })
      .then((res) => {
        if (res && res.isAppointment) {
          appointmentData.value = res;
          startAppointmentCountDown();
        }
      })
      .catch((err) => {
        console.log(err);
        uni.hideLoading();
      });


  fetchProfile().then((res) => {
    if (res.userRechargeRightsList.length) {
      res.userRechargeRightsList.forEach((item: any) => {
        item.discountFormat = Number((Number(item.discount) / 10).toFixed(2));
      });
      discountList.value = res.userRechargeRightsList;
      discountIndex.value = 0;
    }

    if (res.activityList && res.activityList.length) {
      res.activityList[0].rechargeRightsList.sort((a: any, b: any) => {
        return a.discount - b.discount;
      });
      activity.value = res.activityList[0];
      activity.value.minDiscount = Number(
          (
              Number(res.activityList[0].rechargeRightsList[0].discount) / 10
          ).toFixed(1)
      );
    }
  });

});

onShow(() => {
  if (getApp<any>().globalData.lastData.discount) {
    discountIndex.value = getApp<any>().globalData.lastData.discount.index;
    getApp<any>().globalData.lastData.discount = undefined;
  }
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  width: 100%;
  padding: 20rpx;
  background-color: #f6f7fa;
}

.block {
  border-radius: 20rpx;
  background: #fff;
}

.station {
  height: 148rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
}

.hour-placeholder {
  box-sizing: content-box;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
