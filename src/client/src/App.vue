<template>
  <router-view />
</template>

<script>
import { defineComponent } from 'vue'
import {useCounterStore} from "stores/example-store";
import {useQuasar} from "quasar";
import {useArticleStore} from "stores/article-store";
import {base58_to_binary} from "base58-js";

export default defineComponent({
  name: 'App',
  mounted() {
    setInterval(this.checkBalance,10000)
    this.$router.beforeEach((to, from, next) => {
      if(to.href.indexOf('/profile')>-1) {
       next()
      } else {
        setTimeout(() => {
          const IsItABackButton = window.popStateDetected
          window.popStateDetected = false
          if (IsItABackButton) {
            this.store.goBack()
            next()
            return ''
          }
          this.store.selectFeed(to.href === '/' ? ('') : this.toId(to.href.substr(3)))
          next()
        }, 100)
      }

    })
  },
  created() {
    // console.log(window.location.hash)
    // debugger
    // this.store.selectFeed(window.location.hash === '#/' ? ('') : this.toId(window.location.hash.substr(4)))
  },
  methods : {
    toId(ii) {

      if(ii === 'new') {
        return ii;
      }
      let b = base58_to_binary(ii);
      let id = 0;
      for (var i = b.length - 1; i >= 0; i--) {
        id = (id * 256) + b[i];
        console.log(b[i])
      }
      return id;
    },
    checkBalance()
    {
      this.store.verify
    }
  },
  setup () {
    const store = useArticleStore()
    const $q = useQuasar()
    const value = $q.cookies.get('prkey')

    store.user = {
      password:value
    }
    store.$q = $q
    store.verify

    window.popStateDetected = false
    window.addEventListener('popstate', () => {
      console.log("popstate")
      window.popStateDetected = true
    })

    return {
      store,
    }
  }
})
</script>
