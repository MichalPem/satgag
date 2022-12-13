<template>
  <q-page class="bg-black " style="max-height: 100%;overflow: auto;">
    <div class=" q-ma-sm">
      <div v-if="store.user.lnid && mobile" class="row q-pl-lg">
        <div v-if="store.user.lnid " class="col text-center">
          <qrcode-vue  :margin="3" :size="100" :value="this.store.user.lnid+'@'+getHost()" level="H" />
        </div>
      </div>
      <div v-if="store.user.lnid && mobile" class="row">
        <div v-if="store.user.lnid " class="col text-center">
          Your Lightning address is
        </div>
      </div>
      <div v-if="store.user.lnid && mobile" class="row">
        <div v-if="store.user.lnid " class="col text-center">
          <b class="cursor-pointer" @click="copy(store.user.lnid+'@'+getHost())">{{this.store.user.lnid}}@{{getHost()}} <q-icon name="content_copy" /></b>
        </div>
      </div>
      <div class="row  items-center q-mb-lg q-mt-lg" >
        <span style="color: black">.</span>
        <div v-if="store.user.lnid" class="col q-pl-lg">
          Welcome {{this.store.user.nick?this.store.user.nick:this.store.user.lnid}}
          <span v-if="warning">Your login key is below, secure it properly</span>
          You have <b>{{ (store.user.balance ? store.user.balance: 0)}} <q-icon class="saticon" ><img alt="sats"  src="../assets/ssr.png"/></q-icon></b>
        </div>
        <div v-if="store.user.lnid && !mobile" class="col text-center">
          <qrcode-vue  :margin="3" :size="100" :value="this.store.user.lnid+'@'+getHost()" level="H" />
          <br>
          Your Lightning address is <b  class="cursor-pointer" @click="copy(store.user.lnid+'@'+getHost())">{{this.store.user.lnid}}@{{getHost()}} <q-icon name="content_copy" @click="copy" /></b>
        </div>
      </div>


      <div v-if="store.user.lnid" class="row">
      <q-form
              class="col q-mr-lg"
              @submit="onSubmitUser"
      >
        <q-input
          v-model="this.nick"
          bg-color="secondary"
          filled
          hint="Nick"
          label="Nick"
          lazy-rules

        />

        <q-input
          v-model="this.imgurl"
          bg-color="secondary"
          filled
          label="Profile picture url"
          hint="Profile picture url"
          lazy-rules
        />
        <q-input
          v-model="this.satAmount"
          bg-color="secondary"
          filled
          hint="Upvote amount"
          label="Upvote amount"
          lazy-rules
        />
        <div>
          <q-btn class="" color="dark" label="Update" type="submit"/>
        </div>
      </q-form>
        <q-form
          class="col"
          @submit="onSubmitW"
        >
          <q-input
            v-model="wln"
            bg-color="secondary"
            filled
            hint="lightning address"
            label="Withdrawal lightning address"
            lazy-rules

          />

          <q-input
            v-model="wam"
            bg-color="secondary"
            filled
            label="Amount"
            lazy-rules

          />
          <div>
            <q-btn class="q-mt-lg" color="dark" label="Withdraw" type="submit"/>
          </div>
        </q-form>
      </div>

    <div class="row full-width q-mt-lg " >
      <q-input v-model="store.user.password" bg-color="secondary" class="col q-mb-lg" filled label="private key"  label-color="rgba(255, 255, 255, 0.55)" square type="text"   >
        <template v-slot:append>
          <q-icon name="content_copy" style="cursor: pointer" @click="copy(store.user.password)"/>
        </template>
      </q-input>
    </div>



    <div v-if="!store.user.lnid" class="row full-width flex flex-center">
      <div class="col">
        <q-btn  color="dark" label="Login"  size="lg" @click="login"/>
      </div>
      <div class="col ">
        <q-btn  v-if="!store.user.password" color="dark"  label="Register" size="lg" @click="register"/>
      </div>
    </div>
      <div v-if="store.user.lnid" class="row full-width flex flex-center">
        <div class="col">
          <q-btn  color="dark" label="Logout"  size="lg" @click="logout"/>
        </div>
      </div>

    </div>
  </q-page>
</template>

<script>

import {copyToClipboard, useQuasar} from "quasar";
import axios from "axios";
import {generatePrivateKey} from 'nostr-tools'
import {useCounterStore} from "stores/example-store";
import QrcodeVue from 'qrcode.vue'
import {useArticleStore} from "stores/article-store";
import constants from "src/constants";
import {isMobile} from "mobile-device-detect";

export default {
  name: "ProfilePage",
  components: {
    QrcodeVue,
  },
  data : function () {
    return {
      mobile:isMobile,
      warning:false,
      nick:this.store.user.nick,
      imgurl:this.store.user.imgurl,
      satAmount:this.store.user.satAmount,
      wln:null,
      wam:null
    }
  },
  methods: {
    getHost(){
      // console.log(constants.host.substr(constants.host.indexOf("//")+2))
      return constants.host.substr(constants.host.indexOf("//")+2)
    },
    register() {
      this.store.user.password = generatePrivateKey()
      this.store.verify
      this.warning = true
    },

    logout() {
      this.store.user = {
        password:null
      }
      this.imgurl =null;
      this.nick = null;
      this.$q.cookies.remove('prkey')
    },
    login() {
      this.warning = false
      this.store.verify
      setTimeout(()=>{
        this.imgurl = this.store.user.imgurl;
        this.nick = this.store.user.nick;
      },300)
    },
    async isImgUrl(url) {
      let res = await fetch(url, {method: 'HEAD'})
      return res.headers.get('Content-Type').startsWith('image')
    },
    async onSubmitUser() {
      if(!await this.isImgUrl(this.imgurl)) {
        this.$q.notify("Not image");
        this.imgurl = "";
      }
        axios.patch(constants.host + "/api/usr/" + this.store.user.password, null, {
          params: {
            nick: this.nick,
            imgurl: this.imgurl,
            satAmount: this.satAmount
          }
        }).then((response) => {
          this.store.user = response.data;
          this.nick = response.data.nick;
          this.imgurl = response.data.imgurl;
        }).catch((e) => {
          console.log(e)
        })

    },
    onSubmitW() {
      this.$q.loading.show()
      axios.patch(constants.host+"/api/withdrawal/"+this.store.user.password,null, { params: {
          lna:this.wln,
          amount:this.wam
        }}).then((response)=>{
        this.store.user = response.data
        this.wln = ""
        this.wam = ""
        this.$q.notify('Request send')
        this.$q.loading.hide()
      }).catch((e)=>{
        console.log(e)
        this.$q.loading.hide()
        this.$q.notify({
          timeout: 2500,
          type: 'negative',
          message: e.response.data.error ? "Try again" : e.response.data,
        })
      })
    },
    copy(str) {
      copyToClipboard(str)
        .then(() => {
          this.$q.notify("Copied!")
        })
        .catch(() => {
          // fail
        })
    }
  },
  mounted() {
    setTimeout(()=>{
      this.warning = false
      this.imgurl = this.store.user.imgurl;
      this.nick = this.store.user.nick;
    },500)

  },
  setup () {
    const store = useArticleStore()
    return {
      store,
    }
  }
}
</script>

<style scoped>

</style>
