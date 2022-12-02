<template>
  <q-layout ref="container" class="bg-black" view="lHh  lFf">
    <transition  :duration="500"
                enter-active-class="animated flash" leave-active-class="animated fadeOut">
    <q-img v-show="show"
         ref="lighting"
         :src="constants.host+'/l'+lid+'.png'"
         class="absolute z-top fit"
           no-transition
    />
    </transition>
    <q-btn v-if="mobile && $route.path !== '/profile'" class="z-top absolute " color="accent"
           icon="add"
           size="sm"
           style="right: 10px;bottom: 10px"
           text-color="dark"
           @click="prompt = true"
    />
    <q-header class="q-pb-sm">
      <div v-if="mobile" class="row text-center flex-center fit " style=" color: rgba(255, 255, 255, 0.55);">
        <div class="header_bar col-3 cursor-pointer text-left topButton" @click="$router.push('/')" >satgag</div>
      </div>
      <div v-if="mobile" class="row text-center flex-center fit q-mt-xs" style=" color: rgba(255, 255, 255, 0.55);">
        <div class="col-3 cursor-pointer justify-end"  style="white-space: nowrap;float: right">
          {{(store.user.balance ? store.user.balance: 0)}} <q-icon class="saticon" ><img alt="sats"  src="../assets/ssr.png"/></q-icon>
        </div>
      </div>
      <div class="row">
        <div v-if="!mobile" class="col"></div>
        <div class="col">
          <div class="row text-center flex-center fit q-mt-xs" style=" color: rgba(255, 255, 255, 0.55);">
            <div v-if="!mobile" class="header_bar col-3 q-pa-xs cursor-pointer text-left topButton" @click="$router.push('/')">satgag</div>
            <div :class="$route.path==='/' ? 'text-white bg-dark':''" class="col cursor-pointer q-pa-xs topButton" @click="$router.push('/')" @dblclick.prevent="store.selectFeed('')">Top</div>
            <div :class="$route.path==='/a/new' ? 'text-white bg-dark':''" class="col cursor-pointer q-pa-xs topButton" @click="$router.push('/a/new')" @dblclick.prevent="store.selectFeed('new')">New</div>
            <div :class="$route.path==='/profile' ? 'text-white bg-dark':''" class="col cursor-pointer q-pa-xs " style="overflow: hidden" @click="$router.push('/profile')">
              <div :class="!mobile ? 'row no-wrap float-right ':''" >
                <span v-if="!mobile" class="no-wrap vertical-middle" style="white-space: nowrap;overflow: hidden">{{(store.user.balance ? store.user.balance: 0)}} <q-icon class="saticon" ><img alt="sats"  src="../assets/ssr.png"/></q-icon></span>
                <q-icon :name="store.user.imgurl?'img:'+store.user.imgurl:'account_circle'" class="q-ml-sm q-mr-sm cursor-pointer"  size="sm"/>
              </div>
            </div>
            <div v-if="!mobile" class="col-1">
              <q-btn          color="accent"
                              icon="add"
                              size="sm"
                              text-color="dark"
                              @click="prompt = true"
              />
            </div>
          </div>
        </div>
        <div v-if="!mobile" class="col"></div>
      </div>

    </q-header>
    <div class="row fit">
      <div v-if="!mobile" class="col"></div>
      <div class="col">
        <q-page-container :style="{'height':this.sizeH+'px','overfolow':'scroll'}">
          <router-view :key="$route.fullPath" @sat="()=>showLightning()"/>
        </q-page-container>
      </div>
      <div v-if="!mobile" class="col"></div>
    </div>
    <q-dialog v-model="prompt" persistent >
      <ImageSelector />
    </q-dialog>
  </q-layout>
</template>

<script>
import {defineComponent, ref} from 'vue'

import {isMobile} from "mobile-device-detect";
import {useCounterStore} from "stores/example-store";
import ImageSelector from "components/ImageSelector";
import {useArticleStore} from "stores/article-store";
import constants from "src/constants";

export default defineComponent({
  name: 'MainLayout',

  components: {
    ImageSelector

  },
  data: function () {
    return {
      mobile: isMobile,
      selected:"",
      prompt: ref(false),
      sizeH:0,
      lid:0,
      show:false,
      constants:constants
    }
  },
  methods : {
    showLightning()
    {
      this.lid = Math.floor((Math.random() * 5) + 1);
      // console.log(this.lid)
      setTimeout(()=> {
        this.show = true;
        setTimeout(()=>this.show=false,120)
      },10)

    }
  },

  mounted() {
    this.sizeH = this.$refs.container.$el.clientHeight;
  },

  setup () {
    const store = useArticleStore()
    return {
      store,
    }
  }
})
</script>
