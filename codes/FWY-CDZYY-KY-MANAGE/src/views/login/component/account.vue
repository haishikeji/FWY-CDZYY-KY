<template>
  <el-form size="large" class="login-content-form" :model="state.ruleForm" :rules="state.rules">
    <el-form-item class="login-animation1" prop="name">
      <el-input text placeholder="请输入手机号"
                v-model="state.ruleForm.name" clearable autocomplete="off">
        <template #prefix>
          <el-icon class="el-input__icon">
            <ele-User/>
          </el-icon>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item class="login-animation2" prop="pwd">
      <el-input
          type="password"
          :placeholder="$t('message.account.accountPlaceholder2')"
          v-model="state.ruleForm.pwd"
          autocomplete="off"
          @keyup.enter.native="onSignIn"
      >
        <template #prefix>
          <el-icon class="el-input__icon">
            <ele-Unlock/>
          </el-icon>
        </template>
<!--        <template #suffix>
          <i
              class="iconfont el-input__icon login-content-password"
              :class="state.isShowPassword ? 'icon-yincangmima' : 'icon-xianshimima'"
              @click="state.isShowPassword = !state.isShowPassword"
          >
          </i>
        </template>-->
      </el-input>
    </el-form-item>
    <el-form-item class="login-animation4">
      <el-button type="primary" class="login-content-submit" round v-waves @click="onSignIn" :loading="state.loading.signIn">
        <span>{{ $t('message.account.accountBtnText') }}</span>
      </el-button>
<!--      <el-button type="danger" text class="login-content-forget" @click="handleForgetPassword">忘记密码？</el-button>-->
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts" name="loginAccount">
import {reactive, computed, onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {ElMessage, ElNotification} from 'element-plus';
import {useI18n} from 'vue-i18n';
import Cookies from 'js-cookie';
import {storeToRefs} from 'pinia';
import {useThemeConfig} from '/@/stores/themeConfig';
import {Session} from '/@/utils/storage';
import {formatAxis} from '/@/utils/formatTime';
import {NextLoading} from '/@/utils/loading';
import {$body, $get} from '/@/utils/request'
import {useUserInfo} from "/@/stores/userInfo";

import { JSEncrypt } from 'jsencrypt'
import u from "/@/utils/u";

// 定义变量内容
const {t} = useI18n();
const storesThemeConfig = useThemeConfig();
const {themeConfig} = storeToRefs(storesThemeConfig);
const storesUserInfo = useUserInfo();
const route = useRoute();
const router = useRouter();
const state = reactive({
  isShowPassword: false,
  ruleForm: {
    name: '',
    pwd: '',
    code: '',
    sign: '' as string
  },
  loading: {
    signIn: false,
  },
  verifySrc: '' as string,
  userName: ' ' as string,
  nickName: ' ' as string,
  rules:{
    name:[u.validator.required],
    pwd:[u.validator.required],
    code:[u.validator.required],
  }
});

// 时间获取
const currentTime = computed(() => {
  return formatAxis(new Date());
});

onMounted(() => {
});


const handleForgetPassword = () => {
  // Message.alert(`请联系管理员400-1234567`)
  //forgetPasswordRef.open();
}

const encryptData = (str:string)=>{
  let encryptor = new JSEncrypt()
  // 设置公钥 （这是后端直接给我的，看你们项目情况是需要调接口获得，还是程序中直接写死）
  let publicKey = import.meta.env.VITE_PUBLIC_KEY
  encryptor.setPublicKey(publicKey) // publicKey为公钥
  // 加密数据
  return encryptor.encrypt(str)
}

const initData = () => {
  $body("/dataDict/list", {}).then((res: any) => {
    let {list}  = res;
    var dictGroup = u.groupByKey(list,"code");
    Session.set("dicts", dictGroup);
  });

  $get("/admin-user/profile").then((obj: any) => {
    if (obj) {
      let user = obj[0];
      let userInfo = {...user, permList: user.permissions}
      /*     app.user = user;
           app.userId = user.id;
           app.permissions = permissionList;
           app.departmentMemberList = departmentMemberList;*/
      storesUserInfo.setUserInfos(userInfo);
      state.loading.signIn = false;
      ElNotification.success({
        title: 'Success',
        message: "欢迎，" + user.username,
        offset: 100,
      })
    }

    signInSuccess(false);
  }).catch(err => {
    ElMessage.error("登录状态失效，请重新登录管理控制台");
    // Session.clear();
    state.loading.signIn = false;
  });
}

const refreshLogin = () => {
  setInterval(() => {
    $get(`admin-user/refresh`);
  }, 1600 * 1000)
}

// 登录
const onSignIn = async () => {
  state.loading.signIn = true;
  let temp = {
    mobilePhone:state.ruleForm.name,
    password:encryptData(state.ruleForm.pwd)
  }
  $body(`/admin-user/login`, temp).then((res: any) => {
    let {id,accessToken} = res;
    console.log(res)
    if (accessToken) {
      // 存储 token 到浏览器缓存
      Session.set('accessToken', accessToken);
      Cookies.set('userId', id);

      initData();
      // refreshLogin();
      state.loading.signIn = false;


    } else {
      ElMessage.error(res.msg);
      state.loading.signIn = false;
    }
  }).catch(e => {
    state.loading.signIn = false;
    console.error(e)
  });


};
// 登录成功后的跳转
const signInSuccess = (isNoPower: boolean | undefined) => {
  if (isNoPower) {
    ElMessage.warning('抱歉，您没有登录权限');
    Session.clear();
  } else {
    // 初始化登录成功时间问候语
    let currentTimeInfo = currentTime.value;
    // 登录成功，跳到转首页
    // 如果是复制粘贴的路径，非首页/登录页，那么登录成功后重定向到对应的路径中
    if (route.query?.redirect) {
      router.push({
        path: <string>route.query?.redirect,
        query: Object.keys(<string>route.query?.params).length > 0 ? JSON.parse(<string>route.query?.params) : '',
      });
    } else {
      router.push('/');
    }
    // router.replace('/');
    // 登录成功提示
    const signInText = t('message.signInText');
    ElMessage.success(`${currentTimeInfo}，${signInText}`);
    // 添加 loading，防止第一次进入界面时出现短暂空白
    NextLoading.start();
  }
  state.loading.signIn = false;
};
</script>

<style scoped lang="scss">
.login-content-form {
  margin-top: 20px;
  @for $i from 1 through 4 {
    .login-animation#{$i} {
      opacity: 0;
      animation-name: error-num;
      animation-duration: 0.5s;
      animation-fill-mode: forwards;
      animation-delay: calc($i/10) + s;
    }
  }

  .login-content-password {
    display: inline-block;
    width: 20px;
    cursor: pointer;

    &:hover {
      color: #909399;
    }
  }

  .login-content-code {
    width: 100%;
    padding: 0;
    font-weight: bold;
    letter-spacing: 5px;
  }

  .login-content-submit {
    width: 100%;
    letter-spacing: 2px;
    font-weight: 300;
    margin-top: 15px;
  }

  .login-content-forget {
    width: 13%;
    letter-spacing: 2px;
    font-weight: 300;
    margin-top: 15px;
    margin-left: 5%;
  }
}
</style>
